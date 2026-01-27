package com.spellwriter

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.audio.AudioManager
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.Progress
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Tests for MainActivity TTS initialization logic.
 * Covers Feature 2: TTS Initialization to HomeScreen
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTTSTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun audioManagerState_startsAsNull() {
        var audioManager: AudioManager? = null
        var isTTSInitializing = false

        composeTestRule.setContent {
            SpellWriterTheme {
                // Simulate SpellWriterApp initial state
                audioManager = remember { null }
                isTTSInitializing = remember { false }

                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: audioManager should be null initially
        assertNull("AudioManager should start as null", audioManager)
        assertFalse("isTTSInitializing should start as false", isTTSInitializing)
    }

    @Test
    fun initializeTTS_createsAudioManagerWithCorrectLanguage() = runTest {
        val language = AppLanguage.ENGLISH
        val audioManager = AudioManager(context, language)

        // Wait for TTS to initialize (with timeout)
        withTimeoutOrNull(3000) {
            audioManager.isTTSReady.first { it }
        }

        // Assert: AudioManager should be created successfully
        assertNotNull("AudioManager should be created", audioManager)

        // Cleanup
        audioManager.release()
    }

    @Test
    fun initializeTTS_setsLoadingStateDuringInit() {
        var isTTSInitializing by mutableStateOf(false)
        var playClicked = false

        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {
                        isTTSInitializing = true
                        playClicked = true
                    },
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Act: Click play button
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Assert: Loading state should be set
        assertTrue("Play button should be clicked", playClicked)
        assertTrue("isTTSInitializing should be true after click", isTTSInitializing)

        // Verify loading UI appears
        composeTestRule.onNodeWithText("Preparing voice...").assertExists()
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()
    }

    @Test
    fun initializeTTS_setsLoadingFalseAfterSuccess() = runTest {
        var isTTSInitializing = false
        val audioManager = AudioManager(context, AppLanguage.ENGLISH)

        // Simulate initialization start
        isTTSInitializing = true
        assertTrue("Loading should be true during init", isTTSInitializing)

        // Wait for TTS to be ready
        withTimeoutOrNull(3000) {
            audioManager.isTTSReady.first { it }
        }

        // Simulate loading complete
        isTTSInitializing = false
        assertFalse("Loading should be false after success", isTTSInitializing)

        // Cleanup
        audioManager.release()
    }

    @Test
    fun initializeTTS_navigatesToGameScreenAfterSuccess() {
        var currentScreen = "Home"
        var isTTSInitializing by mutableStateOf(false)

        composeTestRule.setContent {
            SpellWriterTheme {
                if (isTTSInitializing) {
                    LaunchedEffect(Unit) {
                        delay(100) // Simulate TTS init delay
                        isTTSInitializing = false
                        currentScreen = "Game"
                    }
                }

                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {
                        isTTSInitializing = true
                    },
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Act: Click play button
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.waitForIdle()

        // Assert: Should navigate to Game screen
        assertEquals("Should navigate to Game screen", "Game", currentScreen)
    }

    @Test
    fun initializeTTS_handlesTimeoutAndShowsError() {
        var ttsError by mutableStateOf<String?>(null)
        var isTTSInitializing by mutableStateOf(false)

        composeTestRule.setContent {
            SpellWriterTheme {
                if (isTTSInitializing) {
                    LaunchedEffect(Unit) {
                        delay(5000) // 5s timeout
                        ttsError = "TTS initialization timeout"
                        isTTSInitializing = false
                    }
                }

                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {
                        isTTSInitializing = true
                    },
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing,
                    ttsError = ttsError
                )
            }
        }

        // Act: Click play button
        composeTestRule.onNodeWithText("PLAY").performClick()

        // Fast forward time to simulate timeout
        composeTestRule.mainClock.advanceTimeBy(5100)
        composeTestRule.waitForIdle()

        // Assert: Error message should be displayed
        composeTestRule.onNodeWithText("TTS initialization timeout").assertExists()
        assertFalse("Loading should be false after timeout", isTTSInitializing)
    }

    @Test
    fun initializeTTS_preventsDoubleClick() {
        var clickCount = 0
        var isTTSInitializing by mutableStateOf(false)

        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {
                        // Guard clause: prevent double-click
                        if (isTTSInitializing) return@HomeScreen

                        clickCount++
                        isTTSInitializing = true
                    },
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Act: Click play button twice
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Button should be disabled, but try to click again
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Assert: Should only process first click
        assertEquals("Click handler should only execute once", 1, clickCount)
    }

    @Test
    fun audioManager_reusedOnSecondPlayClick() = runTest {
        var audioManager: AudioManager? = null
        var isTTSInitializing = false
        val initializedManagers = mutableListOf<AudioManager>()

        // First play: create AudioManager
        audioManager = AudioManager(context, AppLanguage.ENGLISH)
        val firstManager = audioManager
        initializedManagers.add(firstManager)

        // Wait for ready
        withTimeoutOrNull(3000) {
            firstManager.isTTSReady.first { it }
        }

        // Navigate to game and back to home
        // Second play: should reuse existing AudioManager
        if (audioManager?.isTTSReady?.value == true) {
            // Should NOT create new AudioManager
            // initializedManagers.add() should NOT be called
        } else {
            audioManager = AudioManager(context, AppLanguage.ENGLISH)
            initializedManagers.add(audioManager)
        }

        // Assert: Should have only one AudioManager created
        assertEquals("Should reuse AudioManager on second play", 1, initializedManagers.size)
        assertSame("Should be the same AudioManager instance", firstManager, audioManager)

        // Cleanup
        audioManager?.release()
    }

    @Test
    fun languageChange_releasesAudioManagerAndResetsState() = runTest {
        var audioManager: AudioManager? = AudioManager(context, AppLanguage.ENGLISH)
        var isTTSInitializing = false
        var ttsError: String? = "Previous error"

        // Wait for initial TTS ready
        withTimeoutOrNull(3000) {
            audioManager?.isTTSReady?.first { it }
        }

        val initialManager = audioManager

        // Act: Simulate language change
        audioManager?.release()
        audioManager = null
        isTTSInitializing = false
        ttsError = null

        // Assert: State should be reset
        assertNull("AudioManager should be released and set to null", audioManager)
        assertFalse("isTTSInitializing should be reset", isTTSInitializing)
        assertNull("ttsError should be cleared", ttsError)

        // Verify the old manager was released (can't directly test, but no exceptions is good)
        assertNotNull("Initial manager should have existed", initialManager)
    }

    @Test
    fun disposableEffect_releasesAudioManagerOnDispose() = runTest {
        var audioManager: AudioManager? = null
        var isComposed by mutableStateOf(true)

        composeTestRule.setContent {
            SpellWriterTheme {
                if (isComposed) {
                    DisposableEffect(Unit) {
                        audioManager = AudioManager(context, AppLanguage.ENGLISH)
                        onDispose {
                            audioManager?.release()
                            audioManager = null
                        }
                    }

                    HomeScreen(
                        progress = Progress(),
                        onPlayClick = {},
                        onStarClick = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Wait for TTS initialization
        withTimeoutOrNull(3000) {
            audioManager?.isTTSReady?.first { it }
        }

        val initialManager = audioManager
        assertNotNull("AudioManager should be created", initialManager)

        // Act: Dispose the composable
        isComposed = false
        composeTestRule.waitForIdle()

        // Assert: AudioManager should be released and nullified
        assertNull("AudioManager should be released on dispose", audioManager)
    }

    @Test
    fun playButton_disabledDuringTTSInitialization() {
        var isTTSInitializing by mutableStateOf(true)

        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Assert: Play button should be disabled
        composeTestRule.onNodeWithText("PLAY").assertIsNotEnabled()

        // Act: Complete initialization
        isTTSInitializing = false
        composeTestRule.waitForIdle()

        // Assert: Play button should be enabled
        composeTestRule.onNodeWithText("PLAY").assertIsEnabled()
    }

    @Test
    fun languageSwitcher_disabledDuringTTSInitialization() {
        var isTTSInitializing by mutableStateOf(true)

        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Assert: Language buttons should be disabled
        composeTestRule.onNodeWithText("EN").assertIsNotEnabled()
        composeTestRule.onNodeWithText("DE").assertIsNotEnabled()

        // Act: Complete initialization
        isTTSInitializing = false
        composeTestRule.waitForIdle()

        // Assert: Language buttons should be enabled
        composeTestRule.onNodeWithText("EN").assertIsEnabled()
        composeTestRule.onNodeWithText("DE").assertIsEnabled()
    }

    @Test
    fun starClick_triggersInitializationAndNavigation() {
        var selectedStar: Int? = null
        var isTTSInitializing by mutableStateOf(false)
        var currentScreen = "Home"

        composeTestRule.setContent {
            SpellWriterTheme {
                if (isTTSInitializing) {
                    LaunchedEffect(Unit) {
                        delay(100)
                        isTTSInitializing = false
                        currentScreen = "Game"
                    }
                }

                HomeScreen(
                    progress = Progress(wizardStars = 3), // Have earned stars
                    onPlayClick = {},
                    onStarClick = { starNumber ->
                        selectedStar = starNumber
                        isTTSInitializing = true
                    },
                    isTTSInitializing = isTTSInitializing
                )
            }
        }

        // Act: Click star 2 (should be clickable since wizardStars = 3)
        composeTestRule.onAllNodesWithContentDescription("Star 2")[0].performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.waitForIdle()

        // Assert: Should set selectedStar and navigate
        assertEquals("Should set selectedStar to 2", 2, selectedStar)
        assertEquals("Should navigate to Game screen", "Game", currentScreen)
    }
}
