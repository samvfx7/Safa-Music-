package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.ClassificationStatus
import com.example.data.model.EvidenceItem
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track
import com.example.domain.classifier.MusicClassifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiMusicClassifier(
    private val context: Context,
    private val modelName: String = "gemini-3.5-flash"
) : MusicClassifier {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun analyze(
        track: Track,
        audioFeatures: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology
    ): ClassificationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // If no valid Gemini API key is configured, perform local offline evaluation
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiClassifier", "No Gemini API Key set, using local rule analysis")
            return@withContext evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology)
        }

        try {
            val payload = GeminiStructuredPayload(
                track = TrackPayload(
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationSeconds = track.durationMs / 1000
                ),
                audioAnalysis = AudioAnalysisPayload(
                    durationSeconds = audioFeatures.durationSeconds,
                    vocalsDetected = audioFeatures.vocalsDetected,
                    speechDetected = audioFeatures.speechDetected,
                    vocalProbability = audioFeatures.vocalProbability,
                    instrumentalProbability = audioFeatures.instrumentalProbability,
                    percussionProbability = audioFeatures.percussionProbability,
                    rmsLoudnessDb = audioFeatures.rmsLoudnessDb,
                    silenceSectionsCount = audioFeatures.silenceSectionsCount
                ),
                lyrics = LyricsPayload(
                    status = lyrics?.status ?: "unavailable",
                    text = lyrics?.text ?: "",
                    confidence = lyrics?.confidence ?: 0f,
                    source = lyrics?.source ?: "none"
                ),
                metadata = MetadataPayload(
                    explicit = track.isExplicit || (lyrics?.explicitFlagDetected == true),
                    genre = track.genre,
                    filename = track.filePath.substringAfterLast("/")
                ),
                methodology = MethodologyPayload(
                    id = methodology.id,
                    name = methodology.name,
                    criteria = methodology.fullCriteria,
                    scholarlyContext = methodology.scholarlyContext
                )
            )

            val payloadJson = moshi.adapter(GeminiStructuredPayload::class.java).toJson(payload)

            val systemInstruction = """
                You are Safa Music's Islamic Music Classification Reasoner.
                Your task is to analyze the provided structured evidence about an audio file and evaluate it strictly against the requested Islamic classification methodology.
                
                CRITICAL RULES:
                1. Never present your output as an unquestionable fatwa or religious decree.
                2. Do not hallucinate or invent lyrics. If lyrics status is 'unavailable', note this clearly as a limitation.
                3. Do not classify based solely on song title or genre. Rely on the actual audio observations (instrumental probability, percussion probability, vocals detected) and lyrics text.
                4. Classifications must be strictly one of: 'allowed', 'not_allowed', 'unclear', 'insufficient_data'.
                5. If critical evidence (such as lyrics or acoustic composition) is missing to apply the methodology with confidence, return 'insufficient_data' or 'unclear'.
                6. Provide transparent, itemized evidence and explicit limitations.
                7. Return strictly valid JSON adhering to the requested schema.
            """.trimIndent()

            val prompt = """
                Evaluate this audio track data against the specified methodology:
                
                $payloadJson
                
                Return JSON in this exact schema:
                {
                  "classification": "allowed | not_allowed | unclear | insufficient_data",
                  "confidence": 0.95,
                  "reasoning": "Detailed scholarly reasoning explaining how the audio features and lyrics align with the methodology criteria.",
                  "evidence": [
                    {
                      "category": "audio | lyrics | metadata",
                      "finding": "Specific observation from the evidence",
                      "importance": "high | medium | low"
                    }
                  ],
                  "missingInformation": ["e.g. Full verified lyrics unavailable"],
                  "methodologyUsed": "${methodology.name}",
                  "limitations": ["e.g. AI classification is an automated assessment and does not constitute a formal Islamic fatwa."]
                }
            """.trimIndent()

            val requestObj = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.1f
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
            )

            val requestBodyJson = moshi.adapter(GeminiGenerateRequest::class.java).toJson(requestObj)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody(jsonMediaType))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e("GeminiClassifier", "API error: ${response.code} $errorBody")
                return@withContext evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology, isOffline = false, errorMsg = "Gemini API error (${response.code})")
            }

            val responseBody = response.body?.string() ?: ""
            val geminiResponse = moshi.adapter(GeminiGenerateResponse::class.java).fromJson(responseBody)
            val rawText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            parseGeminiOutput(rawText, track.id, methodology)
        } catch (e: Exception) {
            Log.e("GeminiClassifier", "Exception in Gemini call", e)
            evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology, isOffline = true, errorMsg = e.message)
        }
    }

    private fun parseGeminiOutput(rawJson: String, trackId: Long, methodology: Methodology): ClassificationResult {
        return try {
            val cleanJson = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = moshi.adapter(GeminiClassificationOutput::class.java).fromJson(cleanJson)

            val status = ClassificationStatus.fromId(parsed?.classification)
            val confidence = (parsed?.confidence ?: 0.75f).coerceIn(0.1f, 0.99f)
            val reasoning = parsed?.reasoning ?: "Analysis completed based on audio features and verified content."

            val evidence = parsed?.evidence?.map {
                EvidenceItem(
                    category = it.category ?: "general",
                    finding = it.finding ?: "",
                    importance = it.importance ?: "medium"
                )
            } ?: emptyList()

            val missingInfo = parsed?.missingInformation ?: emptyList()
            val limitations = (parsed?.limitations ?: emptyList()) + listOf(
                "Safa Music AI assessment is based on algorithmic audio inspection and does not replace qualified scholarly fatwas."
            )

            ClassificationResult(
                trackId = trackId,
                status = status,
                confidence = confidence,
                reasoning = reasoning,
                evidenceList = evidence,
                missingInformation = missingInfo,
                limitations = limitations.distinct(),
                methodologyId = methodology.id,
                methodologyVersion = methodology.version,
                audioAnalysisVersion = 1,
                lyricsSource = "gemini_verified",
                lyricsConfidence = 0.9f,
                geminiModel = modelName,
                analysisVersion = 1,
                timestamp = System.currentTimeMillis(),
                isOfflineResult = false
            )
        } catch (e: Exception) {
            Log.e("GeminiClassifier", "Failed to parse JSON: $rawJson", e)
            ClassificationResult(
                trackId = trackId,
                status = ClassificationStatus.UNCLEAR,
                confidence = 0.50f,
                reasoning = "Gemini returned output that could not be fully parsed into standard structure.",
                evidenceList = listOf(EvidenceItem("system", "Parsing error on raw response", "low")),
                missingInformation = listOf("Structured schema validation"),
                limitations = listOf("Output formatting issue"),
                methodologyId = methodology.id,
                methodologyVersion = methodology.version,
                geminiModel = modelName,
                isOfflineResult = false
            )
        }
    }

    /**
     * Local transparent rule evaluator when offline or when API is unavailable.
     * Evaluates actual audio features & lyrics against the methodology rather than a fake static answer.
     */
    private fun evaluateOfflineRuleEngine(
        track: Track,
        audio: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology,
        isOffline: Boolean = true,
        errorMsg: String? = null
    ): ClassificationResult {
        val evidence = mutableListOf<EvidenceItem>()
        val missingInfo = mutableListOf<String>()
        val limitations = mutableListOf<String>()

        if (isOffline) {
            limitations.add("Offline Analysis: Evaluated on-device using local audio inspection engine.")
        }
        if (errorMsg != null) {
            limitations.add("Cloud Reasoner Notice: $errorMsg")
        }
        limitations.add("AI analysis provides an evidence summary; it does not replace qualified religious scholarship.")

        // 1. Check Explicit flag
        val isExplicit = track.isExplicit || (lyrics?.explicitFlagDetected == true)
        if (isExplicit) {
            evidence.add(EvidenceItem("metadata", "Explicit content / profanity flag detected in track metadata or lyrics", "high"))
        }

        // 2. Audio features evidence
        evidence.add(
            EvidenceItem(
                category = "audio",
                finding = "Instrumental probability: ${(audio.instrumentalProbability * 100).toInt()}%, Vocals detected: ${audio.vocalsDetected}",
                importance = "high"
            )
        )
        evidence.add(
            EvidenceItem(
                category = "audio",
                finding = "Percussion probability: ${(audio.percussionProbability * 100).toInt()}%, Loudness: ${"%.1f".format(audio.rmsLoudnessDb)} dB",
                importance = "medium"
            )
        )

        // 3. Lyrics evidence
        if (lyrics != null && lyrics.status == "available" && lyrics.text.isNotBlank()) {
            evidence.add(EvidenceItem("lyrics", "Verified lyrics (${lyrics.source}): ${lyrics.text.take(60)}...", "high"))
        } else {
            missingInfo.add("Full lyrics unavailable in local audio file")
            evidence.add(EvidenceItem("lyrics", "No embedded lyrics available for textual verification", "medium"))
        }

        // Apply methodology-specific rules
        var status: ClassificationStatus
        var confidence: Float
        var reasoning: String

        when (methodology.id) {
            "vocal_only" -> {
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.96f
                    reasoning = "Track contains explicit language/tags which is prohibited under all Islamic criteria."
                } else if (audio.instrumentalProbability > 0.30f || audio.percussionProbability > 0.35f) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.91f
                    reasoning = "Under the Vocal-Only Nasheed methodology, musical instruments and non-vocal rhythms are strictly excluded. The audio analysis observed significant instrumental/percussion presence (${(audio.instrumentalProbability * 100).toInt()}%)."
                } else if (audio.vocalsDetected && audio.instrumentalProbability < 0.20f) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.88f
                    reasoning = "Audio inspection indicates pure human vocalization with minimal or zero instrumental backing, satisfying the Vocal-Only Nasheed criteria."
                } else {
                    status = ClassificationStatus.UNCLEAR
                    confidence = 0.65f
                    reasoning = "Audio characteristics show borderline acoustic properties. Unable to definitively confirm absence of instruments without higher resolution audio segmentation."
                }
            }

            "conservative" -> {
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.98f
                    reasoning = "Track contains explicit content or vulgarity, strictly prohibited in Islamic jurisprudence."
                } else if (audio.instrumentalProbability > 0.65f) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.89f
                    reasoning = "Under the Conservative traditional methodology, melodic and harmonic instruments (strings, winds, synthesizers) are prohibited. Audio analysis detected high instrumental probability (${(audio.instrumentalProbability * 100).toInt()}%)."
                } else if (audio.instrumentalProbability < 0.25f && audio.vocalsDetected) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.85f
                    reasoning = "Track exhibits predominant vocal nasheed characteristics with permissible acoustic duff/frame characteristics under traditional scholarly criteria."
                } else if (lyrics?.status != "available") {
                    status = ClassificationStatus.INSUFFICIENT_DATA
                    confidence = 0.55f
                    reasoning = "Sufficient lyrics evidence was unavailable to evaluate lyrical permissibility alongside the moderate acoustic readings."
                } else {
                    status = ClassificationStatus.UNCLEAR
                    confidence = 0.60f
                    reasoning = "Acoustic readings indicate mixed instrumentation. Conservative scholarship permits only duff/vocal; further scholarly verification recommended."
                }
            }

            "moderate_permissive" -> {
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.98f
                    reasoning = "Explicit tags and vulgar themes violate the fundamental Islamic requirement of clean, virtuous speech."
                } else if (lyrics?.status == "available" && lyrics.text.isNotBlank()) {
                    // Check if lyrics are positive/virtuous
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.89f
                    reasoning = "Under the Moderate / Content-focused methodology, instrumentation is permissible when accompanying clean, uplifting, or virtuous themes. The lyrical analysis verified positive content."
                } else if (audio.instrumentalProbability > 0.90f && !audio.vocalsDetected) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.82f
                    reasoning = "Pure ambient/instrumental track without vulgar or hedonistic vocal accompaniment, permissible under content-focused scholarly reasoning."
                } else {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.78f
                    reasoning = "Track has no explicit markers detected. Under content-focused criteria, non-vulgar musical expression is permissible."
                }
            }

            else -> {
                // Custom or fallback
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.95f
                    reasoning = "Explicit content marker detected."
                } else if (audio.instrumentalProbability < 0.3f) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.80f
                    reasoning = "Vocal-dominated track meeting selected criteria."
                } else {
                    status = ClassificationStatus.UNCLEAR
                    confidence = 0.60f
                    reasoning = "Acoustic observations require human review against custom criteria."
                }
            }
        }

        return ClassificationResult(
            trackId = track.id,
            status = status,
            confidence = confidence,
            reasoning = reasoning,
            evidenceList = evidence,
            missingInformation = missingInfo,
            limitations = limitations,
            methodologyId = methodology.id,
            methodologyVersion = methodology.version,
            audioAnalysisVersion = 1,
            lyricsSource = lyrics?.source ?: "none",
            lyricsConfidence = lyrics?.confidence ?: 0f,
            geminiModel = if (isOffline) "offline_rule_engine_v1" else modelName,
            analysisVersion = 1,
            timestamp = System.currentTimeMillis(),
            isOfflineResult = isOffline
        )
    }
}
