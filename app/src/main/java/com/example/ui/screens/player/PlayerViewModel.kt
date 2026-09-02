package com.example.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Track
import com.example.data.player.SafaPlayerManager
import com.example.data.repository.MusicRepository
import com.example.data.scanner.LibraryScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: Int = 0,
    val queue: List<Track> = emptyList(),
    val showLyricsTab: Boolean = false
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedPosition: String
        get() = formatMs(positionMs)

    val formattedDuration: String
        get() = formatMs(durationMs)

    private fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}

class PlayerViewModel(
    private val playerManager: SafaPlayerManager,
    private val repository: MusicRepository,
    private val scannerManager: LibraryScannerManager
) : ViewModel() {

    private val _showLyricsTab = MutableStateFlow(false)

    val uiState: StateFlow<PlayerUiState> = combine(
        playerManager.currentTrack,
        playerManager.isPlaying,
        playerManager.currentPositionMs,
        playerManager.durationMs,
        _showLyricsTab
    ) { track, isPlaying, pos, dur, lyricsTab ->
        // Fetch up to date track with latest details if available
        val freshTrack = if (track != null) {
            repository.getTrackByIdSync(track.id) ?: track
        } else null

        PlayerUiState(
            currentTrack = freshTrack,
            isPlaying = isPlaying,
            positionMs = pos,
            durationMs = dur.coerceAtLeast(freshTrack?.durationMs ?: 0L),
            isShuffle = playerManager.isShuffle.value,
            repeatMode = playerManager.repeatMode.value,
            queue = playerManager.queue.value,
            showLyricsTab = lyricsTab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun toggleLyricsTab() {
        _showLyricsTab.value = !_showLyricsTab.value
    }

    fun toggleFavorite() {
        val track = uiState.value.currentTrack ?: return
        viewModelScope.launch {
            repository.updateFavorite(track.id, !track.isFavorite)
        }
    }

    fun reanalyzeCurrentTrack() {
        val track = uiState.value.currentTrack ?: return
        scannerManager.scanSingleTrack(track.id)
    }
}
