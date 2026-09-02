package com.example.data.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class SafaPlayerManager(
    private val context: Context,
    private val repository: MusicRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private var exoPlayer: ExoPlayer? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var currentIndex = 0

    init {
        initPlayer()
    }

    private fun initPlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startProgressTracking()
                        } else {
                            progressJob?.cancel()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            _durationMs.value = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                        } else if (playbackState == Player.STATE_ENDED) {
                            playNext()
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("SafaPlayerManager", "Failed to initialize ExoPlayer", e)
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = emptyList()) {
        if (newQueue.isNotEmpty()) {
            _queue.value = newQueue
            currentIndex = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.value = listOf(track)
            currentIndex = 0
        } else {
            currentIndex = _queue.value.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        }

        _currentTrack.value = track
        _durationMs.value = track.durationMs
        _currentPositionMs.value = 0L

        scope.launch(Dispatchers.IO) {
            repository.recordPlayback(track.id)
        }

        try {
            val mediaItem = if (track.uriString.startsWith("content://") || track.uriString.startsWith("file://") || track.uriString.startsWith("http")) {
                MediaItem.Builder()
                    .setUri(Uri.parse(track.uriString))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .build()
                    )
                    .build()
            } else {
                // Sample simulation media item
                MediaItem.Builder()
                    .setMediaId(track.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .build()
                    )
                    .build()
            }

            exoPlayer?.stop()
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.play()
            _isPlaying.value = true
            startProgressTracking()
        } catch (e: Exception) {
            Log.e("SafaPlayerManager", "Error playing track: ${track.title}", e)
            // Fallback playback simulation for testing
            _isPlaying.value = true
            startProgressTracking()
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer
        if (_isPlaying.value) {
            player?.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        } else {
            if (_currentTrack.value == null && _queue.value.isNotEmpty()) {
                playTrack(_queue.value[currentIndex])
            } else {
                player?.play()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1000L))
        _currentPositionMs.value = bounded
        exoPlayer?.seekTo(bounded)
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIdx = if (_isShuffle.value) {
            (0 until q.size).random()
        } else {
            (currentIndex + 1) % q.size
        }
        playTrack(q[nextIdx], q)
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val prevIdx = if (currentIndex > 0) currentIndex - 1 else q.size - 1
        playTrack(q[prevIdx], q)
    }

    fun toggleShuffle() {
        _isShuffle.update { !it }
    }

    fun toggleRepeat() {
        _repeatMode.update { mode ->
            when (mode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
        exoPlayer?.repeatMode = _repeatMode.value
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val realPos = exoPlayer?.currentPosition ?: 0L
                if (realPos > 0L) {
                    _currentPositionMs.value = realPos
                } else {
                    // Simulated progress tick if synthetic track
                    val newPos = _currentPositionMs.value + 1000L
                    if (newPos >= _durationMs.value && _durationMs.value > 0) {
                        playNext()
                    } else {
                        _currentPositionMs.value = newPos
                    }
                }
                delay(1000L)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}
