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

@RunWith(AndroidJUnit4::class)
class WorldProgressRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun worldProgressRow_displaysCorrectNumberOfStars() {
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 2,
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun worldProgressRow_allStarsUnearned() {
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 0,
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (locked)").assertExists()
    }

    @Test
    fun worldProgressRow_allStarsEarned() {
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 3,
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").assertExists()
        composeTestRule.onNodeWithContentDescription("Star 3 (earned)").assertExists()
    }

    @Test
    fun earnedStar_isClickable() {
        var clickedStar: Int? = null
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 2,
                onStarClick = { clickedStar = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)").performClick()

        assertEquals(1, clickedStar)
    }

    @Test
    fun earnedStar_clickPassesCorrectStarNumber() {
        var clickedStar: Int? = null
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 3,
                onStarClick = { clickedStar = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 2 (earned)").performClick()
        assertEquals(2, clickedStar)

        composeTestRule.onNodeWithContentDescription("Star 3 (earned)").performClick()
        assertEquals(3, clickedStar)
    }

    @Test
    fun unearnedStar_isNotClickable() {
        var clicked = false
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 1,
                onStarClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 2 (locked)").performClick()

        assertFalse(clicked)
    }

    @Test
    fun stars_meetMinimumTouchTargetSize() {
        composeTestRule.setContent {
            WorldProgressRow(
                earnedStars = 1,
                onStarClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Star 1 (earned)")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithContentDescription("Star 2 (locked)")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }
}
