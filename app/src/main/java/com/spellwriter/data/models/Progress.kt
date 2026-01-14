package com.spellwriter.data.models

/**
 * Represents user progress through the game worlds and star levels.
 * Story 1.2: Tracks star progression for WIZARD and PIRATE worlds.
 * Future: Will be persisted to DataStore (Story 2.3).
 *
 * @param wizardStars Number of stars earned in Wizard World (0-3)
 * @param pirateStars Number of stars earned in Pirate World (0-3)
 * @param currentWorld The world the user is currently playing
 */
data class Progress(
    val wizardStars: Int = 0,
    val pirateStars: Int = 0,
    val currentWorld: World = World.WIZARD
) {
    init {
        require(wizardStars in 0..3) { "wizardStars must be between 0 and 3, got: $wizardStars" }
        require(pirateStars in 0..3) { "pirateStars must be between 0 and 3, got: $pirateStars" }
    }
    /**
     * Returns the current star level (1, 2, or 3) based on progress.
     * The "current star" is the next unearned star, capped at star 3.
     *
     * Examples:
     * - 0 stars earned → current star is 1
     * - 1 star earned → current star is 2
     * - 2 stars earned → current star is 3
     * - 3 stars earned → current star is 3 (max)
     */
    fun getCurrentStar(): Int {
        return when (currentWorld) {
            World.WIZARD -> (wizardStars + 1).coerceIn(1, 3)
            World.PIRATE -> (pirateStars + 1).coerceIn(1, 3)
        }
    }

    /**
     * Checks if a specific world is unlocked for play.
     * WIZARD world is always unlocked.
     * PIRATE world unlocks after earning 3 stars in Wizard world.
     *
     * @param world The world to check
     * @return true if the world is unlocked, false otherwise
     */
    fun isWorldUnlocked(world: World): Boolean {
        return when (world) {
            World.WIZARD -> true  // Always unlocked
            World.PIRATE -> wizardStars >= 3  // Unlocks after completing Wizard world
        }
    }

    /**
     * Checks if a specific star has been earned in the current world.
     *
     * @param star The star number to check (1, 2, or 3)
     * @return true if the star has been earned, false otherwise
     * @throws IllegalArgumentException if star is not in valid range
     */
    fun isStarEarned(star: Int): Boolean {
        require(star in 1..3) { "star must be between 1 and 3, got: $star" }
        return when (currentWorld) {
            World.WIZARD -> star <= wizardStars
            World.PIRATE -> star <= pirateStars
        }
    }
}
