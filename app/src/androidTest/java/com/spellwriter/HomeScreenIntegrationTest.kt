package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.data.models.MAX_STARS
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.screens.HomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class HomeScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysStarProgress() {
        val progress = Progress(stars = 1)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun homeScreen_initialState_allStarsUnearned() {
        val progress = Progress(stars = 0)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun homeScreen_starClickTriggersCallback() {
        val progress = Progress(stars = 2)
        var clickedStar: Int? = null

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = { clickedStar = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 2 (earned)")
            .performClick()

        assertEquals(2, clickedStar)
    }

    @Test
    fun homeScreen_playButtonTriggersCallback() {
        val progress = Progress(stars = 1)
        var playClicked = false

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = { playClicked = true },
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithText("PLAY").performClick()

        assertTrue(playClicked)
    }

    @Test
    fun homeScreen_maintainsVisualHierarchy() {
        val progress = Progress(stars = 0)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithText("Spell Writer").assertExists()
        composeTestRule.onNodeWithContentDescription("Ghost").assertExists()
        composeTestRule.onNodeWithText("Tap PLAY to start your magical spelling adventure!").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()
    }

    @Test
    fun homeScreen_withMaxProgress_allStarsEarned() {
        val progress = Progress(stars = MAX_STARS)

        composeTestRule.setContent {
            HomeScreen(
                progress = progress,
                onPlayClick = {},
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (earned)").assertExists()
    }

    @Test
    fun homeScreen_languageSwitch_triggersCallback() {
        val progress = Progress(stars = 0)
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

        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()

        composeTestRule.onNodeWithText("Deutsch").performClick()

        assertEquals("de", languageChanged)
    }
}
