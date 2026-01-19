package com.spellwriter.data.models

/**
 * Complete session state for persistence and resume.
 * Story 3.1: Session Control & Exit Flow (AC5, AC6)
 *
 * Represents a user's partial session progress that can be saved when they exit
 * and restored when they return to continue their learning session.
 *
 * @param starLevel The star level (1, 2, or 3) being played
 * @param wordsCompleted Number of words successfully completed in this session (0-20)
 * @param completedWords List of words already completed in this session
 * @param remainingWords List of words still to be completed
 * @param currentWordIndex Index of current word in remainingWords list
 * @param timestamp Unix timestamp (milliseconds) when session was saved
 */
data class SavedSession(
    val starLevel: Int,
    val wordsCompleted: Int,
    val completedWords: List<String>,
    val remainingWords: List<String>,
    val currentWordIndex: Int,
    val timestamp: Long
) {
    companion object {
        /**
         * Session expiry time: 24 hours in milliseconds.
         * Sessions older than this are considered expired and won't be restored.
         * This prevents loading very stale sessions that may no longer be relevant.
         */
        const val SESSION_EXPIRY_MS = 24 * 60 * 60 * 1000L

        /**
         * Check if a saved session is still valid (not expired).
         *
         * @param session The session to validate
         * @return true if session is less than 24 hours old, false otherwise
         */
        fun isValid(session: SavedSession): Boolean {
            return System.currentTimeMillis() - session.timestamp < SESSION_EXPIRY_MS
        }
    }
}
