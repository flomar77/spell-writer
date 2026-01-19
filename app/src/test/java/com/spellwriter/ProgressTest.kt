package com.spellwriter

import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.World
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Progress data model (Story 1.2).
 * Tests star progression logic, world unlocking, and star earning.
 */
class ProgressTest {

    @Test
    fun getCurrentStar_returnsCorrectStar() {
        // Test with 0 stars earned - should return star 1
        val progress1 = Progress(wizardStars = 0)
        assertEquals(1, progress1.getCurrentStar())

        // Test with 1 star earned - should return star 2
        val progress2 = Progress(wizardStars = 1)
        assertEquals(2, progress2.getCurrentStar())

        // Test with 2 stars earned - should return star 3
        val progress3 = Progress(wizardStars = 2)
        assertEquals(3, progress3.getCurrentStar())

        // Test with all 3 stars earned - should still return star 3 (max)
        val progress4 = Progress(wizardStars = 3)
        assertEquals(3, progress4.getCurrentStar())
    }

    @Test
    fun getCurrentStar_handlesMaxBoundary() {
        // Edge case: 3 stars (max valid) should return 3
        val progress = Progress(wizardStars = 3)
        assertEquals(3, progress.getCurrentStar())
    }

    @Test
    fun isStarEarned_correctlyIdentifiesEarnedStars() {
        val progress = Progress(wizardStars = 2)

        // Stars 1 and 2 should be earned
        assertTrue(progress.isStarEarned(1))
        assertTrue(progress.isStarEarned(2))

        // Star 3 should not be earned
        assertFalse(progress.isStarEarned(3))
    }

    @Test
    fun isStarEarned_handlesZeroStars() {
        val progress = Progress(wizardStars = 0)

        // No stars should be earned
        assertFalse(progress.isStarEarned(1))
        assertFalse(progress.isStarEarned(2))
        assertFalse(progress.isStarEarned(3))
    }

    @Test
    fun isStarEarned_handlesAllStarsEarned() {
        val progress = Progress(wizardStars = 3)

        // All stars should be earned
        assertTrue(progress.isStarEarned(1))
        assertTrue(progress.isStarEarned(2))
        assertTrue(progress.isStarEarned(3))
    }

    @Test
    fun isWorldUnlocked_wizardAlwaysUnlocked() {
        // Wizard world should always be unlocked regardless of stars
        val progress1 = Progress(wizardStars = 0)
        assertTrue(progress1.isWorldUnlocked(World.WIZARD))

        val progress2 = Progress(wizardStars = 3)
        assertTrue(progress2.isWorldUnlocked(World.WIZARD))
    }

    @Test
    fun isWorldUnlocked_pirateUnlocksAfterThreeStars() {
        // Pirate world should be locked with fewer than 3 stars
        val progress1 = Progress(wizardStars = 0)
        assertFalse(progress1.isWorldUnlocked(World.PIRATE))

        val progress2 = Progress(wizardStars = 1)
        assertFalse(progress2.isWorldUnlocked(World.PIRATE))

        val progress3 = Progress(wizardStars = 2)
        assertFalse(progress3.isWorldUnlocked(World.PIRATE))

        // Pirate world should be unlocked with exactly 3 stars
        val progress4 = Progress(wizardStars = 3)
        assertTrue(progress4.isWorldUnlocked(World.PIRATE))
    }

    @Test
    fun defaultValues_areCorrect() {
        // Test that default constructor values work correctly
        val progress = Progress()

        assertEquals(0, progress.wizardStars)
        assertEquals(0, progress.pirateStars)
        assertEquals(World.WIZARD, progress.currentWorld)
        assertEquals(1, progress.getCurrentStar())  // Should return 1 with 0 stars
    }

    @Test
    fun progress_withPirateWorld_worksCurrent() {
        val progress = Progress(pirateStars = 1, currentWorld = World.PIRATE)

        assertEquals(2, progress.getCurrentStar())
        assertTrue(progress.isStarEarned(1))
        assertFalse(progress.isStarEarned(2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsNegativeWizardStars() {
        Progress(wizardStars = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsTooManyWizardStars() {
        Progress(wizardStars = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsNegativePirateStars() {
        Progress(pirateStars = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun progress_rejectsTooManyPirateStars() {
        Progress(pirateStars = 5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun isStarEarned_rejectsInvalidStarNumber_zero() {
        val progress = Progress(wizardStars = 1)
        progress.isStarEarned(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun isStarEarned_rejectsInvalidStarNumber_tooHigh() {
        val progress = Progress(wizardStars = 1)
        progress.isStarEarned(4)
    }

    // Story 2.4: World completion tests (AC5)

    @Test
    fun isWorldComplete_returnsTrueWhenThreeStarsEarned() {
        val progressWizard = Progress(wizardStars = 3, currentWorld = World.WIZARD)
        assertTrue(progressWizard.isWorldComplete(World.WIZARD))

        val progressPirate = Progress(pirateStars = 3, currentWorld = World.PIRATE)
        assertTrue(progressPirate.isWorldComplete(World.PIRATE))
    }

    @Test
    fun isWorldComplete_returnsFalseWhenFewerThanThreeStars() {
        val progress0 = Progress(wizardStars = 0)
        assertFalse(progress0.isWorldComplete(World.WIZARD))

        val progress1 = Progress(wizardStars = 1)
        assertFalse(progress1.isWorldComplete(World.WIZARD))

        val progress2 = Progress(wizardStars = 2)
        assertFalse(progress2.isWorldComplete(World.WIZARD))
    }

    @Test
    fun isNextWorldReady_returnsTrueWhenCurrentWorldComplete() {
        val progress = Progress(wizardStars = 3, currentWorld = World.WIZARD)
        assertTrue(progress.isNextWorldReady())
    }

    @Test
    fun isNextWorldReady_returnsFalseWhenCurrentWorldIncomplete() {
        val progress = Progress(wizardStars = 2, currentWorld = World.WIZARD)
        assertFalse(progress.isNextWorldReady())
    }

    @Test
    fun getTotalStars_sumsStarsFromAllWorlds() {
        val progress = Progress(wizardStars = 2, pirateStars = 1)
        assertEquals(3, progress.getTotalStars())
    }

    @Test
    fun getTotalStars_returnsZeroWithNoStars() {
        val progress = Progress(wizardStars = 0, pirateStars = 0)
        assertEquals(0, progress.getTotalStars())
    }

    @Test
    fun getTotalStars_returnsMaxWithAllStars() {
        val progress = Progress(wizardStars = 3, pirateStars = 3)
        assertEquals(6, progress.getTotalStars())
    }
}
