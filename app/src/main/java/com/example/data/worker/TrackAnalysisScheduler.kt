package com.example.data.worker

import android.content.Context
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
import java.util.concurrent.TimeUnit

object TrackAnalysisScheduler {

    /**
     * Enqueues an immediate asynchronous analysis task via WorkManager.
     * Iterates through unanalyzed tracks in the database and triggers the analysis pipeline.
     */
    fun scheduleOneTimeAnalysis(
        context: Context,
        forceReanalyze: Boolean = false,
        wifiOnly: Boolean = false
    ) {
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

        WorkManager.getInstance(context).enqueueUniqueWork(
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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            TrackAnalysisWorker.KEY_SINGLE_TRACK_ID to trackId
        )

        val workRequest = OneTimeWorkRequestBuilder<TrackAnalysisWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(TrackAnalysisWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${TrackAnalysisWorker.UNIQUE_WORK_NAME_ONE_TIME}_$trackId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancels any active or scheduled analysis background tasks.
     */
    fun cancelAnalysis(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(TrackAnalysisWorker.UNIQUE_WORK_NAME_ONE_TIME)
        workManager.cancelAllWorkByTag(TrackAnalysisWorker.TAG)
    }

    /**
     * Observes the status and progress of the one-time track analysis task.
     */
    fun getWorkInfosFlow(context: Context): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(TrackAnalysisWorker.UNIQUE_WORK_NAME_ONE_TIME)
    }

    /**
     * Observes all work tagged with TrackAnalysisWorker.TAG
     */
    fun getAllTrackAnalysisWorkInfosFlow(context: Context): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(TrackAnalysisWorker.TAG)
    }
}
