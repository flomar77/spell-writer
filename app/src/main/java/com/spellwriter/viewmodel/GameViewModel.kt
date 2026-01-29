package com.spellwriter.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellwriter.audio.AudioManager
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.SavedSession
import com.spellwriter.data.models.SessionState
import com.spellwriter.data.tracking.TimeoutManager
import com.spellwriter.data.tracking.WordPerformanceTracker
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.data.repository.SessionRepository
import com.spellwriter.data.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for game screen gameplay logic.
 * Manages game state, TTS, sound effects, and word progression.
 * Story 1.4: Core Word Gameplay
 * Story 1.5: Ghost expression management with auto-reset and TTS speaking state
 * Story 2.1: 20-word learning sessions with retry logic and session completion
 * Story 2.3: Progress persistence and word performance tracking
 * Story 3.1: Session control and exit flow with session persistence
 * Hint Feature: Grey hint letters after 5 consecutive failures to help young learners
 * AudioManager Injection: Accepts optional AudioManager for TTS initialization at MainActivity level
 *
 * **Hint Letter Feature:**
 * - Tracks consecutive failures at current position (consecutiveFailuresAtCurrentPosition)
 * - Shows grey hint letter after exactly 5 consecutive incorrect attempts
 * - Hint displays for 2 seconds then auto-clears
 * - Counter resets on correct letter or after hint shown
 * - Hint clears immediately on word completion/failure
 * - Bounds checking prevents crashes at word boundaries
 *
 * @param context Application context for TTS and SoundManager
 * @param starNumber Star level (1, 2, or 3) determining word difficulty
 * @param isReplaySession If true, don't update progress (Story 1.2)
 * @param progressRepository Repository for persisting progress (Story 2.3)
 * @param sessionRepository Repository for persisting session state (Story 3.1)
 * @param initialProgress Initial progress state (Story 2.3)
 * @param audioManager Optional AudioManager instance (null if game runs without audio)
 */
class GameViewModel(
    private val context: Context,
    private val starNumber: Int = 1,
    private val isReplaySession: Boolean = false,
    private val progressRepository: ProgressRepository? = null,
    private val sessionRepository: SessionRepository? = null,
    private val initialProgress: Progress = Progress(),
    private val audioManager: AudioManager? = null
) : ViewModel() {

    // Game state exposed to UI
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Ghost expression state management
    private val _ghostExpression = MutableStateFlow(GhostExpression.NEUTRAL)
    val ghostExpression: StateFlow<GhostExpression> = _ghostExpression.asStateFlow()

    // TTS speaking state for animation synchronization
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Job for expression auto-reset
    private var expressionResetJob: Job? = null

    // Language state management
    private val _currentLanguage = MutableStateFlow(WordRepository.getSystemLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Timeout tracking for encouragement and failure animations
    private val timeoutManager = TimeoutManager(viewModelScope)

    // Encouragement message exposed to UI
    val encouragementMessage: StateFlow<String?> = timeoutManager.encouragementMessage

    // Celebration state management
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _celebrationStarLevel = MutableStateFlow(0)
    val celebrationStarLevel: StateFlow<Int> = _celebrationStarLevel.asStateFlow()

    // Navigation state for auto-progression
    private val _shouldNavigateHome = MutableStateFlow(false)
    val shouldNavigateHome: StateFlow<Boolean> = _shouldNavigateHome.asStateFlow()

    // Exit dialog and session state management
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.ACTIVE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // Word performance tracking
    private val wordPerformanceTracker = WordPerformanceTracker()

    /**
     * Tracks consecutive incorrect letter attempts at the current position.
     * When this counter reaches 5, a grey hint letter is displayed to help
     * the young learner. Counter resets to 0 when:
     * - Correct letter is typed
     * - Hint is shown
     * - Word completes or fails
     */
    private var consecutiveFailuresAtCurrentPosition = 0

    /**
     * Flag to prevent duplicate playback when user manually clicks play/replay.
     * Set to true on manual playback, auto-resets after 200ms.
     */
    private var skipNextAutoPlay = false

    // Expose TTS ready state to UI for proper initialization timing
    // Returns false if audioManager is null (game runs without audio)
    val isTTSReady: StateFlow<Boolean> = audioManager?.isTTSReady
        ?: MutableStateFlow(false).asStateFlow()

    init {
        viewModelScope.launch {
            loadWordsForStar()
        }
    }

    /**
     * Load words for the current star level.
     */
    private suspend fun loadWordsForStar() {
        val words = WordRepository.getWordsForStar(starNumber, _currentLanguage.value)
        wordPerformanceTracker.reset()

        val firstWord = words.firstOrNull() ?: ""
        wordPerformanceTracker.startWordTracking(firstWord)

        _gameState.update {
            it.copy(
                wordPool = words,
                currentWord = firstWord.uppercase(),
                wordsCompleted = 0,
                typedLetters = "",
                sessionComplete = false,
                remainingWords = words.drop(1),
                failedWords = emptyList()
            )
        }

        _ghostExpression.value = GhostExpression.NEUTRAL
        Log.d(TAG, "Loaded ${words.size} words for star $starNumber in ${_currentLanguage.value} mode")
    }

    /**
     * Speak the current word using TTS.
     */
    fun speakCurrentWord() {
        val word = _gameState.value.currentWord

        // Set flag to prevent duplicate auto-play from LaunchedEffect
        skipNextAutoPlay = true
        viewModelScope.launch {
            delay(200L) // Reset flag after 200ms
            skipNextAutoPlay = false
        }

        audioManager?.speakWord(
            word = word,
            onStart = {
                _isSpeaking.value = true
            },
            onDone = {
                _isSpeaking.value = false
            },
            onError = {
                _isSpeaking.value = false
            }
        )

        Log.d(TAG, "Word '${_gameState.value.currentWord}' spoken.")
    }

    /**
     * Check if auto-play should be skipped (user just manually triggered playback).
     * Internal visibility for testing.
     */
    internal fun shouldSkipAutoPlay(): Boolean {
        return skipNextAutoPlay
    }

    /**
     * Handle letter typed by user.
     *
     * @param letter Letter typed by user
     */
    fun onLetterTyped(letter: Char) {
        val currentWord = _gameState.value.currentWord
        val typedLetters = _gameState.value.typedLetters

        if (currentWord.isEmpty()) {
            Log.w(TAG, "No current word - ignoring letter input")
            return
        }

        if (typedLetters.length >= currentWord.length) {
            Log.d(TAG, "Word already complete - ignoring additional input")
            return
        }

        resetTimeouts()

        val expectedLetter = currentWord[typedLetters.length]
        val isCorrect = letter.uppercaseChar() == expectedLetter

        if (isCorrect) {
            handleCorrectLetter(letter.uppercaseChar())
        } else {
            handleIncorrectLetter(letter.uppercaseChar())
        }
    }

    /**
     * Handle correct letter input.
     */
    private fun handleCorrectLetter(letter: Char) {
        Log.d(TAG, "Correct letter: $letter")

        wordPerformanceTracker.recordCorrectAttempt()
        consecutiveFailuresAtCurrentPosition = 0

        _gameState.update {
            it.copy(typedLetters = it.typedLetters + letter)
        }

        setGhostExpression(GhostExpression.HAPPY)

        audioManager?.playSuccess()

        if (_gameState.value.typedLetters == _gameState.value.currentWord) {
            onWordCompleted()
        }
    }

    /**
     * Handle incorrect letter input.
     */
    private fun handleIncorrectLetter(letter: Char) {
        Log.d(TAG, "Incorrect letter: $letter (expected: ${_gameState.value.currentWord[_gameState.value.typedLetters.length]})")

        wordPerformanceTracker.recordIncorrectAttempt()
        consecutiveFailuresAtCurrentPosition++

        Log.d(TAG, "Consecutive failures: $consecutiveFailuresAtCurrentPosition/5")

        if (consecutiveFailuresAtCurrentPosition >= 5) {
            showHintLetter()
        }

        setGhostExpression(GhostExpression.UNHAPPY)

        audioManager?.playError()
    }

    /**
     * Show hint letter after 5 consecutive failures.
     * Displays the correct letter at the current position in grey.
     * Auto-clears after 2 seconds.
     */
    private fun showHintLetter() {
        val currentWord = _gameState.value.currentWord
        val currentPosition = _gameState.value.typedLetters.length

        if (currentPosition >= currentWord.length) {
            Log.w(TAG, "Cannot show hint - position out of bounds")
            return
        }

        val hintLetter = currentWord[currentPosition]
        consecutiveFailuresAtCurrentPosition = 0

        Log.d(TAG, "✨ SHOWING HINT: '$hintLetter' at position $currentPosition")

        _gameState.update {
            it.copy(hintState = com.spellwriter.data.models.HintState(hintLetter, currentPosition))
        }

        viewModelScope.launch {
            delay(2000L)
            Log.d(TAG, "Clearing hint after 2s")
            clearHintLetter()
        }
    }

    /**
     * Clear the hint letter from display.
     */
    private fun clearHintLetter() {
        Log.d(TAG, "Hint cleared")
        _gameState.update { it.copy(hintState = null) }
    }

    /**
     * Set ghost expression with optional auto-reset.
     *
     * @param expression The expression to set
     * @param autoReset If true, automatically reset to NEUTRAL after 500ms
     */
    private fun setGhostExpression(expression: GhostExpression, autoReset: Boolean = true) {
        expressionResetJob?.cancel()

        _ghostExpression.value = expression

        if (autoReset && expression != GhostExpression.NEUTRAL) {
            expressionResetJob = viewModelScope.launch {
                delay(500L)
                _ghostExpression.value = GhostExpression.NEUTRAL
            }
        }
    }

    /**
     * Handle word completion and progression to next word.
     */
    private fun onWordCompleted() {
        val currentWord = _gameState.value.currentWord
        Log.d(TAG, "Word completed: $currentWord")

        val performance = wordPerformanceTracker.completeWord(currentWord)
        Log.d(TAG, "Word performance - $currentWord: ${performance.getAccuracy()}% accuracy, ${performance.completionTimeMs}ms")

        val newWordsCompleted = wordPerformanceTracker.wordsCompletedCount.value

        val updatedFailedWords = _gameState.value.failedWords.filter { it != currentWord }
        val updatedCompletedWords = _gameState.value.completedWords + currentWord

        setGhostExpression(GhostExpression.HAPPY)

        if (newWordsCompleted >= 20) {
            Log.d(TAG, "Session complete - all 20 unique words finished")

            viewModelScope.launch {
                delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

                consecutiveFailuresAtCurrentPosition = 0

                _gameState.update {
                    it.copy(
                        wordsCompleted = newWordsCompleted,
                        typedLetters = "",
                        sessionComplete = true,
                        remainingWords = emptyList(),
                        failedWords = emptyList(),
                        completedWords = updatedCompletedWords,
                        hintState = null
                    )
                }

                if (!isReplaySession && progressRepository != null) {
                    try {
                        val updatedProgress = initialProgress.earnStar(starNumber)
                        progressRepository.saveProgress(updatedProgress)
                        progressRepository.clearSessionState()
                        Log.d(TAG, "Progress saved - Star $starNumber earned")

                        sessionRepository?.clearSession()
                        Log.d(TAG, "Saved session cleared after star completion")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save progress", e)
                    }
                }

                pauseTimeouts()

                _celebrationStarLevel.value = starNumber
                _showCelebration.value = true
                Log.d(TAG, "Celebration triggered for star $starNumber")
            }
            return
        }

        val currentRemaining = _gameState.value.remainingWords
        val nextWord = currentRemaining.firstOrNull()

        if (nextWord != null) {
            wordPerformanceTracker.startWordTracking(nextWord)
        }

        viewModelScope.launch {
            delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

            consecutiveFailuresAtCurrentPosition = 0

            _gameState.update {
                it.copy(
                    wordsCompleted = newWordsCompleted,
                    currentWord = nextWord?.uppercase() ?: "",
                    typedLetters = "",
                    remainingWords = currentRemaining.drop(1),
                    failedWords = updatedFailedWords,
                    completedWords = updatedCompletedWords,
                    hintState = null
                )
            }

            if (progressRepository != null) {
                try {
                    progressRepository.saveSessionState(starNumber, newWordsCompleted)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save session state", e)
                }
            }

            resetTimeouts()

            if (nextWord != null) {
                delay(300)
                speakCurrentWord()
            } else {
                Log.w(TAG, "No remaining words but session not complete - checking session state")
            }
        }
    }

    /**
     * Handle word failure and return to pool for retry.
     */
    fun onWordFailed() {
        val currentWord = _gameState.value.currentWord
        if (currentWord.isEmpty()) {
            Log.w(TAG, "No current word to fail")
            return
        }

        Log.d(TAG, "Word failed: $currentWord")

        val performance = wordPerformanceTracker.failWord(currentWord)
        Log.d(TAG, "Word performance - $currentWord: ${performance.getAccuracy()}% accuracy, ${performance.completionTimeMs}ms")

        val currentFailedWords = _gameState.value.failedWords
        val updatedFailedWords = if (currentFailedWords.contains(currentWord)) {
            currentFailedWords
        } else {
            currentFailedWords + currentWord
        }

        val currentRemaining = _gameState.value.remainingWords
        val insertedRemaining = insertWordByLength(currentWord, currentRemaining)

        val nextWord = currentRemaining.firstOrNull()

        _gameState.update {
            it.copy(
                currentWord = nextWord?.uppercase() ?: currentWord,
                typedLetters = "",
                remainingWords = if (nextWord != null) insertedRemaining.drop(1) else insertedRemaining,
                failedWords = updatedFailedWords,
                hintState = null
            )
        }

        consecutiveFailuresAtCurrentPosition = 0

        setGhostExpression(GhostExpression.DEAD, autoReset = false)

        viewModelScope.launch {
            delay(2000L)
            _ghostExpression.value = GhostExpression.NEUTRAL
            speakCurrentWord()
        }
    }

    /**
     * Insert a word into a list maintaining length-based ordering.
     */
    internal fun insertWordByLength(word: String, words: List<String>): List<String> {
        if (words.isEmpty()) return listOf(word)

        val insertIndex = words.indexOfFirst { it.length > word.length }
        return if (insertIndex == -1) {
            words + word
        } else {
            words.toMutableList().apply { add(insertIndex, word) }
        }
    }

    /**
     * Handle celebration sequence completion.
     */
    fun onCelebrationComplete() {
        _showCelebration.value = false
        _celebrationStarLevel.value = 0
        _shouldNavigateHome.value = true  // Signal GameScreen to navigate home
        Log.d(TAG, "Celebration complete - signaling navigation to home")
    }

    /**
     * Continue to next star session after GIF reward dismissed.
     *
     * Auto-progression flow:
     * - Star 1 complete → Start star 2 session automatically
     * - Star 2 complete → Start star 3 session automatically
     * - Star 3 complete → Return to home screen (no next star)
     *
     * For replay sessions: Always returns to home (no auto-progression)
     *
     * This function:
     * 1. Checks if replay session (if yes, return to home)
     * 2. Fetches current progress to determine next star
     * 3. Clears current session state
     * 4. If next star available (<=3): Loads new word pool for next star
     * 5. If no next star (>3): Returns to home screen
     */
    fun continueToNextStar() {
        viewModelScope.launch {
            // Replay sessions don't auto-progress - return to home immediately
            if (isReplaySession) {
                Log.d(TAG, "Replay session - returning to home without progression")
                onCelebrationComplete()
                return@launch
            }

            // Fetch current progress to determine next star
            val currentProgress = try {
                progressRepository?.progressFlow?.first() ?: initialProgress
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching progress for auto-progression", e)
                initialProgress
            }

            // Determine next star number
            val nextStar = currentProgress.getCurrentStar()
            Log.d(TAG, "Current progress: $currentProgress, next star: $nextStar")

            // Clear celebration state
            _showCelebration.value = false
            _celebrationStarLevel.value = 0

            // Check if next star is available
            if (nextStar <= 3) {
                // Load word pool for next star and continue playing
                Log.d(TAG, "Auto-progressing to star $nextStar")
                loadWordsForGivenStar(nextStar)
            } else {
                // No next star available (completed star 3) - return to home
                Log.d(TAG, "All stars completed - returning to home")
                onCelebrationComplete()
            }
        }
    }

    /**
     * Load words for a specific star level (used for auto-progression).
     * Similar to loadWordsForStar() but accepts star parameter.
     */
    private suspend fun loadWordsForGivenStar(star: Int) {
        val words = WordRepository.getWordsForStar(star, _currentLanguage.value)
        wordPerformanceTracker.reset()

        val firstWord = words.firstOrNull() ?: ""
        wordPerformanceTracker.startWordTracking(firstWord)

        _gameState.update {
            it.copy(
                wordPool = words,
                currentWord = firstWord.uppercase(),
                wordsCompleted = 0,
                typedLetters = "",
                sessionComplete = false,
                remainingWords = words.drop(1),
                failedWords = emptyList()
            )
        }

        _ghostExpression.value = GhostExpression.NEUTRAL
        Log.d(TAG, "Auto-progression: Loaded ${words.size} words for star $star in ${_currentLanguage.value} mode")
    }

    /**
     * Request to exit the session.
     */
    fun requestExit() {
        _showExitDialog.value = true
        Log.d(TAG, "Exit requested - showing confirmation dialog")
    }

    /**
     * Cancel exit and stay in session.
     */
    fun cancelExit() {
        _showExitDialog.value = false
        Log.d(TAG, "Exit cancelled - staying in session")
    }

    /**
     * Confirm exit and save session.
     */
    suspend fun confirmExit() {
        Log.d(TAG, "Exit confirmed - saving session and returning to home")

        saveSessionProgress()

        _sessionState.value = SessionState.EXITED
        _showExitDialog.value = false
    }

    /**
     * Save current session progress to DataStore.
     */
    private suspend fun saveSessionProgress() {
        if (sessionRepository == null) {
            Log.w(TAG, "SessionRepository not available - session won't be saved")
            return
        }

        val currentState = _gameState.value

        val currentWordIndex = if (currentState.remainingWords.contains(currentState.currentWord.lowercase())) {
            currentState.remainingWords.indexOf(currentState.currentWord.lowercase())
        } else {
            0
        }

        val savedSession = SavedSession(
            starLevel = starNumber,
            wordsCompleted = wordPerformanceTracker.completedWords.size,
            completedWords = wordPerformanceTracker.completedWords.toList(),
            remainingWords = currentState.remainingWords,
            currentWordIndex = currentWordIndex,
            timestamp = System.currentTimeMillis()
        )

        try {
            sessionRepository.saveSession(savedSession)
            Log.d(TAG, "Session saved - ${savedSession.wordsCompleted} words completed, ${savedSession.remainingWords.size} remaining")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session", e)
        }
    }

    /**
     * Reset session state to ACTIVE.
     */
    fun resetSession() {
        _sessionState.value = SessionState.ACTIVE
        Log.d(TAG, "Session state reset to ACTIVE")

        viewModelScope.launch {
            try {
                sessionRepository?.clearSession()
                Log.d(TAG, "Saved session cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear saved session", e)
            }
        }
    }

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        Log.d(TAG, "Cleaning up GameViewModel resources")
        audioManager?.release()
        timeoutManager.release()
        super.onCleared()
    }

    /**
     * Reset timeout timers.
     */
    fun resetTimeouts() {
        timeoutManager.resetTimeouts()
    }

    /**
     * Pause timeout monitoring.
     */
    private fun pauseTimeouts() {
        timeoutManager.pauseTimeouts()
    }

    companion object {
        private const val TAG = "GameViewModel"

        const val WORD_COMPLETE_DISPLAY_DELAY_MS = 500L
    }
}
