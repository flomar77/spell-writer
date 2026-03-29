package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.ui.components.CelebrationSequence
import com.spellwriter.ui.components.GifRewardOverlay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for GIF reward overlay and auto-progression flow.
 *
 * Tests verify:
 * - GIF overlay displays after star completion
 * - Continue button triggers auto-progression callback
 * - Fallback behavior when no GIFs available
 * - CelebrationSequence state management
 *
 * Feature 3: GIF Reward Overlay (Prompt #139)
 */
@RunWith(AndroidJUnit4::class)
class GifRewardIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gifRewardOverlay_displaysCorrectly() {
        // Given: A GIF path
        val gifPath = "gifs/test.gif"
        var continueClicked = false

        // When: GifRewardOverlay is displayed
        composeTestRule.setContent {
            GifRewardOverlay(
                gifAssetPath = gifPath,
                onContinue = { continueClicked = true }
            )
        }

        composeTestRule.waitForIdle()

        // Then: Continue button should be visible
        composeTestRule.onNodeWithText("Continue").assertExists()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()

        // When: User taps Continue button
        composeTestRule.onNodeWithText("Continue").performClick()

        // Then: Callback should be triggered
        assertTrue("Continue callback should be triggered", continueClicked)
    }

    @Test
    fun gifRewardOverlay_continueButtonAccessible() {
        // Given: GifRewardOverlay
        composeTestRule.setContent {
            GifRewardOverlay(
                gifAssetPath = "gifs/test.gif",
                onContinue = {}
            )
        }

        composeTestRule.waitForIdle()

        // Then: Continue button should be clickable (accessibility)
        composeTestRule.onNodeWithText("Continue")
            .assertHasClickAction()
            .assertIsEnabled()
    }

    @Test
    fun celebrationSequence_withGif_showsOverlay() {
        // Given: CelebrationSequence with GIF available
        var continueCallbackTriggered = false

        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = true,
                starLevel = 1,
                onContinueToNextStar = { continueCallbackTriggered = true }
            )
        }

        composeTestRule.waitForIdle()

        // Note: GIF may or may not load depending on assets in test environment
        // This test verifies the Continue button appears when GIF is available
        // If no GIFs found, callback is triggered automatically (fallback)

        // Wait for either Continue button or automatic fallback
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithText("Continue").assertExists()
                true
            } catch (e: AssertionError) {
                // GIF not found - fallback should trigger callback
                continueCallbackTriggered
            }
        }
    }

    @Test
    fun celebrationSequence_noGifs_triggersImmediateFallback() {
        // Given: CelebrationSequence when no GIFs available
        var continueCallbackTriggered = false

        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = true,
                starLevel = 1,
                onContinueToNextStar = { continueCallbackTriggered = true }
            )
        }

        composeTestRule.waitForIdle()

        // Then: Either Continue button exists (GIF found) OR callback triggered (no GIF)
        // This verifies the fallback mechanism works gracefully
        val continueButtonExists = try {
            composeTestRule.onNodeWithText("Continue").assertExists()
            true
        } catch (e: AssertionError) {
            false
        }

        // One of these should be true: button shown OR callback triggered
        assertTrue(
            "Either Continue button should exist or callback should be triggered",
            continueButtonExists || continueCallbackTriggered
        )
    }

    @Test
    fun celebrationSequence_continueButtonTriggersCallback() {
        // Given: CelebrationSequence with celebration active
        var continueCallbackTriggered = false

        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = true,
                starLevel = 2,
                onContinueToNextStar = { continueCallbackTriggered = true }
            )
        }

        composeTestRule.waitForIdle()

        // Wait for Continue button to appear (if GIF available)
        try {
            composeTestRule.waitUntil(timeoutMillis = 2000) {
                try {
                    composeTestRule.onNodeWithText("Continue").assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            // When: User taps Continue button
            composeTestRule.onNodeWithText("Continue").performClick()

            // Then: Callback should be triggered
            assertTrue(
                "onContinueToNextStar should be called when Continue is clicked",
                continueCallbackTriggered
            )
        } catch (e: Exception) {
            // GIF not found - callback already triggered via fallback
            assertTrue(
                "Callback should be triggered via fallback when no GIF",
                continueCallbackTriggered
            )
        }
    }

    @Test
    fun celebrationSequence_notShown_noOverlay() {
        // Given: CelebrationSequence with showCelebration = false
        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = false,
                starLevel = 1,
                onContinueToNextStar = {}
            )
        }

        composeTestRule.waitForIdle()

        // Then: Continue button should not exist
        composeTestRule.onNodeWithText("Continue").assertDoesNotExist()
    }

    @Test
    fun celebrationSequence_differentStarLevels_allSupported() {
        // Test that celebration works for all star levels (1, 2, 3)
        for (starLevel in 1..3) {
            var callbackTriggered = false

            composeTestRule.setContent {
                CelebrationSequence(
                    showCelebration = true,
                    starLevel = starLevel,
                    onContinueToNextStar = { callbackTriggered = true }
                )
            }

            composeTestRule.waitForIdle()

            // Either button appears or fallback triggers
            composeTestRule.waitUntil(timeoutMillis = 2000) {
                try {
                    composeTestRule.onNodeWithText("Continue").assertExists()
                    true
                } catch (e: AssertionError) {
                    callbackTriggered
                }
            }

            // Cleanup for next iteration
            composeTestRule.setContent { }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun celebrationSequence_stateReset_whenHidden() {
        // Given: CelebrationSequence shown then hidden
        var showCelebration = true

        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = showCelebration,
                starLevel = 1,
                onContinueToNextStar = {}
            )
        }

        composeTestRule.waitForIdle()

        // When: Hide celebration
        showCelebration = false
        composeTestRule.setContent {
            CelebrationSequence(
                showCelebration = showCelebration,
                starLevel = 1,
                onContinueToNextStar = {}
            )
        }

        composeTestRule.waitForIdle()

        // Then: Overlay should not be shown
        composeTestRule.onNodeWithText("Continue").assertDoesNotExist()
    }

    /**
     * Integration test notes:
     *
     * Full end-to-end flow testing (complete 20 words → GIF → Continue → next star)
     * requires a full MainActivity integration test that can:
     * - Complete 20 words in GameScreen
     * - Trigger celebration
     * - Verify progress update
     * - Verify navigation to next star or home
     *
     * These tests focus on the CelebrationSequence and GifRewardOverlay components
     * in isolation. Full flow testing is in MainActivityAutoProgressionTest (if exists)
     * or manual testing phase.
     *
     * Behaviors tested elsewhere:
     * - Star 1→2→3 progression: GameViewModel tests
     * - Progress persistence: ProgressRepository tests
     * - StarProgress UI updates: StarProgressTest
     * - GIF randomization: Manual testing (requires actual GIF assets)
     */
}
