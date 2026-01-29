package com.spellwriter.data.models

/**
 * Global game configuration constants.
 *
 * WORDS_PER_DIFFICULTY_GROUP: Words in each difficulty group (shorter vs longer).
 *   - First group: shorter words (e.g., 4-letter for Star 1)
 *   - Second group: longer words (e.g., 5-letter for Star 1)
 *
 * WORDS_PER_SESSION: Total words to complete a star (derived as 2x difficulty groups).
 */
object GameConstants {
    const val WORDS_PER_DIFFICULTY_GROUP = 10
    const val WORDS_PER_SESSION = WORDS_PER_DIFFICULTY_GROUP * 2  // = 20
}
