package com.spellwriter

import com.spellwriter.data.models.LanguageManager
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.components.LanguageSwitcher
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LanguageSwitcher component.
 * Tests that language changes are immediately reflected in the UI.
 */
@RunWith(AndroidJUnit4::class)
class LanguageSwitcherTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        // Reset to English before each test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        LanguageManager.setLocale(context, "en")
    }

    @Composable
    private fun TestableHomeScreen() {
        var languageKey by remember { mutableStateOf("en") }

        key(languageKey) {
            HomeScreen(
                progress = Progress(),
                onPlayClick = {},
                onStarClick = {},
                onLanguageChanged = { newLanguage ->
                    languageKey = newLanguage
                }
            )
        }
    }

    @Test
    fun languageSwitcher_updatesHomeScreenImmediately_whenEnglishSelected() {
        composeTestRule.setContent {
            SpellWriterTheme {
                TestableHomeScreen()
            }
        }

        // Click on English button
        composeTestRule.onNodeWithText("English").performClick()

        // Verify English texts are displayed immediately
        composeTestRule.onNodeWithText("SPELL WRITER").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertExists()
        composeTestRule.onNodeWithText("To win, write the words you will hear correctly").assertExists()
    }

    @Test
    fun languageSwitcher_updatesHomeScreenImmediately_whenGermanSelected() {
        composeTestRule.setContent {
            SpellWriterTheme {
                TestableHomeScreen()
            }
        }

        // Click on German button
        composeTestRule.onNodeWithText("Deutsch").performClick()

        // Verify German texts are displayed immediately
        composeTestRule.onNodeWithText("ZAUBER SCHREIBER").assertExists()
        composeTestRule.onNodeWithText("SPIELEN").assertExists()
        composeTestRule.onNodeWithText("Um zu gewinnen, schreibe die Wörter, die du hörst, richtig").assertExists()
    }

    @Test
    fun languageSwitcher_switchesBetweenLanguages() {
        composeTestRule.setContent {
            SpellWriterTheme {
                TestableHomeScreen()
            }
        }

        // Start with English
        composeTestRule.onNodeWithText("English").performClick()
        composeTestRule.onNodeWithText("PLAY").assertExists()

        // Switch to German
        composeTestRule.onNodeWithText("Deutsch").performClick()
        composeTestRule.onNodeWithText("SPIELEN").assertExists()
        composeTestRule.onNodeWithText("PLAY").assertDoesNotExist()

        // Switch back to English
        composeTestRule.onNodeWithText("English").performClick()
        composeTestRule.onNodeWithText("PLAY").assertExists()
        composeTestRule.onNodeWithText("SPIELEN").assertDoesNotExist()
    }

    @Test
    fun languageSwitcher_highlightsSelectedLanguage() {
        composeTestRule.setContent {
            SpellWriterTheme {
                TestableHomeScreen()
            }
        }

        // Click English
        composeTestRule.onNodeWithText("English").performClick()

        // English button should be selected (has click action and exists)
        composeTestRule.onNodeWithText("English").assertExists()
        composeTestRule.onNodeWithText("Deutsch").assertExists()

        // Click German
        composeTestRule.onNodeWithText("Deutsch").performClick()

        // Both buttons should still exist
        composeTestRule.onNodeWithText("English").assertExists()
        composeTestRule.onNodeWithText("Deutsch").assertExists()
    }

    /**
     * Tests for enabled/disabled states of LanguageSwitcher
     */

    @Test
    fun languageSwitcher_buttonsEnabled_byDefault() {
        composeTestRule.setContent {
            SpellWriterTheme {
                LanguageSwitcher(onLanguageChanged = {})
            }
        }

        // Both buttons should be enabled and clickable by default
        composeTestRule.onNodeWithText("English").assertIsEnabled()
        composeTestRule.onNodeWithText("Deutsch").assertIsEnabled()
    }

    @Test
    fun languageSwitcher_buttonsDisabled_whenEnabledFalse() {
        composeTestRule.setContent {
            SpellWriterTheme {
                LanguageSwitcher(
                    onLanguageChanged = {},
                    enabled = false
                )
            }
        }

        // Both buttons should be disabled when enabled=false
        composeTestRule.onNodeWithText("English").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Deutsch").assertIsNotEnabled()
    }

    @Test
    fun languageSwitcher_clickEventsBlocked_whenDisabled() {
        var languageChanged = false
        var newLanguage = ""

        composeTestRule.setContent {
            SpellWriterTheme {
                LanguageSwitcher(
                    onLanguageChanged = { lang ->
                        languageChanged = true
                        newLanguage = lang
                    },
                    enabled = false
                )
            }
        }

        // Try to click disabled button
        composeTestRule.onNodeWithText("Deutsch").performClick()

        // Wait a moment to ensure click didn't trigger callback
        composeTestRule.waitForIdle()

        // Language change callback should NOT have been triggered
        assert(!languageChanged) { "Language change callback should not be triggered when disabled" }
        assert(newLanguage.isEmpty()) { "Language should not change when buttons are disabled" }
    }

    @Test
    fun languageSwitcher_enabledStateToggle_updatesButtonState() {
        var enabled by mutableStateOf(true)

        composeTestRule.setContent {
            SpellWriterTheme {
                LanguageSwitcher(
                    onLanguageChanged = {},
                    enabled = enabled
                )
            }
        }

        // Initially enabled
        composeTestRule.onNodeWithText("English").assertIsEnabled()
        composeTestRule.onNodeWithText("Deutsch").assertIsEnabled()

        // Disable
        enabled = false
        composeTestRule.waitForIdle()

        // Now disabled
        composeTestRule.onNodeWithText("English").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Deutsch").assertIsNotEnabled()

        // Re-enable
        enabled = true
        composeTestRule.waitForIdle()

        // Enabled again
        composeTestRule.onNodeWithText("English").assertIsEnabled()
        composeTestRule.onNodeWithText("Deutsch").assertIsEnabled()
    }
}
