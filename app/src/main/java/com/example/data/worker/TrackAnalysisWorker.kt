package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.SafaApplication
import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track
import com.example.data.remote.GeminiMusicClassifier
import com.example.data.remote.LyricsProvider
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.scanner.AudioProcessor
import com.example.domain.classifier.MusicClassifier
import kotlinx.coroutines.flow.first

class TrackAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "TrackAnalysisWorker"
        const val UNIQUE_WORK_NAME_ONE_TIME = "safa_track_analysis_one_time"
        const val UNIQUE_WORK_NAME_PERIODIC = "safa_track_analysis_periodic"

        const val KEY_FORCE_REANALYZE = "key_force_reanalyze"
        const val KEY_SINGLE_TRACK_ID = "key_single_track_id"
        const val KEY_PROGRESS = "key_progress"
        const val KEY_CURRENT_INDEX = "key_current_index"
        const val KEY_TOTAL_TRACKS = "key_total_tracks"
        const val KEY_CURRENT_TRACK_TITLE = "key_current_track_title"
        const val KEY_CURRENT_TRACK_ARTIST = "key_current_track_artist"
        const val KEY_ANALYZED_COUNT = "key_analyzed_count"
        const val KEY_ERROR_MESSAGE = "key_error_message"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting TrackAnalysisWorker task...")

        val app = applicationContext as? SafaApplication ?: SafaApplication.instance
        val repository: MusicRepository = app.musicRepository
        val preferencesRepository: PreferencesRepository = app.preferencesRepository
        val audioProcessor: AudioProcessor = app.audioProcessor
        val lyricsProvider: LyricsProvider = app.lyricsProvider
        val musicClassifier: MusicClassifier = app.musicClassifier

        val forceReanalyze = inputData.getBoolean(KEY_FORCE_REANALYZE, false)
        val singleTrackId = inputData.getLong(KEY_SINGLE_TRACK_ID, -1L)

        try {
            val activeMethodologyId = preferencesRepository.activeMethodologyId.first()
            val activeMethodology = repository.getMethodologyById(activeMethodologyId)

            val tracksToAnalyze: List<Track> = when {
                singleTrackId != -1L -> {
                    val track = repository.getTrackByIdSync(singleTrackId)
                    if (track != null) listOf(track) else emptyList()
                }
                forceReanalyze -> {
                    repository.getAllTracksSync()
                }
                else -> {
                    repository.getUnanalyzedTracksSync(
                        methodologyId = activeMethodology.id,
                        methodologyVersion = activeMethodology.version
                    )
                }
            }

            val totalCount = tracksToAnalyze.size
            Log.d(TAG, "Found $totalCount tracks requiring analysis for methodology ${activeMethodology.name}")

            if (totalCount == 0) {
                setProgress(
                    workDataOf(
                        KEY_PROGRESS to 100,
                        KEY_CURRENT_INDEX to 0,
                        KEY_TOTAL_TRACKS to 0,
                        KEY_ANALYZED_COUNT to 0
                    )
                )
                return Result.success(
                    workDataOf(
                        KEY_ANALYZED_COUNT to 0,
                        KEY_TOTAL_TRACKS to 0
                    )
                )
            }

            var analyzedCount = 0

            for ((index, track) in tracksToAnalyze.withIndex()) {
                if (isStopped) {
                    Log.w(TAG, "TrackAnalysisWorker cancelled/stopped by system or user")
                    return Result.retry()
                }

                val progress = ((index.toFloat() / totalCount) * 100).toInt()
                setProgress(
                    workDataOf(
                        KEY_PROGRESS to progress,
                        KEY_CURRENT_INDEX to (index + 1),
                        KEY_TOTAL_TRACKS to totalCount,
                        KEY_CURRENT_TRACK_TITLE to track.title,
                        KEY_CURRENT_TRACK_ARTIST to track.artist,
                        KEY_ANALYZED_COUNT to analyzedCount
                    )
                )

                try {
                    // Pipeline Stage 1: Audio Feature Extraction
                    val audioFeatures: AudioFeatures = track.audioFeatures
                        ?: audioProcessor.analyzeAudioFile(track)
                    repository.saveAudioFeatures(audioFeatures)

                    // Pipeline Stage 2: Lyrics Extraction & Verification
                    val lyrics: Lyrics = track.lyrics
                        ?: lyricsProvider.extractOrFetchLyrics(track)
                    repository.saveLyrics(lyrics)

                    // Pipeline Stage 3: Multimodal Islamic Classification Reasoning
                    val classification: ClassificationResult = musicClassifier.analyze(
                        track = track,
                        audioFeatures = audioFeatures,
                        lyrics = lyrics,
                        methodology = activeMethodology
                    )
                    repository.saveClassificationResult(classification)

                    analyzedCount++
                    Log.d(TAG, "Successfully analyzed track [${index + 1}/$totalCount]: ${track.title} -> ${classification.status}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error analyzing track ${track.title} (ID: ${track.id}): ${e.message}", e)
                    // Continue with next track so one corrupted file doesn't block the queue
                }
            }

            setProgress(
                workDataOf(
                    KEY_PROGRESS to 100,
                    KEY_CURRENT_INDEX to totalCount,
                    KEY_TOTAL_TRACKS to totalCount,
                    KEY_ANALYZED_COUNT to analyzedCount
                )
            )

            Log.d(TAG, "TrackAnalysisWorker completed. Successfully analyzed $analyzedCount / $totalCount tracks")
            return Result.success(
                workDataOf(
                    KEY_ANALYZED_COUNT to analyzedCount,
                    KEY_TOTAL_TRACKS to totalCount
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in TrackAnalysisWorker: ${e.message}", e)
            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(
                        KEY_ERROR_MESSAGE to (e.message ?: "Unknown analysis error")
                    )
                )
            }
        }
    }
}
