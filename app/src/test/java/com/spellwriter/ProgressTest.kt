package com.spellwriter

import com.spellwriter.data.models.MAX_STARS
import com.spellwriter.data.models.Progress
import org.junit.Test
import org.junit.Assert.*

class ProgressTest {

    @Test
    fun getCurrentStar_returnsCorrectStar() {
        assertEquals(1, Progress(stars = 0).getCurrentStar())
        assertEquals(2, Progress(stars = 1).getCurrentStar())
        assertEquals(3, Progress(stars = 2).getCurrentStar())
        assertEquals(MAX_STARS, Progress(stars = MAX_STARS).getCurrentStar()) // capped at MAX_STARS
    }

    @Test
    fun isStarEarned_correctlyIdentifiesEarnedStars() {
        val progress = Progress(stars = 2)
        assertTrue(progress.isStarEarned(1))
        assertTrue(progress.isStarEarned(2))
        assertFalse(progress.isStarEarned(3))
    }

    @Test
    fun isStarEarned_handlesZeroStars() {
        val progress = Progress(stars = 0)
        assertFalse(progress.isStarEarned(1))
        assertFalse(progress.isStarEarned(2))
        assertFalse(progress.isStarEarned(3))
    }

    @Test
    fun isStarEarned_handlesAllStarsEarned() {
        val progress = Progress(stars = MAX_STARS)
        for (star in 1..MAX_STARS) {
            assertTrue(progress.isStarEarned(star))
        }
    }

    @Test
    fun earnStar_incrementsStars() {
        val progress = Progress(stars = 0).earnStar(1)
        assertEquals(1, progress.stars)
    }

    @Test(expected = IllegalArgumentException::class)
    fun earnStar_rejectsOutOfOrderStar() {
        Progress(stars = 0).earnStar(2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun earnStar_rejectsAlreadyEarnedStar() {
        Progress(stars = 1).earnStar(1)
    }

    @Test
    fun isComplete_returnsTrueWhenAllStarsEarned() {
        assertTrue(Progress(stars = MAX_STARS).isComplete())
        assertFalse(Progress(stars = MAX_STARS - 1).isComplete())
    }

    @Test
    fun defaultValues_areCorrect() {
        val progress = Progress()
        assertEquals(0, progress.stars)
        assertEquals(1, progress.getCurrentStar())
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsNegativeStars() {
        Progress(stars = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsTooManyStars() {
        Progress(stars = MAX_STARS + 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun isStarEarned_rejectsInvalidStarNumber_zero() {
        Progress(stars = 1).isStarEarned(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun isStarEarned_rejectsInvalidStarNumber_tooHigh() {
        Progress(stars = 1).isStarEarned(MAX_STARS + 1)
    }
}
