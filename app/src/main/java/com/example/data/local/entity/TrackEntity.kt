package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
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
    val playCount: Int = 0
) {
    fun toDomain(): Track = Track(
        id = id,
        uriString = uriString,
        filePath = filePath,
        title = title,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        durationMs = durationMs,
        mimeType = mimeType,
        bitrate = bitrate,
        sampleRate = sampleRate,
        albumArtUri = albumArtUri,
        isExplicit = isExplicit,
        genre = genre,
        dateAdded = dateAdded,
        fileSize = fileSize,
        fileHash = fileHash,
        isFavorite = isFavorite,
        lastPlayedTimestamp = lastPlayedTimestamp,
        playCount = playCount
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            uriString = track.uriString,
            filePath = track.filePath,
            title = track.title,
            artist = track.artist,
            album = track.album,
            albumArtist = track.albumArtist,
            durationMs = track.durationMs,
            mimeType = track.mimeType,
            bitrate = track.bitrate,
            sampleRate = track.sampleRate,
            albumArtUri = track.albumArtUri,
            isExplicit = track.isExplicit,
            genre = track.genre,
            dateAdded = track.dateAdded,
            fileSize = track.fileSize,
            fileHash = track.fileHash,
            isFavorite = track.isFavorite,
            lastPlayedTimestamp = track.lastPlayedTimestamp,
            playCount = track.playCount
        )
    }
}
