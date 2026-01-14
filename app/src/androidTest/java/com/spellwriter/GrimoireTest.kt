package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.components.Grimoire
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Grimoire component (Story 1.3).
 * Tests letter display functionality and placeholder text.
 */
@RunWith(AndroidJUnit4::class)
class GrimoireTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun grimoire_displaysPlaceholderWhenEmpty() {
        composeTestRule.setContent {
            Grimoire(typedLetters = "")
        }

        composeTestRule.onNodeWithText("Type the word...").assertExists()
    }

    @Test
    fun grimoire_displaysTypedLetters() {
        composeTestRule.setContent {
            Grimoire(typedLetters = "HELLO")
        }

        composeTestRule.onNodeWithText("HELLO").assertExists()
        composeTestRule.onNodeWithText("Type the word...").assertDoesNotExist()
    }

    @Test
    fun grimoire_displaysMultipleLetters() {
        composeTestRule.setContent {
            Grimoire(typedLetters = "CAT")
        }

        composeTestRule.onNodeWithText("CAT").assertExists()
    }

    @Test
    fun grimoire_handlesSingleLetter() {
        composeTestRule.setContent {
            Grimoire(typedLetters = "A")
        }

        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun grimoire_updatesWhenLettersChange() {
        var letters = "HE"
        composeTestRule.setContent {
            Grimoire(typedLetters = letters)
        }

        composeTestRule.onNodeWithText("HE").assertExists()

        // Simulate letter being added
        letters = "HEL"
        composeTestRule.setContent {
            Grimoire(typedLetters = letters)
        }

        composeTestRule.onNodeWithText("HEL").assertExists()
    }
}
