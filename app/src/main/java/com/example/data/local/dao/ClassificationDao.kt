package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ClassificationAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationDao {
    @Query("SELECT * FROM classification_analyses WHERE trackId = :trackId LIMIT 1")
    fun getClassificationForTrack(trackId: Long): Flow<ClassificationAnalysisEntity?>

    @Query("SELECT * FROM classification_analyses WHERE trackId = :trackId LIMIT 1")
    suspend fun getClassificationForTrackSync(trackId: Long): ClassificationAnalysisEntity?

    @Query("SELECT * FROM classification_analyses")
    fun getAllClassifications(): Flow<List<ClassificationAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classification: ClassificationAnalysisEntity)

    @Query("DELETE FROM classification_analyses WHERE trackId = :trackId")
    suspend fun deleteForTrack(trackId: Long)

    @Query("DELETE FROM classification_analyses")
    suspend fun deleteAll()
}
