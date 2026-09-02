package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.DefaultMethodologies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safa_music_settings")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_METHODOLOGY_ID = stringPreferencesKey("active_methodology_id")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val AUTO_SCAN_ENABLED = booleanPreferencesKey("auto_scan_enabled")
        val WIFI_ONLY_ANALYSIS = booleanPreferencesKey("wifi_only_analysis")
        val PLAYBACK_FILTER = stringPreferencesKey("playback_filter") // all, allowed_only, etc.
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val NORMALIZE_VOLUME = booleanPreferencesKey("normalize_volume")
        val FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
    }

    val activeMethodologyId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_METHODOLOGY_ID] ?: DefaultMethodologies.CONSERVATIVE.id
    }

    val geminiModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GEMINI_MODEL] ?: "gemini-3.5-flash"
    }

    val autoScanEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCAN_ENABLED] ?: true
    }

    val wifiOnlyAnalysis: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIFI_ONLY_ANALYSIS] ?: false
    }

    val playbackFilter: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PLAYBACK_FILTER] ?: "all"
    }

    val normalizeVolume: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NORMALIZE_VOLUME] ?: true
    }

    val firstRunCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_RUN_COMPLETED] ?: false
    }

    suspend fun setActiveMethodologyId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_METHODOLOGY_ID] = id
        }
    }

    suspend fun setGeminiModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_MODEL] = model
        }
    }

    suspend fun setAutoScanEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCAN_ENABLED] = enabled
        }
    }

    suspend fun setWifiOnlyAnalysis(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_ONLY_ANALYSIS] = wifiOnly
        }
    }

    suspend fun setPlaybackFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_FILTER] = filter
        }
    }

    suspend fun setNormalizeVolume(normalize: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NORMALIZE_VOLUME] = normalize
        }
    }

    suspend fun setFirstRunCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_RUN_COMPLETED] = completed
        }
    }
}
