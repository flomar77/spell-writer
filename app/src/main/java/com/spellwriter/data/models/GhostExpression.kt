package com.spellwriter.data.models

/**
 * Represents the different emotional expressions the ghost character can display.
 * Story 1.5: Added HAPPY, UNHAPPY, DEAD expressions
 * Story 3.2: Added ENCOURAGING expression for 8-second timeout
 */
enum class GhostExpression {
    NEUTRAL,      // Default/starting expression
    HAPPY,        // Correct answer feedback
    UNHAPPY,      // Incorrect answer feedback
    DEAD,         // Failure animation (20-second timeout)
    ENCOURAGING   // Gentle encouragement (8-second timeout)
}
