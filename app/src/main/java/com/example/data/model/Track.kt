package com.example.data.model

import android.net.Uri

data class Track(
    val id: Long,
    val uriString: String,
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String = "",
    val durationMs: Long,
    val mimeType: String,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val albumArtUri: String? = null,
    val isExplicit: Boolean = false,
    val genre: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val fileSize: Long = 0,
    val fileHash: String = "",
    val isFavorite: Boolean = false,
    val lastPlayedTimestamp: Long = 0,
    val playCount: Int = 0,
    // Joined details
    val classification: ClassificationResult? = null,
    val audioFeatures: AudioFeatures? = null,
    val lyrics: Lyrics? = null
) {
    val status: ClassificationStatus
        get() = classification?.status ?: ClassificationStatus.UNANALYZED

    val confidencePercentage: Int
        get() = ((classification?.confidence ?: 0f) * 100).toInt()

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds % 60
            return "%d:%02d".format(minutes, remainingSeconds)
        }
}
