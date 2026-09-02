package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json",
    @Json(name = "temperature") val temperature: Float? = 0.2f
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiStructuredPayload(
    @Json(name = "track") val track: TrackPayload,
    @Json(name = "audioAnalysis") val audioAnalysis: AudioAnalysisPayload,
    @Json(name = "lyrics") val lyrics: LyricsPayload,
    @Json(name = "metadata") val metadata: MetadataPayload,
    @Json(name = "methodology") val methodology: MethodologyPayload
)

@JsonClass(generateAdapter = true)
data class TrackPayload(
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String,
    @Json(name = "album") val album: String,
    @Json(name = "durationSeconds") val durationSeconds: Long
)

@JsonClass(generateAdapter = true)
data class AudioAnalysisPayload(
    @Json(name = "durationSeconds") val durationSeconds: Long,
    @Json(name = "vocalsDetected") val vocalsDetected: Boolean,
    @Json(name = "speechDetected") val speechDetected: Boolean,
    @Json(name = "vocalProbability") val vocalProbability: Float,
    @Json(name = "instrumentalProbability") val instrumentalProbability: Float,
    @Json(name = "percussionProbability") val percussionProbability: Float,
    @Json(name = "rmsLoudnessDb") val rmsLoudnessDb: Float,
    @Json(name = "silenceSectionsCount") val silenceSectionsCount: Int
)

@JsonClass(generateAdapter = true)
data class LyricsPayload(
    @Json(name = "status") val status: String,
    @Json(name = "text") val text: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "source") val source: String
)

@JsonClass(generateAdapter = true)
data class MetadataPayload(
    @Json(name = "explicit") val explicit: Boolean,
    @Json(name = "genre") val genre: String,
    @Json(name = "filename") val filename: String
)

@JsonClass(generateAdapter = true)
data class MethodologyPayload(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "criteria") val criteria: String,
    @Json(name = "scholarlyContext") val scholarlyContext: String
)

@JsonClass(generateAdapter = true)
data class GeminiClassificationOutput(
    @Json(name = "classification") val classification: String?,
    @Json(name = "confidence") val confidence: Float?,
    @Json(name = "evidence") val evidence: List<EvidenceOutput>?,
    @Json(name = "reasoning") val reasoning: String?,
    @Json(name = "missingInformation") val missingInformation: List<String>?,
    @Json(name = "methodologyUsed") val methodologyUsed: String?,
    @Json(name = "limitations") val limitations: List<String>?
)

@JsonClass(generateAdapter = true)
data class EvidenceOutput(
    @Json(name = "category") val category: String?,
    @Json(name = "finding") val finding: String?,
    @Json(name = "importance") val importance: String?
)
