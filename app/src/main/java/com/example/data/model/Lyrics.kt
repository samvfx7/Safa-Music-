package com.example.data.model

data class Lyrics(
    val trackId: Long,
    val text: String,
    val status: String, // available | unavailable | partial
    val source: String, // embedded | authorized_provider | local_transcription
    val confidence: Float,
    val language: String = "en",
    val explicitFlagDetected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
