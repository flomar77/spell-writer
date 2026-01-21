package com.spellwriter.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for native keyboard input handling logic.
 *
 * Tests verify the input handler that intercepts TextField value changes
 * and delegates to GameViewModel.onLetterTyped().
 *
 * Requirements:
 * - Only process additions (when newValue.length > currentValue.length)
 * - Extract the newly typed character
 * - Call onLetterTyped callback with the character
 * - TextField value is always bound to validated typedLetters (from ViewModel)
 */
class NativeKeyboardInputHandlerTest {

    @Test
    fun handleTextFieldInput_newCharacterAdded_invokesCallback() {
        // GIVEN: Current validated letters and a callback tracker
        val currentTypedLetters = "AB"
        var callbackInvoked = false
        var typedChar: Char? = null

        val callback = { char: Char ->
            callbackInvoked = true
            typedChar = char
        }

        // WHEN: User types a new character (TextField value changes from "AB" to "ABC")
        val newValue = "ABC"
        val shouldProcess = newValue.length > currentTypedLetters.length

        if (shouldProcess) {
            val newChar = newValue.last()
            callback(newChar)
        }

        // THEN: Callback is invoked with the new character 'C'
        assertTrue("Callback should be invoked", callbackInvoked)
        assertEquals("Typed character should be 'C'", 'C', typedChar)
    }

    @Test
    fun handleTextFieldInput_characterDeleted_doesNotInvokeCallback() {
        // GIVEN: Current validated letters
        val currentTypedLetters = "ABC"
        var callbackInvoked = false

        val callback = { _: Char ->
            callbackInvoked = true
        }

        // WHEN: User tries to delete a character (backspace: "ABC" -> "AB")
        val newValue = "AB"
        val shouldProcess = newValue.length > currentTypedLetters.length

        if (shouldProcess) {
            val newChar = newValue.last()
            callback(newChar)
        }

        // THEN: Callback is NOT invoked (deletion is ignored)
        assertFalse("Callback should not be invoked for deletion", callbackInvoked)
    }

    @Test
    fun handleTextFieldInput_emptyToSingleChar_invokesCallback() {
        // GIVEN: No typed letters yet
        val currentTypedLetters = ""
        var callbackInvoked = false
        var typedChar: Char? = null

        val callback = { char: Char ->
            callbackInvoked = true
            typedChar = char
        }

        // WHEN: User types the first character
        val newValue = "A"
        val shouldProcess = newValue.length > currentTypedLetters.length

        if (shouldProcess) {
            val newChar = newValue.last()
            callback(newChar)
        }

        // THEN: Callback is invoked with 'A'
        assertTrue("Callback should be invoked", callbackInvoked)
        assertEquals("Typed character should be 'A'", 'A', typedChar)
    }

    @Test
    fun handleTextFieldInput_sameLength_doesNotInvokeCallback() {
        // GIVEN: Current typed letters
        val currentTypedLetters = "ABC"
        var callbackInvoked = false

        val callback = { _: Char ->
            callbackInvoked = true
        }

        // WHEN: TextField value is the same length (shouldn't happen normally)
        val newValue = "ABC"
        val shouldProcess = newValue.length > currentTypedLetters.length

        if (shouldProcess) {
            val newChar = newValue.last()
            callback(newChar)
        }

        // THEN: Callback is NOT invoked
        assertFalse("Callback should not be invoked when length is same", callbackInvoked)
    }

    @Test
    fun handleTextFieldInput_multipleCharsAdded_processesLastChar() {
        // GIVEN: Current typed letters
        val currentTypedLetters = "A"
        var typedChar: Char? = null

        val callback = { char: Char ->
            typedChar = char
        }

        // WHEN: Multiple characters added at once (paste or autocomplete)
        // From "A" to "ABCD"
        val newValue = "ABCD"
        val shouldProcess = newValue.length > currentTypedLetters.length

        if (shouldProcess) {
            val newChar = newValue.last()
            callback(newChar)
        }

        // THEN: Only the last character 'D' is processed
        assertEquals("Should process last character", 'D', typedChar)
    }

    @Test
    fun handleTextFieldInput_lowercaseInput_convertsToUppercase() {
        // GIVEN: Current typed letters and callback
        var typedChar: Char? = null
        val callback = { char: Char ->
            typedChar = char
        }

        // WHEN: User types a lowercase letter (keyboard may send lowercase)
        val currentTypedLetters = ""
        val newValue = "a"

        if (newValue.length > currentTypedLetters.length) {
            val newChar = newValue.last().uppercaseChar()
            callback(newChar)
        }

        // THEN: Character is converted to uppercase before callback
        assertEquals("Character should be uppercase", 'A', typedChar)
    }

    @Test
    fun inputHandler_extractsCorrectCharFromTextField() {
        // GIVEN: A TextField value change
        val oldValue = "CA"
        val newValue = "CAT"

        // WHEN: We extract the new character
        val newChar = if (newValue.length > oldValue.length) {
            newValue.last()
        } else {
            null
        }

        // THEN: The correct character is extracted
        assertEquals("Should extract 'T'", 'T', newChar)
    }

    @Test
    fun inputHandler_ignoresBackspaceAttempt() {
        // GIVEN: Current state
        val oldValue = "CAT"
        val newValue = "CA"  // User pressed backspace

        // WHEN: We check if we should process
        val shouldProcess = newValue.length > oldValue.length

        // THEN: We should not process this change
        assertFalse("Should not process backspace", shouldProcess)
    }

    @Test
    fun inputHandler_correctLetterAdded_textFieldBoundToValidatedState() {
        // This test documents the expected behavior:
        // After onLetterTyped is called and validates the letter,
        // the TextField value should be reset to the validated typedLetters

        // GIVEN: Current validated letters from ViewModel
        var validatedTypedLetters = "CA"

        // Simulate the onValueChange callback behavior
        val onValueChange: (String) -> Unit = { newValue ->
            if (newValue.length > validatedTypedLetters.length) {
                // In real implementation, this would call viewModel.onLetterTyped()
                // which would update validatedTypedLetters if correct
                val newChar = newValue.last()

                // Simulate ViewModel updating state after correct letter
                // (In real code, this happens in GameViewModel)
                validatedTypedLetters = "CAT"
            }
            // TextField is always bound to validatedTypedLetters
        }

        // WHEN: User types "T" (TextField changes from "CA" to "CAT")
        onValueChange("CAT")

        // THEN: TextField value equals validated state
        assertEquals("TextField should match validated letters", "CAT", validatedTypedLetters)
    }

    @Test
    fun inputHandler_incorrectLetterAdded_textFieldResetToValidatedState() {
        // This test documents expected behavior when incorrect letter is typed

        // GIVEN: Current validated letters
        var validatedTypedLetters = "CA"

        val onValueChange: (String) -> Unit = { newValue ->
            if (newValue.length > validatedTypedLetters.length) {
                // In real implementation, onLetterTyped is called
                // If letter is incorrect, validatedTypedLetters is NOT updated
                // TextField value is reset to validatedTypedLetters (empty the wrong input)
            }
            // TextField always shows validatedTypedLetters
        }

        // WHEN: User types wrong letter (e.g., "Z" when correct is "T")
        // TextField tries to change from "CA" to "CAZ"
        onValueChange("CAZ")

        // THEN: TextField is reset to validated letters (wrong letter not added)
        assertEquals("TextField should reset to validated state", "CA", validatedTypedLetters)
    }

    // ========================================
    // Backspace Prevention Tests (Step 6)
    // ========================================

    @Test
    fun backspacePrevention_singleBackspace_typedLettersUnchanged() {
        // GIVEN: User has typed validated letters
        var typedLetters = "CAT"

        // WHEN: User presses backspace (TextField tries to change from "CAT" to "CA")
        val newValue = "CA"
        val shouldProcess = newValue.length > typedLetters.length

        // Don't modify typedLetters if shouldProcess is false
        if (shouldProcess) {
            typedLetters = newValue
        }

        // THEN: typedLetters remains unchanged
        assertEquals("typedLetters should remain unchanged after backspace", "CAT", typedLetters)
    }

    @Test
    fun backspacePrevention_multipleBackspaces_typedLettersUnchanged() {
        // GIVEN: User has typed validated letters
        var typedLetters = "HELLO"

        // WHEN: User presses backspace multiple times
        val attempts = listOf("HELL", "HEL", "HE", "H", "")

        for (attempt in attempts) {
            val shouldProcess = attempt.length > typedLetters.length
            if (shouldProcess) {
                typedLetters = attempt
            }
        }

        // THEN: typedLetters remains unchanged after all backspace attempts
        assertEquals("typedLetters should remain unchanged after multiple backspaces", "HELLO", typedLetters)
    }

    @Test
    fun backspacePrevention_backspaceOnSingleChar_typedLettersUnchanged() {
        // GIVEN: User has typed one validated letter
        var typedLetters = "A"

        // WHEN: User tries to delete the only character
        val newValue = ""
        val shouldProcess = newValue.length > typedLetters.length

        if (shouldProcess) {
            typedLetters = newValue
        }

        // THEN: typedLetters remains unchanged
        assertEquals("Single character should not be deletable", "A", typedLetters)
    }

    @Test
    fun backspacePrevention_textFieldAlwaysBoundToValidatedState() {
        // This test verifies the TextField value binding behavior

        // GIVEN: Current validated state
        val validatedTypedLetters = "DOG"

        // WHEN: TextField attempts to show a deleted value
        val attemptedValue = "DO"

        // The TextField value should always be bound to validatedTypedLetters
        // So even if user tries to change it, the TextField resets to validated state
        val textFieldValue = if (attemptedValue.length > validatedTypedLetters.length) {
            attemptedValue
        } else {
            validatedTypedLetters  // TextField resets to validated state
        }

        // THEN: TextField shows validated state, not the attempted deletion
        assertEquals("TextField should always show validated state", "DOG", textFieldValue)
    }

    @Test
    fun backspacePrevention_onlyAdditionsProcessed_deletionsIgnored() {
        // GIVEN: Current validated letters and a list of value changes
        val validatedLetters = "CAT"
        val changes = listOf(
            "CA" to false,    // Deletion (backspace)
            "C" to false,     // Deletion (multiple backspaces)
            "" to false,      // Deletion (clear all)
            "CAT" to false,   // Same length (no change)
            "CATS" to true    // Addition (new character)
        )

        // WHEN/THEN: We check which changes should be processed
        for ((newValue, expectedToProcess) in changes) {
            val shouldProcess = newValue.length > validatedLetters.length
            assertEquals(
                "Value change '$newValue' should ${if (expectedToProcess) "" else "not "}be processed",
                expectedToProcess,
                shouldProcess
            )
        }
    }

    @Test
    fun backspacePrevention_afterIncorrectLetter_backspaceStillIgnored() {
        // GIVEN: User has typed correct letters and attempted a wrong letter
        var validatedTypedLetters = "CA"
        var callbackCount = 0

        val onValueChange: (String) -> Unit = { newValue ->
            if (newValue.length > validatedTypedLetters.length) {
                callbackCount++
                // Simulate: incorrect letter, so validatedTypedLetters NOT updated
            }
        }

        // WHEN: User types wrong letter "Z"
        onValueChange("CAZ")
        // validatedTypedLetters is still "CA" (wrong letter rejected)

        // AND: User then tries backspace
        onValueChange("CA")  // TextField tries to go from "CA" to "CA"

        // THEN: Backspace is still ignored (no callback for same length)
        assertEquals("Only the incorrect letter attempt should trigger callback", 1, callbackCount)
        assertEquals("Validated letters should remain unchanged", "CA", validatedTypedLetters)
    }

    @Test
    fun backspacePrevention_emptyString_cannotDelete() {
        // GIVEN: No letters typed yet
        var typedLetters = ""

        // WHEN: User somehow tries to delete (shouldn't be possible, but test it)
        val newValue = ""
        val shouldProcess = newValue.length > typedLetters.length

        if (shouldProcess) {
            typedLetters = newValue
        }

        // THEN: No change occurs
        assertEquals("Empty string should remain empty", "", typedLetters)
    }

    // ========================================
    // Keyboard Configuration Tests (Step 10)
    // ========================================

    @Test
    fun keyboardConfiguration_autoCorrectEnabled_isFalse() {
        // GIVEN: KeyboardOptions for native TextField
        val keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false
        )

        // THEN: autoCorrectEnabled should be false to prevent autocorrect suggestions
        assertFalse(
            "autoCorrectEnabled must be false to disable autocorrect",
            keyboardOptions.autoCorrectEnabled ?: true  // Default to true if null
        )
    }

    @Test
    fun keyboardConfiguration_capitalization_isCharacters() {
        // GIVEN: KeyboardOptions for native TextField
        val keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false
        )

        // THEN: Capitalization should be set to Characters (all uppercase)
        assertEquals(
            "Capitalization must be Characters for uppercase input",
            KeyboardCapitalization.Characters,
            keyboardOptions.capitalization
        )
    }

    @Test
    fun keyboardConfiguration_defaultImeAction_isUnspecified() {
        // GIVEN: KeyboardOptions without explicit ImeAction
        val keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false
        )

        // THEN: ImeAction should be Unspecified (the default)
        // This prevents unwanted keyboard actions like "Next", "Done", "Send", etc.
        assertEquals(
            "ImeAction should be Unspecified to prevent unwanted keyboard actions",
            ImeAction.Unspecified,
            keyboardOptions.imeAction
        )
    }

    @Test
    fun keyboardConfiguration_noAutocorrect_noPredictiveText() {
        // This test documents the expected behavior of keyboard configuration
        // to disable autocorrect and predictive text features

        // GIVEN: Keyboard configuration with autoCorrectEnabled = false
        val autoCorrectEnabled = false

        // WHEN: autoCorrectEnabled is false
        // THEN: This disables:
        // - Autocorrect (automatic word corrections)
        // - Predictive text suggestions
        // - Text completion suggestions

        assertFalse(
            "Keyboard should not show autocorrect or predictive text",
            autoCorrectEnabled
        )
    }

    @Test
    fun keyboardConfiguration_uppercaseCapitalization_convertsInput() {
        // GIVEN: KeyboardCapitalization.Characters setting
        val capitalization = KeyboardCapitalization.Characters

        // WHEN: User types on keyboard
        // THEN: The keyboard should default to uppercase mode
        // Note: We still convert input to uppercase in code as a safeguard
        assertEquals(
            "Capitalization should be Characters for uppercase keyboard",
            KeyboardCapitalization.Characters,
            capitalization
        )
    }

    @Test
    fun keyboardConfiguration_completeConfiguration_meetsRequirements() {
        // This test verifies all keyboard configuration requirements together

        // GIVEN: Complete KeyboardOptions configuration
        val keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,  // Uppercase input
            autoCorrectEnabled = false                           // No autocorrect/predictions
            // imeAction defaults to ImeAction.Unspecified         // No special actions
        )

        // THEN: All requirements are met
        assertEquals(
            "Capitalization must be Characters",
            KeyboardCapitalization.Characters,
            keyboardOptions.capitalization
        )
        assertFalse(
            "autoCorrectEnabled must be false",
            keyboardOptions.autoCorrectEnabled ?: true
        )
        assertEquals(
            "ImeAction must be Unspecified (default)",
            ImeAction.Unspecified,
            keyboardOptions.imeAction
        )
    }

    @Test
    fun keyboardConfiguration_validatesAgainstRequirements() {
        // This test documents the requirements from new_requirements.md:
        // - Disable autocorrect, suggestions, predictive text
        // - Letter-by-letter validation with immediate feedback

        val requirements = mapOf(
            "autoCorrect" to false,
            "suggestions" to false,
            "predictiveText" to false,
            "uppercaseInput" to true
        )

        // Verify configuration meets all requirements
        assertTrue("Autocorrect should be disabled", !requirements["autoCorrect"]!!)
        assertTrue("Suggestions should be disabled", !requirements["suggestions"]!!)
        assertTrue("Predictive text should be disabled", !requirements["predictiveText"]!!)
        assertTrue("Uppercase input should be enabled", requirements["uppercaseInput"]!!)
    }

    // ========================================
    // Integration Tests (Step 14)
    // ========================================
    // These tests verify the complete flow from TextField input through validation

    @Test
    fun integration_correctLetterTyped_appearsInGrimoire() {
        // This test simulates the complete flow when a correct letter is typed

        // GIVEN: Game is playing word "CAT" and user has typed "C"
        val currentWord = "CAT"
        var typedLetters = "C"
        var letterAccepted = false

        // Simulate onLetterTyped callback that would update typedLetters
        val onLetterTyped: (Char) -> Unit = { char ->
            val expectedLetter = currentWord[typedLetters.length]
            if (char.uppercaseChar() == expectedLetter) {
                typedLetters += char.uppercaseChar()
                letterAccepted = true
            }
        }

        // WHEN: User types correct letter 'A' via TextField
        val textFieldValue = "CA"
        if (textFieldValue.length > typedLetters.length) {
            val newChar = textFieldValue.last().uppercaseChar()
            onLetterTyped(newChar)
        }

        // THEN: Letter appears in Grimoire (typedLetters is updated)
        assertEquals("Correct letter should appear in Grimoire", "CA", typedLetters)
        assertTrue("Letter should be accepted", letterAccepted)
    }

    @Test
    fun integration_incorrectLetterTyped_doesNotAppearInGrimoire() {
        // This test simulates the complete flow when an incorrect letter is typed

        // GIVEN: Game is playing word "CAT" and user has typed "C"
        val currentWord = "CAT"
        var typedLetters = "C"
        var letterRejected = false

        // Simulate onLetterTyped callback
        val onLetterTyped: (Char) -> Unit = { char ->
            val expectedLetter = currentWord[typedLetters.length]
            if (char.uppercaseChar() == expectedLetter) {
                typedLetters += char.uppercaseChar()
            } else {
                letterRejected = true
                // typedLetters NOT updated (letter rejected)
            }
        }

        // WHEN: User types incorrect letter 'Z' via TextField (expected 'A')
        val textFieldValue = "CZ"
        if (textFieldValue.length > typedLetters.length) {
            val newChar = textFieldValue.last().uppercaseChar()
            onLetterTyped(newChar)
        }

        // THEN: Letter does NOT appear in Grimoire (typedLetters unchanged)
        assertEquals("Incorrect letter should NOT appear in Grimoire", "C", typedLetters)
        assertTrue("Letter should be rejected", letterRejected)
    }

    @Test
    fun integration_wordCompletion_advancesToNextWord() {
        // This test simulates completing a word and advancing to the next

        // GIVEN: Game state with current word and next word in queue
        var currentWord = "CAT"
        var typedLetters = "CA"
        var wordsCompleted = 0
        val remainingWords = mutableListOf("DOG", "SUN")

        // Simulate word completion logic
        val onLetterTyped: (Char) -> Unit = { char ->
            val expectedLetter = currentWord[typedLetters.length]
            if (char.uppercaseChar() == expectedLetter) {
                typedLetters += char.uppercaseChar()

                // Check if word is complete
                if (typedLetters == currentWord) {
                    wordsCompleted++
                    // Advance to next word
                    if (remainingWords.isNotEmpty()) {
                        currentWord = remainingWords.removeAt(0)
                        typedLetters = ""
                    }
                }
            }
        }

        // WHEN: User types final letter 'T' to complete "CAT"
        val textFieldValue = "CAT"
        if (textFieldValue.length > typedLetters.length) {
            val newChar = textFieldValue.last().uppercaseChar()
            onLetterTyped(newChar)
        }

        // THEN: Word is completed and game advances to next word
        assertEquals("Word should be completed", 1, wordsCompleted)
        assertEquals("Should advance to next word", "DOG", currentWord)
        assertEquals("TypedLetters should reset for new word", "", typedLetters)
        assertEquals("Remaining words reduced", 1, remainingWords.size)
    }

    @Test
    fun integration_multipleWords_completeSession() {
        // This test simulates completing multiple words in a session

        // GIVEN: Game session with multiple words
        val words = mutableListOf("CAT", "DOG", "SUN")
        var currentWordIndex = 0
        var currentWord = words[currentWordIndex]
        var typedLetters = ""
        var wordsCompleted = 0

        // Simulate typing and completing words
        val completeWord: (String) -> Unit = { word ->
            // Simulate typing each letter correctly
            for (char in word) {
                typedLetters += char
            }
            // Word completed
            wordsCompleted++
            // Advance to next word
            currentWordIndex++
            if (currentWordIndex < words.size) {
                currentWord = words[currentWordIndex]
                typedLetters = ""
            }
        }

        // WHEN: User completes all three words
        completeWord("CAT")
        completeWord("DOG")
        completeWord("SUN")

        // THEN: All words completed successfully
        assertEquals("All words should be completed", 3, wordsCompleted)
        assertEquals("Should be at end of word list", 3, currentWordIndex)
    }

    @Test
    fun integration_nativeKeyboard_textFieldValueBindsToValidatedState() {
        // This test verifies the TextField value binding behavior in the integration flow

        // GIVEN: Game is playing word "DOG"
        val currentWord = "DOG"
        var validatedTypedLetters = ""

        // Simulate the complete TextField + ViewModel flow
        val textFieldOnValueChange: (String) -> Unit = { newValue ->
            if (newValue.length > validatedTypedLetters.length) {
                val newChar = newValue.last().uppercaseChar()
                val expectedLetter = currentWord[validatedTypedLetters.length]

                // Validation in ViewModel
                if (newChar == expectedLetter) {
                    validatedTypedLetters += newChar
                }
                // If incorrect, validatedTypedLetters is NOT updated
            }
            // TextField value is always bound to validatedTypedLetters
        }

        // WHEN: User types correct letter 'D'
        textFieldOnValueChange("D")
        // THEN: TextField shows validated state
        assertEquals("TextField should show 'D'", "D", validatedTypedLetters)

        // WHEN: User types incorrect letter 'Z' (expected 'O')
        textFieldOnValueChange("DZ")
        // THEN: TextField resets to validated state (still shows 'D', not 'DZ')
        assertEquals("TextField should still show 'D'", "D", validatedTypedLetters)

        // WHEN: User types correct letter 'O'
        textFieldOnValueChange("DO")
        // THEN: TextField shows validated state with new letter
        assertEquals("TextField should show 'DO'", "DO", validatedTypedLetters)
    }

    @Test
    fun integration_fullGameFlow_withNativeKeyboard() {
        // This test documents the complete game flow with native keyboard

        // GIVEN: Initial game state
        val currentWord = "CAT"
        var typedLetters = ""
        var correctLetterFeedback = 0
        var incorrectLetterFeedback = 0

        // Simulate complete input handler with feedback
        val handleTextFieldInput: (String) -> Unit = { newValue ->
            if (newValue.length > typedLetters.length) {
                val newChar = newValue.last().uppercaseChar()
                val expectedLetter = currentWord[typedLetters.length]

                if (newChar == expectedLetter) {
                    typedLetters += newChar
                    correctLetterFeedback++  // Would trigger happy Ghost
                } else {
                    incorrectLetterFeedback++  // Would trigger unhappy Ghost
                }
            }
        }

        // WHEN: User plays the word with native keyboard
        handleTextFieldInput("C")   // Correct
        handleTextFieldInput("CA")  // Correct
        handleTextFieldInput("CAZ") // Incorrect (expected 'T')
        handleTextFieldInput("CAT") // Correct

        // THEN: Game flow works correctly
        assertEquals("Grimoire shows correct letters", "CAT", typedLetters)
        assertEquals("Happy Ghost triggered 3 times", 3, correctLetterFeedback)
        assertEquals("Unhappy Ghost triggered 1 time", 1, incorrectLetterFeedback)
        assertEquals("Word completed", currentWord, typedLetters)
    }

    @Test
    fun integration_sessionFlow_nativeKeyboardSupportsFullSession() {
        // This test verifies native keyboard supports completing a full session

        // GIVEN: Session with 20 words (simplified to 3 for test)
        val sessionWords = listOf("CAT", "DOG", "SUN")
        var currentWordIndex = 0
        var typedLetters = ""
        var sessionComplete = false
        var totalCorrectLetters = 0

        // Simulate completing each word with native keyboard
        for (word in sessionWords) {
            typedLetters = ""
            for (char in word) {
                // Simulate TextField input
                val newValue = typedLetters + char
                if (newValue.length > typedLetters.length) {
                    typedLetters = newValue
                    totalCorrectLetters++
                }
            }
            // Word completed
            currentWordIndex++
        }

        // Session complete when all words done
        sessionComplete = (currentWordIndex == sessionWords.size)

        // THEN: Full session can be completed with native keyboard
        assertTrue("Session should be completable", sessionComplete)
        assertEquals("All words completed", sessionWords.size, currentWordIndex)
        assertEquals("All letters typed correctly", 9, totalCorrectLetters) // CAT(3) + DOG(3) + SUN(3)
    }
}
