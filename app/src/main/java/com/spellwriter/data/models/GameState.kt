package com.spellwriter.data.models

// Story 1.1: GameState is not used yet - will be implemented in Story 1.4
// This file is retained for future use
data class GameState(
    val currentWord: String = "",
    val typedLetters: String = "",
    val wordsCompleted: Int = 0,
    val wordsInPool: List<String> = emptyList(),
    val currentStar: Int = 1,
    val isWordComplete: Boolean = false,
    val lastLetterCorrect: Boolean? = null,
    val isCorrect: Boolean = false,
    val wordIndex: Int = 0
)
