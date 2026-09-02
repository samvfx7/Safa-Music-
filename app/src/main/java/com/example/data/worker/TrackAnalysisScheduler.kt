package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.concurrent.TimeUnit

object TrackAnalysisScheduler {

    private const val TAG = "TrackAnalysisScheduler"

    private fun getWorkManagerSafe(context: Context): WorkManager? {
        return try {
            WorkManager.getInstance(context)
        } catch (e: Exception) {
            Log.w(TAG, "WorkManager is not initialized: ${e.message}")
            null
        }
    }

    /**
     * Enqueues an immediate asynchronous analysis task via WorkManager.
     * Iterates through unanalyzed tracks in the database and triggers the analysis pipeline.
     */
    fun scheduleOneTimeAnalysis(
        context: Context,
        forceReanalyze: Boolean = false,
        wifiOnly: Boolean = false
    ) {
        val wm = getWorkManagerSafe(context) ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            TrackAnalysisWorker.KEY_FORCE_REANALYZE to forceReanalyze
        )

        val workRequest = OneTimeWorkRequestBuilder<TrackAnalysisWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(TrackAnalysisWorker.TAG)
            .build()

        wm.enqueueUniqueWork(
            TrackAnalysisWorker.UNIQUE_WORK_NAME_ONE_TIME,
            if (forceReanalyze) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Schedules periodic background analysis (e.g. daily) for new or unanalyzed tracks.
     */
    fun schedulePeriodicAnalysis(
        context: Context,
        repeatIntervalHours: Long = 24,
        wifiOnly: Boolean = false
    ) {
        val wm = getWorkManagerSafe(context) ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<TrackAnalysisWorker>(
            repeatIntervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(TrackAnalysisWorker.TAG)
            .build()

        wm.enqueueUniquePeriodicWork(
            TrackAnalysisWorker.UNIQUE_WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    /**
     * Schedules asynchronous analysis for a specific track.
     */
    fun scheduleSingleTrackAnalysis(
        context: Context,
        trackId: Long,
        wifiOnly: Boolean = false
    ) {
        val wm = getWorkManagerSafe(context) ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            TrackAnalysisWorker.KEY_FORCE_REANALYZE to true
        )

        val workRequest = OneTimeWorkRequestBuilder<TrackAnalysisWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("track_$trackId")
            .build()

        wm.enqueueUniqueWork(
            "analyze_track_$trackId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Observes current background work status.
     */
    fun getWorkInfosByTagFlow(context: Context, tag: String): Flow<List<WorkInfo>> {
        val wm = getWorkManagerSafe(context) ?: return emptyFlow()
        return wm.getWorkInfosByTagFlow(tag)
    }

    /**
     * Cancels any pending or active analysis work.
     */
    fun cancelAllAnalysisWork(context: Context) {
        val wm = getWorkManagerSafe(context) ?: return
        wm.cancelAllWorkByTag(TrackAnalysisWorker.TAG)
    }

    fun cancelAnalysis(context: Context) {
        cancelAllAnalysisWork(context)
    }
}
