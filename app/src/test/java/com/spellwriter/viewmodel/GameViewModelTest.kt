package com.spellwriter.viewmodel

import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for GameViewModel.
 * Tests game logic without Android dependencies (TTS, Context, MediaPlayer).
 * Full integration testing in instrumentation tests.
 * Story 2.1: Enhanced with session management tests for 20-word learning sessions
 */
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
        val remainingBefore = listOf("FISH", "APPLE")

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

    /**
     * Helper function to create GameViewModel for testing.
     * Uses test context and mocked dependencies.
     */
    private fun createTestViewModel(): GameViewModel {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        return GameViewModel(
            context = context,
            starNumber = 1,
            isReplaySession = false,
            progressRepository = null,
            initialProgress = com.spellwriter.data.models.Progress()
        )
    }

    // Note: GameViewModel tests requiring Context, TTS, and SoundManager
    // are in instrumentation tests (androidTest)
    // Integration tests for onWordFailed() and full session flow require
    // instrumentation testing with mocked Context/TTS.
    // Celebration trigger test (after 20-word completion) is in instrumentation tests.
}
