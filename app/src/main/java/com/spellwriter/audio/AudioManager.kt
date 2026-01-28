package com.spellwriter.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.spellwriter.data.models.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    // TTS ready state to prevent race conditions
    private val _isTTSReady = MutableStateFlow(false)
    val isTTSReady: StateFlow<Boolean> = _isTTSReady

    private var tts: TextToSpeech? = null
    private val soundManager = SoundManager(context)

    init {
        initializeTTS()
    }

    /**
     * Copy espeak-ng-data from assets to external storage.
     * Required for sherpa-onnx TTS which needs file paths (not AssetManager).
     * Skips copying if target directory already exists (optimization).
     *
     * @param espeakDataPath Path to espeak-ng-data in assets (e.g., "vits-piper-de_DE-thorsten-low-int8/espeak-ng-data")
     */
    private suspend fun copyEspeakDataToExternal(espeakDataPath: String) = withContext(Dispatchers.IO) {
        val targetDir = File(context.getExternalFilesDir(null), "espeak-ng-data")

        // Optimization: skip if already copied
        if (targetDir.exists()) {
            Log.d(TAG, "espeak-ng-data already exists at: ${targetDir.absolutePath}")
            return@withContext
        }

        try {
            Log.d(TAG, "Copying espeak-ng-data from assets/$espeakDataPath to ${targetDir.absolutePath}")
            targetDir.mkdirs()
            copyAssetsRecursive(espeakDataPath, targetDir)
            Log.d(TAG, "Successfully copied espeak-ng-data")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy espeak-ng-data: ${e.message}", e)
            throw e
        }
    }

    /**
     * Recursively copy directory contents from assets.
     *
     * @param assetPath Path in assets directory
     * @param targetDir Target directory in external storage
     */
    private fun copyAssetsRecursive(assetPath: String, targetDir: File) {
        val files = context.assets.list(assetPath) ?: emptyArray()

        for (filename in files) {
            val assetFilePath = "$assetPath/$filename"
            val subFiles = context.assets.list(assetFilePath)

            if (subFiles != null && subFiles.isNotEmpty()) {
                // It's a directory, recurse
                val subDir = File(targetDir, filename)
                subDir.mkdirs()
                copyAssetsRecursive(assetFilePath, subDir)
            } else {
                // It's a file, copy it
                copyAssetFile(assetFilePath, File(targetDir, filename))
            }
        }
    }

    /**
     * Copy a single file from assets to target location.
     *
     * @param assetFilePath Path to file in assets
     * @param targetFile Target file location
     */
    private fun copyAssetFile(assetFilePath: String, targetFile: File) {
        context.assets.open(assetFilePath).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
                Log.d(TAG, "Copied: $assetFilePath -> ${targetFile.name}")
            }
        }
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
                val isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isReady) {
                    tts?.setSpeechRate(0.9f)
                    _isTTSReady.value = true
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

        if (_isTTSReady.value && tts != null) {
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
