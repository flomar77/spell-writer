package com.spellwriter.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class GameConstantsTest {

    @Test
    fun wordsPerDifficultyGroup_equals5() {
        assertEquals(5, GameConstants.WORDS_PER_DIFFICULTY_GROUP)
    }

    @Test
    fun wordsPerSession_equals10() {
        assertEquals(10, GameConstants.WORDS_PER_SESSION)
    }

    @Test
    fun wordsPerSession_isDerivedFromDifficultyGroup() {
        assertEquals(
            GameConstants.WORDS_PER_DIFFICULTY_GROUP * 2,
            GameConstants.WORDS_PER_SESSION
        )
    }
}
