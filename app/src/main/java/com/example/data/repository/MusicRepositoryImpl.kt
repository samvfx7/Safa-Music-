package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AudioAnalysisEntity
import com.example.data.local.entity.ClassificationAnalysisEntity
import com.example.data.local.entity.LyricsEntity
import com.example.data.local.entity.MethodologyEntity
import com.example.data.local.entity.TrackEntity
import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.DefaultMethodologies
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl(private val database: AppDatabase) : MusicRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        return database.trackDao().getAllTracksWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getAllTracksSync(): List<Track> {
        return database.trackDao().getAllTracksWithDetailsSync().map { it.toDomain() }
    }

    override suspend fun getUnanalyzedTracksSync(methodologyId: String, methodologyVersion: Int): List<Track> {
        val allTracks = getAllTracksSync()
        return allTracks.filter { track ->
            track.classification == null ||
                    track.classification.methodologyId != methodologyId ||
                    track.classification.methodologyVersion != methodologyVersion
        }
    }

    override fun getTrackById(id: Long): Flow<Track?> {
        return database.trackDao().getTrackWithDetailsById(id).map { it?.toDomain() }
    }

    override suspend fun getTrackByIdSync(id: Long): Track? {
        return database.trackDao().getTrackWithDetailsByIdSync(id)?.toDomain()
    }

    override suspend fun saveDiscoveredTracks(tracks: List<Track>) {
        val existingEntities = database.trackDao().getAllTracksWithDetailsSync().associateBy { it.track.id }
        val entitiesToInsert = tracks.map { track ->
            val existing = existingEntities[track.id]
            TrackEntity.fromDomain(
                track.copy(
                    isFavorite = existing?.track?.isFavorite ?: track.isFavorite,
                    playCount = existing?.track?.playCount ?: track.playCount,
                    lastPlayedTimestamp = existing?.track?.lastPlayedTimestamp ?: track.lastPlayedTimestamp
                )
            )
        }
        database.trackDao().insertTracks(entitiesToInsert)
    }

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        database.trackDao().updateFavorite(id, isFavorite)
    }

    override suspend fun recordPlayback(id: Long) {
        database.trackDao().recordPlayback(id, System.currentTimeMillis())
    }

    override suspend fun saveAudioFeatures(features: AudioFeatures) {
        database.audioAnalysisDao().insert(AudioAnalysisEntity.fromDomain(features))
    }

    override suspend fun saveLyrics(lyrics: Lyrics) {
        database.lyricsDao().insert(LyricsEntity.fromDomain(lyrics))
    }

    override suspend fun saveClassificationResult(result: ClassificationResult) {
        database.classificationDao().insert(ClassificationAnalysisEntity.fromDomain(result))
    }

    override fun getMethodologies(): Flow<List<Methodology>> {
        return database.methodologyDao().getAllMethodologies().map { list ->
            if (list.isEmpty()) {
                DefaultMethodologies.ALL
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun getMethodologyById(id: String): Methodology {
        val entity = database.methodologyDao().getMethodologyById(id)
        return entity?.toDomain() ?: DefaultMethodologies.getById(id)
    }

    override suspend fun saveMethodology(methodology: Methodology) {
        database.methodologyDao().insertMethodology(MethodologyEntity.fromDomain(methodology))
    }

    override suspend fun clearAllAnalysisData() {
        database.classificationDao().deleteAll()
        database.audioAnalysisDao().deleteAll()
        database.lyricsDao().deleteAll()
    }

    override suspend fun clearCachedAudioAnalysis() {
        database.audioAnalysisDao().deleteAll()
    }

    override suspend fun clearCachedLyrics() {
        database.lyricsDao().deleteAll()
    }

    override suspend fun deleteTrack(id: Long) {
        database.trackDao().deleteTrack(id)
    }
}
