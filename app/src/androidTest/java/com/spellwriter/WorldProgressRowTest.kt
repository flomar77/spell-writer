package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.components.WorldProgressRow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * UI tests for WorldProgressRow component (Story 1.2).
 * Tests star display, click handling, and accessibility.
 */
@RunWith(AndroidJUnit4::class)
class WorldProgressRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun worldProgressRow_displaysWorldName() {
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 0,
                onStarClick = {}
            )
        }

        // Verify world name is displayed
        composeTestRule.onNodeWithText("Wizard World").assertExists()
    }

    @Test
    fun worldProgressRow_displaysCorrectNumberOfStars() {
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 2,
                onStarClick = {}
            )
        }

        // Verify 3 stars are displayed: 2 earned, 1 locked
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun worldProgressRow_allStarsUnearned() {
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 0,
                onStarClick = {}
            )
        }

        // All stars should be locked
        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun worldProgressRow_allStarsEarned() {
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 3,
                onStarClick = {}
            )
        }

        // All stars should be earned
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (earned)").assertExists()
    }

    @Test
    fun earnedStar_isClickable() {
        var clickedStar: Int? = null
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 2,
                onStarClick = { clickedStar = it }
            )
        }

        // Click on star 1 (earned)
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)")
            .performClick()

        // Verify callback was triggered with star number 1
        assertEquals(1, clickedStar)
    }

    @Test
    fun earnedStar_clickPassesCorrectStarNumber() {
        var clickedStar: Int? = null
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 3,
                onStarClick = { clickedStar = it }
            )
        }

        // Click on star 2
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)")
            .performClick()

        assertEquals(2, clickedStar)

        // Click on star 3
        composeTestRule.onNodeWithContentDescription("Star 3 (earned)")
            .performClick()

        assertEquals(3, clickedStar)
    }

    @Test
    fun unearnedStar_isNotClickable() {
        var clicked = false
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 1,
                onStarClick = { clicked = true }
            )
        }

        // Try to click on star 2 (unearned/locked)
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)")
            .performClick()

        // Verify callback was NOT triggered
        assertFalse(clicked)
    }

    @Test
    fun stars_meetMinimumTouchTargetSize() {
        composeTestRule.setContent {
            WorldProgressRow(
                worldName = "Wizard World",
                earnedStars = 1,
                onStarClick = {}
            )
        }

        // Verify each star meets 48dp minimum (we use 56dp per story requirements)
        composeTestRule.onNodeWithContentDescription("Star 1 (earned)")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithContentDescription("Star 2 (locked)")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }
}
