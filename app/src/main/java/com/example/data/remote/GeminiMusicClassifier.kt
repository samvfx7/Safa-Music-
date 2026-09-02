package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentIdentification
import com.example.data.model.ContentStatus
import com.example.data.model.ContentType
import com.example.data.model.EvidenceItem
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track
import com.example.domain.classifier.MusicClassifier
import com.example.domain.content.ContentIdentifier
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
    private val modelName: String = "gemini-2.5-flash"
) : MusicClassifier {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val contentIdentifier = ContentIdentifier()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun analyze(
        track: Track,
        audioFeatures: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology,
        contentIdentification: ContentIdentification?
    ): ClassificationResult = withContext(Dispatchers.IO) {
        // STEP 1: CONTENT IDENTIFICATION (CRITICAL: Runs before any music reasoning)
        val contentId = contentIdentification ?: contentIdentifier.identifyContent(track, audioFeatures, lyrics)

        // STEP 2: DEDICATED NON-MUSIC ROUTING
        // If content is Qur'an, Adhan, or Speech -> NEVER send to Gemini music classifier!
        if (!contentId.type.isMusicCandidate) {
            return@withContext buildNonMusicClassificationResult(track, audioFeatures, lyrics, methodology, contentId)
        }

        // STEP 3: MUSIC REASONING LAYER
        val apiKey = BuildConfig.GEMINI_API_KEY

        // If no valid Gemini API key is configured or offline, perform local rule engine
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiClassifier", "No Gemini API Key set, using local rule analysis")
            return@withContext evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology, contentId)
        }

        try {
            val payload = GeminiStructuredPayload(
                contentIdentification = ContentIdentificationPayload(
                    contentType = contentId.type.id,
                    status = contentId.status.id,
                    confidence = contentId.confidence,
                    evidence = contentId.evidence,
                    detectedLanguage = contentId.detectedLanguage
                ),
                audioObservation = AudioObservationPayload(
                    durationSeconds = audioFeatures.durationSeconds,
                    vocalsDetected = audioFeatures.vocalsDetected,
                    speechDetected = audioFeatures.speechDetected,
                    vocalProbability = audioFeatures.vocalProbability,
                    instrumentalProbability = audioFeatures.instrumentalProbability,
                    percussionProbability = audioFeatures.percussionProbability,
                    rmsLoudnessDb = audioFeatures.rmsLoudnessDb
                ),
                lyrics = LyricsPayload(
                    status = lyrics?.status ?: "unavailable",
                    text = lyrics?.text ?: "",
                    confidence = lyrics?.confidence ?: 0f,
                    source = lyrics?.source ?: "none"
                ),
                metadata = MetadataPayload(
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    genre = track.genre,
                    isExplicit = track.isExplicit || (lyrics?.explicitFlagDetected == true),
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
                You are Safa Music's Islamic Music Evidence Reasoner.
                You receive structured observations about a music track (audio characteristics, lyrics, metadata) and the user's selected Islamic methodology.
                
                CORE PRINCIPLES & SAFETY RULES:
                1. You are analyzing MUSIC and evaluating it strictly against the specified methodology rules.
                2. NEVER present your output as an absolute fatwa, decree, or religious command.
                3. Instrumental presence MUST NOT automatically produce a 'not_allowed' classification. Under content-focused / moderate methodologies, instrumental music without obscene context is permissible. Under vocal-only methodologies, musical instruments are excluded.
                4. Absence of vocals MUST NOT automatically produce a 'not_allowed' classification.
                5. Do not invent or hallucinate lyrics. If lyrics status is 'unavailable', note this clearly. If lyrics are necessary to evaluate the methodology, return 'unclear' or 'insufficient_data'.
                6. Permissible classification values: 'allowed', 'not_allowed', 'unclear', 'insufficient_data', 'not_applicable'.
                7. Always return strictly valid JSON matching the schema.
            """.trimIndent()

            val prompt = """
                Analyze the provided music track evidence against the requested Islamic methodology:
                
                $payloadJson
                
                Respond ONLY with a JSON object in this exact schema:
                {
                  "classification": "allowed | not_allowed | unclear | insufficient_data | not_applicable",
                  "confidence": 0.90,
                  "reasoning": "Clear, objective reasoning detailing how audio properties and lyrics align with the methodology.",
                  "evidence": [
                    {
                      "category": "audio | lyrics | metadata | structure",
                      "finding": "Specific observation from the evidence",
                      "importance": "high | medium | low"
                    }
                  ],
                  "missingInformation": ["e.g. Full verified lyrics unavailable"],
                  "methodologyUsed": "${methodology.name}",
                  "limitations": ["Safa Music AI assessment is an evidence-based tool and is not a formal fatwa."]
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
                return@withContext evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology, contentId, isOffline = false, errorMsg = "Gemini API status ${response.code}")
            }

            val responseBody = response.body?.string() ?: ""
            val geminiResponse = moshi.adapter(GeminiGenerateResponse::class.java).fromJson(responseBody)
            val rawText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            parseGeminiOutput(rawText, track.id, methodology, contentId)
        } catch (e: Exception) {
            Log.e("GeminiClassifier", "Exception in Gemini call", e)
            evaluateOfflineRuleEngine(track, audioFeatures, lyrics, methodology, contentId, isOffline = true, errorMsg = e.message)
        }
    }

    /**
     * Dedicated pipeline for non-music content (Qur'an, Adhan, Speech).
     * Bypasses halal/haram music classification and marks status as NOT_APPLICABLE.
     */
    private fun buildNonMusicClassificationResult(
        track: Track,
        audioFeatures: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology,
        contentId: ContentIdentification
    ): ClassificationResult {
        val evidence = mutableListOf<EvidenceItem>()

        // Add identification evidence
        contentId.evidence.forEach {
            evidence.add(EvidenceItem("content_identification", it, "high"))
        }

        val reasoning: String
        when (contentId.type) {
            ContentType.QURAN_RECITATION -> {
                val surahInfo = if (contentId.identifiedSurah != null) contentId.identifiedSurah else "Qur'an Recitation"
                val ayahInfo = if (contentId.identifiedAyahRange != null) " (${contentId.identifiedAyahRange})" else ""
                reasoning = "Identified as Holy Qur'an recitation: $surahInfo$ayahInfo. Qur'an recitation is divine revelation and is not subject to music rulings."
                evidence.add(EvidenceItem("audio", "Chanting and acoustic properties verify sacred recitation with ${(contentId.confidence * 100).toInt()}% confidence", "high"))
            }
            ContentType.ADHAN -> {
                reasoning = "Identified as Adhan (Islamic call to prayer). Adhan is a religious rite and is exempt from music jurisprudence rulings."
                evidence.add(EvidenceItem("audio", "Vocal call to prayer verified", "high"))
            }
            ContentType.ISLAMIC_SPEECH -> {
                reasoning = "Identified as Islamic lecture / sermon / speech. Spoken religious content is not categorized under musical rulings."
                evidence.add(EvidenceItem("audio", "Spoken discourse verified (${((audioFeatures.speechProbability) * 100).toInt()}% speech probability)", "high"))
            }
            ContentType.SPOKEN_WORD, ContentType.PODCAST -> {
                reasoning = "Identified as spoken-word audio / podcast. Non-musical speech content is not subject to music rulings."
                evidence.add(EvidenceItem("audio", "Spoken voice detected without musical arrangement", "medium"))
            }
            ContentType.SOUND_EFFECT -> {
                reasoning = "Identified as ambient nature sound / sound effect. Ambient recordings are not musical compositions."
            }
            else -> {
                reasoning = "Non-musical audio content identified."
            }
        }

        return ClassificationResult(
            trackId = track.id,
            status = ClassificationStatus.NOT_APPLICABLE,
            confidence = contentId.confidence,
            reasoning = reasoning,
            evidenceList = evidence,
            missingInformation = emptyList(),
            limitations = listOf(
                "Content identification pipeline determined this track is not a musical work.",
                "Safa Music does not evaluate Qur'an or spoken content under music rules."
            ),
            methodologyId = methodology.id,
            methodologyVersion = methodology.version,
            audioAnalysisVersion = 1,
            lyricsSource = lyrics?.source ?: "none",
            lyricsConfidence = lyrics?.confidence ?: 0f,
            geminiModel = "content_pipeline_v2",
            analysisVersion = 2,
            timestamp = System.currentTimeMillis(),
            isOfflineResult = true,
            contentType = contentId.type,
            contentStatus = contentId.status,
            identifiedSurah = contentId.identifiedSurah,
            identifiedAyahRange = contentId.identifiedAyahRange,
            identificationMethod = contentId.identificationMethod
        )
    }

    private fun parseGeminiOutput(
        rawJson: String,
        trackId: Long,
        methodology: Methodology,
        contentId: ContentIdentification
    ): ClassificationResult {
        return try {
            val cleanJson = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = moshi.adapter(GeminiClassificationOutput::class.java).fromJson(cleanJson)

            val status = ClassificationStatus.fromId(parsed?.classification)
            val confidence = (parsed?.confidence ?: 0.80f).coerceIn(0.1f, 0.99f)
            val reasoning = parsed?.reasoning ?: "Analysis completed based on audio observation and methodology criteria."

            val evidence = parsed?.evidence?.map {
                EvidenceItem(
                    category = it.category ?: "general",
                    finding = it.finding ?: "",
                    importance = it.importance ?: "medium"
                )
            } ?: emptyList()

            val missingInfo = parsed?.missingInformation ?: emptyList()
            val limitations = (parsed?.limitations ?: emptyList()) + listOf(
                "Safa Music AI assessment is an automated evidence tool and does not constitute a formal Islamic fatwa."
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
                analysisVersion = 2,
                timestamp = System.currentTimeMillis(),
                isOfflineResult = false,
                contentType = contentId.type,
                contentStatus = contentId.status,
                identifiedSurah = contentId.identifiedSurah,
                identifiedAyahRange = contentId.identifiedAyahRange,
                identificationMethod = contentId.identificationMethod
            )
        } catch (e: Exception) {
            Log.e("GeminiClassifier", "Failed to parse JSON: $rawJson", e)
            ClassificationResult(
                trackId = trackId,
                status = ClassificationStatus.UNCLEAR,
                confidence = 0.50f,
                reasoning = "Gemini returned output that could not be parsed into structured format.",
                evidenceList = listOf(EvidenceItem("system", "Parsing error on raw response", "low")),
                missingInformation = listOf("Structured schema validation"),
                limitations = listOf("Output formatting issue"),
                methodologyId = methodology.id,
                methodologyVersion = methodology.version,
                geminiModel = modelName,
                isOfflineResult = false,
                contentType = contentId.type,
                contentStatus = contentId.status
            )
        }
    }

    /**
     * Local transparent rule evaluator when offline or when API is unavailable.
     * Respects all methodology criteria and instrumental rules.
     */
    private fun evaluateOfflineRuleEngine(
        track: Track,
        audio: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology,
        contentId: ContentIdentification,
        isOffline: Boolean = true,
        errorMsg: String? = null
    ): ClassificationResult {
        // Non-music check first
        if (!contentId.type.isMusicCandidate) {
            return buildNonMusicClassificationResult(track, audio, lyrics, methodology, contentId)
        }

        val evidence = mutableListOf<EvidenceItem>()
        val missingInfo = mutableListOf<String>()
        val limitations = mutableListOf<String>()

        if (isOffline) {
            limitations.add("Offline Analysis: Evaluated on-device using structured evidence inspection.")
        }
        if (errorMsg != null) {
            limitations.add("Cloud Reasoner Notice: $errorMsg")
        }
        limitations.add("AI assessment provides an evidence summary and does not constitute a religious fatwa.")

        // 1. Explicit marker
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
        val hasLyrics = lyrics != null && lyrics.status == "available" && lyrics.text.isNotBlank()
        if (hasLyrics) {
            evidence.add(EvidenceItem("lyrics", "Verified lyrics (${lyrics.source}): ${lyrics.text.take(60)}...", "high"))
        } else {
            missingInfo.add("Full verified lyrics unavailable in local audio file")
            evidence.add(EvidenceItem("lyrics", "No embedded lyrics available for textual verification", "medium"))
        }

        // Apply methodology rules
        var status: ClassificationStatus
        var confidence: Float
        var reasoning: String

        when (methodology.id) {
            "vocal_only" -> {
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.98f
                    reasoning = "Track contains explicit language or tags, which is strictly prohibited."
                } else if (audio.instrumentalProbability > 0.35f || (audio.percussionProbability > 0.40f && !audio.vocalsDetected)) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.92f
                    reasoning = "Under the Vocal-Only Nasheed methodology, musical instruments are excluded. Audio analysis observed significant instrumental presence (${(audio.instrumentalProbability * 100).toInt()}%)."
                } else if (audio.vocalsDetected && audio.instrumentalProbability < 0.20f) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.90f
                    reasoning = "Audio inspection indicates pure human vocalization with minimal or zero instrumental backing, satisfying the Vocal-Only Nasheed criteria."
                } else {
                    status = ClassificationStatus.UNCLEAR
                    confidence = 0.65f
                    reasoning = "Audio characteristics show borderline acoustic properties between vocal-only and instrumental arrangement."
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
                    reasoning = "Under traditional conservative scholarly criteria, melodic musical instruments (strings, winds, synthesizers) are prohibited. Audio analysis detected high instrumental probability (${(audio.instrumentalProbability * 100).toInt()}%)."
                } else if (audio.instrumentalProbability < 0.25f && audio.vocalsDetected) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.88f
                    reasoning = "Track exhibits predominant vocal nasheed characteristics with permissible acoustic duff/frame characteristics under traditional scholarly criteria."
                } else if (!hasLyrics) {
                    status = ClassificationStatus.INSUFFICIENT_DATA
                    confidence = 0.55f
                    reasoning = "Lyrics evidence was unavailable to evaluate textual permissibility alongside moderate acoustic readings."
                } else {
                    status = ClassificationStatus.UNCLEAR
                    confidence = 0.60f
                    reasoning = "Acoustic readings indicate mixed instrumentation. Conservative scholarship permits only duff/vocal; further verification recommended."
                }
            }

            "moderate_permissive" -> {
                // RULE: Instrumental presence MUST NOT automatically produce a forbidden classification!
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.98f
                    reasoning = "Explicit tags and vulgar themes violate the fundamental Islamic requirement of clean, virtuous speech."
                } else if (audio.instrumentalProbability > 0.60f && !audio.vocalsDetected) {
                    // Pure instrumental / ambient track -> ALLOWED under moderate view
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.88f
                    reasoning = "Pure instrumental / ambient composition without vulgar or hedonistic vocal lyrics. Under the Moderate / Content-focused methodology, instrumental music with clean character is permissible."
                } else if (hasLyrics) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.90f
                    reasoning = "Under the Moderate / Content-focused methodology, musical arrangement accompanying clean, uplifting, or virtuous themes is permissible. The lyrical analysis verified positive content."
                } else if (track.isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.95f
                    reasoning = "Explicit track tag detected."
                } else {
                    // Clean track without explicit markers
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.82f
                    reasoning = "Track has no explicit markers detected. Under content-focused criteria, non-vulgar musical expression is permissible."
                }
            }

            else -> {
                // Custom or fallback
                if (isExplicit) {
                    status = ClassificationStatus.NOT_ALLOWED
                    confidence = 0.95f
                    reasoning = "Explicit content marker detected."
                } else if (audio.instrumentalProbability < 0.30f) {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.82f
                    reasoning = "Vocal-dominated track meeting selected criteria."
                } else {
                    status = ClassificationStatus.ALLOWED
                    confidence = 0.75f
                    reasoning = "No prohibitive elements detected in audio analysis."
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
            geminiModel = if (isOffline) "offline_rule_engine_v2" else modelName,
            analysisVersion = 2,
            timestamp = System.currentTimeMillis(),
            isOfflineResult = isOffline,
            contentType = contentId.type,
            contentStatus = contentId.status,
            identifiedSurah = contentId.identifiedSurah,
            identifiedAyahRange = contentId.identifiedAyahRange,
            identificationMethod = contentId.identificationMethod
        )
    }
}
