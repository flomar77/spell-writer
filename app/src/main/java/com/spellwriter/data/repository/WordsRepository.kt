package com.spellwriter.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.GameConstants
import com.spellwriter.data.network.RetrofitInstance
import com.spellwriter.data.repository.WordRepository.getSystemLanguage
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class WordsRepository(private val context: Context) {
    // Extension property for DataStore instance
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "spell_writer_words"
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val api = RetrofitInstance.api

    companion object {
        private const val TAG = "WordsRepository"
        private const val CACHE_TTL_DAYS = 30L
        private const val CACHE_TTL_MILLIS = CACHE_TTL_DAYS * 24 * 60 * 60 * 1000
    }

    /**
     * Fetch words from API and cache them.
     *
     * @param lang Language code ("de" or "en")
     * @return Result with list of words or error
     */
    suspend fun fetchAndCacheNewWords(language: AppLanguage = getSystemLanguage()) {
        val langCode = when (language) {
            AppLanguage.GERMAN -> "de"
            AppLanguage.ENGLISH -> "en"
        }
        Log.d(TAG, "Loading words in $language mode")
    }

    /**
     * Fetch words from API and cache them, based on stars and language.
     *
     * @param star Star level (1, 2, or 3)
     * @param lang Language code ("de" or "en")
     * @return Result with list of words or error
     */
    suspend fun fetchAndCacheWords(star: Int, lang: String): Result<List<String>> {
        return try {
            val (shortLength, longLength) = getLengthsForStar(star)

            // Fetch two groups of words
            val shortWords = api.getWords(number = GameConstants.WORDS_PER_DIFFICULTY_GROUP, length = shortLength, lang = lang)
            val longWords = api.getWords(number = GameConstants.WORDS_PER_DIFFICULTY_GROUP, length = longLength, lang = lang)

            val allWords = (shortWords + longWords).map { it.uppercase() }.distinct()

            // Validate word count and lengths
            if (allWords.size < GameConstants.WORDS_PER_SESSION) {
                Log.w(TAG, "API returned insufficient words: ${allWords.size} for star $star")
                return Result.failure(Exception("Insufficient words returned from API"))
            }

            val validWords = allWords.take(GameConstants.WORDS_PER_SESSION)

            // Validate lengths
            val shortCount = validWords.count { it.length == shortLength }
            val longCount = validWords.count { it.length == longLength }

            if (shortCount < GameConstants.WORDS_PER_DIFFICULTY_GROUP || longCount < GameConstants.WORDS_PER_DIFFICULTY_GROUP) {
                Log.w(TAG, "Invalid word distribution: $shortCount short, $longCount long for star $star")
                return Result.failure(Exception("Invalid word length distribution"))
            }

            // Save to cache
            saveWordsToCache(star, lang, validWords.distinct())

            Log.i(TAG, "Successfully fetched and cached ${validWords.size} words for star $star ($lang)")
            Result.success(validWords)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch words for star $star ($lang): ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get cached words for a star level and language.
     *
     * @param star Star level (1, 2, or 3)
     * @param lang Language code ("de" or "en")
     * @return Cached words or null if not available or stale
     */
    suspend fun getCachedWords(star: Int, lang: String): List<String>? {
        return try {
            val prefs = context.dataStore.data.first()
            val prefsAsString = prefs.toString()
            Log.d(TAG, "Prefs in cache: $prefsAsString")
            val wordsKey = stringPreferencesKey("words_star${star}_${lang}")
            val timestampKey = longPreferencesKey("words_star${star}_${lang}_timestamp")

            val wordsJson = prefs[wordsKey] ?: return null
            val timestamp = prefs[timestampKey] ?: return null

            // Check if cache is stale
            val now = System.currentTimeMillis()
            if (now - timestamp > CACHE_TTL_MILLIS) {
                Log.d(TAG, "Cache expired for star $star ($lang)")
                return null
            }

            val words = json.decodeFromString<List<String>>(wordsJson)
            Log.d(TAG, "Retrieved ${words.size} cached words for star $star ($lang)")
            words
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read cached words for star $star ($lang): ${e.message}", e)
            null
        }
    }

    /**
     * Save words to cache with timestamp.
     */
    private suspend fun saveWordsToCache(star: Int, lang: String, words: List<String>) {
        try {
            val wordsKey = stringPreferencesKey("words_star${star}_${lang}")
            val timestampKey = longPreferencesKey("words_star${star}_${lang}_timestamp")
            val wordsJson = json.encodeToString(words)
            context.dataStore.edit { prefs ->
                prefs[wordsKey] = wordsJson
                prefs[timestampKey] = System.currentTimeMillis()
            }

            Log.d(TAG, "Saved ${words.size} words to cache for star $star ($lang)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save words to cache for star $star ($lang): ${e.message}", e)
        }
    }

    /**
     * Get word lengths for a star level.
     * FIXME This class actually doesnt need to know about Stars (single responsibility principle)
     */
    private fun getLengthsForStar(star: Int): Pair<Int, Int> {
    return when (star) {
        1 -> 4 to 5
        2 -> 5 to 6
        3 -> 6 to 7
        else -> 3 to 4
    }
    }
    private fun getAllLength() = listOf(3,4,5,6)
}
