package com.spellwriter.data.models

import org.junit.Assert.*
import org.junit.Test

class WordPoolTest {

    // (shortLen, longLen) per star
    private val starLengths = mapOf(
        1 to (3 to 4),
        2 to (4 to 5),
        3 to (5 to 6),
        4 to (6 to 7),
        5 to (7 to 8),
        6 to (8 to 9)
    )

    @Test
    fun getWordsForStar_returnsCorrectCount() {
        for (star in 1..MAX_STARS) {
            val words = WordPool.getWordsForStar(star, "de")
            assertEquals("Star $star de should return ${GameConstants.WORDS_PER_SESSION} words", GameConstants.WORDS_PER_SESSION, words.size)

            val words2 = WordPool.getWordsForStar(star, "en")
            assertEquals("Star $star en should return ${GameConstants.WORDS_PER_SESSION} words", GameConstants.WORDS_PER_SESSION, words2.size)
        }
    }

    @Test
    fun getWordsForStar_hasCorrectLengths_german() {
        for (star in 1..MAX_STARS) {
            val (short, long) = starLengths[star]!!
            val words = WordPool.getWordsForStar(star, "de")
            val shortWords = words.filter { it.length == short }
            val longWords = words.filter { it.length == long }
            assertEquals("Star $star de: expected ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} $short-letter words", GameConstants.WORDS_PER_DIFFICULTY_GROUP, shortWords.size)
            assertEquals("Star $star de: expected ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} $long-letter words", GameConstants.WORDS_PER_DIFFICULTY_GROUP, longWords.size)
        }
    }

    @Test
    fun getWordsForStar_hasCorrectLengths_english() {
        for (star in 1..MAX_STARS) {
            val (short, long) = starLengths[star]!!
            val words = WordPool.getWordsForStar(star, "en")
            val shortWords = words.filter { it.length == short }
            val longWords = words.filter { it.length == long }
            assertEquals("Star $star en: expected ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} $short-letter words", GameConstants.WORDS_PER_DIFFICULTY_GROUP, shortWords.size)
            assertEquals("Star $star en: expected ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} $long-letter words", GameConstants.WORDS_PER_DIFFICULTY_GROUP, longWords.size)
        }
    }

    @Test
    fun getWordsForStar_shortWordsBeforeLongWords_allStars() {
        for (star in 1..MAX_STARS) {
            val (short, long) = starLengths[star]!!
            repeat(5) {
                for (lang in listOf("de", "en")) {
                    val words = WordPool.getWordsForStar(star, lang)
                    val firstHalf = words.take(GameConstants.WORDS_PER_DIFFICULTY_GROUP)
                    val secondHalf = words.drop(GameConstants.WORDS_PER_DIFFICULTY_GROUP)
                    assertTrue("Star $star $lang: first group should be $short-letter", firstHalf.all { it.length == short })
                    assertTrue("Star $star $lang: second group should be $long-letter", secondHalf.all { it.length == long })
                }
            }
        }
    }

    @Test
    fun getWordsForStar_maintainsNonDecreasingLengthOrder() {
        for (star in 1..MAX_STARS) {
            for (lang in listOf("de", "en")) {
                val words = WordPool.getWordsForStar(star, lang)
                for (i in 0 until words.size - 1) {
                    assertTrue("Star $star $lang: lengths should not decrease", words[i].length <= words[i + 1].length)
                }
            }
        }
    }

    @Test
    fun getWordsForStar_starLevelsShowClearProgression() {
        for (lang in listOf("de", "en")) {
            val avgs = (1..MAX_STARS).map { star ->
                WordPool.getWordsForStar(star, lang).map { it.length }.average()
            }
            for (i in 0 until avgs.size - 1) {
                assertTrue("$lang: star ${i+2} avg length should exceed star ${i+1}", avgs[i + 1] > avgs[i])
            }
        }
    }

    @Test
    fun getWordsForStar_multipleCallsReturnDifferentOrderings() {
        val orderings = (1..10).map { WordPool.getWordsForStar(1, "de") }
        assertTrue("Multiple calls should produce different orderings", orderings.toSet().size > 1)
    }

    @Test
    fun getWordsForStar_invalidStar_defaultsToStar1() {
        val words = WordPool.getWordsForStar(99, "de")
        assertEquals(GameConstants.WORDS_PER_SESSION, words.size)
    }

    @Test
    fun getWordsForStar_allWordsAreUppercase() {
        for (star in 1..MAX_STARS) {
            for (lang in listOf("de", "en")) {
                val words = WordPool.getWordsForStar(star, lang)
                assertTrue("Star $star $lang: all words must be uppercase", words.all { it == it.uppercase() })
            }
        }
    }

    @Test
    fun validateWordPool_succeeds() {
        WordPool.validateWordPool()
    }
}
