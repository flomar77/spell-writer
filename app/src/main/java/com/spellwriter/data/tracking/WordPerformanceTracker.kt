package com.spellwriter.data.tracking

import android.util.Log
import com.spellwriter.data.models.WordPerformance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tracks performance metrics for words during gameplay.
 * Manages timing, attempts, accuracy, and completion statistics.
 */
class WordPerformanceTracker {
    // Word performance data
    private val _wordPerformanceData = mutableMapOf<String, WordPerformance>()
    val wordPerformanceData: Map<String, WordPerformance> get() = _wordPerformanceData

    // Current word tracking
    private var currentWordStartTime: Long = 0L
    private var currentWordAttempts: Int = 0
    private var currentWordIncorrectAttempts: Int = 0

    // Completed words tracking
    private val _completedWords = mutableSetOf<String>()
    val completedWords: Set<String> get() = _completedWords

    // State flows for external observation
    private val _currentWordPerformance = MutableStateFlow<WordPerformance?>(null)
    val currentWordPerformance: StateFlow<WordPerformance?> = _currentWordPerformance

    private val _wordsCompletedCount = MutableStateFlow(0)
    val wordsCompletedCount: StateFlow<Int> = _wordsCompletedCount

    /**
     * Start tracking performance for a new word.
     *
     * @param word The word to start tracking
     */
    fun startWordTracking(word: String) {
        currentWordStartTime = System.currentTimeMillis()
        currentWordAttempts = 0
        currentWordIncorrectAttempts = 0

        Log.d(TAG, "Started tracking performance for word: $word")
    }

    /**
     * Record a correct letter attempt.
     */
    fun recordCorrectAttempt() {
        currentWordAttempts++
        updateCurrentWordPerformance()
    }

    /**
     * Record an incorrect letter attempt.
     */
    fun recordIncorrectAttempt() {
        currentWordAttempts++
        currentWordIncorrectAttempts++
        updateCurrentWordPerformance()
    }

    /**
     * Update the current word performance state.
     */
    private fun updateCurrentWordPerformance() {
        val currentWord = _currentWordPerformance.value?.word ?: return
        val completionTime = System.currentTimeMillis() - currentWordStartTime

        _currentWordPerformance.value = WordPerformance(
            word = currentWord,
            attempts = currentWordAttempts,
            incorrectAttempts = currentWordIncorrectAttempts,
            completionTimeMs = completionTime,
            success = false // Will be updated when word is completed
        )
    }

    /**
     * Complete the current word and save performance data.
     *
     * @param word The word that was completed
     * @return The WordPerformance for the completed word
     */
    fun completeWord(word: String): WordPerformance {
        val completionTime = System.currentTimeMillis() - currentWordStartTime

        val performance = WordPerformance(
            word = word,
            attempts = currentWordAttempts,
            incorrectAttempts = currentWordIncorrectAttempts,
            completionTimeMs = completionTime,
            success = true
        )

        _wordPerformanceData[word] = performance
        _completedWords.add(word)
        _wordsCompletedCount.value = _completedWords.size

        Log.d(TAG, "Word completed: $word - ${performance.getAccuracy()}% accuracy, ${completionTime}ms")

        return performance
    }

    /**
     * Fail the current word and save performance data.
     *
     * @param word The word that failed
     * @return The WordPerformance for the failed word
     */
    fun failWord(word: String): WordPerformance {
        val completionTime = System.currentTimeMillis() - currentWordStartTime

        val performance = WordPerformance(
            word = word,
            attempts = currentWordAttempts,
            incorrectAttempts = currentWordIncorrectAttempts,
            completionTimeMs = completionTime,
            success = false
        )

        _wordPerformanceData[word] = performance
        Log.d(TAG, "Word failed: $word - ${performance.getAccuracy()}% accuracy, ${completionTime}ms")

        return performance
    }

    /**
     * Get performance statistics for all tracked words.
     *
     * @return Map of word to its performance statistics
     */
    fun getPerformanceStatistics(): Map<String, WordPerformance> {
        return _wordPerformanceData.toMap()
    }

    /**
     * Get average accuracy across all completed words.
     *
     * @return Average accuracy percentage, or 100.0 if no words completed
     */
    fun getAverageAccuracy(): Double {
        if (_completedWords.isEmpty()) return 100.0

        val totalAccuracy = _completedWords.sumOf { word ->
            val performance = _wordPerformanceData[word]
            performance?.getAccuracy() ?: 100.0
        }

        return totalAccuracy / _completedWords.size
    }

    /**
     * Get count of difficult words (3+ incorrect attempts).
     *
     * @return Number of difficult words
     */
    fun getDifficultWordCount(): Int {
        return _completedWords.count { word ->
            val performance = _wordPerformanceData[word]
            performance?.wasDifficult() ?: false
        }
    }

    /**
     * Reset all tracking data.
     */
    fun reset() {
        _wordPerformanceData.clear()
        _completedWords.clear()
        currentWordStartTime = 0L
        currentWordAttempts = 0
        currentWordIncorrectAttempts = 0
        _currentWordPerformance.value = null
        _wordsCompletedCount.value = 0

        Log.d(TAG, "Performance tracking reset")
    }

    companion object {
        private const val TAG = "WordPerformanceTracker"
    }
}
