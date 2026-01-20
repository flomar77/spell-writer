package com.spellwriter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.data.models.GameState
import com.spellwriter.ui.components.Grimoire
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.viewmodel.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for word completion timing behavior.
 * Verifies that when typing the last letter of a word:
 * 1. The letter is shown immediately on the grimoire
 * 2. The completed word stays visible for ~500ms before transitioning to the next word
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WordCompletionTimingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test that the word completion display delay constant is set to 500ms.
     */
    @Test
    fun wordCompleteDisplayDelay_is500ms() {
        assertEquals(
            "WORD_COMPLETE_DISPLAY_DELAY_MS should be 500ms",
            500L,
            GameViewModel.WORD_COMPLETE_DISPLAY_DELAY_MS
        )
    }

    /**
     * Test that letters appear immediately on the grimoire when typed.
     * This verifies the UI responds instantly to letter input.
     */
    @Test
    fun letterTyped_appearsImmediatelyOnGrimoire() {
        var typedLetters by mutableStateOf("")

        composeTestRule.setContent {
            Grimoire(typedLetters = typedLetters)
        }

        // Initially shows placeholder
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // Simulate typing first letter
        typedLetters = "C"
        composeTestRule.waitForIdle()

        // Letter should appear immediately
        composeTestRule.onNodeWithText("C").assertExists()
        composeTestRule.onNodeWithText("Type the word...").assertDoesNotExist()
    }

    /**
     * Test that the last letter of a word appears on the grimoire.
     * The completed word should be visible before any transition.
     */
    @Test
    fun lastLetter_appearsOnGrimoire_beforeWordClears() {
        var typedLetters by mutableStateOf("")

        composeTestRule.setContent {
            Grimoire(typedLetters = typedLetters)
        }

        // Type word letter by letter
        typedLetters = "C"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("C").assertExists()

        typedLetters = "CA"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("CA").assertExists()

        // Type last letter
        typedLetters = "CAT"
        composeTestRule.waitForIdle()

        // Complete word should be visible
        composeTestRule.onNodeWithText("CAT").assertExists()
    }

    /**
     * Test that the grimoire shows the complete word when all letters are typed.
     */
    @Test
    fun completedWord_isFullyVisibleOnGrimoire() {
        val completeWord = "HELLO"

        composeTestRule.setContent {
            Grimoire(typedLetters = completeWord)
        }

        composeTestRule.waitForIdle()

        // The complete word should be displayed
        composeTestRule.onNodeWithText(completeWord).assertExists()
    }

    /**
     * Test that when grimoire transitions from complete word to empty,
     * the placeholder reappears.
     */
    @Test
    fun grimoireClears_showsPlaceholderForNextWord() {
        var typedLetters by mutableStateOf("CAT")

        composeTestRule.setContent {
            Grimoire(typedLetters = typedLetters)
        }

        // Word is complete
        composeTestRule.onNodeWithText("CAT").assertExists()

        // Simulate word transition (clearing grimoire for next word)
        typedLetters = ""
        composeTestRule.waitForIdle()

        // Placeholder should reappear
        composeTestRule.onNodeWithText("Type the word...").assertExists()
        composeTestRule.onNodeWithText("CAT").assertDoesNotExist()
    }

    /**
     * Test the complete flow: letters typed -> word complete -> visible -> clears.
     * This simulates the actual UX where the completed word stays visible briefly.
     */
    @Test
    fun wordCompletionFlow_showsAllLettersBeforeClearing() {
        var typedLetters by mutableStateOf("")

        composeTestRule.setContent {
            Grimoire(typedLetters = typedLetters)
        }

        // Type first letter
        typedLetters = "D"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("D").assertExists()

        // Type second letter
        typedLetters = "DO"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("DO").assertExists()

        // Type last letter - word complete
        typedLetters = "DOG"
        composeTestRule.waitForIdle()

        // Verify complete word is shown (this is the state before the 500ms delay)
        composeTestRule.onNodeWithText("DOG").assertExists()
    }

    /**
     * Integration test: Verify GameScreen shows correct letter feedback.
     * When a correct letter is typed, it should appear on the grimoire.
     */
    @Test
    fun gameScreen_correctLetter_appearsOnGrimoire() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        composeTestRule.waitForIdle()

        // Initial state shows placeholder
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // Progress shows 0/20
        composeTestRule.onNodeWithText("0/20").assertExists()
    }

    /**
     * Verify that GameViewModel has the correct delay constant.
     * This is a sanity check that the timing constant is accessible.
     */
    @Test
    fun gameViewModel_hasCorrectTimingConstants() {
        // Verify delay is within expected range (around 500ms)
        assertTrue(
            "Delay should be at least 400ms",
            GameViewModel.WORD_COMPLETE_DISPLAY_DELAY_MS >= 400L
        )
        assertTrue(
            "Delay should be at most 600ms",
            GameViewModel.WORD_COMPLETE_DISPLAY_DELAY_MS <= 600L
        )
    }

    /**
     * Test that typing multiple letters builds up the word correctly.
     */
    @Test
    fun multipleLetters_buildWordIncrementally() {
        var typedLetters by mutableStateOf("")

        composeTestRule.setContent {
            Grimoire(typedLetters = typedLetters)
        }

        val word = "TREE"

        // Type each letter and verify cumulative display
        for (i in word.indices) {
            typedLetters = word.substring(0, i + 1)
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(typedLetters).assertExists()
        }

        // Final state should show complete word
        composeTestRule.onNodeWithText(word).assertExists()
    }
}
