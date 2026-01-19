package com.spellwriter.data.models

/**
 * Tracks performance metrics for a single word attempt.
 * Story 2.3: Session Completion & Tracking
 *
 * Foundation for future adaptive learning features (FR4.5).
 * Captures timing, attempt count, and success/failure status.
 *
 * AC3, AC7: Internal progress tracking and performance data foundation
 *
 * @param word The word that was attempted
 * @param attempts Number of letter attempts (both correct and incorrect)
 * @param incorrectAttempts Number of incorrect letter attempts
 * @param completionTimeMs Time taken to complete the word in milliseconds
 * @param success True if word was completed successfully, false if failed/timed out
 */
data class WordPerformance(
    val word: String,
    val attempts: Int = 0,
    val incorrectAttempts: Int = 0,
    val completionTimeMs: Long = 0L,
    val success: Boolean = false
) {
    init {
        require(attempts >= 0) { "attempts must be non-negative, got: $attempts" }
        require(incorrectAttempts >= 0) { "incorrectAttempts must be non-negative, got: $incorrectAttempts" }
        require(incorrectAttempts <= attempts) { "incorrectAttempts ($incorrectAttempts) cannot exceed total attempts ($attempts)" }
        require(completionTimeMs >= 0) { "completionTimeMs must be non-negative, got: $completionTimeMs" }
    }

    /**
     * Calculate accuracy as percentage of correct attempts.
     * Returns 100.0 if no attempts (defensive).
     */
    fun getAccuracy(): Double {
        return if (attempts == 0) 100.0
        else ((attempts - incorrectAttempts).toDouble() / attempts.toDouble()) * 100.0
    }

    /**
     * Check if this word was difficult based on multiple incorrect attempts.
     * Threshold: 3+ incorrect attempts indicates difficulty.
     */
    fun wasDifficult(): Boolean {
        return incorrectAttempts >= 3
    }
}
