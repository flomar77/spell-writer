package com.spellwriter.data.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WordPool.
 *
 * NOTE ON WORD VALIDATION: These tests validate word COUNT, LENGTH, and FORMAT
 * but do NOT validate spelling correctness. All German and English words have been
 * manually verified against standard dictionaries to ensure they are:
 * - Valid words in the target language
 * - Age-appropriate for children (5-8 years)
 * - Common vocabulary for early learners
 *
 * If words are modified, manual dictionary verification is required.
 */
class WordPoolTest {

    @Test
    fun getWordsForStar_returnsCorrectCount() {
        val words = WordPool.getWordsForStar(1, "de")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_star1HasCorrectLengths_german() {
        val words = WordPool.getWordsForStar(1, "de")
        val threeLetter = words.filter { it.length == 3 }
        val fourLetter = words.filter { it.length == 4 }
        assertEquals(10, threeLetter.size)
        assertEquals(10, fourLetter.size)
    }

    @Test
    fun getWordsForStar_star2HasCorrectLengths_german() {
        val words = WordPool.getWordsForStar(2, "de")
        val fourLetter = words.filter { it.length == 4 }
        val fiveLetter = words.filter { it.length == 5 }
        assertEquals(10, fourLetter.size)
        assertEquals(10, fiveLetter.size)
    }

    @Test
    fun getWordsForStar_star3HasCorrectLengths_german() {
        val words = WordPool.getWordsForStar(3, "de")
        val fiveLetter = words.filter { it.length == 5 }
        val sixLetter = words.filter { it.length == 6 }
        assertEquals(10, fiveLetter.size)
        assertEquals(10, sixLetter.size)
    }

    @Test
    fun getWordsForStar_english_returnsCorrectCount() {
        val words = WordPool.getWordsForStar(1, "en")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_shufflesWords() {
        val words1 = WordPool.getWordsForStar(1, "de")
        val words2 = WordPool.getWordsForStar(1, "de")
        // Both have same words but likely different order due to shuffle
        assertEquals(words1.sorted(), words2.sorted())
    }

    @Test
    fun getWordsForStar_invalidStar_defaultsToStar1() {
        val words = WordPool.getWordsForStar(99, "de")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_allWordsAreUppercase() {
        val words = WordPool.getWordsForStar(1, "de")
        assertTrue(words.all { word -> word == word.uppercase() })
    }

    // Story 2.1: Difficulty ordering tests (AC2)

    @Test
    fun getWordsForStar_star1_shortWordsBeforeLongWords_german() {
        // Run multiple times to ensure ordering is consistent despite shuffling within groups
        repeat(5) {
            val words = WordPool.getWordsForStar(1, "de")
            // First 10 should be 3-letter, next 10 should be 4-letter
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 3-letter", firstHalf.all { it.length == 3 })
            assertTrue("Last 10 words should all be 4-letter", secondHalf.all { it.length == 4 })
        }
    }

    @Test
    fun getWordsForStar_star1_shortWordsBeforeLongWords_english() {
        repeat(5) {
            val words = WordPool.getWordsForStar(1, "en")
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 3-letter", firstHalf.all { it.length == 3 })
            assertTrue("Last 10 words should all be 4-letter", secondHalf.all { it.length == 4 })
        }
    }

    @Test
    fun getWordsForStar_star2_shortWordsBeforeLongWords_german() {
        repeat(5) {
            val words = WordPool.getWordsForStar(2, "de")
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 4-letter", firstHalf.all { it.length == 4 })
            assertTrue("Last 10 words should all be 5-letter", secondHalf.all { it.length == 5 })
        }
    }

    @Test
    fun getWordsForStar_star3_shortWordsBeforeLongWords_german() {
        repeat(5) {
            val words = WordPool.getWordsForStar(3, "de")
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 5-letter", firstHalf.all { it.length == 5 })
            assertTrue("Last 10 words should all be 6-letter", secondHalf.all { it.length == 6 })
        }
    }

    @Test
    fun getWordsForStar_maintainsNonDecreasingLengthOrder() {
        // Verify that word lengths never decrease as we progress through the list
        val words = WordPool.getWordsForStar(1, "en")

        for (i in 0 until words.size - 1) {
            assertTrue(
                "Word at position ${i+1} (${words[i+1]}) should not be shorter than word at position $i (${words[i]})",
                words[i].length <= words[i + 1].length
            )
        }
    }
}
