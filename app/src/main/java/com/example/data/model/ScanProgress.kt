package com.example.data.model

enum class ScanStage(val title: String, val icon: String) {
    IDLE("Idle", "○"),
    READING_FILE("Reading audio file", "📂"),
    ANALYZING_AUDIO("Processing waveform & acoustic properties", "🔊"),
    FINDING_LYRICS("Extracting & verifying text/lyrics", "📝"),
    CONTENT_IDENTIFICATION("Identifying content type (Qur'an / Speech / Music)", "🔍"),
    GEMINI_ASSESSMENT("Islamic methodology reasoning", "✨"),
    APPLYING_METHODOLOGY("Validating evidence & schema", "⚖️"),
    SAVING_RESULT("Recording evidence to library", "💾"),
    COMPLETED("Track scan complete", "✓"),
    ERROR("Scan encountered error", "⚠️")
}

data class ScanProgress(
    val isScanning: Boolean = false,
    val isPaused: Boolean = false,
    val totalTracks: Int = 0,
    val analyzedTracks: Int = 0,
    val currentlyAnalyzingIndex: Int = 0,
    val currentTrackTitle: String = "",
    val currentTrackArtist: String = "",
    val currentStage: ScanStage = ScanStage.IDLE,
    val completedStages: Set<ScanStage> = emptySet(),
    val queuedTracksCount: Int = 0,
    val failedTracksCount: Int = 0,
    val currentError: String? = null
) {
    val progressFraction: Float
        get() = if (totalTracks > 0) analyzedTracks.toFloat() / totalTracks.toFloat() else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt().coerceIn(0, 100)
}
