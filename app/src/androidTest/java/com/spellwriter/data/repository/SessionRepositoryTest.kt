package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.data.models.SavedSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for SessionRepository.
 * Story 3.1: Session Control & Exit Flow (AC5, AC6, AC7)
 *
 * Tests verify:
 * - Session save/load round-trip
 * - Session expiry handling (24-hour timeout)
 * - Session clearing
 * - Empty list handling
 * - Data persistence integrity
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SessionRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SessionRepository

    // Extension property for test DataStore
    private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "test_session_datastore"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = SessionRepository(context)
    }

    @After
    fun tearDown() = runTest {
        // Clear DataStore after each test
        context.testDataStore.edit { it.clear() }
    }

    /**
     * AC5, AC6: Test complete save and load cycle.
     * Verify all session data is preserved correctly.
     */
    @Test
    fun saveAndLoadSession_restoresCompleteState() = runTest {
        // Given: A complete session with multiple words
        val session = SavedSession(
            starLevel = 2,
            wordsCompleted = 5,
            completedWords = listOf("CAT", "DOG", "HAT", "BAT", "RAT"),
            remainingWords = listOf("TREE", "BIRD", "FISH", "FROG"),
            currentWordIndex = 0,
            timestamp = System.currentTimeMillis()
        )

        // When: Session is saved and then loaded
        repository.saveSession(session)
        val loaded = repository.loadSession()

        // Then: All fields are restored correctly
        assertNotNull("Loaded session should not be null", loaded)
        assertEquals("Star level mismatch", session.starLevel, loaded?.starLevel)
        assertEquals("Words completed mismatch", session.wordsCompleted, loaded?.wordsCompleted)
        assertEquals("Completed words mismatch", session.completedWords, loaded?.completedWords)
        assertEquals("Remaining words mismatch", session.remainingWords, loaded?.remainingWords)
        assertEquals("Current word index mismatch", session.currentWordIndex, loaded?.currentWordIndex)
        assertEquals("Timestamp mismatch", session.timestamp, loaded?.timestamp)
    }

    /**
     * AC6: Edge case - verify expired sessions are not loaded.
     * Sessions older than 24 hours should be automatically cleared.
     */
    @Test
    fun loadSession_returnsNullForExpiredSession() = runTest {
        // Given: A session that expired 25 hours ago
        val expiredTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        val expiredSession = SavedSession(
            starLevel = 1,
            wordsCompleted = 3,
            completedWords = listOf("CAT", "DOG"),
            remainingWords = listOf("HAT"),
            currentWordIndex = 0,
            timestamp = expiredTimestamp
        )

        // When: Expired session is saved and then loaded
        repository.saveSession(expiredSession)
        val loaded = repository.loadSession()

        // Then: Load returns null (session expired and cleared)
        assertNull("Expired session should return null", loaded)
    }

    /**
     * AC7: Verify session clearing removes all data.
     */
    @Test
    fun clearSession_removesAllSessionData() = runTest {
        // Given: A saved session exists
        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 2,
            completedWords = listOf("CAT"),
            remainingWords = listOf("DOG"),
            currentWordIndex = 0,
            timestamp = System.currentTimeMillis()
        )
        repository.saveSession(session)

        // When: Session is cleared
        repository.clearSession()

        // Then: Loading returns null (no session exists)
        val loaded = repository.loadSession()
        assertNull("Cleared session should return null", loaded)
    }

    /**
     * AC6: Edge case - handle empty completed words list.
     */
    @Test
    fun saveAndLoadSession_handlesEmptyCompletedWords() = runTest {
        // Given: Session with no completed words yet
        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 0,
            completedWords = emptyList(),
            remainingWords = listOf("CAT", "DOG", "HAT"),
            currentWordIndex = 0,
            timestamp = System.currentTimeMillis()
        )

        // When: Session is saved and loaded
        repository.saveSession(session)
        val loaded = repository.loadSession()

        // Then: Empty list is preserved
        assertNotNull("Loaded session should not be null", loaded)
        assertTrue("Completed words should be empty", loaded?.completedWords?.isEmpty() == true)
        assertEquals("Words completed should be 0", 0, loaded?.wordsCompleted)
    }

    /**
     * AC6: Edge case - handle empty remaining words list.
     */
    @Test
    fun saveAndLoadSession_handlesEmptyRemainingWords() = runTest {
        // Given: Session with all words completed (edge case)
        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 3,
            completedWords = listOf("CAT", "DOG", "HAT"),
            remainingWords = emptyList(),
            currentWordIndex = 0,
            timestamp = System.currentTimeMillis()
        )

        // When: Session is saved and loaded
        repository.saveSession(session)
        val loaded = repository.loadSession()

        // Then: Empty list is preserved
        assertNotNull("Loaded session should not be null", loaded)
        assertTrue("Remaining words should be empty", loaded?.remainingWords?.isEmpty() == true)
    }

    /**
     * AC6: Verify no session returns null on first load.
     */
    @Test
    fun loadSession_returnsNullWhenNoSessionSaved() = runTest {
        // Given: No session has been saved

        // When: Attempting to load session
        val loaded = repository.loadSession()

        // Then: Returns null
        assertNull("Should return null when no session exists", loaded)
    }

    /**
     * AC5, AC7: Verify session data persists across repository instances.
     * Simulates app restart scenario.
     */
    @Test
    fun saveAndLoadSession_persistsAcrossInstances() = runTest {
        // Given: A session saved with first repository instance
        val firstRepo = SessionRepository(context)
        val session = SavedSession(
            starLevel = 3,
            wordsCompleted = 10,
            completedWords = listOf("APPLE", "BANANA", "CHERRY"),
            remainingWords = listOf("DATE", "ELDERBERRY"),
            currentWordIndex = 1,
            timestamp = System.currentTimeMillis()
        )
        firstRepo.saveSession(session)

        // When: Loading with new repository instance (simulates app restart)
        val secondRepo = SessionRepository(context)
        val loaded = secondRepo.loadSession()

        // Then: Data is still available
        assertNotNull("Session should persist across instances", loaded)
        assertEquals("Star level should persist", session.starLevel, loaded?.starLevel)
        assertEquals("Words completed should persist", session.wordsCompleted, loaded?.wordsCompleted)
    }

    /**
     * AC5: Verify session timestamp is recorded correctly.
     */
    @Test
    fun saveSession_recordsTimestampCorrectly() = runTest {
        // Given: Current timestamp before save
        val beforeSave = System.currentTimeMillis()

        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 1,
            completedWords = listOf("CAT"),
            remainingWords = listOf("DOG"),
            currentWordIndex = 0,
            timestamp = beforeSave
        )

        // When: Session is saved and loaded
        repository.saveSession(session)
        val loaded = repository.loadSession()

        // Then: Timestamp is preserved
        assertNotNull("Loaded session should not be null", loaded)
        assertEquals("Timestamp should be preserved", beforeSave, loaded?.timestamp)
    }

    /**
     * AC6: Verify SavedSession.isValid() correctly identifies valid sessions.
     */
    @Test
    fun savedSessionIsValid_returnsTrueForRecentSession() {
        // Given: A session created right now
        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 1,
            completedWords = listOf("CAT"),
            remainingWords = listOf("DOG"),
            currentWordIndex = 0,
            timestamp = System.currentTimeMillis()
        )

        // When: Checking if valid
        val isValid = SavedSession.isValid(session)

        // Then: Should be valid
        assertTrue("Recent session should be valid", isValid)
    }

    /**
     * AC6: Verify SavedSession.isValid() correctly identifies expired sessions.
     */
    @Test
    fun savedSessionIsValid_returnsFalseForExpiredSession() {
        // Given: A session created 25 hours ago
        val expiredTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        val session = SavedSession(
            starLevel = 1,
            wordsCompleted = 1,
            completedWords = listOf("CAT"),
            remainingWords = listOf("DOG"),
            currentWordIndex = 0,
            timestamp = expiredTimestamp
        )

        // When: Checking if valid
        val isValid = SavedSession.isValid(session)

        // Then: Should be invalid (expired)
        assertFalse("Expired session should be invalid", isValid)
    }
}
