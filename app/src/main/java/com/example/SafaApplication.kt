package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.player.SafaPlayerManager
import com.example.data.remote.GeminiMusicClassifier
import com.example.data.remote.LyricsProvider
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import com.example.data.repository.PreferencesRepository
import com.example.data.scanner.AudioProcessor
import com.example.data.scanner.LibraryScannerManager
import com.example.data.scanner.MediaStoreScanner

class SafaApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var mediaStoreScanner: MediaStoreScanner
        private set

    lateinit var audioProcessor: AudioProcessor
        private set

    lateinit var lyricsProvider: LyricsProvider
        private set

    lateinit var musicClassifier: GeminiMusicClassifier
        private set

    lateinit var libraryScannerManager: LibraryScannerManager
        private set

    lateinit var playerManager: SafaPlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        musicRepository = MusicRepositoryImpl(database)
        preferencesRepository = PreferencesRepository(this)
        mediaStoreScanner = MediaStoreScanner(this)
        audioProcessor = AudioProcessor(this)
        lyricsProvider = LyricsProvider(this)
        musicClassifier = GeminiMusicClassifier(this)

        libraryScannerManager = LibraryScannerManager(
            context = this,
            repository = musicRepository,
            preferencesRepository = preferencesRepository,
            mediaStoreScanner = mediaStoreScanner,
            audioProcessor = audioProcessor,
            lyricsProvider = lyricsProvider,
            musicClassifier = musicClassifier
        )

        playerManager = SafaPlayerManager(this, musicRepository)

        // Schedule periodic background analysis for unanalyzed tracks via WorkManager
        com.example.data.worker.TrackAnalysisScheduler.schedulePeriodicAnalysis(
            context = this,
            repeatIntervalHours = 24
        )
    }

    companion object {
        lateinit var instance: SafaApplication
            private set
    }
}
