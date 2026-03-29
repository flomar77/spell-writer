package com.spellwriter.data.models

/**
 * Static word pool for spell-writing game.
 * Provides word lists organized by star level and language.
 *
 * This is a pure data holder — no network or repository dependencies.
 * API/cache orchestration lives in WordRepository.
 *
 * WORD DISTRIBUTION REQUIREMENTS (per PRD FR5.1, FR5.2, FR5.3):
 * - Star 1: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 4-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 5-letter words (${GameConstants.WORDS_PER_SESSION} total)
 * - Star 2: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 5-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 6-letter words (${GameConstants.WORDS_PER_SESSION} total)
 * - Star 3: ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 6-letter words + ${GameConstants.WORDS_PER_DIFFICULTY_GROUP} x 7-letter words (${GameConstants.WORDS_PER_SESSION} total)
 */
object WordPool {
    // German word lists
    private val germanStar1 = listOf(
        // 4-letter words (5)
        "HAUS", "BAUM", "HUND", "BUCH", "GRAS",
        // 5-letter words (5)
        "TISCH", "STUHL", "LAMPE", "BLUME", "KATZE"
    )

    private val germanStar2 = listOf(
        // 5-letter words (5)
        "SCHAF", "PFERD", "VOGEL", "FUCHS", "REGEN",
        // 6-letter words (5)
        "SCHULE", "GARTEN", "MUTTER", "BUTTER", "KUCHEN"
    )

    private val germanStar3 = listOf(
        // 6-letter words (5)
        "KINDER", "SOMMER", "WINTER", "ABENDS", "STUNDE",
        // 7-letter words (5)
        "FENSTER", "SCHRANK", "DRUCKER", "STEMPEL", "WOHNUNG"
    )

    // English word lists
    private val englishStar1 = listOf(
        // 4-letter words (5)
        "BIRD", "FISH", "FROG", "BEAR", "WOLF",
        // 5-letter words (5)
        "CHAIR", "TABLE", "PLANT", "CLOCK", "BRUSH"
    )

    private val englishStar2 = listOf(
        // 5-letter words (5)
        "TIGER", "EAGLE", "SNAKE", "SHEEP", "HORSE",
        // 6-letter words (5)
        "SCHOOL", "GARDEN", "BUTTER", "FINGER", "WINTER"
    )

    private val englishStar3 = listOf(
        // 6-letter words (5)
        "CASTLE", "BRIDGE", "FLOWER", "SILVER", "FOREST",
        // 7-letter words (5)
        "CHICKEN", "WHISPER", "LANTERN", "BLANKET", "JOURNEY"
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
    // FIXME should pass, do we need this on init?
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

        check(words.size >= shortCount + longCount) {
            "$name: Expected ${shortCount + longCount} words, got ${words.size}"
        }
        check(shortWords.size >= shortCount) {
            "$name: Expected $shortCount $shortLength-letter words, got ${shortWords.size}"
        }
        check(longWords.size >= longCount) {
            "$name: Expected $longCount $longLength-letter words, got ${longWords.size}"
        }
        check(words.all { it == it.uppercase() }) {
            "$name: All words must be uppercase"
        }
    }

    /**
     * Get static words for specified star level and language, shuffled by length group.
     *
     * @param starNumber Star level (1, 2, or 3). Defaults to 1 if invalid.
     * @param language Language code ("de" for German, "en" for English).
     * @return List of words ordered by difficulty (short→long) with randomization within groups.
     */
    fun getWordsForStar(starNumber: Int, language: String): List<String> {
        val lang = if (language.startsWith("de")) "de" else "en"
        return shuffleByLength(getStaticWords(starNumber, lang))
    }

    /**
     * Get static words for a star level and language.
     */
    fun getStaticWords(starNumber: Int, lang: String): List<String> {
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
    fun shuffleByLength(words: List<String>): List<String> {
        return words
            .groupBy { it.length }
            .toSortedMap()
            .flatMap { (_, wordGroup) ->
                wordGroup.shuffled().take(GameConstants.WORDS_PER_DIFFICULTY_GROUP)
            }
    }
}
