package com.spellwriter.data.models

import java.util.Locale

/**
 * Word pool for spell-writing game.
 * Provides word lists organized by star level and language.
 * Story 1.4: Core Word Gameplay
 */
object WordPool {
    // German word lists
    private val germanStar1 = listOf(
        // 3-letter words (10)
        "OHR", "ARM", "EIS", "HAT", "ZUG", "TAG", "TON", "BAD", "NAH", "ORT",
        // 4-letter words (10)
        "BAUM", "HAUS", "BALL", "BOOT", "TANZ", "HAND", "WOLF", "BROT", "GELD", "WIND"
    )

    private val germanStar2 = listOf(
        // 4-letter words (10)
        "BEIN", "TIER", "BLAU", "GRAU", "BUCH", "KIND", "KOPF", "LAMM", "RING", "SAND",
        // 5-letter words (10)
        "APFEL", "KATZE", "BLUME", "FEUER", "STERN", "TISCH", "STUHL", "GROSS", "KLEIN", "LEBEN"
    )

    private val germanStar3 = listOf(
        // 5-letter words (10)
        "BIRNE", "LAMPE", "SONNE", "STEIN", "LIEBE", "BLATT", "FISCH", "VOGEL", "PFERD", "MUSIK",
        // 6-letter words (10)
        "ORANGE", "BANANE", "GARTEN", "KELLER", "HIMMEL", "SCHULE", "FREUND", "WINTER", "SOMMER", "HERBST"
    )

    // English word lists
    private val englishStar1 = listOf(
        // 3-letter words (10)
        "CAT", "DOG", "SUN", "HAT", "BED", "CUP", "PEN", "BAT", "NET", "POT",
        // 4-letter words (10)
        "TREE", "FISH", "BIRD", "BOOK", "DESK", "LAMP", "DOOR", "STAR", "MOON", "HAND"
    )

    private val englishStar2 = listOf(
        // 4-letter words (10)
        "BEAR", "MILK", "RAIN", "WIND", "SNOW", "LEAF", "ROCK", "SAND", "COIN", "RING",
        // 5-letter words (10)
        "APPLE", "HORSE", "HOUSE", "WATER", "BREAD", "LIGHT", "MUSIC", "CLOCK", "TABLE", "CHAIR"
    )

    private val englishStar3 = listOf(
        // 5-letter words (10)
        "SNAKE", "BEACH", "LEMON", "STONE", "GRASS", "CLOUD", "PLANT", "RIVER", "OCEAN", "MOUSE",
        // 6-letter words (10)
        "RABBIT", "GARDEN", "CHEESE", "FLOWER", "WINDOW", "BUTTER", "CIRCLE", "SQUARE", "PENCIL", "BASKET"
    )

    /**
     * Get words for specified star level and language.
     * Story 2.1: Returns words in difficulty order (shorter words first, then longer words)
     * with shuffling within each length group for variety while maintaining progression.
     *
     * @param starNumber Star level (1, 2, or 3). Defaults to 1 if invalid.
     * @param language Language code ("de" for German, "en" for English). Defaults to device locale.
     * @return List of 20 words ordered by difficulty (short→long) with randomization within groups.
     */
    fun getWordsForStar(starNumber: Int, language: String = Locale.getDefault().language): List<String> {
        val wordList = when {
            language.startsWith("de") -> when (starNumber) {
                1 -> germanStar1
                2 -> germanStar2
                3 -> germanStar3
                else -> germanStar1  // Default to star 1 for invalid star numbers
            }
            else -> when (starNumber) {  // English or other languages default to English
                1 -> englishStar1
                2 -> englishStar2
                3 -> englishStar3
                else -> englishStar1  // Default to star 1 for invalid star numbers
            }
        }
        // Story 2.1 (AC2): Order by difficulty - shorter words first, then longer words
        // Shuffle within each length group for variety while maintaining difficulty progression
        return wordList
            .groupBy { it.length }
            .toSortedMap()
            .flatMap { (_, words) -> words.shuffled() }
    }
}
