package com.example.data.scanner

import android.content.Context
import android.util.Log
import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.ScanProgress
import com.example.data.model.ScanStage
import com.example.data.model.Track
import com.example.data.remote.LyricsProvider
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository
import com.example.domain.classifier.MusicClassifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryScannerManager(
    private val context: Context,
    private val repository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val mediaStoreScanner: MediaStoreScanner,
    private val audioProcessor: AudioProcessor,
    private val lyricsProvider: LyricsProvider,
    private val musicClassifier: MusicClassifier
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var scanJob: Job? = null

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    @Volatile
    private var isPaused = false

    fun startScan(forceReanalyze: Boolean = false) {
        if (scanJob?.isActive == true && !isPaused) return

        if (isPaused) {
            isPaused = false
            _scanProgress.update { it.copy(isPaused = false) }
            return
        }

        scanJob = scope.launch {
            try {
                _scanProgress.update {
                    it.copy(
                        isScanning = true,
                        isPaused = false,
                        currentStage = ScanStage.READING_FILE,
                        currentError = null
                    )
                }

                // 1. Discover tracks from storage
                val discoveredTracks = mediaStoreScanner.scanDeviceAudioFiles()
                repository.saveDiscoveredTracks(discoveredTracks)

                // 2. Fetch fresh tracks from DB to get existing analysis state
                val allTracks = repository.getAllTracks().first()
                val activeMethodologyId = preferencesRepository.activeMethodologyId.first()
                val activeMethodology = repository.getMethodologyById(activeMethodologyId)

                val tracksToAnalyze = if (forceReanalyze) {
                    allTracks
                } else {
                    allTracks.filter { track ->
                        track.classification == null ||
                                track.classification.methodologyId != activeMethodology.id ||
                                track.classification.methodologyVersion != activeMethodology.version
                    }
                }

                _scanProgress.update {
                    it.copy(
                        totalTracks = allTracks.size,
                        analyzedTracks = allTracks.size - tracksToAnalyze.size,
                        queuedTracksCount = tracksToAnalyze.size
                    )
                }

                for ((index, track) in tracksToAnalyze.withIndex()) {
                    while (isPaused) {
                        delay(500)
                    }

                    _scanProgress.update {
                        it.copy(
                            currentlyAnalyzingIndex = index + 1,
                            currentTrackTitle = track.title,
                            currentTrackArtist = track.artist,
                            completedStages = emptySet()
                        )
                    }

                    processSingleTrackPipeline(track, activeMethodology)

                    _scanProgress.update {
                        it.copy(
                            analyzedTracks = it.analyzedTracks + 1,
                            queuedTracksCount = (it.queuedTracksCount - 1).coerceAtLeast(0)
                        )
                    }
                }

                _scanProgress.update {
                    it.copy(
                        isScanning = false,
                        currentStage = ScanStage.COMPLETED
                    )
                }

            } catch (e: CancellationException) {
                Log.d("LibraryScanner", "Scan was cancelled")
                _scanProgress.update { it.copy(isScanning = false, currentStage = ScanStage.IDLE) }
            } catch (e: Exception) {
                Log.e("LibraryScanner", "Error during scan", e)
                _scanProgress.update {
                    it.copy(
                        isScanning = false,
                        currentStage = ScanStage.ERROR,
                        currentError = e.message ?: "Unknown scanning error"
                    )
                }
            }
        }
    }

    fun scanSingleTrack(trackId: Long) {
        scope.launch {
            try {
                val track = repository.getTrackByIdSync(trackId) ?: return@launch
                val activeMethodologyId = preferencesRepository.activeMethodologyId.first()
                val activeMethodology = repository.getMethodologyById(activeMethodologyId)

                _scanProgress.update {
                    it.copy(
                        isScanning = true,
                        totalTracks = 1,
                        analyzedTracks = 0,
                        currentTrackTitle = track.title,
                        currentTrackArtist = track.artist,
                        currentStage = ScanStage.READING_FILE,
                        completedStages = emptySet()
                    )
                }

                processSingleTrackPipeline(track, activeMethodology)

                _scanProgress.update {
                    it.copy(
                        isScanning = false,
                        analyzedTracks = 1,
                        currentStage = ScanStage.COMPLETED
                    )
                }
            } catch (e: Exception) {
                Log.e("LibraryScanner", "Error scanning single track", e)
                _scanProgress.update {
                    it.copy(
                        isScanning = false,
                        currentStage = ScanStage.ERROR,
                        currentError = e.message
                    )
                }
            }
        }
    }

    private suspend fun processSingleTrackPipeline(track: Track, methodology: Methodology) {
        val completed = mutableSetOf<ScanStage>()

        // Stage 1: Reading file
        _scanProgress.update { it.copy(currentStage = ScanStage.READING_FILE) }
        delay(120)
        completed.add(ScanStage.READING_FILE)
        _scanProgress.update { it.copy(completedStages = completed.toSet()) }

        // Stage 2: Audio Analysis
        _scanProgress.update { it.copy(currentStage = ScanStage.ANALYZING_AUDIO) }
        val audioFeatures = track.audioFeatures ?: audioProcessor.analyzeAudioFile(track)
        repository.saveAudioFeatures(audioFeatures)
        delay(150)
        completed.add(ScanStage.ANALYZING_AUDIO)
        _scanProgress.update { it.copy(completedStages = completed.toSet()) }

        // Stage 3: Lyrics Extraction & Verification
        _scanProgress.update { it.copy(currentStage = ScanStage.FINDING_LYRICS) }
        val lyrics = track.lyrics ?: lyricsProvider.extractOrFetchLyrics(track)
        repository.saveLyrics(lyrics)
        delay(120)
        completed.add(ScanStage.FINDING_LYRICS)
        _scanProgress.update { it.copy(completedStages = completed.toSet()) }

        // Stage 4: Gemini Classification Reasoning
        _scanProgress.update { it.copy(currentStage = ScanStage.GEMINI_ASSESSMENT) }
        val classification = musicClassifier.analyze(
            track = track,
            audioFeatures = audioFeatures,
            lyrics = lyrics,
            methodology = methodology
        )
        completed.add(ScanStage.GEMINI_ASSESSMENT)
        _scanProgress.update { it.copy(completedStages = completed.toSet()) }

        // Stage 5: Saving result
        _scanProgress.update { it.copy(currentStage = ScanStage.SAVING_RESULT) }
        repository.saveClassificationResult(classification)
        completed.add(ScanStage.SAVING_RESULT)
        _scanProgress.update { it.copy(completedStages = completed.toSet()) }
    }

    fun pauseScan() {
        isPaused = true
        _scanProgress.update { it.copy(isPaused = true) }
    }

    fun resumeScan() {
        isPaused = false
        _scanProgress.update { it.copy(isPaused = false) }
    }

    fun cancelScan() {
        scanJob?.cancel()
        isPaused = false
        _scanProgress.update { it.copy(isScanning = false, isPaused = false, currentStage = ScanStage.IDLE) }
    }
}
