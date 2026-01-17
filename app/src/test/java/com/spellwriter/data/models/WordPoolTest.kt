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

    // Story 2.2: Comprehensive word length validation tests (AC1, AC2, AC3)

    @Test
    fun getWordsForStar_star1HasCorrectLengths_english() {
        val words = WordPool.getWordsForStar(1, "en")
        val threeLetter = words.filter { it.length == 3 }
        val fourLetter = words.filter { it.length == 4 }
        assertEquals("Star 1 English should have exactly 10 three-letter words", 10, threeLetter.size)
        assertEquals("Star 1 English should have exactly 10 four-letter words", 10, fourLetter.size)
    }

    @Test
    fun getWordsForStar_star2HasCorrectLengths_english() {
        val words = WordPool.getWordsForStar(2, "en")
        val fourLetter = words.filter { it.length == 4 }
        val fiveLetter = words.filter { it.length == 5 }
        assertEquals("Star 2 English should have exactly 10 four-letter words", 10, fourLetter.size)
        assertEquals("Star 2 English should have exactly 10 five-letter words", 10, fiveLetter.size)
    }

    @Test
    fun getWordsForStar_star3HasCorrectLengths_english() {
        val words = WordPool.getWordsForStar(3, "en")
        val fiveLetter = words.filter { it.length == 5 }
        val sixLetter = words.filter { it.length == 6 }
        assertEquals("Star 3 English should have exactly 10 five-letter words", 10, fiveLetter.size)
        assertEquals("Star 3 English should have exactly 10 six-letter words", 10, sixLetter.size)
    }

    @Test
    fun getWordsForStar_star2ReturnsCorrectCount_german() {
        val words = WordPool.getWordsForStar(2, "de")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_star3ReturnsCorrectCount_german() {
        val words = WordPool.getWordsForStar(3, "de")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_star2ReturnsCorrectCount_english() {
        val words = WordPool.getWordsForStar(2, "en")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_star3ReturnsCorrectCount_english() {
        val words = WordPool.getWordsForStar(3, "en")
        assertEquals(20, words.size)
    }

    @Test
    fun getWordsForStar_shufflesWords() {
        val words1 = WordPool.getWordsForStar(1, "de")
        val words2 = WordPool.getWordsForStar(1, "de")
        // Both have same words but likely different order due to shuffle
        assertEquals(words1.sorted(), words2.sorted())
    }

    // Story 2.2: Word randomization tests (AC5)

    @Test
    fun getWordsForStar_multipleCallsReturnDifferentOrderings_german() {
        // Call multiple times and verify at least one ordering differs
        // (statistically near-certain with 10 items per group)
        val orderings = (1..10).map { WordPool.getWordsForStar(1, "de") }
        val uniqueOrderings = orderings.toSet()
        assertTrue(
            "Multiple calls should produce different orderings (got ${uniqueOrderings.size} unique out of 10)",
            uniqueOrderings.size > 1
        )
    }

    @Test
    fun getWordsForStar_multipleCallsReturnDifferentOrderings_english() {
        val orderings = (1..10).map { WordPool.getWordsForStar(1, "en") }
        val uniqueOrderings = orderings.toSet()
        assertTrue(
            "Multiple calls should produce different orderings (got ${uniqueOrderings.size} unique out of 10)",
            uniqueOrderings.size > 1
        )
    }

    @Test
    fun getWordsForStar_shufflingOccursWithinLengthGroups_german() {
        // Verify that shuffling happens within length groups, not across them
        // By checking that short words always come before long words
        repeat(10) {
            val words = WordPool.getWordsForStar(1, "de")
            val firstTen = words.take(10)
            val lastTen = words.drop(10)

            // All first 10 must be 3-letter (shorter group)
            assertTrue("First group should all be 3-letter words", firstTen.all { it.length == 3 })
            // All last 10 must be 4-letter (longer group)
            assertTrue("Second group should all be 4-letter words", lastTen.all { it.length == 4 })
        }
    }

    @Test
    fun getWordsForStar_shufflingOccursWithinLengthGroups_english() {
        repeat(10) {
            val words = WordPool.getWordsForStar(1, "en")
            val firstTen = words.take(10)
            val lastTen = words.drop(10)

            assertTrue("First group should all be 3-letter words", firstTen.all { it.length == 3 })
            assertTrue("Second group should all be 4-letter words", lastTen.all { it.length == 4 })
        }
    }

    @Test
    fun getWordsForStar_bothLanguagesUseSameShufflingLogic() {
        // Verify both languages follow same structure: words sorted by length with shuffle within groups
        val germanWords = WordPool.getWordsForStar(2, "de")
        val englishWords = WordPool.getWordsForStar(2, "en")

        // Both should have same structure: 10 shorter words, then 10 longer words
        val germanShorter = germanWords.take(10)
        val germanLonger = germanWords.drop(10)
        val englishShorter = englishWords.take(10)
        val englishLonger = englishWords.drop(10)

        // German Star 2: 4-letter then 5-letter
        assertTrue("German shorter should be 4-letter", germanShorter.all { it.length == 4 })
        assertTrue("German longer should be 5-letter", germanLonger.all { it.length == 5 })

        // English Star 2: 4-letter then 5-letter (same pattern)
        assertTrue("English shorter should be 4-letter", englishShorter.all { it.length == 4 })
        assertTrue("English longer should be 5-letter", englishLonger.all { it.length == 5 })
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

    // Story 2.2: Difficulty progression validation tests (AC4, AC6)

    @Test
    fun getWordsForStar_maintainsNonDecreasingLengthOrder_allStarsGerman() {
        // Verify non-decreasing length order for all German star levels
        for (star in 1..3) {
            repeat(5) {
                val words = WordPool.getWordsForStar(star, "de")
                for (i in 0 until words.size - 1) {
                    assertTrue(
                        "Star $star German: Word at position ${i+1} should not be shorter than word at position $i",
                        words[i].length <= words[i + 1].length
                    )
                }
            }
        }
    }

    @Test
    fun getWordsForStar_maintainsNonDecreasingLengthOrder_allStarsEnglish() {
        // Verify non-decreasing length order for all English star levels
        for (star in 1..3) {
            repeat(5) {
                val words = WordPool.getWordsForStar(star, "en")
                for (i in 0 until words.size - 1) {
                    assertTrue(
                        "Star $star English: Word at position ${i+1} should not be shorter than word at position $i",
                        words[i].length <= words[i + 1].length
                    )
                }
            }
        }
    }

    @Test
    fun getWordsForStar_starLevelsShowClearProgression_german() {
        // AC4: Each star level provides appropriate challenge increase
        val star1Words = WordPool.getWordsForStar(1, "de")
        val star2Words = WordPool.getWordsForStar(2, "de")
        val star3Words = WordPool.getWordsForStar(3, "de")

        // Calculate average word length for each star
        val star1Avg = star1Words.map { it.length }.average()
        val star2Avg = star2Words.map { it.length }.average()
        val star3Avg = star3Words.map { it.length }.average()

        // Star 1: 3+4 letter words, avg = 3.5
        // Star 2: 4+5 letter words, avg = 4.5
        // Star 3: 5+6 letter words, avg = 5.5
        assertTrue("Star 2 should have longer average word length than Star 1", star2Avg > star1Avg)
        assertTrue("Star 3 should have longer average word length than Star 2", star3Avg > star2Avg)
    }

    @Test
    fun getWordsForStar_starLevelsShowClearProgression_english() {
        val star1Words = WordPool.getWordsForStar(1, "en")
        val star2Words = WordPool.getWordsForStar(2, "en")
        val star3Words = WordPool.getWordsForStar(3, "en")

        val star1Avg = star1Words.map { it.length }.average()
        val star2Avg = star2Words.map { it.length }.average()
        val star3Avg = star3Words.map { it.length }.average()

        assertTrue("Star 2 should have longer average word length than Star 1", star2Avg > star1Avg)
        assertTrue("Star 3 should have longer average word length than Star 2", star3Avg > star2Avg)
    }

    @Test
    fun getWordsForStar_star2_shortWordsBeforeLongWords_english() {
        repeat(5) {
            val words = WordPool.getWordsForStar(2, "en")
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 4-letter", firstHalf.all { it.length == 4 })
            assertTrue("Last 10 words should all be 5-letter", secondHalf.all { it.length == 5 })
        }
    }

    @Test
    fun getWordsForStar_star3_shortWordsBeforeLongWords_english() {
        repeat(5) {
            val words = WordPool.getWordsForStar(3, "en")
            val firstHalf = words.take(10)
            val secondHalf = words.drop(10)

            assertTrue("First 10 words should all be 5-letter", firstHalf.all { it.length == 5 })
            assertTrue("Last 10 words should all be 6-letter", secondHalf.all { it.length == 6 })
        }
    }

    @Test
    fun getWordsForStar_shorterWordsPresentedBeforeLonger_sessionOrder() {
        // AC6: Within a session, shorter words are presented before longer words
        // Verify this behavior holds across multiple calls for each star/language combo
        val configurations = listOf(
            Triple(1, "de", Pair(3, 4)),
            Triple(1, "en", Pair(3, 4)),
            Triple(2, "de", Pair(4, 5)),
            Triple(2, "en", Pair(4, 5)),
            Triple(3, "de", Pair(5, 6)),
            Triple(3, "en", Pair(5, 6))
        )

        configurations.forEach { (star, lang, expectedLengths) ->
            repeat(3) {
                val words = WordPool.getWordsForStar(star, lang)
                val firstHalf = words.take(10)
                val secondHalf = words.drop(10)

                assertTrue(
                    "Star $star $lang: First 10 should be ${expectedLengths.first}-letter",
                    firstHalf.all { it.length == expectedLengths.first }
                )
                assertTrue(
                    "Star $star $lang: Last 10 should be ${expectedLengths.second}-letter",
                    secondHalf.all { it.length == expectedLengths.second }
                )
            }
        }
    }

    // Story 2.2: WordPool validation tests (Task 5)

    @Test
    fun validateWordPool_succeeds() {
        // Calling validateWordPool should not throw any exception
        // This also tests that the init block ran successfully
        WordPool.validateWordPool()
    }

    @Test
    fun validateWordPool_calledOnInitialization() {
        // Simply accessing WordPool triggers the init block which calls validateWordPool
        // If the data is invalid, this would throw IllegalStateException
        val words = WordPool.getWordsForStar(1, "en")
        assertEquals(20, words.size)
    }
}
