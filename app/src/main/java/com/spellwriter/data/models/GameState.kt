package com.spellwriter.data.models

/**
 * Hint state for displaying hint letters after consecutive failures.
 * Shows a grey letter at the current position to help young learners.
 *
 * @param letter The letter to display as a hint
 * @param positionIndex The index position where the hint should appear
 */
data class HintState(
    val letter: Char,
    val positionIndex: Int
)

/**
 * Game state for spell-writing gameplay.
 * Holds all UI state managed by GameViewModel.
 * Story 1.4: Core Word Gameplay
 * Story 1.5: Removed ghostExpression (now managed separately in ViewModel with StateFlow)
 * Story 2.1: Added session tracking for 20-word learning sessions with retry logic
 *
 * @param currentWord The word the player is currently spelling
 * @param typedLetters Letters correctly typed so far for current word
 * @param wordsCompleted Number of unique words completed in current session (0-20)
 * @param sessionStars Stars earned in current session (0-3, Story 2.4 handles earning logic)
 * @param wordPool Original word pool for this session (20 words)
 * @param sessionComplete True when all 20 unique words have been completed (AC6)
 * @param remainingWords Words not yet completed (decreases as words are completed) (AC1, AC4)
 * @param failedWords Words that were failed and need retry (AC3, AC5)
 * @param completedWords List of words completed in order (for display under Grimoire)
 * @param hintState Optional hint state for displaying grey hint letters after consecutive failures
 */
data class GameState(
    val currentWord: String = "",
    val typedLetters: String = "",
    val wordsCompleted: Int = 0,
    val sessionStars: Int = 0,
    val wordPool: List<String> = emptyList(),
    val sessionComplete: Boolean = false,
    val remainingWords: List<String> = emptyList(),
    val failedWords: List<String> = emptyList(),
    val completedWords: List<String> = emptyList(),
    val hintState: HintState? = null
)
