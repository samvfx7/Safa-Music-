package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AudioFeatures

@Entity(
    tableName = "audio_analyses",
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
data class AudioAnalysisEntity(
    @PrimaryKey val trackId: Long,
    val durationSeconds: Long,
    val channels: Int,
    val sampleRate: Int,
    val bitrateKbps: Int,
    val rmsLoudnessDb: Float,
    val peakAmplitude: Float,
    val silenceSectionsCount: Int,
    val vocalsDetected: Boolean,
    val vocalProbability: Float,
    val speechDetected: Boolean,
    val speechProbability: Float,
    val instrumentalProbability: Float,
    val percussionProbability: Float,
    val waveformPoints: List<Float>,
    val spectrogramDescription: String,
    val analysisVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): AudioFeatures = AudioFeatures(
        trackId = trackId,
        durationSeconds = durationSeconds,
        channels = channels,
        sampleRate = sampleRate,
        bitrateKbps = bitrateKbps,
        rmsLoudnessDb = rmsLoudnessDb,
        peakAmplitude = peakAmplitude,
        silenceSectionsCount = silenceSectionsCount,
        vocalsDetected = vocalsDetected,
        vocalProbability = vocalProbability,
        speechDetected = speechDetected,
        speechProbability = speechProbability,
        instrumentalProbability = instrumentalProbability,
        percussionProbability = percussionProbability,
        waveformPoints = waveformPoints,
        spectrogramDescription = spectrogramDescription,
        analysisVersion = analysisVersion,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(features: AudioFeatures): AudioAnalysisEntity = AudioAnalysisEntity(
            trackId = features.trackId,
            durationSeconds = features.durationSeconds,
            channels = features.channels,
            sampleRate = features.sampleRate,
            bitrateKbps = features.bitrateKbps,
            rmsLoudnessDb = features.rmsLoudnessDb,
            peakAmplitude = features.peakAmplitude,
            silenceSectionsCount = features.silenceSectionsCount,
            vocalsDetected = features.vocalsDetected,
            vocalProbability = features.vocalProbability,
            speechDetected = features.speechDetected,
            speechProbability = features.speechProbability,
            instrumentalProbability = features.instrumentalProbability,
            percussionProbability = features.percussionProbability,
            waveformPoints = features.waveformPoints,
            spectrogramDescription = features.spectrogramDescription,
            analysisVersion = features.analysisVersion,
            timestamp = features.timestamp
        )
    }
}
