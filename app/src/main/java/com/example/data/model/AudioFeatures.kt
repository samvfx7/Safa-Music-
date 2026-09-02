package com.example.data.model

data class AudioFeatures(
    val trackId: Long,
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
    val waveformPoints: List<Float> = emptyList(),
    val spectrogramDescription: String = "",
    val analysisVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
