package com.spellwriter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.tts.ModelConfig
import com.spellwriter.tts.TtsModelConfig
import com.spellwriter.tts.sherpa.OfflineTts
import com.spellwriter.tts.sherpa.getOfflineTtsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

    // Coroutine scope for async TTS operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var tts: OfflineTts? = null
    private var track: AudioTrack? = null
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
     * Initialize AudioTrack for PCM audio playback.
     * Creates AudioTrack with sample rate from TTS, PCM_FLOAT encoding, mono channel.
     */
    private fun initializeAudioTrack() {
        try {
            val sampleRate = tts?.sampleRate() ?: 22050
            Log.d(TAG, "Initializing AudioTrack with sample rate: $sampleRate Hz")

            // Calculate buffer size for streaming audio
            val bufferLength = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
            )

            // Build audio attributes for speech/media playback
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            // Build audio format for PCM float mono
            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            // Create AudioTrack in streaming mode
            track = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferLength,
                AudioTrack.MODE_STREAM,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            // Start AudioTrack in play state (ready to receive samples)
            track?.play()
            Log.d(TAG, "AudioTrack initialized and started")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack: ${e.message}", e)
            track = null
        }
    }

    /**
     * Initialize sherpa-onnx TTS with appropriate model.
     * Sets up OfflineTts asynchronously with model loading.
     */
    private fun initializeTTS() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing sherpa-onnx TTS for language: $language")

                // Get model configuration for selected language
                val modelConfig = TtsModelConfig.getConfigForLanguage(language)

                // Copy espeak-ng-data to external storage (required for file paths)
                copyEspeakDataToExternal(modelConfig.espeakDataPath)

                // Get external espeak-ng-data path
                val externalEspeakPath = File(
                    context.getExternalFilesDir(null),
                    "espeak-ng-data"
                ).absolutePath

                // Build OfflineTts configuration
                val config = getOfflineTtsConfig(
                    modelDir = modelConfig.modelDir,
                    modelName = modelConfig.modelName,
                    acousticModelName = "", // Not using Matcha
                    vocoder = "", // Not using Matcha
                    voices = "", // Not using Kokoro/Kitten
                    lexicon = "", // Piper models don't use separate lexicon
                    dataDir = externalEspeakPath,
                    dictDir = "", // Unused
                    ruleFsts = "",
                    ruleFars = "",
                    numThreads = 2 // Balance of speed and CPU usage
                )

                // Create OfflineTts instance with AssetManager
                tts = OfflineTts(
                    assetManager = context.assets,
                    config = config
                )

                // Verify TTS was created successfully
                val sampleRate = tts?.sampleRate() ?: 0
                if (sampleRate > 0) {
                    // Initialize AudioTrack with TTS sample rate
                    initializeAudioTrack()

                    _isTTSReady.value = true
                    Log.d(TAG, "TTS initialized successfully - sample rate: $sampleRate Hz")
                } else {
                    Log.w(TAG, "TTS initialization failed - invalid sample rate")
                    _isTTSReady.value = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize TTS: ${e.message}", e)
                _isTTSReady.value = false
                // Continue without audio - app should remain functional
            }
        }
    }

    /**
     * Speak the given word using TTS.
     * TODO: Phase 7 - Replace with sherpa-onnx generateWithCallback() and AudioTrack
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
            // TODO: Phase 7 - Implement streaming audio with generateWithCallback
            // For now, just simulate basic behavior
            Log.d(TAG, "TODO: Implement sherpa-onnx speakWord - word: $word")
            _isSpeaking.value = true
            onStart()
            // Temporary: immediately call onDone
            _isSpeaking.value = false
            onDone()
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
        coroutineScope.cancel()
        track?.stop()
        track?.release()
        track = null
        tts?.free()
        tts = null
        soundManager.release()
        Log.d(TAG, "Audio resources released")
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}
