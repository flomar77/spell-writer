package com.spellwriter.data.models

/**
 * Story 2.4: Celebration sequence phases for orchestrating star achievement animations.
 * Defines the sequential phases of the celebration: explosion → dragon → star pop → complete.
 *
 * AC6: Smooth animation flow with seamless transitions
 */
enum class CelebrationPhase {
    /** No celebration active */
    NONE,

    /** Stars explosion animation (500ms) - AC1 */
    EXPLOSION,

    /** Dragon fly-through animation (2000ms) - AC2, AC3 */
    DRAGON,

    /** Star pop lock-in animation (800ms) - AC4 */
    STAR_POP,

    /** GIF reward overlay (user-dismissed) - displays random cat GIF */
    GIF_REWARD,

    /** Celebration complete, ready to return to normal state - AC7 */
    COMPLETE
}
