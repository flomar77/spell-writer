package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.World
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Repository for persisting user progress using DataStore Preferences.
 * Story 2.3: Session Completion & Tracking
 *
 * Provides persistence for:
 * - Star progression (wizard and pirate worlds)
 * - Current world selection
 * - Session state for resume capability
 *
 * NFR3.1: Progress saved immediately after each word completion
 * NFR3.2: Progress saved on app backgrounding (within 100ms)
 * NFR3.3: Resume from last completed word on restart
 */
class ProgressRepository(private val context: Context) {

    // Extension property for DataStore instance
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "spell_writer_progress"
    )

    /**
     * Preference keys for all persisted data.
     */
    private object PreferencesKeys {
        val WIZARD_STARS = intPreferencesKey("wizard_stars")
        val PIRATE_STARS = intPreferencesKey("pirate_stars")
        val CURRENT_WORLD = intPreferencesKey("current_world")
        val LAST_SESSION_STAR = intPreferencesKey("last_session_star")
        val LAST_WORD_INDEX = intPreferencesKey("last_word_index")
    }

    /**
     * Flow of Progress data that automatically updates when DataStore changes.
     * Handles exceptions gracefully by emitting default Progress on errors.
     *
     * AC4: Load saved progress on app startup
     * NFR3.3: Resume from last completed word
     */
    val progressFlow: Flow<Progress> = context.dataStore.data
        .catch { exception ->
            // Handle DataStore read errors gracefully
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapToProgress(preferences)
        }

    /**
     * Save complete progress data to DataStore.
     * Called immediately after star completion.
     *
     * AC2, AC4: Star achievement saved immediately
     * NFR3.1: Progress saved after each word completion
     *
     * @param progress The Progress object to persist
     */
    suspend fun saveProgress(progress: Progress) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIZARD_STARS] = progress.wizardStars
            preferences[PreferencesKeys.PIRATE_STARS] = progress.pirateStars
            preferences[PreferencesKeys.CURRENT_WORLD] = progress.currentWorld.ordinal
        }
    }

    /**
     * Save current session state for resume capability.
     * Called on app backgrounding or session exit.
     *
     * AC6: Session state persistence on exit
     * NFR3.2: Save on backgrounding within 100ms
     *
     * @param starLevel Current star level being played
     * @param wordIndex Index of last completed word (0-19)
     */
    suspend fun saveSessionState(starLevel: Int, wordIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SESSION_STAR] = starLevel
            preferences[PreferencesKeys.LAST_WORD_INDEX] = wordIndex
        }
    }

    /**
     * Load session state for resume capability.
     * Returns null if no session state exists.
     *
     * AC6: Resume session from appropriate point
     * NFR3.3: Resume from last completed word
     *
     * @return Pair of (starLevel, wordIndex) or null if no saved state
     */
    suspend fun loadSessionState(): Pair<Int, Int>? {
        val preferences = context.dataStore.data.map { it }.catch { emit(emptyPreferences()) }
        var result: Pair<Int, Int>? = null

        preferences.collect { prefs ->
            val starLevel = prefs[PreferencesKeys.LAST_SESSION_STAR]
            val wordIndex = prefs[PreferencesKeys.LAST_WORD_INDEX]

            result = if (starLevel != null && wordIndex != null) {
                Pair(starLevel, wordIndex)
            } else {
                null
            }
        }

        return result
    }

    /**
     * Clear session state after successful session completion.
     * Called when session completes and star is earned.
     *
     * AC6: Clear session state on completion
     */
    suspend fun clearSessionState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_SESSION_STAR)
            preferences.remove(PreferencesKeys.LAST_WORD_INDEX)
        }
    }

    /**
     * Map DataStore Preferences to Progress object.
     * Provides default values if keys don't exist.
     */
    private fun mapToProgress(preferences: Preferences): Progress {
        val wizardStars = preferences[PreferencesKeys.WIZARD_STARS] ?: 0
        val pirateStars = preferences[PreferencesKeys.PIRATE_STARS] ?: 0
        val currentWorldOrdinal = preferences[PreferencesKeys.CURRENT_WORLD] ?: World.WIZARD.ordinal

        val currentWorld = World.values().getOrElse(currentWorldOrdinal) { World.WIZARD }

        return Progress(
            wizardStars = wizardStars,
            pirateStars = pirateStars,
            currentWorld = currentWorld
        )
    }
}
