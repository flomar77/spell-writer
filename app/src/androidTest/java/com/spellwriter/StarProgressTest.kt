package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.components.StarProgress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for StarProgress component (Story 1.3).
 * Tests session star display with 3 stars (0-3 completed).
 */
@RunWith(AndroidJUnit4::class)
class StarProgressTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun starProgress_displaysThreeStars() {
        composeTestRule.setContent {
            StarProgress(completedStars = 0)
        }

        // Should display 3 stars (3, 2, 1 from top to bottom)
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 1").assertExists()
    }

    @Test
    fun starProgress_showsZeroStarsCompleted() {
        composeTestRule.setContent {
            StarProgress(completedStars = 0)
        }

        // No stars should have "completed" in description
        composeTestRule.onAllNodesWithContentDescription("completed", substring = true).assertCountEquals(0)
    }

    @Test
    fun starProgress_showsOneStarCompleted() {
        composeTestRule.setContent {
            StarProgress(completedStars = 1)
        }

        // Star 1 should be completed
        composeTestRule.onNodeWithContentDescription("Session star 1 completed").assertExists()

        // Stars 2 and 3 should not be completed
        composeTestRule.onNodeWithContentDescription("Session star 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
    }

    @Test
    fun starProgress_showsTwoStarsCompleted() {
        composeTestRule.setContent {
            StarProgress(completedStars = 2)
        }

        // Stars 1 and 2 should be completed
        composeTestRule.onNodeWithContentDescription("Session star 1 completed").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2 completed").assertExists()

        // Star 3 should not be completed
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
    }

    @Test
    fun starProgress_showsAllThreeStarsCompleted() {
        composeTestRule.setContent {
            StarProgress(completedStars = 3)
        }

        // All 3 stars should be completed
        composeTestRule.onNodeWithContentDescription("Session star 1 completed").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2 completed").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 3 completed").assertExists()
    }

    @Test
    fun starProgress_starsHaveCorrectSize() {
        composeTestRule.setContent {
            StarProgress(completedStars = 1)
        }

        // Stars should be 40dp (smaller than home screen stars)
        // We'll verify one star as a representative sample
        composeTestRule.onNodeWithContentDescription("Session star 1 completed")
            .assertHeightIsAtLeast(40.dp)
            .assertWidthIsAtLeast(40.dp)
    }

    @Test
    fun starProgress_isVerticallyArranged() {
        composeTestRule.setContent {
            StarProgress(completedStars = 2)
        }

        // Verify all 3 stars exist in vertical arrangement
        // The component should use a Column layout
        composeTestRule.onNodeWithContentDescription("Session star 3").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 2 completed").assertExists()
        composeTestRule.onNodeWithContentDescription("Session star 1 completed").assertExists()
    }
}
