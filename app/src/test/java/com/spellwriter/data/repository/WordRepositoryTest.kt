package com.spellwriter.data.repository

import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.GameConstants
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Unit tests for WordRepository language detection and word selection.
 * Story 3.3: Language Support & Switching
 */
class WordRepositoryTest {

    private var originalLocale: Locale? = null

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        originalLocale?.let { Locale.setDefault(it) }
    }

    // AC1: Automatic Language Detection

    @Test
    fun getSystemLanguage_germanLocale_returnsGerman() {
        Locale.setDefault(Locale.GERMANY)
        assertEquals(AppLanguage.GERMAN, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_germanVariants_returnsGerman() {
        Locale.setDefault(Locale("de", "AT"))
        assertEquals(AppLanguage.GERMAN, WordRepository.getSystemLanguage())

        Locale.setDefault(Locale("de", "CH"))
        assertEquals(AppLanguage.GERMAN, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_englishLocale_returnsEnglish() {
        Locale.setDefault(Locale.US)
        assertEquals(AppLanguage.ENGLISH, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_englishVariants_returnsEnglish() {
        Locale.setDefault(Locale.UK)
        assertEquals(AppLanguage.ENGLISH, WordRepository.getSystemLanguage())

        Locale.setDefault(Locale.CANADA)
        assertEquals(AppLanguage.ENGLISH, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_unsupportedLocale_defaultsToEnglish() {
        val unsupportedLocales = listOf(
            Locale.FRANCE, Locale.ITALY, Locale.JAPAN, Locale.CHINA, Locale("es", "ES")
        )
        unsupportedLocales.forEach { locale ->
            Locale.setDefault(locale)
            assertEquals("Locale ${locale.language} should default to ENGLISH",
                AppLanguage.ENGLISH, WordRepository.getSystemLanguage())
        }
    }

    // Fallback chain: wordsRepository is null → falls back to WordPool static words

    @Test
    fun getWordsForStar_withoutRepository_fallsBackToStaticWords() = runBlocking {
        // WordRepository.wordsRepository is null by default (no DI in tests)
        // Should still return valid words from WordPool static fallback
        for (star in 1..3) {
            val words = WordRepository.getWordsForStar(star, AppLanguage.GERMAN)
            assertEquals("Star $star should return ${GameConstants.WORDS_PER_SESSION} words",
                GameConstants.WORDS_PER_SESSION, words.size)
        }
    }

    @Test
    fun getWordsForStar_fallback_returnsCorrectLengthDistribution() = runBlocking {
        val expectedLengths = mapOf(1 to Pair(3, 4), 2 to Pair(4, 5), 3 to Pair(5, 6))

        for ((star, lengths) in expectedLengths) {
            for (lang in listOf(AppLanguage.GERMAN, AppLanguage.ENGLISH)) {
                val words = WordRepository.getWordsForStar(star, lang)
                val shortWords = words.filter { it.length == lengths.first }
                val longWords = words.filter { it.length == lengths.second }

                assertEquals("Star $star $lang: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} short words",
                    GameConstants.WORDS_PER_DIFFICULTY_GROUP, shortWords.size)
                assertEquals("Star $star $lang: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} long words",
                    GameConstants.WORDS_PER_DIFFICULTY_GROUP, longWords.size)
            }
        }
    }

    @Test
    fun getWordsForStar_fallback_shortWordsBeforeLongWords() = runBlocking {
        val words = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)
        for (i in 0 until words.size - 1) {
            assertTrue("Words should be in non-decreasing length order",
                words[i].length <= words[i + 1].length)
        }
    }

    @Test
    fun getWordsForStar_defaultsToSystemLanguage() = runBlocking {
        Locale.setDefault(Locale.GERMANY)

        val words = WordRepository.getWordsForStar(1)

        assertEquals(GameConstants.WORDS_PER_SESSION, words.size)
        // Star 1 German: 3 and 4 letter words
        assertTrue(words.all { it.length in 3..4 })
    }

    @Test
    fun getWordsForStar_starLevelsHaveDifferentWords() = runBlocking {
        val star1 = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)
        val star3 = WordRepository.getWordsForStar(3, AppLanguage.GERMAN)

        // Different stars have different length ranges, so no overlap
        val star1Lengths = star1.map { it.length }.toSet()
        val star3Lengths = star3.map { it.length }.toSet()
        assertTrue("Star 1 and 3 should have non-overlapping lengths",
            star1Lengths.intersect(star3Lengths).isEmpty())
    }
}
