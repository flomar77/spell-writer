package com.spellwriter.data.tracking

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages timeout and encouragement logic for gameplay.
 * Handles idle time detection, encouragement messages, and timeout monitoring.
 */
class TimeoutManager(
    private val coroutineScope: CoroutineScope
) {
    // Timeout tracking
    private val _lastInputTime = MutableStateFlow(System.currentTimeMillis())
    val lastInputTime: StateFlow<Long> = _lastInputTime

    private val _isEncouragementShown = MutableStateFlow(false)
    val isEncouragementShown: StateFlow<Boolean> = _isEncouragementShown

    // Encouragement state
    private val _encouragementMessage = MutableStateFlow<String?>(null)
    val encouragementMessage: StateFlow<String?> = _encouragementMessage

    // Timeout job
    private var timeoutJob: Job? = null

    // Configuration
    private var encouragementTimeoutMs: Long = 8_000L
    private var timerTickMs: Long = 1_000L

    init {
        startTimeoutMonitoring()
    }

    /**
     * Set timeout configuration.
     *
     * @param encouragementTimeoutMs Time in milliseconds before showing encouragement
     * @param timerTickMs Time in milliseconds between timeout checks
     */
    fun setTimeoutConfiguration(
        encouragementTimeoutMs: Long = 8_000L,
        timerTickMs: Long = 1_000L
    ) {
        this.encouragementTimeoutMs = encouragementTimeoutMs
        this.timerTickMs = timerTickMs
    }

    /**
     * Start timeout monitoring coroutine.
     */
    private fun startTimeoutMonitoring() {
        timeoutJob = coroutineScope.launch {
            while (isActive) {
                delay(timerTickMs)
                checkTimeouts()
            }
        }
    }

    /**
     * Check for timeout conditions.
     */
    private fun checkTimeouts() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastInput = currentTime - _lastInputTime.value

        if (timeSinceLastInput >= encouragementTimeoutMs && !_isEncouragementShown.value) {
            showEncouragement()
        }
    }

    /**
     * Show encouraging message.
     */
    private fun showEncouragement() {
        coroutineScope.launch {
            _isEncouragementShown.value = true

            // Generate encouragement message
            val messages = listOf(
                "Keep going!",
                "You're doing great!",
                "Almost there!",
                "Don't give up!",
                "You can do it!"
            )
            _encouragementMessage.value = messages.random()

            delay(2000)

            _encouragementMessage.value = null
            _isEncouragementShown.value = false
        }
    }

    /**
     * Reset timeout timers.
     */
    fun resetTimeouts() {
        _lastInputTime.value = System.currentTimeMillis()
        _isEncouragementShown.value = false
    }

    /**
     * Pause timeout monitoring.
     */
    fun pauseTimeouts() {
        timeoutJob?.cancel()
        timeoutJob = null
        Log.d(TAG, "Timeout monitoring paused")
    }

    /**
     * Resume timeout monitoring.
     */
    fun resumeTimeouts() {
        if (timeoutJob == null || timeoutJob?.isCancelled == true) {
            startTimeoutMonitoring()
        }
    }

    /**
     * Clean up resources.
     */
    fun release() {
        timeoutJob?.cancel()
        Log.d(TAG, "Timeout manager released")
    }

    companion object {
        private const val TAG = "TimeoutManager"
    }
}
