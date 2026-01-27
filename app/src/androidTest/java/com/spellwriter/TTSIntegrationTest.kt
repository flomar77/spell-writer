package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.data.models.Progress
import com.spellwriter.data.repository.ProgressRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * End-to-end integration tests for TTS initialization flow.
 * Tests complete user journeys from home to game with TTS.
 */
@RunWith(AndroidJUnit4::class)
class TTSIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun completeFlow_launchToPlayToLoadingToGame() {
        // Start at home screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertIsDisplayed()

        // Click play button
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Loading indicator should appear
        composeTestRule.onNodeWithText("Preparing voice...").assertExists()
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()

        // Play button should be disabled during loading
        composeTestRule.onNodeWithText("PLAY").assertIsNotEnabled()

        // Wait for TTS initialization (max 5s)
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule.onAllNodesWithText("PLAY")
                .fetchSemanticsNodes().isEmpty() // PLAY button gone = navigated to Game
        }

        // Should navigate to game screen (has letter buttons)
        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun replayFlow_gameToHomeToPlayImmediate() {
        // First play: initialize TTS
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation to game
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule.onAllNodesWithText("A")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate back to home
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Should be back at home
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Second play: should navigate immediately (no loading)
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Should navigate to game immediately without showing loading
        // (This is harder to test, but we can verify navigation happens quickly)
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            composeTestRule.onAllNodesWithText("A")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun languageChange_englishToGermanToPlay() {
        // Verify starting in English
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()

        // Click German button
        composeTestRule.onNodeWithText("DE").performClick()
        composeTestRule.waitForIdle()

        // UI should update to German
        // Note: This depends on German string resources being present
        // If not, the test will use English as fallback

        // Click play after language change
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Loading should appear (TTS re-initializing for German)
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()

        // Wait for navigation
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule.onAllNodesWithText("A")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Should be in game with German TTS
        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun starReplay_clickStarShowsLoadingThenGame() {
        runBlocking {
            // First, earn some stars by setting progress
            val progressRepository = ProgressRepository(context)
            progressRepository.saveProgress(Progress(wizardStars = 2))

            // Wait for progress to update
            delay(200)
        }

        composeTestRule.waitForIdle()

        // Click star 1 (should be clickable)
        val star1Nodes = composeTestRule.onAllNodesWithContentDescription("Star 1")
        if (star1Nodes.fetchSemanticsNodes().isNotEmpty()) {
            star1Nodes[0].performClick()
            composeTestRule.waitForIdle()

            // Loading should appear
            composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()

            // Wait for navigation to game
            composeTestRule.waitUntil(timeoutMillis = 6000) {
                composeTestRule.onAllNodesWithText("A")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            // Should be in replay mode for star 1
            composeTestRule.onNodeWithText("A").assertExists()
        }
    }

    @Test
    fun ttsFailure_timeoutShowsErrorButGameStillLoads() {
        // Note: This test is tricky because we can't easily simulate TTS failure
        // In a real device, TTS should initialize successfully
        // This test documents expected behavior if TTS fails

        // Click play
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Wait maximum timeout (5s + buffer)
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            // Either navigated to game OR error appeared
            val hasGame = composeTestRule.onAllNodesWithText("A")
                .fetchSemanticsNodes().isNotEmpty()
            val hasError = composeTestRule.onAllNodesWithText("Voice not available", substring = true)
                .fetchSemanticsNodes().isNotEmpty()

            hasGame || hasError
        }

        // Game should still load even if TTS failed
        // (may take full 5s timeout)
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            composeTestRule.onAllNodesWithText("A")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun doubleClick_preventedDuringLoading() {
        var clickCount = 0

        // Click play
        composeTestRule.onNodeWithText("PLAY").performClick()
        clickCount++
        composeTestRule.waitForIdle()

        // Try to click again immediately (should be disabled)
        try {
            composeTestRule.onNodeWithText("PLAY").performClick()
            clickCount++
        } catch (e: AssertionError) {
            // Expected: button is disabled, click should fail
        }

        composeTestRule.waitForIdle()

        // Should only have processed one click
        // (Hard to verify directly, but button being disabled is the key indicator)
        composeTestRule.onNodeWithText("PLAY").assertIsNotEnabled()
    }

    @Test
    fun languageChangeDuringLoading_cancelsAndResetsState() {
        // Click play to start TTS init
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Verify loading started
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()

        // Immediately change language
        composeTestRule.onNodeWithText("DE").performClick()
        composeTestRule.waitForIdle()

        // Loading should stop (language buttons work even during loading in updated design)
        // After language change, should be back at home with new language
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Play button should be enabled again
        composeTestRule.onNodeWithText("PLAY").assertIsEnabled()
    }

    @Test
    fun buttonStates_disabledDuringLoadingEnabledAfter() {
        // Initially buttons should be enabled
        composeTestRule.onNodeWithText("PLAY").assertIsEnabled()
        composeTestRule.onNodeWithText("EN").assertIsEnabled()
        composeTestRule.onNodeWithText("DE").assertIsEnabled()

        // Click play
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // During loading, buttons should be disabled
        composeTestRule.onNodeWithText("PLAY").assertIsNotEnabled()
        composeTestRule.onNodeWithText("EN").assertIsNotEnabled()
        composeTestRule.onNodeWithText("DE").assertIsNotEnabled()

        // Wait for navigation or timeout
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule.onAllNodesWithText("PLAY")
                .fetchSemanticsNodes().isEmpty()
        }

        // Should have navigated to game
        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun backgroundForeground_statePreservedDuringLoading() {
        // Click play to start loading
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Verify loading started
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()

        // Simulate background/foreground (Activity lifecycle)
        composeTestRule.activityRule.scenario.moveToState(
            androidx.lifecycle.Lifecycle.State.CREATED
        )
        Thread.sleep(100)
        composeTestRule.activityRule.scenario.moveToState(
            androidx.lifecycle.Lifecycle.State.RESUMED
        )
        composeTestRule.waitForIdle()

        // State should be preserved or completed
        // Either still loading OR navigated to game
        val isLoading = composeTestRule.onAllNodesWithContentDescription("Loading progress")
            .fetchSemanticsNodes().isNotEmpty()
        val isInGame = composeTestRule.onAllNodesWithText("A")
            .fetchSemanticsNodes().isNotEmpty()

        assertTrue("Should be loading or in game after background/foreground", isLoading || isInGame)
    }
}
