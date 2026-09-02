package com.example.ui.screens.scanner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Methodology
import com.example.data.model.ScanProgress
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.scanner.LibraryScannerManager
import com.example.data.worker.TrackAnalysisScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ScannerUiState(
    val scanProgress: ScanProgress = ScanProgress(),
    val activeMethodology: Methodology? = null,
    val totalLibraryTracks: Int = 0
)

class ScannerViewModel(
    private val repository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val scannerManager: LibraryScannerManager
) : ViewModel() {

    val uiState: StateFlow<ScannerUiState> = combine(
        scannerManager.scanProgress,
        preferencesRepository.activeMethodologyId,
        repository.getAllTracks()
    ) { progress, methodologyId, tracks ->
        val methodology = repository.getMethodologyById(methodologyId)
        ScannerUiState(
            scanProgress = progress,
            activeMethodology = methodology,
            totalLibraryTracks = tracks.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    fun startScan(forceReanalyze: Boolean = false) {
        scannerManager.startScan(forceReanalyze)
    }

    fun pauseScan() {
        scannerManager.pauseScan()
    }

    fun resumeScan() {
        scannerManager.resumeScan()
    }

    fun cancelScan() {
        scannerManager.cancelScan()
    }

    fun triggerBackgroundAnalysis(context: Context, forceReanalyze: Boolean = false) {
        TrackAnalysisScheduler.scheduleOneTimeAnalysis(context, forceReanalyze)
    }

    fun cancelBackgroundAnalysis(context: Context) {
        TrackAnalysisScheduler.cancelAnalysis(context)
    }
}
