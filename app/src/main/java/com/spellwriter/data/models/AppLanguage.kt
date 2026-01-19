package com.spellwriter.data.models

/**
 * Supported app languages for internationalization.
 * Story 3.3: Language Support & Switching (AC1, AC8)
 *
 * This enum provides type-safe language selection throughout the app,
 * ensuring consistent language handling for:
 * - Word list selection (German vs English word pools)
 * - TTS voice configuration (de-DE vs en-US)
 * - UI localization (res/values-de vs res/values)
 *
 * FR8.1: German language support
 * FR8.2: English language support
 * FR8.9: English as default fallback for unsupported system languages
 */
enum class AppLanguage {
    /**
     * German language (Deutsch).
     * Maps to:
     * - System locale: "de" (German)
     * - TTS locale: Locale.GERMANY (de-DE)
     * - Word pool: germanStar1, germanStar2, germanStar3
     * - String resources: res/values-de
     */
    GERMAN,

    /**
     * English language.
     * Maps to:
     * - System locale: "en" (English)
     * - TTS locale: Locale.US (en-US)
     * - Word pool: englishStar1, englishStar2, englishStar3
     * - String resources: res/values (default)
     *
     * Used as fallback for unsupported system languages (FR8.9).
     */
    ENGLISH
}
