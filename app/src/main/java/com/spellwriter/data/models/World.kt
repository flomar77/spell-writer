package com.spellwriter.data.models

/**
 * Represents the different worlds in the Spell Writer game.
 * Story 1.2: Only WIZARD world is active. PIRATE is a placeholder for future implementation.
 */
enum class World {
    WIZARD,   // Active world for MVP (Stories 1-3)
    PIRATE    // Future world (unlocks after earning 3 stars in Wizard world)
}
