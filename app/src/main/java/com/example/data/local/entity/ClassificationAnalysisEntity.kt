package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ClassificationResult
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentStatus
import com.example.data.model.ContentType
import com.example.data.model.EvidenceItem

@Entity(
    tableName = "classification_analyses",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackId"], unique = true)]
)
data class ClassificationAnalysisEntity(
    @PrimaryKey val trackId: Long,
    val statusId: String,
    val confidence: Float,
    val reasoning: String,
    val evidenceList: List<EvidenceItem>,
    val missingInformation: List<String>,
    val limitations: List<String>,
    val methodologyId: String,
    val methodologyVersion: Int = 1,
    val audioAnalysisVersion: Int = 1,
    val lyricsSource: String = "unknown",
    val lyricsConfidence: Float = 0f,
    val geminiModel: String = "gemini-3.5-flash",
    val analysisVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineResult: Boolean = false,
    val contentTypeId: String = "music",
    val contentStatusId: String = "confirmed",
    val identifiedSurah: String? = null,
    val identifiedAyahRange: String? = null,
    val identificationMethod: String? = null
) {
    fun toDomain(): ClassificationResult = ClassificationResult(
        trackId = trackId,
        status = ClassificationStatus.fromId(statusId),
        confidence = confidence,
        reasoning = reasoning,
        evidenceList = evidenceList,
        missingInformation = missingInformation,
        limitations = limitations,
        methodologyId = methodologyId,
        methodologyVersion = methodologyVersion,
        audioAnalysisVersion = audioAnalysisVersion,
        lyricsSource = lyricsSource,
        lyricsConfidence = lyricsConfidence,
        geminiModel = geminiModel,
        analysisVersion = analysisVersion,
        timestamp = timestamp,
        isOfflineResult = isOfflineResult,
        contentType = ContentType.fromId(contentTypeId),
        contentStatus = ContentStatus.fromId(contentStatusId),
        identifiedSurah = identifiedSurah,
        identifiedAyahRange = identifiedAyahRange,
        identificationMethod = identificationMethod
    )

    companion object {
        fun fromDomain(result: ClassificationResult): ClassificationAnalysisEntity =
            ClassificationAnalysisEntity(
                trackId = result.trackId,
                statusId = result.status.id,
                confidence = result.confidence,
                reasoning = result.reasoning,
                evidenceList = result.evidenceList,
                missingInformation = result.missingInformation,
                limitations = result.limitations,
                methodologyId = result.methodologyId,
                methodologyVersion = result.methodologyVersion,
                audioAnalysisVersion = result.audioAnalysisVersion,
                lyricsSource = result.lyricsSource,
                lyricsConfidence = result.lyricsConfidence,
                geminiModel = result.geminiModel,
                analysisVersion = result.analysisVersion,
                timestamp = result.timestamp,
                isOfflineResult = result.isOfflineResult,
                contentTypeId = result.contentType.id,
                contentStatusId = result.contentStatus.id,
                identifiedSurah = result.identifiedSurah,
                identifiedAyahRange = result.identifiedAyahRange,
                identificationMethod = result.identificationMethod
            )
    }
}
