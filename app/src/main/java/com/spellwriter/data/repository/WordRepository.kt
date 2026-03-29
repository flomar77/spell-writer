package com.spellwriter.data.repository

import android.util.Log
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.GameConstants
import com.spellwriter.data.models.WordPool
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

/**
 * Repository for language-aware word selection.
 *
 * Orchestrates word loading: cache → API → static fallback.
 * Language detection maps device locale to supported AppLanguage.
 */
object WordRepository {
    private const val TAG = "WordRepository"
    private const val API_TIMEOUT_MS = 5000L

    private var wordsRepository: WordsRepository? = null

    /**
     * Initialize with WordsRepository for API/cache access.
     */
    fun initialize(context: android.content.Context) {
        wordsRepository = WordsRepository(context)
    }

    /**
     * Detects device system language and maps to supported app languages.
     *
     * Language mapping:
     * - "de" (German) → AppLanguage.GERMAN
     * - "en" (English) → AppLanguage.ENGLISH
     * - All others → AppLanguage.ENGLISH (fallback)
     *
     * Handles locale variants appropriately:
     * - de-DE (Germany), de-AT (Austria), de-CH (Switzerland) → GERMAN
     * - en-US (USA), en-GB (UK), en-CA (Canada) → ENGLISH
     *
     * @return Detected AppLanguage (GERMAN or ENGLISH)
     */
    fun getSystemLanguage(): AppLanguage {
        val systemLocale = Locale.getDefault()
        val language = systemLocale.language

        Log.d(TAG, "System locale detected: $language (${systemLocale.displayLanguage})")

        return when (language) {
            "de" -> {
                Log.d(TAG, "Using German language mode")
                AppLanguage.GERMAN
            }

            "en" -> {
                Log.d(TAG, "Using English language mode")
                AppLanguage.ENGLISH
            }

            else -> {
                Log.d(TAG, "Unsupported language '$language', defaulting to English (FR8.9)")
                AppLanguage.ENGLISH  // FR8.9: Default fallback
            }
        }
    }

    /**
     * Get words for specific star level in specified language.
     *
     * Loading strategy:
     * 1. Try cached words from WordsRepository
     * 2. If cache miss, try API fetch (with timeout)
     * 3. If API fails, use static fallback from WordPool
     *
     * @param star Star level (1, 2, or 3)
     * @param language App language (defaults to system language)
     * @return List of words for the star level
     */
    suspend fun getWordsForStar(star: Int, language: AppLanguage = getSystemLanguage()): List<String> {
        val langCode = when (language) {
            AppLanguage.GERMAN -> "de"
            AppLanguage.ENGLISH -> "en"
        }

        Log.d(TAG, "Loading words for star $star in $language mode")

        val repo = wordsRepository
        if (repo != null) {
            // Try cache first
            val cachedWords = repo.getCachedWords(star, langCode)
            if (cachedWords != null && cachedWords.size >= GameConstants.WORDS_PER_SESSION) {
                Log.d(TAG, "Using cached words for star $star ($language)")
                return WordPool.shuffleByLength(cachedWords)
            }

            // Cache miss — try API with timeout
            try {
                val result = withTimeoutOrNull(API_TIMEOUT_MS) {
                    repo.fetchAndCacheWords(star, langCode)
                }
                val words = result?.getOrNull()
                if (words != null && words.size >= GameConstants.WORDS_PER_SESSION) {
                    Log.i(TAG, "Using fresh API words for star $star ($language)")
                    return WordPool.shuffleByLength(words)
                }
            } catch (e: Exception) {
                Log.w(TAG, "API fetch failed for star $star ($language): ${e.message}")
            }
        }

        // Fallback to static words
        Log.i(TAG, "Using static fallback words for star $star ($language)")
        return WordPool.getWordsForStar(star, langCode)
    }
}