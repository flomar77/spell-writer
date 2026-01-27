package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for HomeScreen composable.
 * Tests all acceptance criteria for Story 1.1.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAllRequiredElements() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - AC1: App title, ghost, and instruction text
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("To win, write the words you will hear correctly").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Ghost character should be visible (using content description)
        composeTestRule.onNodeWithContentDescription("Ghost character with neutral expression").assertExists()
    }

    @Test
    fun homeScreen_appTitleIsProminentlyDisplayed() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - Title should be large and bold (AC1)
        val titleNode = composeTestRule.onNodeWithText("SPELL WRITER")
        titleNode.assertExists()
        titleNode.assertIsDisplayed()
    }

    @Test
    fun homeScreen_ghostCharacterDisplayed() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - AC1: Ghost with neutral expression
        composeTestRule
            .onNodeWithContentDescription("Ghost character with neutral expression")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_instructionTextDisplayed() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - AC1: Instruction text explaining how to win
        composeTestRule
            .onNodeWithText("To win, write the words you will hear correctly")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun playButton_hasMinimumTouchTarget() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - AC2: Touch target ≥48dp (we use 56dp)
        val playButton = composeTestRule.onNodeWithText("PLAY")
        playButton.assertExists()
        playButton.assertHeightIsAtLeast(48.dp)
        playButton.assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun playButton_isAccessibleAndClickable() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - AC2: Button is accessible and clickable
        val playButton = composeTestRule.onNodeWithText("PLAY")
        playButton.assertExists()
        playButton.assertIsDisplayed()
        playButton.assertHasClickAction()
    }

    @Test
    fun playButton_triggersNavigationOnClick() {
        // Arrange
        var playClicked = false
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = { playClicked = true },
                    onStarClick = {}
                )
            }
        }

        // Act - AC3: Tapping PLAY button navigates to game
        composeTestRule.onNodeWithText("PLAY").performClick()

        // Assert
        assert(playClicked) { "Play button click should trigger onPlayClick callback" }
    }

    @Test
    fun homeScreen_allElementsVisibleWithoutScrolling() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {}
                )
            }
        }

        // Assert - All elements should be visible in viewport (AC2: child-friendly)
        composeTestRule.onNodeWithText("SPELL WRITER").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ghost character with neutral expression").assertIsDisplayed()
        composeTestRule.onNodeWithText("To win, write the words you will hear correctly").assertIsDisplayed()
        composeTestRule.onNodeWithText("PLAY").assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_displaysWhenTTSInitializing() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = true
                )
            }
        }

        // Assert - Loading indicator should be visible
        composeTestRule.onNodeWithText("Preparing voice...").assertExists()
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()
    }

    @Test
    fun loadingText_displaysCorrectStringResource() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = true
                )
            }
        }

        // Assert - Loading text should match string resource
        composeTestRule.onNodeWithText("Preparing voice...").assertExists()
    }

    @Test
    fun linearProgressIndicator_rendersDuringLoading() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = true
                )
            }
        }

        // Assert - LinearProgressIndicator should be visible
        composeTestRule.onNodeWithContentDescription("Loading progress").assertExists()
    }

    @Test
    fun playButton_disabledWhenTTSInitializing() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = true
                )
            }
        }

        // Assert - Play button should be disabled
        composeTestRule.onNodeWithText("PLAY").assertIsNotEnabled()
    }

    @Test
    fun playButton_enabledWhenTTSNotInitializing() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = false
                )
            }
        }

        // Assert - Play button should be enabled
        composeTestRule.onNodeWithText("PLAY").assertIsEnabled()
    }

    @Test
    fun errorMessage_displaysWhenTTSErrorNotNull() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    ttsError = "TTS initialization failed"
                )
            }
        }

        // Assert - Error message should be visible
        composeTestRule.onNodeWithText("TTS initialization failed").assertExists()
    }

    @Test
    fun errorMessage_hiddenWhenTTSErrorNull() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    ttsError = null
                )
            }
        }

        // Assert - Error message should not be visible
        composeTestRule.onNodeWithText("TTS initialization failed").assertDoesNotExist()
    }

    @Test
    fun languageSwitcher_disabledDuringLoading() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                HomeScreen(
                    progress = com.spellwriter.data.models.Progress(),
                    onPlayClick = {},
                    onStarClick = {},
                    isTTSInitializing = true
                )
            }
        }

        // Assert - Language buttons should be disabled
        composeTestRule.onNodeWithText("EN").assertIsNotEnabled()
        composeTestRule.onNodeWithText("DE").assertIsNotEnabled()
    }
}
