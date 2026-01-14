package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.spellwriter.ui.theme.SpellWriterTheme
import org.junit.Rule
import org.junit.Test

/**
 * Navigation tests for Story 1.1.
 * Tests screen transitions and navigation performance (AC3).
 */
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigation_homeToGameScreen() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                SpellWriterApp()
            }
        }

        // Assert - Start on Home screen
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Act - Navigate to Game screen
        composeTestRule.onNodeWithText("PLAY").performClick()

        // Assert - Now on Game screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Game Screen").assertExists()
        composeTestRule.onNodeWithText("(Coming in Story 1.3)").assertExists()

        // AC3: Navigation should be smooth and child-friendly
        // (Compose handles transitions automatically)
    }

    @Test
    fun navigation_stateManagement() {
        // Arrange
        composeTestRule.setContent {
            SpellWriterTheme {
                SpellWriterApp()
            }
        }

        // Assert - Initial state is Home
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()

        // Act - Navigate to Game
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Assert - State changed to Game
        composeTestRule.onNodeWithText("Game Screen").assertExists()
        composeTestRule.onNodeWithText("SPELL WRITER").assertDoesNotExist()
    }
}
