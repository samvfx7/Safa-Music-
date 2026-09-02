package com.example.data.model

data class AudioFeatures(
    val trackId: Long,
    val durationSeconds: Long = 180L,
    val channels: Int = 2,
    val sampleRate: Int = 44100,
    val bitrateKbps: Int = 320,
    val rmsLoudnessDb: Float = -14.0f,
    val peakAmplitude: Float = 0.95f,
    val silenceSectionsCount: Int = 0,
    val vocalsDetected: Boolean = true,
    val vocalProbability: Float = 0.8f,
    val speechDetected: Boolean = false,
    val speechProbability: Float = 0.1f,
    val instrumentalProbability: Float = 0.5f,
    val percussionProbability: Float = 0.4f,
    val waveformPoints: List<Float> = emptyList(),
    val spectrogramDescription: String = "",
    val analysisVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
