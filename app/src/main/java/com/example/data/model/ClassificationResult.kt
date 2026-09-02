package com.example.data.model

data class EvidenceItem(
    val category: String, // lyrics | audio | metadata | structure | content_identification
    val finding: String,
    val importance: String // high | medium | low
)

data class ClassificationResult(
    val trackId: Long,
    val status: ClassificationStatus,
    val confidence: Float, // 0.0 - 1.0
    val reasoning: String,
    val evidenceList: List<EvidenceItem> = emptyList(),
    val missingInformation: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val methodologyId: String,
    val methodologyVersion: Int = 1,
    val audioAnalysisVersion: Int = 1,
    val lyricsSource: String = "unknown",
    val lyricsConfidence: Float = 0f,
    val geminiModel: String = "gemini-3.5-flash",
    val analysisVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineResult: Boolean = false,
    val contentType: ContentType = ContentType.MUSIC,
    val contentStatus: ContentStatus = ContentStatus.CONFIRMED,
    val identifiedSurah: String? = null,
    val identifiedAyahRange: String? = null,
    val identificationMethod: String? = null
) {
    val isQuranRecitation: Boolean
        get() = contentType == ContentType.QURAN_RECITATION

    val isNonMusic: Boolean
        get() = !contentType.isMusicCandidate
}
