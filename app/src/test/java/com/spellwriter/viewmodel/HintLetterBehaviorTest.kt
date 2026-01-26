package com.spellwriter.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.spellwriter.data.models.HintState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for hint letter behavior in GameViewModel.
 * Tests verify consecutive failure tracking and hint triggering logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HintLetterBehaviorTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var viewModel: GameViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        viewModel = GameViewModel(context = context, starNumber = 1)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun hintState_initiallyNull() {
        // Verify hint state starts as null
        assertNull("HintState should be null initially", viewModel.gameState.value.hintState)
    }

    @Test
    fun incorrectLetter_incrementsCounter() {
        val currentWord = viewModel.gameState.value.currentWord

        // Type 3 incorrect letters
        repeat(3) {
            viewModel.onLetterTyped('Z')
        }

        // Hint should not appear yet (need 5 failures)
        assertNull("HintState should still be null after 3 failures",
            viewModel.gameState.value.hintState)
    }

    @Test
    fun correctLetter_resetsCounterToZero() {
        val currentWord = viewModel.gameState.value.currentWord
        val firstLetter = currentWord.first()

        // Type 3 incorrect letters
        repeat(3) {
            viewModel.onLetterTyped('Z')
        }

        // Type correct letter
        viewModel.onLetterTyped(firstLetter)
        testDispatcher.scheduler.advanceUntilIdle()

        // Counter should be reset - type 4 more incorrect letters and hint should not show
        repeat(4) {
            viewModel.onLetterTyped('Z')
        }

        assertNull("HintState should be null - counter was reset by correct letter",
            viewModel.gameState.value.hintState)
    }

    @Test
    fun fiveConsecutiveFailures_showsHint() {
        val currentWord = viewModel.gameState.value.currentWord
        val expectedLetter = currentWord.first()

        // Type 5 incorrect letters
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Hint should now appear
        assertNotNull("HintState should be set after 5 failures",
            viewModel.gameState.value.hintState)

        val hintState = viewModel.gameState.value.hintState!!
        assertEquals("Hint should show correct letter", expectedLetter, hintState.letter)
        assertEquals("Hint should show at position 0", 0, hintState.positionIndex)
    }

    @Test
    fun hintContainsCorrectLetterAndPosition() {
        val currentWord = viewModel.gameState.value.currentWord
        val firstLetter = currentWord.first()

        // Type correct first letter
        viewModel.onLetterTyped(firstLetter)
        testDispatcher.scheduler.advanceUntilIdle()

        // Now at position 1, type 5 incorrect letters
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val hintState = viewModel.gameState.value.hintState
        assertNotNull("HintState should be set at position 1", hintState)

        if (currentWord.length > 1) {
            assertEquals("Hint should show letter at position 1",
                currentWord[1], hintState!!.letter)
            assertEquals("Hint should show at position 1", 1, hintState.positionIndex)
        }
    }

    @Test
    fun counterResetsAfterHintShown() {
        val currentWord = viewModel.gameState.value.currentWord

        // Type 5 incorrect letters to trigger hint
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Hint should be shown
        assertNotNull("Hint should be shown after 5 failures",
            viewModel.gameState.value.hintState)

        // Wait for hint to auto-clear (2000ms)
        testDispatcher.scheduler.advanceTimeBy(2100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Type 4 more incorrect letters - hint should not show (counter was reset)
        repeat(4) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("Hint should not show again after only 4 more failures",
            viewModel.gameState.value.hintState)
    }

    @Test
    fun positionOutOfBounds_handledSafely() {
        val currentWord = viewModel.gameState.value.currentWord

        // Complete the entire word correctly
        currentWord.forEach { letter ->
            viewModel.onLetterTyped(letter)
        }

        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()

        // Now try to trigger hint when word is complete (position out of bounds)
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash and hint should remain null
        assertNull("Hint should not show when position is out of bounds",
            viewModel.gameState.value.hintState)
    }

    @Test
    fun hintShowsAtDifferentPositions() {
        val currentWord = viewModel.gameState.value.currentWord

        if (currentWord.length < 2) {
            // Skip test if word is too short
            return
        }

        // Type first letter correctly
        viewModel.onLetterTyped(currentWord[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // Trigger hint at position 1
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val hintState = viewModel.gameState.value.hintState
        assertNotNull("Hint should show at position 1", hintState)
        assertEquals("Hint position should be 1", 1, hintState!!.positionIndex)
        assertEquals("Hint letter should match position 1", currentWord[1], hintState.letter)
    }

    @Test
    fun rapidTyping_doesNotCorruptState() {
        val currentWord = viewModel.gameState.value.currentWord

        // Rapidly type 10 incorrect letters
        repeat(10) {
            viewModel.onLetterTyped('Z')
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Should have shown hint after 5 failures, counter reset
        val hintState = viewModel.gameState.value.hintState
        assertNotNull("Hint should be shown", hintState)
        assertEquals("Hint should be at position 0", 0, hintState!!.positionIndex)
        assertEquals("Hint should show correct letter", currentWord[0], hintState.letter)

        // Typed letters should still be empty (no incorrect letters accepted)
        assertEquals("No letters should be typed", "", viewModel.gameState.value.typedLetters)
    }

    @Test
    fun multipleHintsAtSamePosition_workCorrectly() {
        val currentWord = viewModel.gameState.value.currentWord

        // Trigger first hint
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("First hint should be shown", viewModel.gameState.value.hintState)

        // Wait for hint to clear
        testDispatcher.scheduler.advanceTimeBy(2100)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("Hint should be cleared", viewModel.gameState.value.hintState)

        // Trigger second hint at same position
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Second hint should be shown", viewModel.gameState.value.hintState)
        assertEquals("Second hint should be at same position",
            0, viewModel.gameState.value.hintState!!.positionIndex)
    }

    @Test
    fun wordChangeBeforeHintClears_clearsHintImmediately() {
        val currentWord = viewModel.gameState.value.currentWord

        // Trigger hint
        repeat(5) {
            viewModel.onLetterTyped('Z')
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Hint should be shown", viewModel.gameState.value.hintState)

        // Complete the word (simulating word change)
        currentWord.forEach { letter ->
            viewModel.onLetterTyped(letter)
        }
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()

        // Hint should be cleared when word completes
        assertNull("Hint should be cleared on word change",
            viewModel.gameState.value.hintState)
    }
}
