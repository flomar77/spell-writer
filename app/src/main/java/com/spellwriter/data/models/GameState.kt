package com.spellwriter.data.models

data class GameState(
    val currentWord: String = "",
    val typedLetters: String = "",
    val wordsCompleted: Int = 0,
    val wordsInPool: List<String> = emptyList(),
    val currentStar: Int = 1,
    val isWordComplete: Boolean = false,
    val lastLetterCorrect: Boolean? = null
)

enum class GhostExpression {
    NEUTRAL,
    HAPPY,
    UNHAPPY,
    DEAD
}
