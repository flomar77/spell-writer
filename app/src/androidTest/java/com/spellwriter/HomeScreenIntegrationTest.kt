package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.screens.HomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for HomeScreen with Progress integration (Story 1.2).
 * Tests that HomeScreen properly displays and interacts with star progress.
 */
@RunWith(AndroidJUnit4::class)
class
HomeScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysStarProgress() {
        val progress = Progress(wizardStars = 1)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        // Verify world name is displayed
        composeTestRule.onNodeWithText("Wizard World").assertExists()

        // Verify stars are displayed correctly (1 earned, 2 locked)
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun homeScreen_initialState_allStarsUnearned() {
        val progress = Progress(wizardStars = 0)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        // All stars should be locked for new user
        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun homeScreen_starClickTriggersCallback() {
        val progress = Progress(wizardStars = 2)
        var clickedStar: Int? = null

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = { clickedStar = it }
            )
        }

        // Click on star 2 (earned)
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)")
            .performClick()

        // Verify callback was triggered with correct star number
        assertEquals(2, clickedStar)
    }

    @Test
    fun homeScreen_playButtonTriggersCallback() {
        val progress = Progress(wizardStars = 1)
        var playClicked = false

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = { playClicked = true },
                onStarClick = {}
            )
        }

        // Click PLAY button
        composeTestRule.onNodeWithText("PLAY").performClick()

        // Verify callback was triggered
        assertTrue(playClicked)
        // Note: Actual star selection logic (getCurrentStar) is in MainActivity
    }

    @Test
    fun homeScreen_maintainsVisualHierarchy() {
        val progress = Progress(wizardStars = 0)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        // Verify all main elements exist in proper order
        composeTestRule.onNodeWithText("Spell Writer").assertExists()  // Title
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()  // Ghost
        composeTestRule.onNodeWithText("Tap PLAY to start your magical spelling adventure!").assertExists()  // Instruction
        composeTestRule.onNodeWithText("Wizard World").assertExists()  // Stars
        composeTestRule.onNodeWithText("PLAY").assertExists()  // Play button
    }

    @Test
    fun homeScreen_withMaxProgress_allStarsEarned() {
        val progress = Progress(wizardStars = 3)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        // All stars should be earned
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (earned)").assertExists()
    }

    @Test
    fun homeScreen_languageSwitch_triggersCallback() {
        val progress = Progress(wizardStars = 0)
        var languageChanged: String? = null

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {},
                onLanguageChanged = { newLanguage ->
                    languageChanged = newLanguage
                }
            )
        }

        // Verify English strings are initially displayed
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Switch to German
        composeTestRule.onNodeWithText("Deutsch").performClick()

        // Verify callback was triggered with correct language code
        // In actual app, this triggers activity.recreate() which reloads with German strings
        assertEquals("de", languageChanged)
    }
}
