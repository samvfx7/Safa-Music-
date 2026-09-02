package com.example.data.repository

import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllTracks(): Flow<List<Track>>
    suspend fun getAllTracksSync(): List<Track>
    suspend fun getUnanalyzedTracksSync(methodologyId: String, methodologyVersion: Int): List<Track>
    fun getTrackById(id: Long): Flow<Track?>
    suspend fun getTrackByIdSync(id: Long): Track?
    suspend fun saveDiscoveredTracks(tracks: List<Track>)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
    suspend fun recordPlayback(id: Long)
    suspend fun saveAudioFeatures(features: AudioFeatures)
    suspend fun saveLyrics(lyrics: Lyrics)
    suspend fun saveClassificationResult(result: ClassificationResult)
    fun getMethodologies(): Flow<List<Methodology>>
    suspend fun getMethodologyById(id: String): Methodology
    suspend fun saveMethodology(methodology: Methodology)
    suspend fun clearAllAnalysisData()
    suspend fun clearCachedAudioAnalysis()
    suspend fun clearCachedLyrics()
    suspend fun deleteTrack(id: Long)
}
