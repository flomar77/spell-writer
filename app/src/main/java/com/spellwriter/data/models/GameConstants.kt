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

    // WORDS_PER_DIFFICULTY_GROUP: diff filters words by difficulty. Only works when requesting 5 or fewer words.
    const val WORDS_PER_DIFFICULTY_GROUP = 5 // DO NOT CHANGE WORDS_PER_DIFFICULTY_GROUP
    const val WORDS_PER_SESSION = WORDS_PER_DIFFICULTY_GROUP * 2

    /**
     * Controls all application state persistence.
     *
     * When true (production mode):
     * - Session persists on exit (mid-session resume)
     * - Progress persists (stars earned, world selection)
     * - Word cache persists (API words cached 30 days)
     *
     * When false (testing/demo mode):
     * - Exit clears session (no resume)
     * - Exit clears progress (stars reset to 0)
     * - Exit clears word cache (fresh API fetch)
     *
     * Default: false (testing). Set true for production.
     */
    const val PERSIST_ALL_STATE = false
}
