package com.spellwriter.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellwriter.audio.SoundManager
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.SavedSession
import com.spellwriter.data.models.SessionState
import com.spellwriter.data.models.WordPerformance
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.data.repository.SessionRepository
import com.spellwriter.data.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Locale.GERMANY
import java.util.Locale.US

/**
 * ViewModel for game screen gameplay logic.
 * Manages game state, TTS, sound effects, and word progression.
 * Story 1.4: Core Word Gameplay
 * Story 1.5: Ghost expression management with auto-reset and TTS speaking state
 * Story 2.1: 20-word learning sessions with retry logic and session completion
 * Story 2.3: Progress persistence and word performance tracking
 * Story 3.1: Session control and exit flow with session persistence
 *
 * @param context Application context for TTS and SoundManager
 * @param starNumber Star level (1, 2, or 3) determining word difficulty
 * @param isReplaySession If true, don't update progress (Story 1.2)
 * @param progressRepository Repository for persisting progress (Story 2.3)
 * @param sessionRepository Repository for persisting session state (Story 3.1)
 * @param initialProgress Initial progress state (Story 2.3)
 */
class GameViewModel(
    private val context: Context,
    private val starNumber: Int = 1,
    private val isReplaySession: Boolean = false,
    private val progressRepository: ProgressRepository? = null,
    private val sessionRepository: SessionRepository? = null,
    private val initialProgress: Progress = Progress()
) : ViewModel() {

    // Game state exposed to UI
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Story 1.5: Ghost expression state management
    private val _ghostExpression = MutableStateFlow(GhostExpression.NEUTRAL)
    val ghostExpression: StateFlow<GhostExpression> = _ghostExpression.asStateFlow()

    // Story 1.5: TTS speaking state for animation synchronization (AC2)
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Story 1.5: Job for expression auto-reset (AC6)
    private var expressionResetJob: Job? = null

    // Story 3.3: Language state management (AC1, AC8, FR8.8)
    private val _currentLanguage = MutableStateFlow(WordRepository.getSystemLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Story 3.2: Timeout tracking for encouragement and failure animations (AC1, AC2)
    private val _lastInputTime = MutableStateFlow(System.currentTimeMillis())
    private val lastInputTime: StateFlow<Long> = _lastInputTime.asStateFlow()

    private val _isEncouragementShown = MutableStateFlow(false)
    val isEncouragementShown: StateFlow<Boolean> = _isEncouragementShown.asStateFlow()

    private var timeoutJob: Job? = null

    // Story 2.4: Celebration state management (AC7)
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _celebrationStarLevel = MutableStateFlow(0)
    val celebrationStarLevel: StateFlow<Int> = _celebrationStarLevel.asStateFlow()

    // Story 3.1: Exit dialog and session state management (AC2, AC3, AC4, AC5)
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.ACTIVE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // Story 2.1: Internal tracking for session management (AC3, AC5)
    private val completedWords = mutableSetOf<String>()

    // Story 2.3: Word performance tracking (AC3, AC7)
    private val wordPerformanceData = mutableMapOf<String, WordPerformance>()
    private var currentWordStartTime: Long = 0L
    private var currentWordAttempts: Int = 0
    private var currentWordIncorrectAttempts: Int = 0

    // Audio components
    private var tts: TextToSpeech? = null
    private var isTTSReady = false
    private val soundManager = SoundManager(context)

    init {
        initializeTTS()
        viewModelScope.launch {
            loadWordsForStar()
        }
        // Story 3.2: Start timeout monitoring (AC1, AC2)
        startTimeoutMonitoring()
    }

    /**
     * Initialize TextToSpeech with appropriate locale.
     * Sets up TTS asynchronously with OnInitListener.
     * AC1, AC2, AC6: TTS initialization and fallback handling
     */
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = getTTSLocale()
                val result = tts?.setLanguage(locale)
                isTTSReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isTTSReady) {
                    // Slightly slower speech rate for children (AC1)
                    tts?.setSpeechRate(0.9f)
                    Log.d(TAG, "TTS initialized successfully with locale: $locale")

                    // Speak first word after TTS is ready
                    speakCurrentWord()
                } else {
                    Log.w(TAG, "TTS language not supported: $locale - continuing without audio")
                }
            } else {
                Log.w(TAG, "TTS initialization failed - continuing without audio (AC6)")
            }
        }
    }

    /**
     * Get appropriate TTS locale based on app language.
     * Story 3.3 (AC6): TTS locale matching for proper pronunciation.
     * FR8.6: TTS language matches app language.
     *
     * @return Locale.GERMANY for German, Locale.US for English
     */
    private fun getTTSLocale(): Locale {
        return when (_currentLanguage.value) {
            AppLanguage.GERMAN -> {
                d("WordRepository", "TTS locale: German (de-DE)")
                GERMANY
            }

            AppLanguage.ENGLISH -> {
                d("WordRepository", "TTS locale: English (en-US)")
                US
            }
        }
    }

    /**
     * Load words for the current star level.
     * Initializes word pool and sets first word.
     * Story 1.5: Ghost expression now managed separately
     * Story 2.1: Initializes session tracking with 20 words in difficulty order (AC1, AC2)
     * Story 2.3: Initialize performance tracking (AC3, AC7)
     * Story 3.3: Language-aware word loading (AC2, AC3)
     * AC5: Word loading from pool
     */
    private suspend fun loadWordsForStar() {
        val words = WordRepository.getWordsForStar(starNumber, _currentLanguage.value)

        // Story 2.1: Initialize session tracking
        completedWords.clear()

        // Story 2.3: Initialize performance tracking (AC3, AC7)
        wordPerformanceData.clear()
        startWordTracking(words.firstOrNull() ?: "")

        _gameState.update {
            it.copy(
                wordPool = words,
                currentWord = words.firstOrNull()?.uppercase() ?: "",
                wordsCompleted = 0,
                typedLetters = "",
                // Story 2.1: Initialize session state (AC1)
                sessionComplete = false,
                remainingWords = words.drop(1),  // All words except current
                failedWords = emptyList()
            )
        }

        // Story 1.5: Initialize ghost expression
        _ghostExpression.value = GhostExpression.NEUTRAL

        Log.d(TAG, "Loaded ${words.size} words for star $starNumber in ${_currentLanguage.value} mode")
    }

    /**
     * Story 2.3: Start tracking performance for a new word (AC3, AC7).
     */
    private fun startWordTracking(word: String) {
        currentWordStartTime = System.currentTimeMillis()
        currentWordAttempts = 0
        currentWordIncorrectAttempts = 0
    }

    /**
     * Story 2.3: Save word performance data (AC3, AC7).
     */
    private fun saveWordPerformance(word: String, success: Boolean) {
        val completionTime = System.currentTimeMillis() - currentWordStartTime
        val performance = WordPerformance(
            word = word,
            attempts = currentWordAttempts,
            incorrectAttempts = currentWordIncorrectAttempts,
            completionTimeMs = completionTime,
            success = success
        )
        wordPerformanceData[word] = performance
        Log.d(TAG, "Word performance - $word: ${performance.getAccuracy()}% accuracy, ${completionTime}ms")
    }

    /**
     * Speak the current word using TTS.
     * Story 1.5: Added UtteranceProgressListener for speaking state tracking (AC2)
     * AC1: Word audio playback within 500ms
     * AC2: Repeat functionality + speaking animation synchronization
     * AC6: Graceful degradation if TTS unavailable
     */
    fun speakCurrentWord() {
        val word = _gameState.value.currentWord

        if (word.isEmpty()) {
            Log.w(TAG, "No current word to speak")
            return
        }

        if (isTTSReady && tts != null) {
            // Story 1.5: Set up utterance callbacks for speaking animation (AC2)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    Log.d(TAG, "TTS started speaking: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    Log.d(TAG, "TTS finished speaking: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    Log.w(TAG, "TTS error for utterance: $utteranceId")
                }
            })

            // QUEUE_FLUSH ensures clean audio playback (AC1, AC2)
            // Use unique utteranceId for callbacks
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_${System.currentTimeMillis()}")
            Log.d(TAG, "Speaking word: $word")
        } else {
            // AC6: Game continues without audio if TTS unavailable
            Log.w(TAG, "TTS not ready - continuing without audio")
        }
    }

    /**
     * Handle letter typed by user.
     * Validates letter and triggers appropriate feedback.
     * Story 3.2: Reset timeouts on any key press (AC1, AC2)
     * AC3: Correct letter feedback
     * AC4: Incorrect letter feedback
     * NFR1.3: Feedback within 100ms
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

        // Check if letter position exceeds word length
        if (typedLetters.length >= currentWord.length) {
            Log.d(TAG, "Word already complete - ignoring additional input")
            return
        }

        // Story 3.2: Reset timeouts on ANY key press (AC1, AC2)
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
     * Story 1.5: Uses new expression management system (AC3)
     * Story 2.3: Track attempt count for performance data (AC3, AC7)
     * AC3: Correct letter feedback (animation, sound, happy ghost)
     * NFR1.3: Feedback within 100ms
     */
    private fun handleCorrectLetter(letter: Char) {
        Log.d(TAG, "Correct letter: $letter")

        // Story 2.3: Track correct attempt (AC3, AC7)
        currentWordAttempts++

        // Update state immediately for responsive feedback (NFR1.3)
        _gameState.update {
            it.copy(typedLetters = it.typedLetters + letter)
        }

        // AC3: Set happy expression with auto-reset (Story 1.5)
        setGhostExpression(GhostExpression.HAPPY)

        // AC3: Play success sound
        soundManager.playSuccess()

        // Check if word is complete
        if (_gameState.value.typedLetters == _gameState.value.currentWord) {
            onWordCompleted()
        }
    }

    /**
     * Handle incorrect letter input.
     * Story 1.5: Uses new expression management system (AC4)
     * Story 2.3: Track incorrect attempt count for performance data (AC3, AC7)
     * AC4: Incorrect letter feedback (wobble animation, error sound, unhappy ghost)
     * NFR1.3: Feedback within 100ms
     */
    private fun handleIncorrectLetter(letter: Char) {
        Log.d(TAG, "Incorrect letter: $letter (expected: ${_gameState.value.currentWord[_gameState.value.typedLetters.length]})")

        // Story 2.3: Track incorrect attempt (AC3, AC7)
        currentWordAttempts++
        currentWordIncorrectAttempts++

        // AC4: Set unhappy expression with auto-reset (Story 1.5)
        setGhostExpression(GhostExpression.UNHAPPY)

        // AC4: Play gentle error sound
        soundManager.playError()
    }

    /**
     * Story 1.5: Set ghost expression with optional auto-reset (AC3, AC4, AC6).
     * Implements Job cancellation pattern to handle rapid input scenarios.
     *
     * @param expression The expression to set
     * @param autoReset If true, automatically reset to NEUTRAL after 500ms
     */
    private fun setGhostExpression(expression: GhostExpression, autoReset: Boolean = true) {
        // Cancel any pending reset from previous interaction (AC7: rapid input handling)
        expressionResetJob?.cancel()

        // Update expression immediately for responsive feedback
        _ghostExpression.value = expression

        // Schedule auto-reset to NEUTRAL after 500ms (AC6)
        if (autoReset && expression != GhostExpression.NEUTRAL) {
            expressionResetJob = viewModelScope.launch {
                delay(500L)
                _ghostExpression.value = GhostExpression.NEUTRAL
            }
        }
    }


    /**
     * Handle word completion and progression to next word.
     * Story 1.5: Ghost expression now managed separately
     * Story 2.1: Enhanced with session tracking and completion detection (AC4, AC6)
     * Story 2.3: Save performance data and persist progress (AC2, AC3, AC4, AC7)
     * AC5: Word completion and progression
     * NFR1.4: Animations run at 60fps
     *
     * Word stays visible for WORD_COMPLETE_DISPLAY_DELAY_MS before transitioning to next word.
     */
    private fun onWordCompleted() {
        val currentWord = _gameState.value.currentWord
        Log.d(TAG, "Word completed: $currentWord")

        // Story 2.3: Save word performance data (AC3, AC7)
        saveWordPerformance(currentWord, success = true)

        // Story 2.1: Track completed word (AC4)
        completedWords.add(currentWord)
        val newWordsCompleted = completedWords.size

        // Story 2.1: Remove from failed words if it was a retry (AC5)
        val updatedFailedWords = _gameState.value.failedWords.filter { it != currentWord }

        // Add to completed words list for display
        val updatedCompletedWords = _gameState.value.completedWords + currentWord

        // Story 1.5: Show happy expression on word completion
        setGhostExpression(GhostExpression.HAPPY)

        // Story 2.1, 2.3: Check session completion FIRST (AC6)
        if (newWordsCompleted >= 20) {
            Log.d(TAG, "Session complete - all 20 unique words finished")

            // Keep word visible briefly before showing completion
            viewModelScope.launch {
                delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

                _gameState.update {
                    it.copy(
                        wordsCompleted = newWordsCompleted,
                        typedLetters = "",
                        sessionComplete = true,
                        remainingWords = emptyList(),
                        failedWords = emptyList(),
                        completedWords = updatedCompletedWords
                    )
                }

                // Story 2.3: Save progress immediately after star completion (AC2, AC4, NFR3.1)
                if (!isReplaySession && progressRepository != null) {
                    try {
                        val updatedProgress = initialProgress.earnStar(starNumber)
                        progressRepository.saveProgress(updatedProgress)
                        progressRepository.clearSessionState()
                        Log.d(TAG, "Progress saved - Star $starNumber earned")

                        // Story 3.1: Clear saved session since session is complete (AC6)
                        sessionRepository?.clearSession()
                        Log.d(TAG, "Saved session cleared after star completion")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save progress", e)
                    }
                }

                // Story 3.2: Pause timeouts during celebration (AC5)
                pauseTimeouts()

                // Story 2.4: Trigger celebration after save (AC7)
                _celebrationStarLevel.value = starNumber
                _showCelebration.value = true
                Log.d(TAG, "Celebration triggered for star $starNumber")
            }
            return
        }

        // Story 2.1: Get next word from remaining pool (AC4)
        val currentRemaining = _gameState.value.remainingWords
        val nextWord = currentRemaining.firstOrNull()

        // Story 2.3: Start tracking next word (AC3, AC7)
        if (nextWord != null) {
            startWordTracking(nextWord)
        }

        // Keep completed word visible briefly, then transition to next word
        viewModelScope.launch {
            // Wait for word to be displayed before clearing
            delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

            // Update state with progression
            _gameState.update {
                it.copy(
                    wordsCompleted = newWordsCompleted,
                    currentWord = nextWord?.uppercase() ?: "",
                    typedLetters = "",  // Clear for next word
                    remainingWords = currentRemaining.drop(1),
                    failedWords = updatedFailedWords,
                    completedWords = updatedCompletedWords
                )
            }

            // Story 2.3: Save session state after each word (AC6, NFR3.1)
            if (progressRepository != null) {
                try {
                    progressRepository.saveSessionState(starNumber, newWordsCompleted)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save session state", e)
                }
            }

            // Story 3.2: Reset timeouts for next word (AC5)
            resetTimeouts()

            // Speak next word after additional delay
            if (nextWord != null) {
                delay(300)  // Brief pause before speaking next word
                speakCurrentWord()
            } else {
                // This shouldn't happen if session logic is correct
                Log.w(TAG, "No remaining words but session not complete - checking session state")
            }
        }
    }

    /**
     * Story 2.1: Handle word failure and return to pool for retry (AC3, AC5).
     * Called when a word cannot be completed within timeout or explicit failure condition.
     * Maintains difficulty ordering by inserting failed word at appropriate position.
     */
    fun onWordFailed() {
        val currentWord = _gameState.value.currentWord
        if (currentWord.isEmpty()) {
            Log.w(TAG, "No current word to fail")
            return
        }

        Log.d(TAG, "Word failed: $currentWord")

        // Story 2.1: Track failed word (AC3)
        val currentFailedWords = _gameState.value.failedWords
        val updatedFailedWords = if (currentFailedWords.contains(currentWord)) {
            currentFailedWords  // Already tracked as failed
        } else {
            currentFailedWords + currentWord
        }

        // Story 2.1: Insert failed word back into remaining pool at correct position (AC5)
        // Maintain length ordering: insert before first word longer than failed word
        val currentRemaining = _gameState.value.remainingWords
        val insertedRemaining = insertWordByLength(currentWord, currentRemaining)

        // Move to next word
        val nextWord = currentRemaining.firstOrNull()

        _gameState.update {
            it.copy(
                currentWord = nextWord?.uppercase() ?: currentWord,  // Stay on current if no next
                typedLetters = "",
                remainingWords = if (nextWord != null) insertedRemaining.drop(1) else insertedRemaining,
                failedWords = updatedFailedWords
            )
        }

        // Story 1.5: Show DEAD expression on failure
        setGhostExpression(GhostExpression.DEAD, autoReset = false)

        // Animate failure and speak next word
        viewModelScope.launch {
            delay(2000L)  // Wait for failure animation
            _ghostExpression.value = GhostExpression.NEUTRAL
            speakCurrentWord()
        }
    }

    /**
     * Story 2.1: Insert a word into a list maintaining length-based ordering (AC5).
     * Words are ordered short to long, so insert before first word longer than the given word.
     * Made internal for testing purposes.
     */
    internal fun insertWordByLength(word: String, words: List<String>): List<String> {
        if (words.isEmpty()) return listOf(word)

        val insertIndex = words.indexOfFirst { it.length > word.length }
        return if (insertIndex == -1) {
            // No words longer - add at end
            words + word
        } else {
            // Insert before first longer word
            words.toMutableList().apply { add(insertIndex, word) }
        }
    }

    /**
     * Story 2.4: Handle celebration sequence completion (AC7).
     * Called when all celebration animations finish.
     * Returns to normal state where user can continue or return to home.
     */
    fun onCelebrationComplete() {
        _showCelebration.value = false
        _celebrationStarLevel.value = 0
        Log.d(TAG, "Celebration complete - returned to normal state")
    }

    /**
     * Story 3.1: Request to exit the session (AC1).
     * Shows the exit confirmation dialog.
     * Called when user taps the Exit button.
     */
    fun requestExit() {
        _showExitDialog.value = true
        Log.d(TAG, "Exit requested - showing confirmation dialog")
    }

    /**
     * Story 3.1: Cancel exit and stay in session (AC4).
     * Dismisses the exit confirmation dialog.
     * Called when user taps "Stay" button.
     */
    fun cancelExit() {
        _showExitDialog.value = false
        Log.d(TAG, "Exit cancelled - staying in session")
    }

    /**
     * Story 3.1: Confirm exit and save session (AC5).
     * Saves session progress to DataStore, then updates state to trigger navigation.
     * CRITICAL: Save MUST complete before changing state.
     *
     * Called when user taps "Leave" button in confirmation dialog.
     */
    suspend fun confirmExit() {
        Log.d(TAG, "Exit confirmed - saving session and returning to home")

        // CRITICAL: Save session progress FIRST (AC5, FR9.4)
        saveSessionProgress()

        // Update state to trigger navigation (AC5)
        _sessionState.value = SessionState.EXITED
        _showExitDialog.value = false
    }

    /**
     * Story 3.1: Save current session progress to DataStore (AC5).
     * Creates SavedSession with current game state and persists it.
     * Enables session resume when user returns (AC6).
     */
    private suspend fun saveSessionProgress() {
        if (sessionRepository == null) {
            Log.w(TAG, "SessionRepository not available - session won't be saved")
            return
        }

        val currentState = _gameState.value

        // Get current word index in remaining words
        val currentWordIndex = if (currentState.remainingWords.contains(currentState.currentWord.lowercase())) {
            currentState.remainingWords.indexOf(currentState.currentWord.lowercase())
        } else {
            0
        }

        val savedSession = SavedSession(
            starLevel = starNumber,
            wordsCompleted = completedWords.size,
            completedWords = completedWords.toList(),
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
     * Story 3.1: Reset session state to ACTIVE (AC5).
     * Called after navigation completes to prepare for next session.
     * Also clears saved session from DataStore.
     */
    fun resetSession() {
        _sessionState.value = SessionState.ACTIVE
        Log.d(TAG, "Session state reset to ACTIVE")

        // Clear saved session since user returned to home
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
     * CRITICAL: Prevents memory leaks by releasing TTS and audio resources.
     * Story 3.2: Cancel timeout job (AC lifecycle management)
     */
    override fun onCleared() {
        Log.d(TAG, "Cleaning up GameViewModel resources")
        tts?.stop()
        tts?.shutdown()
        soundManager.release()
        timeoutJob?.cancel()  // Story 3.2: Cancel timeout monitoring
        super.onCleared()
    }

    /**
     * Story 3.2: Start timeout monitoring coroutine (AC1, AC2).
     * Runs continuously with 1-second intervals to check for timeouts.
     */
    private fun startTimeoutMonitoring() {
        timeoutJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)
                checkTimeouts()
            }
        }
    }

    /**
     * Story 3.2: Check for timeout conditions (AC1).
     * Triggers encouragement at 8s.
     */
    private fun checkTimeouts() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastInput = currentTime - _lastInputTime.value
        val currentWord = _gameState.value.currentWord

        // Only check timeouts if word is active
        if (currentWord.isEmpty()) {
            return
        }

        // Don't trigger timeouts during animations or celebrations
        if (_ghostExpression.value == GhostExpression.DEAD || _showCelebration.value) {
            return
        }

        // 8-second encouragement timeout (AC1) - only once per word attempt
        if (timeSinceLastInput >= ENCOURAGEMENT_TIMEOUT_MS && !_isEncouragementShown.value) {
            showEncouragement()
        }
    }

    /**
     * Story 3.2: Show encouraging ghost expression (AC1).
     * Gentle nudge after 8 seconds of inactivity.
     */
    private fun showEncouragement() {
        viewModelScope.launch {
            _isEncouragementShown.value = true

            // Show encouraging expression
            _ghostExpression.value = GhostExpression.ENCOURAGING

            // Show for 2 seconds
            delay(2000)

            // Return to neutral
            _ghostExpression.value = GhostExpression.NEUTRAL
        }
    }

    /**
     * Story 3.2: Reset timeout timers (AC1).
     * Called on any key press to reset encouragement timer.
     */
    fun resetTimeouts() {
        _lastInputTime.value = System.currentTimeMillis()
        _isEncouragementShown.value = false
    }

    /**
     * Story 3.2: Pause timeout monitoring (AC5).
     * Used during celebrations and other animations.
     */
    private fun pauseTimeouts() {
        timeoutJob?.cancel()
        timeoutJob = null
        Log.d(TAG, "Timeout monitoring paused")
    }

    /**
     * Story 3.2: Resume timeout monitoring (AC5).
     * Restarts timeout system after celebrations or animations.
     */
    private fun resumeTimeouts() {
        if (timeoutJob == null) {
            startTimeoutMonitoring()
            resetTimeouts()  // Fresh start after resume
            Log.d(TAG, "Timeout monitoring resumed")
        }
    }

    companion object {
        private const val TAG = "GameViewModel"

        // Story 3.2: Timeout constants (AC1)
        const val ENCOURAGEMENT_TIMEOUT_MS = 8_000L  // 8 seconds
        const val TIMER_TICK_MS = 1_000L             // Check every second

        // Word completion display delay - keeps completed word visible before transitioning
        const val WORD_COMPLETE_DISPLAY_DELAY_MS = 500L  // 500ms
    }
}
