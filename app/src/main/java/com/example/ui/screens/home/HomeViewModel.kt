package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClassificationStatus
import com.example.data.model.Methodology
import com.example.data.model.ScanProgress
import com.example.data.model.Track
import com.example.data.player.SafaPlayerManager
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.scanner.LibraryScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeStats(
    val totalTracks: Int = 0,
    val allowedCount: Int = 0,
    val notAllowedCount: Int = 0,
    val unclearCount: Int = 0,
    val insufficientDataCount: Int = 0,
    val notApplicableCount: Int = 0,
    val unanalyzedCount: Int = 0
)

data class HomeUiState(
    val tracks: List<Track> = emptyList(),
    val stats: HomeStats = HomeStats(),
    val recentTracks: List<Track> = emptyList(),
    val activeMethodology: Methodology? = null,
    val scanProgress: ScanProgress = ScanProgress(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val scannerManager: LibraryScannerManager,
    private val playerManager: SafaPlayerManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllTracks(),
        preferencesRepository.activeMethodologyId,
        scannerManager.scanProgress,
        _isLoading
    ) { tracks, methodologyId, scanProgress, isLoading ->
        val methodology = repository.getMethodologyById(methodologyId)

        val stats = HomeStats(
            totalTracks = tracks.size,
            allowedCount = tracks.count { it.status == ClassificationStatus.ALLOWED },
            notAllowedCount = tracks.count { it.status == ClassificationStatus.NOT_ALLOWED },
            unclearCount = tracks.count { it.status == ClassificationStatus.UNCLEAR },
            insufficientDataCount = tracks.count { it.status == ClassificationStatus.INSUFFICIENT_DATA },
            notApplicableCount = tracks.count { it.status == ClassificationStatus.NOT_APPLICABLE },
            unanalyzedCount = tracks.count { it.status == ClassificationStatus.UNANALYZED || it.status == ClassificationStatus.ANALYZING }
        )

        val recent = tracks.sortedByDescending {
            it.classification?.timestamp ?: it.lastPlayedTimestamp
        }.take(8)

        HomeUiState(
            tracks = tracks,
            stats = stats,
            recentTracks = recent,
            activeMethodology = methodology,
            scanProgress = scanProgress,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun startScan(forceReanalyze: Boolean = false) {
        scannerManager.startScan(forceReanalyze)
    }

    fun playTrack(track: Track) {
        val tracks = uiState.value.tracks
        playerManager.playTrack(track, tracks)
    }
}
