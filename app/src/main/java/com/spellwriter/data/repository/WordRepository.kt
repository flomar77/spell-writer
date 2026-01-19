package com.spellwriter.data.repository

import android.util.Log
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.WordPool
import java.util.Locale

/**
 * Repository for language-aware word and TTS locale management.
 * Story 3.3: Language Support & Switching
 *
 * Provides centralized language detection and configuration for:
 * - Automatic system language detection (AC1)
 * - Language-to-word-pool mapping (AC2, AC3)
 * - TTS locale matching (AC6)
 *
 * This repository wraps WordPool and adds language abstraction layer,
 * converting from device system language to supported AppLanguage enum.
 *
 * @see AppLanguage for supported languages
 * @see WordPool for underlying word lists
 */
object WordRepository {
    private const val TAG = "WordRepository"

    /**
     * Detects device system language and maps to supported app languages.
     * Story 3.3 (AC1): Automatic language detection on app launch
     *
     * FR8.8: App language follows device system language setting
     * FR8.9: Defaults to ENGLISH for unsupported languages
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
     * Story 3.3 (AC2, AC3): Language-aware word selection
     *
     * FR8.3: German word list (60 words across 3 stars)
     * FR8.4: English word list (60 words across 3 stars)
     *
     * Delegates to WordPool with appropriate language code conversion.
     *
     * @param star Star level (1, 2, or 3)
     * @param language App language (defaults to system language if not specified)
     * @return List of 20 words for the star level in specified language
     */
    suspend fun getWordsForStar(star: Int, language: AppLanguage = getSystemLanguage()): List<String> {
        val langCode = when (language) {
            AppLanguage.GERMAN -> "de"
            AppLanguage.ENGLISH -> "en"
        }

        Log.d(TAG, "Loading words for star $star in $language mode")

        val words = WordPool.getWordsForStar(star, langCode)

        Log.d(TAG, "Loaded ${words.size} words for star $star ($language)")

        return words
    }

    /**
     * Get TTS locale matching app language.
     * Story 3.3 (AC6): TTS Locale Matching
     *
     * FR8.6: TTS language matches app language
     *
     * Locale mapping:
     * - AppLanguage.GERMAN → Locale.GERMANY (de-DE)
     * - AppLanguage.ENGLISH → Locale.US (en-US)
     *
     * These locales ensure proper pronunciation:
     * - German TTS voice for German words
     * - American English TTS voice for English words
     *
     * @param language App language
     * @return Locale for TTS configuration
     */
    fun getTTSLocale(language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.GERMAN -> {
                Log.d(TAG, "TTS locale: German (de-DE)")
                Locale.GERMANY
            }
            AppLanguage.ENGLISH -> {
                Log.d(TAG, "TTS locale: English (en-US)")
                Locale.US
            }
        }
    }
}
