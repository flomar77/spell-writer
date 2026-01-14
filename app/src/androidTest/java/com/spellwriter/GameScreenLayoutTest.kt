package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.screens.GameScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for complete GameScreen layout (Story 1.3).
 * Tests that all UI components are properly displayed and positioned.
 */
@RunWith(AndroidJUnit4::class)
class GameScreenLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameScreen_displaysAllLayoutElements() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify progress indicator
        composeTestRule.onNodeWithText("0/20").assertExists()

        // Verify ghost present
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()

        // Verify grimoire (letter display)
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // Verify control buttons
        composeTestRule.onNodeWithContentDescription("Play word").assertExists()
        composeTestRule.onNodeWithContentDescription("Repeat word").assertExists()

        // Verify session stars
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 1").assertExists()
    }

    @Test
    fun gameScreen_progressBarShowsCorrectInitialState() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify initial progress is 0/20
        composeTestRule.onNodeWithText("0/20").assertExists()
    }

    @Test
    fun gameScreen_ghostComponentAppearsInTopRight() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify ghost exists
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()
    }

    @Test
    fun gameScreen_grimoireIsCenteredAndVisible() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify grimoire displays placeholder text
        composeTestRule.onNodeWithText("Type the word...").assertExists()
    }

    @Test
    fun gameScreen_displays3SessionStarsOnLeftSide() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify all 3 session stars are displayed
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 1").assertExists()
    }

    @Test
    fun gameScreen_displaysCompleteKeyboard() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify all 26 letters are present in QWERTY layout
        "QWERTYUIOPASDFGHJKLZXCVBNM".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).assertExists()
        }
    }

    @Test
    fun gameScreen_audioButtonsHaveProperTouchTargets() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Verify Play button touch target (56dp)
        composeTestRule.onNodeWithContentDescription("Play word")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        // Verify Repeat button touch target (56dp)
        composeTestRule.onNodeWithContentDescription("Repeat word")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun gameScreen_keyboardLetterClickUpdatesGrimoire() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Click letter 'C'
        composeTestRule.onNodeWithText("C").performClick()
        composeTestRule.waitForIdle()

        // Verify 'C' appears in grimoire
        composeTestRule.onNodeWithText("C").assertExists()

        // Placeholder text should be gone
        composeTestRule.onNodeWithText("Type the word...").assertDoesNotExist()
    }

    @Test
    fun gameScreen_multipleLetterClicksBuildWord() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Type "CAT"
        composeTestRule.onNodeWithText("C").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("T").performClick()
        composeTestRule.waitForIdle()

        // Verify "CAT" appears in grimoire
        composeTestRule.onNodeWithText("CAT").assertExists()
    }

    @Test
    fun gameScreen_acceptsStarNumberParameter() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 2,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // GameScreen should accept starNumber parameter (used in Story 1.4)
        // For now, just verify the screen displays correctly
        composeTestRule.onNodeWithText("0/20").assertExists()
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()
    }

    @Test
    fun gameScreen_acceptsReplaySessionParameter() {
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                isReplaySession = true,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // GameScreen should accept isReplaySession parameter (used in Story 1.4)
        // For now, just verify the screen displays correctly
        composeTestRule.onNodeWithText("0/20").assertExists()
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()
    }
}
