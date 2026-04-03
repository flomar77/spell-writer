package com.spellwriter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.spellwriter.data.models.AppLanguage
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import com.spellwriter.tts.TtsModelConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val speakMutex = Mutex()
    private var tts: OfflineTts? = null
    private val soundManager = SoundManager(context)

    init {
        initializeTTS()
    }

    /**
     * Copy model directory from assets to external storage.
     * Required for sherpa-onnx TTS which needs file paths (not AssetManager).
     * Loading large ONNX models via AssetManager uses mmap and can corrupt
     * HWUI's native heap on some Samsung devices — file path avoids this.
     * Skips copying if target directory already exists (optimization).
     *
     * @param modelDir Asset-relative model directory (e.g., "vits-piper-de_DE-kerstin-low-int8")
     */
    private suspend fun copyModelToExternal(modelDir: String) = withContext(Dispatchers.IO) {
        val targetDir = File(context.getExternalFilesDir(null), modelDir)

        // Optimization: skip if already copied
        if (targetDir.exists()) {
            Log.d(TAG, "Model already exists at: ${targetDir.absolutePath}")
            return@withContext
        }

        try {
            Log.d(TAG, "Copying model from assets/$modelDir to ${targetDir.absolutePath}")
            targetDir.mkdirs()
            copyAssetsRecursive(modelDir, targetDir)
            Log.d(TAG, "Successfully copied model directory")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model directory: ${e.message}", e)
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
     * Initialize sherpa-onnx TTS with appropriate model.
     * Sets up OfflineTts asynchronously with model loading.
     */
    private fun initializeTTS() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "=== Starting sherpa-onnx TTS initialization ===")
                Log.d(TAG, "Language: $language")
                Log.d(TAG, "External files dir: ${context.getExternalFilesDir(null)?.absolutePath}")

                // Get model configuration for selected language
                val modelConfig = TtsModelConfig.getConfigForLanguage(language)
                Log.d(TAG, "Model config: modelDir=${modelConfig.modelDir}, modelName=${modelConfig.modelName}")

                // Copy model directory to external storage.
                // Loading 60MB ONNX via AssetManager (mmap) corrupts HWUI heap on Samsung devices.
                // File path loading avoids this entirely.
                Log.d(TAG, "Copying model directory: ${modelConfig.modelDir}")
                copyModelToExternal(modelConfig.modelDir)
                Log.d(TAG, "Model copy completed")

                // Resolve absolute external paths for model files
                val externalModelDir = File(
                    context.getExternalFilesDir(null),
                    modelConfig.modelDir
                ).absolutePath
                val externalEspeakPath = "$externalModelDir/espeak-ng-data"
                Log.d(TAG, "External model dir: $externalModelDir")

                // Build OfflineTts configuration with absolute file paths.
                // Build manually to avoid getOfflineTtsConfig constructing a non-empty
                // lexicon path ("$modelDir/") when lexicon is empty — piper doesn't use lexicon.
                Log.d(TAG, "Building OfflineTts configuration...")
                val config = OfflineTtsConfig(
                    model = OfflineTtsModelConfig(
                        vits = OfflineTtsVitsModelConfig(
                            model = "$externalModelDir/${modelConfig.modelName}",
                            lexicon = "", // Piper uses espeak-ng, not lexicon
                            tokens = "$externalModelDir/tokens.txt",
                            dataDir = externalEspeakPath,
                        ),
                        numThreads = 2,
                        debug = true,
                        provider = "cpu",
                    ),
                )
                Log.d(TAG, "Config built successfully")

                // Create OfflineTts instance via file path (assetManager = null → newFromFile)
                Log.d(TAG, "Creating OfflineTts instance...")
                tts = OfflineTts(
                    assetManager = null,
                    config = config
                )
                Log.d(TAG, "OfflineTts instance created")

                // Verify TTS was created successfully
                val sampleRate = tts?.sampleRate() ?: 0
                Log.d(TAG, "Got sample rate: $sampleRate Hz")
                if (sampleRate > 0) {
                    _isTTSReady.value = true
                    Log.d(TAG, "TTS initialized successfully - sample rate: $sampleRate Hz")
                } else {
                    Log.w(TAG, "TTS initialization failed - invalid sample rate")
                    _isTTSReady.value = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "=== TTS Initialization FAILED ===")
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                Log.e(TAG, "Stack trace:", e)
                _isTTSReady.value = false
                // Continue without audio - app should remain functional
            }
        }
    }

    /**
     * Speak the given word using sherpa-onnx TTS with a fresh AudioTrack per word.
     * State is managed internally via [isSpeaking] StateFlow — callers observe that flow
     * instead of receiving callbacks.
     *
     * @param word The word to speak
     */
    suspend fun speakWord(word: String) {
        if (word.isEmpty()) {
            Log.w(TAG, "No word to speak")
            return
        }

        if (!_isTTSReady.value || tts == null) {
            Log.w(TAG, "TTS not ready - continuing without audio")
            return
        }

        speakMutex.withLock {
        try {
            Log.d(TAG, "Speaking word: $word")

            _isSpeaking.value = true

            withContext(Dispatchers.IO) {
                val audio = tts?.generate(
                    text = word,
                    sid = 0,
                    speed = 0.7f
                )

                if (audio != null) {
                    // Convert float [-1, 1] to 16-bit PCM shorts
                    val shorts = ShortArray(audio.samples.size) { i ->
                        (audio.samples[i].coerceIn(-1f, 1f) * 32767).toInt().toShort()
                    }

                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()

                    val audioFormat = AudioFormat.Builder()
                        .setSampleRate(audio.sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()

                    val minBufSize = AudioTrack.getMinBufferSize(
                        audio.sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    // Fresh MODE_STREAM AudioTrack per word — play() before write() so engine
                    // starts immediately with no startup gap
                    val wordTrack = AudioTrack(
                        audioAttributes,
                        audioFormat,
                        maxOf(shorts.size * 2, minBufSize),
                        AudioTrack.MODE_STREAM,
                        android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
                    )

                    val silenceSamples = audio.sampleRate / 4 // 250ms padding before and after
                    val silence = ShortArray(silenceSamples)
                    wordTrack.play()
                    wordTrack.write(silence, 0, silence.size)
                    wordTrack.write(shorts, 0, shorts.size)
                    wordTrack.write(silence, 0, silence.size)

                    val durationMs = ((shorts.size + silenceSamples * 2) * 1000L / audio.sampleRate)
                    Thread.sleep(durationMs + 200)

                    wordTrack.stop()
                    wordTrack.release()
                } else {
                    Log.w(TAG, "Audio generation returned null")
                }
            }

            _isSpeaking.value = false
            Log.d(TAG, "Finished speaking: $word")

        } catch (e: Exception) {
            Log.e(TAG, "Error speaking word: ${e.message}", e)
            _isSpeaking.value = false
        }
        } // speakMutex
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
        tts?.free()
        tts = null
        soundManager.release()
        Log.d(TAG, "Audio resources released")
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}
