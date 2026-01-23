package com.spellwriter.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
    private val _lastInputTime = MutableStateFlow(System.currentTimeMillis())
    private val lastInputTime: StateFlow<Long> = _lastInputTime.asStateFlow()

    private val _isEncouragementShown = MutableStateFlow(false)
    val isEncouragementShown: StateFlow<Boolean> = _isEncouragementShown.asStateFlow()

    private var timeoutJob: Job? = null

    // Celebration state management
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _celebrationStarLevel = MutableStateFlow(0)
    val celebrationStarLevel: StateFlow<Int> = _celebrationStarLevel.asStateFlow()

    // Exit dialog and session state management
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.ACTIVE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // Internal tracking for session management
    private val completedWords = mutableSetOf<String>()

    // Word performance tracking
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
        startTimeoutMonitoring()
    }

    /**
     * Initialize TextToSpeech with appropriate locale.
     * Sets up TTS asynchronously with OnInitListener.
     */
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = getTTSLocale()
                val result = tts?.setLanguage(locale)
                isTTSReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isTTSReady) {
                    tts?.setSpeechRate(0.9f)
                    Log.d(TAG, "TTS initialized successfully with locale: $locale")
                    speakCurrentWord()
                } else {
                    Log.w(TAG, "TTS language not supported: $locale - continuing without audio")
                }
            } else {
                Log.w(TAG, "TTS initialization failed - continuing without audio")
            }
        }
    }

    /**
     * Get appropriate TTS locale based on app language.
     *
     * @return Locale.GERMANY for German, Locale.US for English
     */
    private fun getTTSLocale(): Locale {
        return when (_currentLanguage.value) {
            AppLanguage.GERMAN -> Locale.GERMANY
            AppLanguage.ENGLISH -> Locale.US
        }
    }

    /**
     * Load words for the current star level.
     */
    private suspend fun loadWordsForStar() {
        val words = WordRepository.getWordsForStar(starNumber, _currentLanguage.value)
        completedWords.clear()
        wordPerformanceData.clear()
        startWordTracking(words.firstOrNull() ?: "")

        _gameState.update {
            it.copy(
                wordPool = words,
                currentWord = words.firstOrNull()?.uppercase() ?: "",
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
     * Start tracking performance for a new word.
     */
    private fun startWordTracking(word: String) {
        currentWordStartTime = System.currentTimeMillis()
        currentWordAttempts = 0
        currentWordIncorrectAttempts = 0
    }

    /**
     * Save word performance data.
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
     */
    fun speakCurrentWord() {
        val word = _gameState.value.currentWord

        if (word.isEmpty()) {
            Log.w(TAG, "No current word to speak")
            return
        }

        if (isTTSReady && tts != null) {
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

            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_${System.currentTimeMillis()}")
            Log.d(TAG, "Speaking word: $word")
        } else {
            Log.w(TAG, "TTS not ready - continuing without audio")
        }
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

        currentWordAttempts++

        _gameState.update {
            it.copy(typedLetters = it.typedLetters + letter)
        }

        setGhostExpression(GhostExpression.HAPPY)

        soundManager.playSuccess()

        if (_gameState.value.typedLetters == _gameState.value.currentWord) {
            onWordCompleted()
        }
    }

    /**
     * Handle incorrect letter input.
     */
    private fun handleIncorrectLetter(letter: Char) {
        Log.d(TAG, "Incorrect letter: $letter (expected: ${_gameState.value.currentWord[_gameState.value.typedLetters.length]})")

        currentWordAttempts++
        currentWordIncorrectAttempts++

        setGhostExpression(GhostExpression.UNHAPPY)

        soundManager.playError()
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

        saveWordPerformance(currentWord, success = true)

        completedWords.add(currentWord)
        val newWordsCompleted = completedWords.size

        val updatedFailedWords = _gameState.value.failedWords.filter { it != currentWord }
        val updatedCompletedWords = _gameState.value.completedWords + currentWord

        setGhostExpression(GhostExpression.HAPPY)

        if (newWordsCompleted >= 20) {
            Log.d(TAG, "Session complete - all 20 unique words finished")

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
            startWordTracking(nextWord)
        }

        viewModelScope.launch {
            delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

            _gameState.update {
                it.copy(
                    wordsCompleted = newWordsCompleted,
                    currentWord = nextWord?.uppercase() ?: "",
                    typedLetters = "",
                    remainingWords = currentRemaining.drop(1),
                    failedWords = updatedFailedWords,
                    completedWords = updatedCompletedWords
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
                failedWords = updatedFailedWords
            )
        }

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
        Log.d(TAG, "Celebration complete - returned to normal state")
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
        tts?.stop()
        tts?.shutdown()
        soundManager.release()
        timeoutJob?.cancel()
        super.onCleared()
    }

    /**
     * Start timeout monitoring coroutine.
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
     * Check for timeout conditions.
     */
    private fun checkTimeouts() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastInput = currentTime - _lastInputTime.value
        val currentWord = _gameState.value.currentWord

        if (currentWord.isEmpty()) {
            return
        }

        if (_ghostExpression.value == GhostExpression.DEAD || _showCelebration.value) {
            return
        }

        if (timeSinceLastInput >= ENCOURAGEMENT_TIMEOUT_MS && !_isEncouragementShown.value) {
            showEncouragement()
        }
    }

    /**
     * Show encouraging ghost expression.
     */
    private fun showEncouragement() {
        viewModelScope.launch {
            _isEncouragementShown.value = true

            _ghostExpression.value = GhostExpression.ENCOURAGING

            delay(2000)

            _ghostExpression.value = GhostExpression.NEUTRAL
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
    private fun pauseTimeouts() {
        timeoutJob?.cancel()
        timeoutJob = null
        Log.d(TAG, "Timeout monitoring paused")
    }

    companion object {
        private const val TAG = "GameViewModel"

        const val ENCOURAGEMENT_TIMEOUT_MS = 8_000L
        const val TIMER_TICK_MS = 1_000L
        const val WORD_COMPLETE_DISPLAY_DELAY_MS = 500L
    }
}
