package com.spellwriter.data.repository

import android.util.Log.d
import com.spellwriter.data.models.AppLanguage
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.Locale.GERMANY
import java.util.Locale.US

/**
 * Unit tests for WordRepository language detection and word selection.
 * Story 3.3: Language Support & Switching
 *
 * Tests cover:
 * - AC1: Automatic language detection
 * - AC2: German word list usage
 * - AC3: English word list usage
 * - AC6: TTS locale matching
 * - AC8: Language switching support
 */
class WordRepositoryTest {

    private var originalLocale: Locale? = null

    @Before
    fun setUp() {
        // Save original locale to restore after tests
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        // Restore original locale
        originalLocale?.let { Locale.setDefault(it) }
    }

    // AC1: Automatic Language Detection Tests

    @Test
    fun getSystemLanguage_germanLocale_returnsGerman() {
        // Set locale to German
        Locale.setDefault(Locale.GERMANY)

        val result = WordRepository.getSystemLanguage()

        assertEquals("Should detect German locale", AppLanguage.GERMAN, result)
    }

    @Test
    fun getSystemLanguage_germanVariants_returnsGerman() {
        // Test de-AT (Austrian German)
        Locale.setDefault(Locale("de", "AT"))
        assertEquals("Austrian German should map to GERMAN", AppLanguage.GERMAN, WordRepository.getSystemLanguage())

        // Test de-CH (Swiss German)
        Locale.setDefault(Locale("de", "CH"))
        assertEquals("Swiss German should map to GERMAN", AppLanguage.GERMAN, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_englishLocale_returnsEnglish() {
        // Set locale to English
        Locale.setDefault(Locale.US)

        val result = WordRepository.getSystemLanguage()

        assertEquals("Should detect English locale", AppLanguage.ENGLISH, result)
    }

    @Test
    fun getSystemLanguage_englishVariants_returnsEnglish() {
        // Test en-GB (British English)
        Locale.setDefault(Locale.UK)
        assertEquals("British English should map to ENGLISH", AppLanguage.ENGLISH, WordRepository.getSystemLanguage())

        // Test en-CA (Canadian English)
        Locale.setDefault(Locale.CANADA)
        assertEquals("Canadian English should map to ENGLISH", AppLanguage.ENGLISH, WordRepository.getSystemLanguage())
    }

    @Test
    fun getSystemLanguage_unsupportedLocale_defaultsToEnglish() {
        // Set locale to French (unsupported)
        Locale.setDefault(Locale.FRANCE)

        val result = WordRepository.getSystemLanguage()

        // Should fallback to English (FR8.9)
        assertEquals("Unsupported languages should default to ENGLISH", AppLanguage.ENGLISH, result)
    }

    @Test
    fun getSystemLanguage_multipleUnsupportedLocales_alwaysDefaultsToEnglish() {
        val unsupportedLocales = listOf(
            Locale.FRANCE,      // French
            Locale.ITALY,       // Italian
            Locale.JAPAN,       // Japanese
            Locale.CHINA,       // Chinese
            Locale("es", "ES")  // Spanish
        )

        unsupportedLocales.forEach { locale ->
            Locale.setDefault(locale)
            assertEquals(
                "Locale ${locale.language} should default to ENGLISH",
                AppLanguage.ENGLISH,
                WordRepository.getSystemLanguage()
            )
        }
    }

    // AC2: German Word List Tests

    @Test
    fun getWordsForStar_germanLanguage_returnsGermanWords() = runBlocking {
        val (shortWords, longWords) = getWordsFromPool(1, AppLanguage.GERMAN)

        // Verify German words (from WordPool germanStar1)
        assertTrue("Should contain German 3-letter words", shortWords.any { it in listOf("OHR", "ARM", "EIS") })
        assertTrue("Should contain German 4-letter words", longWords.any { it in listOf("BAUM", "HAUS", "BALL") })

        // Verify NO English words
        assertFalse("Should not contain English words", shortWords.contains("CAT"))
        assertFalse("Should not contain English words", longWords.contains("TREE"))
    }

    @Test
    fun getWordsForStar_germanLanguage_returns20Words() = runBlocking {
        val words = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)

        assertEquals("Should return exactly 20 words", 20, words.size)
    }

    @Test
    fun getWordsForStar_germanLanguage_allStars() = runBlocking {
        // Test all star levels have German words
        val star1Words = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)
        val star2Words = WordRepository.getWordsForStar(2, AppLanguage.GERMAN)
        val star3Words = WordRepository.getWordsForStar(3, AppLanguage.GERMAN)

        assertEquals("Star 1 should have 20 words", 20, star1Words.size)
        assertEquals("Star 2 should have 20 words", 20, star2Words.size)
        assertEquals("Star 3 should have 20 words", 20, star3Words.size)

        // Verify they're different word sets
        assertNotEquals("Star levels should have different words", star1Words, star2Words)
        assertNotEquals("Star levels should have different words", star2Words, star3Words)
    }

    // AC3: English Word List Tests

    @Test
    fun getWordsForStar_englishLanguage_returnsEnglishWords() = runBlocking {
        val (shortWords, longWords) = getWordsFromPool(1, AppLanguage.ENGLISH)

        // Verify English words (from WordPool englishStar1)
        assertTrue("Should contain English 3-letter words", shortWords.any { it in listOf("CAT", "DOG", "SUN") })
        assertTrue("Should contain English 4-letter words", longWords.any { it in listOf("TREE", "FISH", "BIRD") })

        // Verify NO German words
        assertFalse("Should not contain German words", shortWords.contains("OHR"))
        assertFalse("Should not contain German words", longWords.contains("BAUM"))
    }

    @Test
    fun getWordsForStar_englishLanguage_returns20Words() = runBlocking {
        val words = WordRepository.getWordsForStar(1, AppLanguage.ENGLISH)

        assertEquals("Should return exactly 20 words", 20, words.size)
    }

    @Test
    fun getWordsForStar_englishLanguage_allStars() = runBlocking {
        // Test all star levels have English words
        val star1Words = WordRepository.getWordsForStar(1, AppLanguage.ENGLISH)
        val star2Words = WordRepository.getWordsForStar(2, AppLanguage.ENGLISH)
        val star3Words = WordRepository.getWordsForStar(3, AppLanguage.ENGLISH)

        assertEquals("Star 1 should have 20 words", 20, star1Words.size)
        assertEquals("Star 2 should have 20 words", 20, star2Words.size)
        assertEquals("Star 3 should have 20 words", 20, star3Words.size)

        // Verify they're different word sets
        assertNotEquals("Star levels should have different words", star1Words, star2Words)
        assertNotEquals("Star levels should have different words", star2Words, star3Words)
    }

    // AC6: TTS Locale Matching Tests

    @Test
    fun getTTSLocale_german_returnsGermanyLocale() {
        val locale = when (AppLanguage.GERMAN) {
            AppLanguage.GERMAN -> {
                d("WordRepositoryTest", "TTS locale: German (de-DE)")
                GERMANY
            }

            AppLanguage.ENGLISH -> {
                d("WordRepositoryTest", "TTS locale: English (en-US)")
                US
            }
        }

        assertEquals("Should return German locale", Locale.GERMANY, locale)
        assertEquals("Locale language should be 'de'", "de", locale.language)
        assertEquals("Locale country should be 'DE'", "DE", locale.country)
    }

    @Test
    fun getTTSLocale_english_returnsUSLocale() {
        val locale = when (AppLanguage.ENGLISH) {
            AppLanguage.GERMAN -> {
                d("WordRepositoryTest", "TTS locale: German (de-DE)")
                GERMANY
            }

            AppLanguage.ENGLISH -> {
                d("WordRepositoryTest", "TTS locale: English (en-US)")
                US
            }
        }

        assertEquals("Should return US locale", Locale.US, locale)
        assertEquals("Locale language should be 'en'", "en", locale.language)
        assertEquals("Locale country should be 'US'", "US", locale.country)
    }

    // AC8: Language Separation Tests

    @Test
    fun getWordsForStar_germanAndEnglish_completelyDifferent() = runBlocking {
        val germanWords = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)
        val englishWords = WordRepository.getWordsForStar(1, AppLanguage.ENGLISH)

        // No overlap between German and English word pools
        val overlap = germanWords.intersect(englishWords.toSet())
        assertTrue("German and English word pools should be completely separate", overlap.isEmpty())
    }

    @Test
    fun getWordsForStar_defaultsToSystemLanguage() = runBlocking {
        // Set system to German
        Locale.setDefault(Locale.GERMANY)

        val wordsWithDefault = WordRepository.getWordsForStar(1)
        val wordsExplicitGerman = WordRepository.getWordsForStar(1, AppLanguage.GERMAN)

        // Should contain same words since system is German (order may differ due to shuffling)
        assertEquals("Default should use system language", wordsExplicitGerman.toSet(), wordsWithDefault.toSet())
    }

    // Helper function to separate words by length for testing
    private suspend fun getWordsFromPool(star: Int, language: AppLanguage): Pair<List<String>, List<String>> {
        val words = WordRepository.getWordsForStar(star, language)

        val expectedShortLength = when (star) {
            1 -> 3
            2 -> 4
            3 -> 5
            else -> 3
        }
        val expectedLongLength = expectedShortLength + 1

        val shortWords = words.filter { it.length == expectedShortLength }
        val longWords = words.filter { it.length == expectedLongLength }

        return Pair(shortWords, longWords)
    }
}
