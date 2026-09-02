package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Lyrics

@Entity(
    tableName = "lyrics",
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
data class LyricsEntity(
    @PrimaryKey val trackId: Long,
    val text: String,
    val status: String,
    val source: String,
    val confidence: Float,
    val language: String = "en",
    val explicitFlagDetected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Lyrics = Lyrics(
        trackId = trackId,
        text = text,
        status = status,
        source = source,
        confidence = confidence,
        language = language,
        explicitFlagDetected = explicitFlagDetected,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(lyrics: Lyrics): LyricsEntity = LyricsEntity(
            trackId = lyrics.trackId,
            text = lyrics.text,
            status = lyrics.status,
            source = lyrics.source,
            confidence = lyrics.confidence,
            language = lyrics.language,
            explicitFlagDetected = lyrics.explicitFlagDetected,
            timestamp = lyrics.timestamp
        )
    }
}
