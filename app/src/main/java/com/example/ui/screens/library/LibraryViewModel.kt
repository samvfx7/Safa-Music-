package com.example.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentType
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

enum class SortOption(val displayName: String) {
    DATE_ADDED("Date Added"),
    TITLE_AZ("Title (A-Z)"),
    ARTIST_AZ("Artist (A-Z)"),
    CONFIDENCE_HIGH("Highest Confidence")
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val selectedFilter: String = "all", // all | allowed | not_allowed | unclear | insufficient_data | not_applicable | unanalyzed | favorites
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_ADDED,
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val repository: MusicRepository,
    private val scannerManager: LibraryScannerManager,
    private val playerManager: SafaPlayerManager
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("all")
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.getAllTracks(),
        _selectedFilter,
        _searchQuery,
        _sortOption
    ) { allTracks, filter, query, sort ->
        val filtered = allTracks.filter { track ->
            val matchesFilter = when (filter) {
                "all" -> true
                "favorites" -> track.isFavorite
                "allowed" -> track.status == ClassificationStatus.ALLOWED
                "not_allowed" -> track.status == ClassificationStatus.NOT_ALLOWED
                "unclear" -> track.status == ClassificationStatus.UNCLEAR
                "insufficient_data" -> track.status == ClassificationStatus.INSUFFICIENT_DATA
                "not_applicable", "quran" -> track.status == ClassificationStatus.NOT_APPLICABLE || track.classification?.contentType == ContentType.QURAN_RECITATION
                "unanalyzed" -> track.status == ClassificationStatus.UNANALYZED || track.status == ClassificationStatus.ANALYZING
                else -> true
            }

            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                track.title.contains(query, ignoreCase = true) ||
                        track.artist.contains(query, ignoreCase = true) ||
                        track.album.contains(query, ignoreCase = true) ||
                        track.genre.contains(query, ignoreCase = true) ||
                        (track.classification?.identifiedSurah?.contains(query, ignoreCase = true) == true)
            }

            matchesFilter && matchesSearch
        }.let { list ->
            when (sort) {
                SortOption.DATE_ADDED -> list.sortedByDescending { it.dateAdded }
                SortOption.TITLE_AZ -> list.sortedBy { it.title.lowercase() }
                SortOption.ARTIST_AZ -> list.sortedBy { it.artist.lowercase() }
                SortOption.CONFIDENCE_HIGH -> list.sortedByDescending { it.classification?.confidence ?: 0f }
            }
        }

        LibraryUiState(
            tracks = allTracks,
            filteredTracks = filtered,
            selectedFilter = filter,
            searchQuery = query,
            sortOption = sort,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isLoading = true)
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.updateFavorite(track.id, !track.isFavorite)
        }
    }

    fun playTrack(track: Track) {
        val currentTracks = uiState.value.filteredTracks.ifEmpty { uiState.value.tracks }
        playerManager.playTrack(track, currentTracks)
    }

    fun reanalyzeTrack(track: Track) {
        scannerManager.scanSingleTrack(track.id)
    }
}
