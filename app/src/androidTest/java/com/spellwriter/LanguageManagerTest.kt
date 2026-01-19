package com.spellwriter.ui.components

import LanguageSwitcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LanguageSwitcherTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun languageSwitcher_displaysTitle() {
        // Given
        composeTestRule.setContent {
            LanguageSwitcher()
        }

        // When & Then
        composeTestRule.onNodeWithText("Select Language").assertIsDisplayed()
    }

    @Test
    fun languageSwitcher_displaysLanguageButtons() {
        // Given
        composeTestRule.setContent {
            LanguageSwitcher()
        }

        // When & Then
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deutsch").assertIsDisplayed()
    }

    @Test
    fun languageSwitcher_clicksEnglishButton() {
        // Given
        composeTestRule.setContent {
            LanguageSwitcher()
        }

        // When
        composeTestRule.onNodeWithText("English").performClick()

        // Then
        composeTestRule.onNodeWithText("Language changed to en").assertIsDisplayed()
    }

    @Test
    fun languageSwitcher_clicksGermanButton() {
        // Given
        composeTestRule.setContent {
            LanguageSwitcher()
        }

        // When
        composeTestRule.onNodeWithText("Deutsch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deutsch").performClick()

        // Then
        composeTestRule.onNodeWithText("Language changed to de").assertIsDisplayed()
    }
}