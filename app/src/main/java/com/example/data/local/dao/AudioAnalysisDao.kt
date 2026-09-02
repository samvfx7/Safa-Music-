package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AudioAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioAnalysisDao {
    @Query("SELECT * FROM audio_analyses WHERE trackId = :trackId LIMIT 1")
    fun getAudioAnalysisForTrack(trackId: Long): Flow<AudioAnalysisEntity?>

    @Query("SELECT * FROM audio_analyses WHERE trackId = :trackId LIMIT 1")
    suspend fun getAudioAnalysisForTrackSync(trackId: Long): AudioAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioAnalysis: AudioAnalysisEntity)

    @Query("DELETE FROM audio_analyses WHERE trackId = :trackId")
    suspend fun deleteForTrack(trackId: Long)

    @Query("DELETE FROM audio_analyses")
    suspend fun deleteAll()
}
