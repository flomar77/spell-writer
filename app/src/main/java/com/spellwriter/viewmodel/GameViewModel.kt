package com.spellwriter.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.World
import com.spellwriter.data.repository.WordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _ghostExpression = MutableStateFlow(GhostExpression.NEUTRAL)
    val ghostExpression: StateFlow<GhostExpression> = _ghostExpression.asStateFlow()

    private val _progress = MutableStateFlow(Progress())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private val _showStarAnimation = MutableStateFlow(false)
    val showStarAnimation: StateFlow<Boolean> = _showStarAnimation.asStateFlow()

    private val _showDragonAnimation = MutableStateFlow(false)
    val showDragonAnimation: StateFlow<Boolean> = _showDragonAnimation.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var isTTSReady = false

    private var remainingWords = mutableListOf<String>()
    private var completedWords = mutableListOf<String>()

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = WordRepository.getTTSLocale()
                textToSpeech?.language = locale
                isTTSReady = true
            }
        }
    }

    fun startNewSession(star: Int) {
        val shortWords = WordRepository.getShortWordsForStar(star)
        val longWords = WordRepository.getLongWordsForStar(star)

        // Start with short words, then long words
        remainingWords = (shortWords.shuffled() + longWords.shuffled()).toMutableList()
        completedWords.clear()

        _gameState.value = GameState(
            currentStar = star,
            wordsInPool = remainingWords.toList(),
            wordsCompleted = 0
        )

        loadNextWord()
    }

    private fun loadNextWord() {
        if (remainingWords.isNotEmpty()) {
            val nextWord = remainingWords.removeAt(0)
            _gameState.value = _gameState.value.copy(
                currentWord = nextWord,
                typedLetters = "",
                isWordComplete = false,
                lastLetterCorrect = null
            )
            _ghostExpression.value = GhostExpression.NEUTRAL
        }
    }

    fun speakCurrentWord() {
        if (isTTSReady) {
            textToSpeech?.speak(
                _gameState.value.currentWord,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "word"
            )
        }
    }

    fun onLetterTyped(letter: Char) {
        val state = _gameState.value
        val expectedLetter = state.currentWord.getOrNull(state.typedLetters.length)

        if (expectedLetter != null) {
            if (letter.uppercaseChar() == expectedLetter.uppercaseChar()) {
                // Correct letter
                val newTypedLetters = state.typedLetters + letter.uppercaseChar()
                _gameState.value = state.copy(
                    typedLetters = newTypedLetters,
                    lastLetterCorrect = true,
                    isWordComplete = newTypedLetters.length == state.currentWord.length
                )
                _ghostExpression.value = GhostExpression.HAPPY

                // Check if word complete
                if (newTypedLetters.length == state.currentWord.length) {
                    onWordComplete()
                }
            } else {
                // Wrong letter
                _gameState.value = state.copy(lastLetterCorrect = false)
                _ghostExpression.value = GhostExpression.UNHAPPY

                viewModelScope.launch {
                    delay(500)
                    _ghostExpression.value = GhostExpression.NEUTRAL
                    _gameState.value = _gameState.value.copy(lastLetterCorrect = null)
                }
            }
        }
    }

    private fun onWordComplete() {
        completedWords.add(_gameState.value.currentWord)
        val newWordsCompleted = completedWords.size

        _gameState.value = _gameState.value.copy(
            wordsCompleted = newWordsCompleted
        )

        viewModelScope.launch {
            delay(1000) // Celebration delay

            if (newWordsCompleted >= 20) {
                // Star complete!
                onStarComplete()
            } else {
                loadNextWord()
            }
        }
    }

    private fun onStarComplete() {
        _showStarAnimation.value = true
        _showDragonAnimation.value = true

        // Update progress
        val currentWorld = _progress.value.currentWorld
        val currentStar = _gameState.value.currentStar

        _progress.value = when (currentWorld) {
            World.WIZARD -> _progress.value.copy(
                wizardStars = maxOf(_progress.value.wizardStars, currentStar)
            )
            World.PIRATE -> _progress.value.copy(
                pirateStars = maxOf(_progress.value.pirateStars, currentStar)
            )
        }
    }

    fun onDragonAnimationComplete() {
        _showDragonAnimation.value = false
    }

    fun onStarAnimationComplete() {
        _showStarAnimation.value = false
    }

    fun triggerFailureAnimation() {
        _ghostExpression.value = GhostExpression.DEAD
        viewModelScope.launch {
            delay(2000)
            _ghostExpression.value = GhostExpression.NEUTRAL
        }
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
}
