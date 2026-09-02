package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MethodologyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MethodologyDao {
    @Query("SELECT * FROM methodologies ORDER BY isCustom ASC, id ASC")
    fun getAllMethodologies(): Flow<List<MethodologyEntity>>

    @Query("SELECT * FROM methodologies WHERE id = :id LIMIT 1")
    suspend fun getMethodologyById(id: String): MethodologyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethodologies(methodologies: List<MethodologyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethodology(methodology: MethodologyEntity)

    @Query("DELETE FROM methodologies WHERE id = :id")
    suspend fun deleteMethodology(id: String)
}
