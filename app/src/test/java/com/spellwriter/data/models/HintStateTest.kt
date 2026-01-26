package com.spellwriter.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for HintState data class.
 * Tests verify proper construction, equality, and hashCode behavior.
 */
class HintStateTest {

    @Test
    fun `HintState holds letter and positionIndex`() {
        val hintState = HintState(letter = 'A', positionIndex = 0)

        assertEquals('A', hintState.letter)
        assertEquals(0, hintState.positionIndex)
    }

    @Test
    fun `HintState can be created with valid values`() {
        val hintState1 = HintState(letter = 'C', positionIndex = 2)
        val hintState2 = HintState(letter = 'Z', positionIndex = 5)

        assertEquals('C', hintState1.letter)
        assertEquals(2, hintState1.positionIndex)

        assertEquals('Z', hintState2.letter)
        assertEquals(5, hintState2.positionIndex)
    }

    @Test
    fun `equals works correctly for state comparisons`() {
        val hintState1 = HintState(letter = 'A', positionIndex = 0)
        val hintState2 = HintState(letter = 'A', positionIndex = 0)
        val hintState3 = HintState(letter = 'B', positionIndex = 0)
        val hintState4 = HintState(letter = 'A', positionIndex = 1)

        // Same values should be equal
        assertEquals(hintState1, hintState2)

        // Different letter should not be equal
        assertNotEquals(hintState1, hintState3)

        // Different position should not be equal
        assertNotEquals(hintState1, hintState4)
    }

    @Test
    fun `hashCode works correctly for state comparisons`() {
        val hintState1 = HintState(letter = 'A', positionIndex = 0)
        val hintState2 = HintState(letter = 'A', positionIndex = 0)
        val hintState3 = HintState(letter = 'B', positionIndex = 0)

        // Same values should have same hashCode
        assertEquals(hintState1.hashCode(), hintState2.hashCode())

        // Different values should have different hashCode (not guaranteed but expected)
        assertNotEquals(hintState1.hashCode(), hintState3.hashCode())
    }

    @Test
    fun `HintState can be used in collections`() {
        val hintStates = setOf(
            HintState(letter = 'A', positionIndex = 0),
            HintState(letter = 'A', positionIndex = 0), // Duplicate
            HintState(letter = 'B', positionIndex = 1)
        )

        // Set should only contain 2 unique elements
        assertEquals(2, hintStates.size)
    }
}
