package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.components.SpellKeyboard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for SpellKeyboard component (Story 1.5).
 * Tests alphabetical keyboard layout, touch targets, and letter click handling.
 */
@RunWith(AndroidJUnit4::class)
class SpellKeyboardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun keyboard_displaysAllTwentySixLetters() {
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = {})
        }

        // Verify all 26 letters are present (A-Z)
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).assertExists()
        }
    }

    @Test
    fun keyboard_hasCorrectAlphabeticalLayout() {
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = {})
        }

        // Row 1: ABCDEFGHI (9 keys)
        "ABCDEFGHI".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).assertExists()
        }

        // Row 2: JKLMNOPQR (9 keys)
        "JKLMNOPQR".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).assertExists()
        }

        // Row 3: STUVWXYZ (8 keys)
        "STUVWXYZ".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).assertExists()
        }
    }

    @Test
    fun keyboard_usesUppercaseLettersOnly() {
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = {})
        }

        // Verify uppercase letters exist
        composeTestRule.onNodeWithText("A").assertExists()
        composeTestRule.onNodeWithText("Z").assertExists()

        // Verify no lowercase letters
        composeTestRule.onNodeWithText("a").assertDoesNotExist()
        composeTestRule.onNodeWithText("z").assertDoesNotExist()
    }

    @Test
    fun keyboard_keysHaveMinimumTouchTargets() {
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = {})
        }

        // Test first key (A) meets minimum 48dp size
        composeTestRule.onNodeWithText("A")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        // Test middle key (M)
        composeTestRule.onNodeWithText("M")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        // Test last key (Z)
        composeTestRule.onNodeWithText("Z")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun keyboard_letterClickTriggersCallback() {
        var clickedLetter: String? = null
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = { clickedLetter = it })
        }

        composeTestRule.onNodeWithText("A").performClick()
        assertEquals("A", clickedLetter)
    }

    @Test
    fun keyboard_multipleLetterClicksWork() {
        val clickedLetters = mutableListOf<String>()
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = { clickedLetters.add(it) })
        }

        composeTestRule.onNodeWithText("C").performClick()
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.onNodeWithText("T").performClick()

        assertEquals(listOf("C", "A", "T"), clickedLetters)
    }

    @Test
    fun keyboard_allKeysAreClickable() {
        var clickCount = 0
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = { clickCount++ })
        }

        // Click each letter once (A-Z)
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ".forEach { letter ->
            composeTestRule.onNodeWithText(letter.toString()).performClick()
        }

        assertEquals(26, clickCount)
    }

    @Test
    fun keyboard_doesNotHaveSpecialCharacters() {
        composeTestRule.setContent {
            SpellKeyboard(onLetterClick = {})
        }

        // Verify no numbers or special characters
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("!").assertDoesNotExist()
        composeTestRule.onNodeWithText(".").assertDoesNotExist()
        composeTestRule.onNodeWithText(",").assertDoesNotExist()
    }
}
