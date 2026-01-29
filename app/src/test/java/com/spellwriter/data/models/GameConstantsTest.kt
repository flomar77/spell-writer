package com.spellwriter.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for GameConstants.
 * Validates compile-time game configuration constants.
 */
class GameConstantsTest {

    @Test
    fun wordsPerDifficultyGroup_isCorrect() {
        assertEquals(10, GameConstants.WORDS_PER_DIFFICULTY_GROUP)
    }

    @Test
    fun wordsPerSession_isDerivedCorrectly() {
        assertEquals(20, GameConstants.WORDS_PER_SESSION)
        assertEquals(
            GameConstants.WORDS_PER_DIFFICULTY_GROUP * 2,
            GameConstants.WORDS_PER_SESSION
        )
    }

    @Test
    fun constants_maintainCorrectRelationship() {
        // WORDS_PER_SESSION should always be double WORDS_PER_DIFFICULTY_GROUP
        assertEquals(
            "WORDS_PER_SESSION should be 2x WORDS_PER_DIFFICULTY_GROUP",
            GameConstants.WORDS_PER_DIFFICULTY_GROUP * 2,
            GameConstants.WORDS_PER_SESSION
        )
    }
}
