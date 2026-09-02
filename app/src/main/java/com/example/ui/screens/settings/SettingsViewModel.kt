package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DefaultMethodologies
import com.example.data.model.Methodology
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.scanner.LibraryScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeMethodologyId: String = DefaultMethodologies.CONSERVATIVE.id,
    val methodologies: List<Methodology> = DefaultMethodologies.ALL,
    val geminiModel: String = "gemini-3.5-flash",
    val autoScanEnabled: Boolean = true,
    val wifiOnlyAnalysis: Boolean = false,
    val normalizeVolume: Boolean = true,
    val playbackFilter: String = "all",
    val actionMessage: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val repository: MusicRepository,
    private val scannerManager: LibraryScannerManager
) : ViewModel() {

    private val _actionMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.activeMethodologyId,
        repository.getMethodologies(),
        preferencesRepository.geminiModel,
        preferencesRepository.autoScanEnabled,
        preferencesRepository.normalizeVolume
    ) { methId, methodologies, model, autoScan, normalize ->
        SettingsUiState(
            activeMethodologyId = methId,
            methodologies = if (methodologies.isNotEmpty()) methodologies else DefaultMethodologies.ALL,
            geminiModel = model,
            autoScanEnabled = autoScan,
            wifiOnlyAnalysis = false,
            normalizeVolume = normalize,
            playbackFilter = "all",
            actionMessage = _actionMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setActiveMethodology(id: String) {
        viewModelScope.launch {
            preferencesRepository.setActiveMethodologyId(id)
            _actionMessage.value = "Active methodology set to ${DefaultMethodologies.getById(id).name}"
        }
    }

    fun setGeminiModel(model: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiModel(model)
        }
    }

    fun setAutoScan(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoScanEnabled(enabled)
        }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWifiOnlyAnalysis(wifiOnly)
        }
    }

    fun setNormalizeVolume(normalize: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNormalizeVolume(normalize)
        }
    }

    fun setPlaybackFilter(filter: String) {
        viewModelScope.launch {
            preferencesRepository.setPlaybackFilter(filter)
        }
    }

    fun clearCachedAudio() {
        viewModelScope.launch {
            repository.clearCachedAudioAnalysis()
            _actionMessage.value = "Audio waveform and acoustic cache cleared."
        }
    }

    fun clearCachedLyrics() {
        viewModelScope.launch {
            repository.clearCachedLyrics()
            _actionMessage.value = "Lyrics cache cleared."
        }
    }

    fun resetAllAnalysis() {
        viewModelScope.launch {
            repository.clearAllAnalysisData()
            _actionMessage.value = "All analysis data reset. Starting fresh scan."
            scannerManager.startScan(forceReanalyze = true)
        }
    }

    fun triggerBackgroundAnalysis(context: android.content.Context) {
        com.example.data.worker.TrackAnalysisScheduler.scheduleOneTimeAnalysis(context, forceReanalyze = false)
        _actionMessage.value = "WorkManager background analysis queued for unanalyzed tracks."
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
