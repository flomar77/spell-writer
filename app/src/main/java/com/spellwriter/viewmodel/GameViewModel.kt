package com.spellwriter.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellwriter.audio.SoundManager
import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.WordPool
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel for game screen gameplay logic.
 * Manages game state, TTS, sound effects, and word progression.
 * Story 1.4: Core Word Gameplay
 * Story 1.5: Ghost expression management with auto-reset and TTS speaking state
 * Story 2.1: 20-word learning sessions with retry logic and session completion
 *
 * @param context Application context for TTS and SoundManager
 * @param starNumber Star level (1, 2, or 3) determining word difficulty
 * @param isReplaySession If true, don't update progress (Story 1.2)
 */
class GameViewModel(
    private val context: Context,
    private val starNumber: Int = 1,
    private val isReplaySession: Boolean = false
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

    // Story 2.1: Internal tracking for session management (AC3, AC5)
    private val completedWords = mutableSetOf<String>()

    // Audio components
    private var tts: TextToSpeech? = null
    private var isTTSReady = false
    private val soundManager = SoundManager(context)

    init {
        initializeTTS()
        loadWordsForStar()
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
     * Returns Locale.GERMANY for German, Locale.US for English.
     */
    private fun getTTSLocale(): Locale {
        return when (Locale.getDefault().language) {
            "de" -> Locale.GERMANY
            else -> Locale.US
        }
    }

    /**
     * Load words for the current star level.
     * Initializes word pool and sets first word.
     * Story 1.5: Ghost expression now managed separately
     * Story 2.1: Initializes session tracking with 20 words in difficulty order (AC1, AC2)
     * AC5: Word loading from pool
     */
    private fun loadWordsForStar() {
        val language = Locale.getDefault().language
        val words = WordPool.getWordsForStar(starNumber, language)

        // Story 2.1: Initialize session tracking
        completedWords.clear()

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

        Log.d(TAG, "Loaded ${words.size} words for star $starNumber, language: $language")
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
     * AC3: Correct letter feedback (animation, sound, happy ghost)
     * NFR1.3: Feedback within 100ms
     */
    private fun handleCorrectLetter(letter: Char) {
        Log.d(TAG, "Correct letter: $letter")

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
     * AC4: Incorrect letter feedback (wobble animation, error sound, unhappy ghost)
     * NFR1.3: Feedback within 100ms
     */
    private fun handleIncorrectLetter(letter: Char) {
        Log.d(TAG, "Incorrect letter: $letter (expected: ${_gameState.value.currentWord[_gameState.value.typedLetters.length]})")

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
     * Story 1.5: Trigger failure animation with DEAD expression (AC5).
     * Prepared for Story 3.2 timeout functionality.
     * Does not auto-reset - manually resets after animation completes.
     */
    fun triggerFailureAnimation() {
        setGhostExpression(GhostExpression.DEAD, autoReset = false)

        viewModelScope.launch {
            // Wait for failure animation duration (configurable for Story 3.2)
            delay(2000L)
            _ghostExpression.value = GhostExpression.NEUTRAL
        }
    }

    /**
     * Handle word completion and progression to next word.
     * Story 1.5: Ghost expression now managed separately
     * Story 2.1: Enhanced with session tracking and completion detection (AC4, AC6)
     * AC5: Word completion and progression
     * NFR1.4: Animations run at 60fps
     */
    private fun onWordCompleted() {
        val currentWord = _gameState.value.currentWord
        Log.d(TAG, "Word completed: $currentWord")

        // Story 2.1: Track completed word (AC4)
        completedWords.add(currentWord)
        val newWordsCompleted = completedWords.size

        // Story 2.1: Remove from failed words if it was a retry (AC5)
        val updatedFailedWords = _gameState.value.failedWords.filter { it != currentWord }

        // Story 2.1: Check session completion FIRST (AC6)
        if (newWordsCompleted >= 20) {
            Log.d(TAG, "Session complete - all 20 unique words finished")
            _gameState.update {
                it.copy(
                    wordsCompleted = newWordsCompleted,
                    typedLetters = "",
                    sessionComplete = true,
                    remainingWords = emptyList(),
                    failedWords = emptyList()
                )
            }
            // Story 1.5: Show happy expression on session completion
            setGhostExpression(GhostExpression.HAPPY)
            // Story 2.4 handles session completion celebrations
            return
        }

        // Story 2.1: Get next word from remaining pool (AC4)
        val currentRemaining = _gameState.value.remainingWords
        val nextWord = currentRemaining.firstOrNull()

        // Update state with progression
        _gameState.update {
            it.copy(
                wordsCompleted = newWordsCompleted,
                currentWord = nextWord?.uppercase() ?: "",
                typedLetters = "",  // Clear for next word
                remainingWords = currentRemaining.drop(1),
                failedWords = updatedFailedWords
            )
        }

        // Story 1.5: Show happy expression on word completion
        setGhostExpression(GhostExpression.HAPPY)

        // Speak next word after short delay
        if (nextWord != null) {
            viewModelScope.launch {
                delay(800)  // Brief pause before next word
                speakCurrentWord()
            }
        } else {
            // This shouldn't happen if session logic is correct
            Log.w(TAG, "No remaining words but session not complete - checking session state")
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

        // Story 1.5: Show unhappy/dead expression on failure
        triggerFailureAnimation()

        // Speak next word after failure animation
        viewModelScope.launch {
            delay(2000L)  // Wait for failure animation
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
     * Clean up resources when ViewModel is destroyed.
     * CRITICAL: Prevents memory leaks by releasing TTS and audio resources.
     */
    override fun onCleared() {
        Log.d(TAG, "Cleaning up GameViewModel resources")
        tts?.stop()
        tts?.shutdown()
        soundManager.release()
        super.onCleared()
    }

    companion object {
        private const val TAG = "GameViewModel"
    }
}
