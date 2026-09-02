package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.AudioAnalysisEntity
import com.example.data.local.entity.ClassificationAnalysisEntity
import com.example.data.local.entity.LyricsEntity
import com.example.data.local.entity.TrackEntity
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

data class TrackWithDetails(
    @Embedded val track: TrackEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "trackId"
    )
    val classification: ClassificationAnalysisEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "trackId"
    )
    val audioAnalysis: AudioAnalysisEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "trackId"
    )
    val lyrics: LyricsEntity?
) {
    fun toDomain(): Track {
        return track.toDomain().copy(
            classification = classification?.toDomain(),
            audioFeatures = audioAnalysis?.toDomain(),
            lyrics = lyrics?.toDomain()
        )
    }
}

@Dao
interface TrackDao {
    @Transaction
    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getAllTracksWithDetails(): Flow<List<TrackWithDetails>>

    @Transaction
    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    fun getTrackWithDetailsById(id: Long): Flow<TrackWithDetails?>

    @Transaction
    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackWithDetailsByIdSync(id: Long): TrackWithDetails?

    @Transaction
    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    suspend fun getAllTracksWithDetailsSync(): List<TrackWithDetails>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE tracks SET lastPlayedTimestamp = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun recordPlayback(id: Long, timestamp: Long)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: Long)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
}
