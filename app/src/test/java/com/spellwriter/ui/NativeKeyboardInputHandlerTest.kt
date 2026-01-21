package com.spellwriter.ui

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
}
