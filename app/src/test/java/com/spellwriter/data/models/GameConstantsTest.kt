package com.spellwriter.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for GameConstants.
 * Validates compile-time game configuration constants.
 */
class GameConstantsTest {

    @Test
    fun wordsPerDifficultyGroup_equals2() {
        assertEquals(2, GameConstants.WORDS_PER_DIFFICULTY_GROUP)
    }

    @Test
    fun wordsPerSession_equals4() {
        assertEquals(4, GameConstants.WORDS_PER_SESSION)
    }

    @Test
    fun wordsPerSession_isDerivedFromDifficultyGroup() {
        assertEquals(
            "WORDS_PER_SESSION should be 2x WORDS_PER_DIFFICULTY_GROUP",
            GameConstants.WORDS_PER_DIFFICULTY_GROUP * 2,
            GameConstants.WORDS_PER_SESSION
        )
    }
}
