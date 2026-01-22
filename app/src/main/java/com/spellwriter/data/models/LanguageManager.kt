package com.spellwriter.data.models

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREF_NAME = "AppPrefs"
    private const val KEY_LANGUAGE = "language"

    fun setLocale(context: Context, languageCode: String) {
        // Save the language preference
        saveLanguage(context, languageCode)

        // Apply the locale
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getCurrentLanguage(context: Context): String {
        // First try to get from preferences
        val savedLanguage = getSavedLanguage(context)
        if (savedLanguage != null) {
            return savedLanguage
        }

        // Fallback to device locale
        return context.resources.configuration.locales[0]?.toString() ?: "en"
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    private fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }
}
