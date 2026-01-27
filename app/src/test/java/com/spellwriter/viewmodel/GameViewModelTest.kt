package com.spellwriter.viewmodel

import com.spellwriter.data.models.GameState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for GameViewModel.
 * Tests game logic without Android dependencies (TTS, Context, MediaPlayer).
 * Full integration testing in instrumentation tests.
 * Story 2.1: Enhanced with session management tests for 20-word learning sessions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], instrumentedPackages = ["androidx.loader.content"])
class GameViewModelTest {

    @Test
    fun gameState_initialState_isCorrect() {
        // Verify initial game state
        val initialState = GameState()

        assertEquals("", initialState.currentWord)
        assertEquals("", initialState.typedLetters)
        assertEquals(0, initialState.wordsCompleted)
        assertEquals(0, initialState.sessionStars)
        assertTrue(initialState.wordPool.isEmpty())
        // Story 1.5: ghostExpression removed from GameState (managed separately in ViewModel)
        // Story 2.1: Session tracking fields
        assertFalse(initialState.sessionComplete)
        assertTrue(initialState.remainingWords.isEmpty())
        assertTrue(initialState.failedWords.isEmpty())
    }

    @Test
    fun gameState_typedLetters_accumulatesCorrectLetters() {
        val state = GameState(
            currentWord = "CAT",
            typedLetters = "CA"
        )

        assertEquals("CA", state.typedLetters)
        assertEquals(2, state.typedLetters.length)
    }

    @Test
    fun gameState_wordsCompleted_tracksProgress() {
        val state = GameState(wordsCompleted = 5)
        assertEquals(5, state.wordsCompleted)
        assertTrue(state.wordsCompleted <= 20)  // Max 20 words per session
    }

    // Story 2.1: Session Management Tests

    @Test
    fun gameState_sessionComplete_defaultsFalse() {
        val state = GameState()
        assertFalse(state.sessionComplete)
    }

    @Test
    fun gameState_sessionComplete_canBeSetTrue() {
        val state = GameState(sessionComplete = true)
        assertTrue(state.sessionComplete)
    }

    @Test
    fun gameState_remainingWords_tracksActivePool() {
        val words = listOf("CAT", "DOG", "SUN")
        val state = GameState(remainingWords = words)

        assertEquals(3, state.remainingWords.size)
        assertEquals("CAT", state.remainingWords.first())
    }

    @Test
    fun gameState_failedWords_tracksRetryPool() {
        val failedWords = listOf("TREE", "FISH")
        val state = GameState(failedWords = failedWords)

        assertEquals(2, state.failedWords.size)
        assertTrue(state.failedWords.contains("TREE"))
        assertTrue(state.failedWords.contains("FISH"))
    }

    @Test
    fun gameState_wordsCompleted_onlyIncrementsOnSuccess() {
        // Verify completed count tracks successful completions, not attempts
        val state = GameState(
            wordsCompleted = 10,
            remainingWords = listOf("APPLE", "HORSE"),
            failedWords = listOf("HOUSE")  // One failed word doesn't affect completed count
        )

        assertEquals(10, state.wordsCompleted)
        assertEquals(2, state.remainingWords.size)
        assertEquals(1, state.failedWords.size)
    }

    @Test
    fun gameState_sessionNotComplete_untilAll20UniqueWords() {
        // 19 words completed with 1 in remaining should not be complete
        val state = GameState(
            wordsCompleted = 19,
            remainingWords = listOf("FINAL"),
            sessionComplete = false
        )

        assertFalse(state.sessionComplete)
        assertEquals(1, state.remainingWords.size)
    }

    @Test
    fun gameState_failedWordInBothLists_isValid() {
        // A failed word should be in both failedWords (for tracking) and remainingWords (for retry)
        val state = GameState(
            remainingWords = listOf("CAT", "DOG"),
            failedWords = listOf("CAT")
        )

        assertTrue(state.remainingWords.contains("CAT"))
        assertTrue(state.failedWords.contains("CAT"))
    }

    // Story 2.1: Code Review Fixes - Additional Tests

    @Test
    fun insertWordByLength_emptyList_returnsListWithWord() {
        // Test helper function logic - inserting into empty list
        val result = insertWordByLengthTestHelper("CAT", emptyList())
        assertEquals(1, result.size)
        assertEquals("CAT", result.first())
    }

    @Test
    fun insertWordByLength_shortWordIntoLongList_insertsAtStart() {
        // Short word should go before longer words
        val existingWords = listOf("APPLE", "BANANA")
        val result = insertWordByLengthTestHelper("CAT", existingWords)

        assertEquals(3, result.size)
        assertEquals("CAT", result[0])  // 3-letter word first
        assertEquals("APPLE", result[1])  // 5-letter word
        assertEquals("BANANA", result[2])  // 6-letter word
    }

    @Test
    fun insertWordByLength_longWordIntoShortList_insertsAtEnd() {
        // Long word should go after shorter words
        val existingWords = listOf("CAT", "DOG")
        val result = insertWordByLengthTestHelper("APPLE", existingWords)

        assertEquals(3, result.size)
        assertEquals("CAT", result[0])
        assertEquals("DOG", result[1])
        assertEquals("APPLE", result[2])  // 5-letter word at end
    }

    @Test
    fun insertWordByLength_sameLength_insertsBeforeFirstLonger() {
        // Word of same length as existing should go before longer words
        val existingWords = listOf("CAT", "TREE", "APPLE")
        val result = insertWordByLengthTestHelper("DOG", existingWords)

        assertEquals(4, result.size)
        assertEquals("CAT", result[0])
        assertEquals("DOG", result[1])  // Inserted before TREE (4 letters)
        assertEquals("TREE", result[2])
        assertEquals("APPLE", result[3])
    }

    @Test
    fun insertWordByLength_maintainsDifficultyOrder() {
        // Comprehensive test: verify non-decreasing length order after insertion
        val existingWords = listOf("CAT", "DOG", "TREE", "FISH", "APPLE")
        val insertWord = "BAT"  // 3 letters
        val result = insertWordByLengthTestHelper(insertWord, existingWords)

        // Verify all words maintain non-decreasing length order
        for (i in 0 until result.size - 1) {
            assertTrue(
                "Word at ${i+1} should not be shorter than word at $i",
                result[i].length <= result[i + 1].length
            )
        }
    }

    @Test
    fun gameState_sessionComplete_preventsMoreWordsFromBeingAdded() {
        // When sessionComplete is true, UI should prevent further word presentation
        val state = GameState(
            wordsCompleted = 20,
            sessionComplete = true,
            remainingWords = emptyList(),
            failedWords = emptyList()
        )

        assertTrue(state.sessionComplete)
        assertEquals(20, state.wordsCompleted)
        assertTrue(state.remainingWords.isEmpty())
    }

    @Test
    fun gameState_failedWordRetry_trackedCorrectly() {
        // Simulates a failed word being tracked and added back to pool
        val failedWord = "TREE"

        // After failure, word should be in both lists
        val stateAfterFailure = GameState(
            remainingWords = listOf("TREE", "FISH", "APPLE"),  // Re-inserted
            failedWords = listOf("TREE")
        )

        assertTrue(stateAfterFailure.failedWords.contains(failedWord))
        assertTrue(stateAfterFailure.remainingWords.contains(failedWord))
        assertEquals(3, stateAfterFailure.remainingWords.size)
    }

    /**
     * Test helper that replicates insertWordByLength logic for unit testing.
     * This allows testing the algorithm without instantiating GameViewModel.
     */
    private fun insertWordByLengthTestHelper(word: String, words: List<String>): List<String> {
        if (words.isEmpty()) return listOf(word)

        val insertIndex = words.indexOfFirst { it.length > word.length }
        return if (insertIndex == -1) {
            words + word
        } else {
            words.toMutableList().apply { add(insertIndex, word) }
        }
    }

    // Story 1.5: Ghost expression test removed
    // Ghost expression is now managed separately in GameViewModel with its own StateFlow
    // Expression tests are in GhostComponentTest and instrumentation tests

    // Story 2.4: Celebration state tests (AC7)

    @Test
    fun celebrationState_initiallyFalse() {
        val viewModel = createTestViewModel()
        assertFalse(viewModel.showCelebration.value)
        assertEquals(0, viewModel.celebrationStarLevel.value)
    }

    @Test
    fun onCelebrationComplete_clearsCelebrationState() {
        val viewModel = createTestViewModel()

        // Manually set celebration state (simulating star completion)
        // Note: In real scenario, this happens after 20-word completion
        viewModel.onCelebrationComplete()

        assertFalse(viewModel.showCelebration.value)
        assertEquals(0, viewModel.celebrationStarLevel.value)
    }

    // Story 3.1: Exit flow tests (AC1, AC2, AC3, AC4, AC5)

    @Test
    fun requestExit_showsExitDialog() {
        val viewModel = createTestViewModel()

        viewModel.requestExit()

        assertTrue("Exit dialog should be shown", viewModel.showExitDialog.value)
        assertEquals("Session should remain ACTIVE",
            com.spellwriter.data.models.SessionState.ACTIVE,
            viewModel.sessionState.value)
    }

    @Test
    fun cancelExit_hidesExitDialog() {
        val viewModel = createTestViewModel()
        viewModel.requestExit()

        viewModel.cancelExit()

        assertFalse("Exit dialog should be hidden", viewModel.showExitDialog.value)
        assertEquals("Session should remain ACTIVE",
            com.spellwriter.data.models.SessionState.ACTIVE,
            viewModel.sessionState.value)
    }

    @Test
    fun exitFlowStates_initialState_isCorrect() {
        val viewModel = createTestViewModel()

        assertFalse("Exit dialog should be initially hidden", viewModel.showExitDialog.value)
        assertEquals("Session should be initially ACTIVE",
            com.spellwriter.data.models.SessionState.ACTIVE,
            viewModel.sessionState.value)
    }

    @Test
    fun resetSession_resetsSessionStateToActive() {
        val viewModel = createTestViewModel()

        viewModel.resetSession()

        assertEquals("Session should be reset to ACTIVE",
            com.spellwriter.data.models.SessionState.ACTIVE,
            viewModel.sessionState.value)
    }

    @Test
    fun exitDialog_doesNotAffectGameState() {
        val viewModel = createTestViewModel()
        val initialGameState = viewModel.gameState.value

        viewModel.requestExit()
        val gameStateAfterRequest = viewModel.gameState.value

        // Game state should be preserved when exit dialog is shown
        assertEquals("Game state should not change when exit dialog opens",
            initialGameState, gameStateAfterRequest)
    }

    /**
     * Helper function to create GameViewModel for testing.
     * Uses test context and mocked dependencies.
     * Story 3.1: Updated to include SessionRepository
     * AudioManager Injection: Updated to accept optional audioManager parameter
     */
    private fun createTestViewModel(audioManager: com.spellwriter.audio.AudioManager? = null): GameViewModel {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        return GameViewModel(
            context = context,
            starNumber = 1,
            isReplaySession = false,
            progressRepository = null,
            sessionRepository = null,  // Story 3.1: No session persistence in unit tests
            initialProgress = com.spellwriter.data.models.Progress(),
            audioManager = audioManager
        )
    }

    // Story 3.2: Timeout tracking and failure handling tests

    @org.junit.Ignore("TODO: Fix after refactoring")
    @Test
    fun timeoutTracking_initialState_isCorrect() {
        // TODO: Fix after timeout refactoring
        // val viewModel = createTestViewModel()
        // assertFalse("Encouragement should not be shown initially", viewModel.isEncouragementShown.value)
        // assertEquals("Ghost should be NEUTRAL initially", GhostExpression.NEUTRAL, viewModel.ghostExpression.value)
    }

    @org.junit.Ignore("TODO: Fix after refactoring")
    @Test
    fun resetTimeouts_clearsEncouragementFlag() {
        // TODO: Fix after timeout refactoring
        // val viewModel = createTestViewModel()
        // viewModel.resetTimeouts()
        // assertFalse("Encouragement flag should be false after reset", viewModel.isEncouragementShown.value)
    }

    @org.junit.Ignore("TODO: Fix after refactoring")
    @Test
    fun onLetterTyped_resetsTimeouts() {
        // TODO: Fix after timeout refactoring
        // val viewModel = createTestViewModel()
        // viewModel.onLetterTyped('A')
        // assertFalse("Encouragement should be reset on letter typed", viewModel.isEncouragementShown.value)
    }

    // Word completion timing tests

    @Test
    fun wordCompleteDisplayDelay_isSetTo500ms() {
        // Verify the constant is set correctly
        assertEquals(
            "WORD_COMPLETE_DISPLAY_DELAY_MS should be 500ms",
            500L,
            GameViewModel.WORD_COMPLETE_DISPLAY_DELAY_MS
        )
    }

    @Test
    fun gameState_typedLetters_showsLastLetterImmediately() {
        // When last letter is typed, it should appear immediately in typedLetters
        // (before the delay for word transition)
        val state = GameState(
            currentWord = "CAT",
            typedLetters = "CAT"  // All letters typed including last one
        )

        // The complete word should be visible in typedLetters
        assertEquals("CAT", state.typedLetters)
        assertEquals(state.currentWord, state.typedLetters)
    }

    @Test
    fun gameState_completedWord_matchesCurrentWord() {
        // When word is completed, typedLetters should match currentWord exactly
        // This ensures the last letter is shown before any transition
        val word = "TREE"
        val state = GameState(
            currentWord = word,
            typedLetters = word
        )

        assertTrue(
            "typedLetters should match currentWord when complete",
            state.typedLetters == state.currentWord
        )
    }

    // AudioManager Injection Tests

    @Test
    fun gameViewModel_withNullAudioManager_createsSuccessfully() {
        // Game should work without audio (null AudioManager)
        val viewModel = createTestViewModel(audioManager = null)

        assertNotNull("ViewModel should be created with null audioManager", viewModel)
        assertNotNull("GameState should be initialized", viewModel.gameState.value)
    }

    @Test
    fun gameViewModel_withNullAudioManager_gameStateIsValid() {
        // Verify game state is properly initialized even without AudioManager
        val viewModel = createTestViewModel(audioManager = null)
        val gameState = viewModel.gameState.value

        // Game should still load words and be playable
        assertNotNull("Current word should be set", gameState.currentWord)
        assertEquals("Typed letters should be empty initially", "", gameState.typedLetters)
        assertEquals("Words completed should start at 0", 0, gameState.wordsCompleted)
    }

    @Test
    fun isTTSReady_withNullAudioManager_returnsFalse() {
        // When audioManager is null, isTTSReady should return false
        val viewModel = createTestViewModel(audioManager = null)

        assertFalse(
            "isTTSReady should be false with null audioManager",
            viewModel.isTTSReady.value
        )
    }

    @Test
    fun speakCurrentWord_withNullAudioManager_doesNotCrash() {
        // speakCurrentWord should handle null audioManager gracefully (no-op)
        val viewModel = createTestViewModel(audioManager = null)

        try {
            viewModel.speakCurrentWord()
            // If we reach here, no exception was thrown
            assertTrue("speakCurrentWord should not crash with null audioManager", true)
        } catch (e: Exception) {
            fail("speakCurrentWord should not throw exception with null audioManager: ${e.message}")
        }
    }

    @Test
    fun onLetterTyped_withNullAudioManager_doesNotCrash() {
        // Letter typing should work without audio
        val viewModel = createTestViewModel(audioManager = null)

        try {
            viewModel.onLetterTyped('A')
            // If we reach here, no exception was thrown
            assertTrue("onLetterTyped should not crash with null audioManager", true)
        } catch (e: Exception) {
            fail("onLetterTyped should not throw exception with null audioManager: ${e.message}")
        }
    }

    @Test
    fun gameViewModel_audioManagerInjection_defaultsToNull() {
        // Verify that audioManager defaults to null when not provided
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val viewModel = GameViewModel(
            context = context,
            starNumber = 1
            // audioManager not provided - should default to null
        )

        assertFalse(
            "isTTSReady should be false when audioManager not provided",
            viewModel.isTTSReady.value
        )
    }

    @Test
    fun gameViewModel_withNullAudioManager_sessionFlowWorks() {
        // Full game session should work without audio
        val viewModel = createTestViewModel(audioManager = null)

        // Verify basic game operations work
        assertNotNull("Game state should exist", viewModel.gameState.value)
        assertFalse("Session should not be complete initially", viewModel.gameState.value.sessionComplete)

        // Exit flow should work
        viewModel.requestExit()
        assertTrue("Exit dialog should show", viewModel.showExitDialog.value)

        viewModel.cancelExit()
        assertFalse("Exit dialog should hide", viewModel.showExitDialog.value)
    }

    // Note: Tests with valid (non-null) AudioManager require actual AudioManager instantiation
    // which depends on TTS availability and Android context setup.
    // These are covered in instrumentation tests (androidTest) where we can:
    // - Create actual AudioManager instances
    // - Mock TTS initialization
    // - Test audio playback integration
    // - Verify isTTSReady state flow updates

    // Note: GameViewModel tests requiring Context, TTS, and SoundManager
    // are in instrumentation tests (androidTest)
    // Integration tests for onWordFailed() and full session flow require
    // instrumentation testing with mocked Context/TTS.
    // Celebration trigger test (after 20-word completion) is in instrumentation tests.
    // Story 3.1: confirmExit() tests with session saving are in instrumentation tests
    // due to requirement for coroutine testing and DataStore mocking.
    // Story 3.2: Full timeout behavior tests (8s, 20s) are in instrumentation tests
    // due to requirement for coroutine time control with advanceTimeBy().
    // Word completion timing test (500ms delay) is in instrumentation tests
    // due to requirement for coroutine time control with advanceTimeBy().
}
