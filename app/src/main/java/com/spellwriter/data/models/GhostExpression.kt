package com.spellwriter.data.models

/**
 * Represents the different emotional expressions the ghost character can display.
 * For Story 1.1, only NEUTRAL is implemented. Other expressions will be added in Story 1.5.
 */
enum class GhostExpression {
    NEUTRAL,    // Default/starting expression - used in Story 1.1
    HAPPY,      // Future: correct answer feedback
    UNHAPPY,    // Future: incorrect answer feedback
    DEAD        // Future: game over state
}
