package com.spellwriter.data.models

import android.util.Log
import com.spellwriter.data.repository.WordsRepository
import kotlinx.coroutines.withTimeout
import java.util.Locale

/**
 * Word pool for spell-writing game.
 * Provides word lists organized by star level and language.
 *
 * Story 1.4: Core Word Gameplay
 * Story 2.2: Progressive Difficulty System
 *
 * WORD DISTRIBUTION REQUIREMENTS (per PRD FR5.1, FR5.2, FR5.3):
 * - Star 1: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 4-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 5-letter words (${GameConstants.WORDS_PER_SESSION} total)
 * - Star 2: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 5-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 6-letter words (${GameConstants.WORDS_PER_SESSION} total)
 * - Star 3: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 6-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 7-letter words (${GameConstants.WORDS_PER_SESSION} total)
 *
 * Each star level provides progressive difficulty with longer words.
 * Words are shuffled within length groups but maintain difficulty order (short→long).
 *
 * Word sources:
 * 1. Cached words from API (30-day TTL)
 * 2. Fresh API fetch (if cache miss)
 * 3. Static fallback words (if API fails)
 */
object WordPool {
    private const val TAG = "WordPool"
    private const val API_TIMEOUT_MS = 5000L

    lateinit var repository: WordsRepository
    // German word lists
    private val germanStar1 = listOf(
        // 4-letter words (10)
        "AAAA", "BBBB", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ",
        // 5-letter words (10)
        "AAAAA", "BBBBB", "CCCCC", "DDDDD", "EEEEE", "FFFFF", "GGGGG", "HHHHH", "IIIII", "JJJJJ"
    )

    private val germanStar2 = listOf(
        // 5-letter words (10)
        "KKKKK", "LLLLL", "MMMMM", "NNNNN", "OOOOO", "PPPPP", "QQQQQ", "RRRRR", "SSSSS", "TTTTT",
        // 6-letter words (10)
        "AAAAAA", "BBBBBB", "CCCCCC", "DDDDDD", "EEEEEE", "FFFFFF", "GGGGGG", "HHHHHH", "IIIIII", "JJJJJJ"
    )

    private val germanStar3 = listOf(
        // 6-letter words (10)
        "KKKKKK", "LLLLLL", "MMMMMM", "NNNNNN", "OOOOOO", "PPPPPP", "QQQQQQ", "RRRRRR", "SSSSSS", "TTTTTT",
        // 7-letter words (10)
        "AAAAAAA", "BBBBBBB", "CCCCCCC", "DDDDDDD", "EEEEEEE", "FFFFFFF", "GGGGGGG", "HHHHHHH", "IIIIIII", "JJJJJJJ"
    )

    // English word lists
    private val englishStar1 = listOf(
        // 4-letter words (10)
        "AAAA", "BBBB", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ",
        // 5-letter words (10)
        "AAAAA", "BBBBB", "CCCCC", "DDDDD", "EEEEE", "FFFFF", "GGGGG", "HHHHH", "IIIII", "JJJJJ"
    )

    private val englishStar2 = listOf(
        // 5-letter words (10)
        "KKKKK", "LLLLL", "MMMMM", "NNNNN", "OOOOO", "PPPPP", "QQQQQ", "RRRRR", "SSSSS", "TTTTT",
        // 6-letter words (10)
        "AAAAAA", "BBBBBB", "CCCCCC", "DDDDDD", "EEEEEE", "FFFFFF", "GGGGGG", "HHHHHH", "IIIIII", "JJJJJJ"
    )

    private val englishStar3 = listOf(
        // 6-letter words (10)
        "KKKKKK", "LLLLLL", "MMMMMM", "NNNNNN", "OOOOOO", "PPPPPP", "QQQQQQ", "RRRRRR", "SSSSSS", "TTTTTT",
        // 7-letter words (10)
        "AAAAAAA", "BBBBBBB", "CCCCCCC", "DDDDDDD", "EEEEEEE", "FFFFFFF", "GGGGGGG", "HHHHHHH", "IIIIIII", "JJJJJJJ"
    )

    // Story 2.2: Init-time validation ensures word pool integrity
    init {
        validateWordPool()
    }

    /**
     * Validates that all word lists conform to the required distribution.
     * Story 2.2 (AC1, AC2, AC3): Ensures data integrity at initialization.
     *
     * @throws IllegalStateException if any word list has incorrect distribution
     */
    fun validateWordPool() {
        validateWordList("German Star 1", germanStar1, 4 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 5 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
        validateWordList("German Star 2", germanStar2, 5 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 6 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
        validateWordList("German Star 3", germanStar3, 6 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 7 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
        validateWordList("English Star 1", englishStar1, 4 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 5 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
        validateWordList("English Star 2", englishStar2, 5 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 6 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
        validateWordList("English Star 3", englishStar3, 6 to GameConstants.WORDS_PER_DIFFICULTY_GROUP, 7 to GameConstants.WORDS_PER_DIFFICULTY_GROUP)
    }

    private fun validateWordList(
        name: String,
        words: List<String>,
        shortRequirement: Pair<Int, Int>,
        longRequirement: Pair<Int, Int>
    ) {
        val (shortLength, shortCount) = shortRequirement
        val (longLength, longCount) = longRequirement

        val shortWords = words.filter { it.length == shortLength }
        val longWords = words.filter { it.length == longLength }

        check(words.size == shortCount + longCount) {
            "$name: Expected ${shortCount + longCount} words, got ${words.size}"
        }
        check(shortWords.size == shortCount) {
            "$name: Expected $shortCount $shortLength-letter words, got ${shortWords.size}"
        }
        check(longWords.size == longCount) {
            "$name: Expected $longCount $longLength-letter words, got ${longWords.size}"
        }
        check(words.all { it == it.uppercase() }) {
            "$name: All words must be uppercase"
        }
    }

    /**
     * Get words for specified star level and language.
     * Story 2.1: Returns words in difficulty order (shorter words first, then longer words)
     * with shuffling within each length group for variety while maintaining progression.
     *
     * Word loading strategy:
     * 1. Try cached words from repository
     * 2. If cache miss, try API fetch (with timeout)
     * 3. If API fails, use static fallback words
     *
     * @param starNumber Star level (1, 2, or 3). Defaults to 1 if invalid.
     * @param language Language code ("de" for German, "en" for English). Defaults to device locale.
     * @return List of ${GameConstants.WORDS_PER_SESSION} words ordered by difficulty (short→long) with randomization within groups.
     */
    suspend fun getWordsForStar(starNumber: Int, language: String = Locale.getDefault().language): List<String> {
        val lang = if (language.startsWith("de")) "de" else "en"

        // Try cached words first
        // FIXME get new words anyway, even with cached words. Do not save a word multiple times. Random Words
        if (::repository.isInitialized) {
            val cachedWords = repository.getCachedWords(starNumber, lang)
            if (cachedWords != null && cachedWords.size >= GameConstants.WORDS_PER_SESSION) {
                Log.d(TAG, "Using cached words for star $starNumber ($lang)")
                return shuffleByLength(cachedWords)
            }

            // Cache miss - try API fetch with timeout
            try {
                withTimeout(API_TIMEOUT_MS) {
                    val result = repository.fetchAndCacheWords(starNumber, lang)
                    if (result.isSuccess) {
                        val words = result.getOrNull()
                        if (words != null && words.size >= GameConstants.WORDS_PER_SESSION) {
                            Log.i(TAG, "Using fresh API words for star $starNumber ($lang)")
                            return@withTimeout shuffleByLength(words)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "API fetch failed for star $starNumber ($lang): ${e.message}")
            }
        }

        // Fallback to static words
        Log.i(TAG, "Using static fallback words for star $starNumber ($lang)")
        val wordList = getStaticWords(starNumber, lang)
        return shuffleByLength(wordList)
    }

    /**
     * Get static fallback words for a star level and language.
     */
    private fun getStaticWords(starNumber: Int, lang: String): List<String> {
        return when (lang) {
            "de" -> when (starNumber) {
                1 -> germanStar1
                2 -> germanStar2
                3 -> germanStar3
                else -> germanStar1
            }
            else -> when (starNumber) {
                1 -> englishStar1
                2 -> englishStar2
                3 -> englishStar3
                else -> englishStar1
            }
        }
    }

    /**
     * Shuffle words within length groups while maintaining difficulty order.
     * Story 2.1 (AC2): Order by difficulty - shorter words first, then longer words.
     */
    private fun shuffleByLength(words: List<String>): List<String> {
        return words
            .groupBy { it.length }
            .toSortedMap()
            .flatMap { (_, wordGroup) -> wordGroup.shuffled() }
    }
}
