package com.spellwriter.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.spellwriter.data.models.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Manages audio functionality including TTS and sound effects.
 * Handles word pronunciation, audio state tracking, and resource cleanup.
 */
class AudioManager(
    private val context: Context,
    private val language: AppLanguage
) {
    // TTS speaking state for animation synchronization
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var tts: TextToSpeech? = null
    private var isTTSReady = false
    private val soundManager = SoundManager(context)

    init {
        initializeTTS()
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
        return when (language) {
            AppLanguage.GERMAN -> Locale.GERMANY
            AppLanguage.ENGLISH -> Locale.US
        }
    }

    /**
     * Speak the given word using TTS.
     *
     * @param word The word to speak
     * @param onStart Callback when TTS starts speaking
     * @param onDone Callback when TTS finishes speaking
     * @param onError Callback when TTS encounters an error
     */
    fun speakWord(
        word: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: () -> Unit
    ) {
        if (word.isEmpty()) {
            Log.w(TAG, "No word to speak")
            return
        }

        if (isTTSReady && tts != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    onStart()
                    Log.d(TAG, "TTS started speaking: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onDone()
                    Log.d(TAG, "TTS finished speaking: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onError()
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
     * Play success sound effect.
     */
    fun playSuccess() {
        soundManager.playSuccess()
    }

    /**
     * Play error sound effect.
     */
    fun playError() {
        soundManager.playError()
    }

    /**
     * Clean up audio resources.
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        soundManager.release()
        Log.d(TAG, "Audio resources released")
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}
