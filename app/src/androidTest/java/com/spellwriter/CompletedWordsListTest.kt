package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.components.CompletedWordsList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for CompletedWordsList component.
 * Tests display of completed words under the Grimoire with max 10 words limit.
 */
@RunWith(AndroidJUnit4::class)
class CompletedWordsListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun completedWordsList_displaysCompletedWords() {
        val words = listOf("CAT", "DOG", "FISH")

        composeTestRule.setContent {
            CompletedWordsList(completedWords = words)
        }

        composeTestRule.onNodeWithText("CAT").assertExists()
        composeTestRule.onNodeWithText("DOG").assertExists()
        composeTestRule.onNodeWithText("FISH").assertExists()
    }

    @Test
    fun completedWordsList_showsEmptyStateWhenNoWords() {
        composeTestRule.setContent {
            CompletedWordsList(completedWords = emptyList())
        }

        // Should not crash and should show nothing or placeholder
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun completedWordsList_limitsToMaximum10Words() {
        val words = listOf(
            "ONE", "TWO", "THREE", "FOUR", "FIVE",
            "SIX", "SEVEN", "EIGHT", "NINE", "TEN",
            "ELEVEN", "TWELVE"
        )

        composeTestRule.setContent {
            CompletedWordsList(completedWords = words)
        }

        // Last 10 words should be visible (THREE through TWELVE)
        composeTestRule.onNodeWithText("THREE").assertExists()
        composeTestRule.onNodeWithText("TWELVE").assertExists()

        // First 2 words should NOT be displayed (oldest get dropped)
        composeTestRule.onNodeWithText("ONE").assertDoesNotExist()
        composeTestRule.onNodeWithText("TWO").assertDoesNotExist()
    }

    @Test
    fun completedWordsList_showsLast10WordsWhenMoreProvided() {
        val words = (1..15).map { "WORD$it" }

        composeTestRule.setContent {
            CompletedWordsList(completedWords = words)
        }

        // Last 10 words (6-15) should exist
        (6..15).forEach { i ->
            composeTestRule.onNodeWithText("WORD$i").assertExists()
        }

        // First 5 words should NOT exist (oldest get dropped)
        (1..5).forEach { i ->
            composeTestRule.onNodeWithText("WORD$i").assertDoesNotExist()
        }
    }

    @Test
    fun completedWordsList_displaysSingleWord() {
        composeTestRule.setContent {
            CompletedWordsList(completedWords = listOf("HELLO"))
        }

        composeTestRule.onNodeWithText("HELLO").assertExists()
    }
}
