package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AudioAnalysisDao
import com.example.data.local.dao.ClassificationDao
import com.example.data.local.dao.LyricsDao
import com.example.data.local.dao.MethodologyDao
import com.example.data.local.dao.TrackDao
import com.example.data.local.entity.AudioAnalysisEntity
import com.example.data.local.entity.ClassificationAnalysisEntity
import com.example.data.local.entity.LyricsEntity
import com.example.data.local.entity.MethodologyEntity
import com.example.data.local.entity.TrackEntity
import com.example.data.model.DefaultMethodologies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrackEntity::class,
        AudioAnalysisEntity::class,
        LyricsEntity::class,
        ClassificationAnalysisEntity::class,
        MethodologyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun audioAnalysisDao(): AudioAnalysisDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun classificationDao(): ClassificationDao
    abstract fun methodologyDao(): MethodologyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safa_music.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).methodologyDao().insertMethodologies(
                                    DefaultMethodologies.ALL.map { MethodologyEntity.fromDomain(it) }
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
