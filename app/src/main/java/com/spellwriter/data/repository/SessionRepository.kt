package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spellwriter.data.models.SavedSession
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Repository for persisting partial session state using DataStore Preferences.
 * Story 3.1: Session Control & Exit Flow (AC5, AC6, AC7)
 *
 * Provides persistence for:
 * - Partial session progress (word completion count, word pools)
 * - Session resume capability after exit
 * - Session expiry (24-hour timeout)
 *
 * This enables children to exit mid-session safely (Critical Issue C3)
 * and resume their learning later without losing progress.
 */
class SessionRepository(private val context: Context) {

    // Extension property for DataStore instance (separate from progress)
    private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "spell_writer_session"
    )

    /**
     * Preference keys for session state.
     */
    private object PreferencesKeys {
        val STAR_LEVEL = intPreferencesKey("session_star_level")
        val WORDS_COMPLETED = intPreferencesKey("session_words_completed")
        val COMPLETED_WORDS = stringPreferencesKey("session_completed_words")
        val REMAINING_WORDS = stringPreferencesKey("session_remaining_words")
        val CURRENT_WORD_INDEX = intPreferencesKey("session_current_word_index")
        val TIMESTAMP = longPreferencesKey("session_timestamp")
    }

    /**
     * Save current session state to DataStore.
     * Called when user confirms exit from game session.
     *
     * AC5: Save session progress immediately on exit
     * FR9.4: Current word progress saved before returning to Home
     * NFR3.2: Save completes within 100ms (DataStore is fast)
     *
     * @param session The SavedSession object containing complete session state
     */
    suspend fun saveSession(session: SavedSession) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.STAR_LEVEL] = session.starLevel
            preferences[PreferencesKeys.WORDS_COMPLETED] = session.wordsCompleted
            // Store lists as comma-separated strings
            preferences[PreferencesKeys.COMPLETED_WORDS] = session.completedWords.joinToString(",")
            preferences[PreferencesKeys.REMAINING_WORDS] = session.remainingWords.joinToString(",")
            preferences[PreferencesKeys.CURRENT_WORD_INDEX] = session.currentWordIndex
            preferences[PreferencesKeys.TIMESTAMP] = session.timestamp
        }
    }

    /**
     * Load saved session state from DataStore.
     * Returns null if no saved session exists or if session has expired.
     *
     * AC6: Load saved session progress on game start
     * AC6: Session resume from appropriate point
     * Edge case: Returns null for expired sessions (24+ hours old)
     *
     * @return SavedSession if valid session exists, null otherwise
     */
    suspend fun loadSession(): SavedSession? {
        val preferences = context.sessionDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        // Check if all required keys exist
        val starLevel = preferences[PreferencesKeys.STAR_LEVEL] ?: return null
        val wordsCompleted = preferences[PreferencesKeys.WORDS_COMPLETED] ?: return null
        val completedWordsStr = preferences[PreferencesKeys.COMPLETED_WORDS] ?: return null
        val remainingWordsStr = preferences[PreferencesKeys.REMAINING_WORDS] ?: return null
        val currentWordIndex = preferences[PreferencesKeys.CURRENT_WORD_INDEX] ?: return null
        val timestamp = preferences[PreferencesKeys.TIMESTAMP] ?: return null

        // Parse comma-separated word lists
        // Handle empty lists (empty string) correctly
        val completedWords = if (completedWordsStr.isEmpty()) {
            emptyList()
        } else {
            completedWordsStr.split(",")
        }

        val remainingWords = if (remainingWordsStr.isEmpty()) {
            emptyList()
        } else {
            remainingWordsStr.split(",")
        }

        val session = SavedSession(
            starLevel = starLevel,
            wordsCompleted = wordsCompleted,
            completedWords = completedWords,
            remainingWords = remainingWords,
            currentWordIndex = currentWordIndex,
            timestamp = timestamp
        )

        // Validate session is not expired (AC6: edge case handling)
        return if (SavedSession.isValid(session)) {
            session
        } else {
            // Session expired - clear it and return null
            clearSession()
            null
        }
    }

    /**
     * Clear all session state from DataStore.
     * Called when:
     * - Session completes successfully (star earned)
     * - Session expires (24+ hours old)
     * - User explicitly starts a new session
     *
     * AC6: Clear session after completion
     * AC7: Prevent data corruption by clearing stale sessions
     */
    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.STAR_LEVEL)
            preferences.remove(PreferencesKeys.WORDS_COMPLETED)
            preferences.remove(PreferencesKeys.COMPLETED_WORDS)
            preferences.remove(PreferencesKeys.REMAINING_WORDS)
            preferences.remove(PreferencesKeys.CURRENT_WORD_INDEX)
            preferences.remove(PreferencesKeys.TIMESTAMP)
        }
    }
}
