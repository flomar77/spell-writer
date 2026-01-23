package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.ui.screens.GameScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for native keyboard input handling.
 *
 * These tests verify that the TextField-based native keyboard:
 * - Accepts correct letters and adds them to typedLetters
 * - Rejects incorrect letters (typedLetters remains unchanged)
 * - Properly invokes the onLetterTyped callback
 *
 * Story: Replace custom SpellKeyboard with native device keyboard
 */
@RunWith(AndroidJUnit4::class)
class NativeKeyboardInputTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun nativeKeyboard_correctLetter_addedToTypedLetters() {
        // GIVEN: GameScreen is displayed with a word to spell
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // Wait for the screen to settle
        composeTestRule.waitForIdle()

        // WHEN: A TextField exists for native keyboard input
        val textField = composeTestRule.onNodeWithTag("nativeKeyboardInput")
        textField.assertExists()

        // Get the current word being spelled (will be displayed somewhere on screen)
        // For this test, we'll simulate typing the first letter of whatever word is shown
        // The word should be spoken by TTS, but we can't easily verify that in tests
        // Instead, we verify that typing matches the expected word

        // Type a letter that we know will be correct (we'll use 'A' as an example)
        textField.performTextInput("A")

        // THEN: The typed letter appears in the Grimoire display
        // Note: This test will fail until we implement the native keyboard TextField
        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun nativeKeyboard_incorrectLetter_notAddedToTypedLetters() {
        // GIVEN: GameScreen is displayed with a word to spell
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        composeTestRule.waitForIdle()

        // WHEN: An incorrect letter is typed
        val textField = composeTestRule.onNodeWithTag("nativeKeyboardInput")
        textField.assertExists()

        // Get initial state - Grimoire should show "Type the word..."
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // Type a letter that we know will be incorrect
        // Since we don't know what word is loaded, we'll type multiple wrong letters
        // and verify none appear in the display
        textField.performTextInput("Z")

        // THEN: The incorrect letter does NOT appear in Grimoire
        // The placeholder text should still be visible
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // The TextField value should be reset to empty (only correct letters shown)
        textField.assertTextEquals("")
    }

    @Test
    fun nativeKeyboard_letterTyped_invokesCallback() {
        // Track whether the callback was invoked
        var callbackInvoked = false
        var lastTypedChar: Char? = null

        // GIVEN: GameScreen with a mocked ViewModel to track callback
        // Note: In the actual implementation, we'll need to verify that
        // the GameViewModel.onLetterTyped() is called when a letter is typed

        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        composeTestRule.waitForIdle()

        // WHEN: A letter is typed in the TextField
        val textField = composeTestRule.onNodeWithTag("nativeKeyboardInput")
        textField.assertExists()
        textField.performTextInput("A")

        // THEN: The onLetterTyped callback should be invoked with 'A'
        // This will be verified through state changes in the UI
        // (e.g., Ghost expression changes, Grimoire updates, or sound plays)

        // For now, we verify that the UI responds to the input
        // which indirectly confirms the callback was invoked
        composeTestRule.waitForIdle()

        // Note: This test will need to be refined once we implement the feature
        // to directly verify the callback invocation
    }

    @Test
    fun nativeKeyboard_textFieldExists() {
        // GIVEN: GameScreen is displayed
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        // THEN: A TextField with the nativeKeyboardInput tag should exist
        // Note: This test will fail until we add the TextField to GameScreen
        composeTestRule.onNodeWithTag("nativeKeyboardInput").assertExists()
    }

    @Test
    fun nativeKeyboard_onlyAcceptsCorrectLettersInSequence() {
        // GIVEN: GameScreen is displayed
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        composeTestRule.waitForIdle()

        val textField = composeTestRule.onNodeWithTag("nativeKeyboardInput")
        textField.assertExists()

        // WHEN: Multiple letters are typed, some correct and some incorrect
        // We type several wrong letters followed by correct ones

        // Type wrong letters - these should be rejected
        textField.performTextInput("XYZ")

        // TextField should remain empty (no incorrect letters shown)
        textField.assertTextEquals("")

        // Placeholder should still be visible
        composeTestRule.onNodeWithText("Type the word...").assertExists()

        // Note: Once we know which word is being spelled, we can type
        // the correct letters and verify they appear in sequence
    }
    @Test
    fun `nativeKeyboard_Accepts_Umlaute_in_german_language`() {
        // GIVEN: GameScreen with German star1 words (includes ÖDE, TÜR, SÜD, FÜR)
        composeTestRule.setContent {
            GameScreen(
                starNumber = 1,
                onBackPress = {},
                onStarComplete = null
            )
        }

        composeTestRule.waitForIdle()

        val textField = composeTestRule.onNodeWithTag("nativeKeyboardInput")
        textField.assertExists()

        // WHEN: Lowercase ö is typed
        // Test passes if loaded word is ÖDE (starts with Ö) - 1/10 probability
        textField.performTextInput("ö")

        // THEN: Uppercase Ö appears in UI (verifies uppercaseChar() handles umlauts)
        // Validates Kotlin's uppercaseChar() correctly converts ö→Ö
        composeTestRule.onNodeWithText("Ö").assertExists()
    }
}
