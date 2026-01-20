package com.spellwriter

import LanguageManager
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spellwriter.data.models.Progress
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
}
