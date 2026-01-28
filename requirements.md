# Sherpa-ONNX TTS Integration Plan

## Objective
Replace Android system TTS with sherpa-onnx offline TTS using existing Piper models (German & English) with extensibility for more languages.

## Architecture Overview
**Current:** AudioManager → Android TextToSpeech → System TTS engine
**Target:** AudioManager → OfflineTts (sherpa-onnx) → AudioTrack → PCM playback

**API Contract (preserved):**
- `isTTSReady: StateFlow<Boolean>`
- `isSpeaking: StateFlow<Boolean>`
- `speakWord(word, onStart, onDone, onError)`
- `release()`

## Prerequisites: Download Native Libraries

### Step 1: Download sherpa-onnx Android package
```bash
cd /Users/florentmartin/Sites/bmad-spell-writer/spell-writer
wget https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.12.23/sherpa-onnx-v1.12.23-android.tar.bz2
tar -xjf sherpa-onnx-v1.12.23-android.tar.bz2
```

### Step 2: Extract .so files to jniLibs
```bash
# Navigate to extracted directory
cd sherpa-onnx-v1.12.23-android

# Copy for each architecture (prioritize arm64-v8a for modern devices)
cp lib/arm64-v8a/*.so /Users/florentmartin/Sites/bmad-spell-writer/spell-writer/app/src/main/jniLibs/arm64-v8a/
cp lib/armeabi-v7a/*.so /Users/florentmartin/Sites/bmad-spell-writer/spell-writer/app/src/main/jniLibs/armeabi-v7a/
cp lib/x86_64/*.so /Users/florentmartin/Sites/bmad-spell-writer/spell-writer/app/src/main/jniLibs/x86_64/
cp lib/x86/*.so /Users/florentmartin/Sites/bmad-spell-writer/spell-writer/app/src/main/jniLibs/x86/
```

**Files needed per architecture:**
- `libsherpa-onnx-jni.so` (~2-4MB) - JNI bridge
- `libonnxruntime.so` (~6-9MB) - ONNX Runtime engine
- `libsherpa-onnx-core.so` (~1-2MB) - Core library

### Step 3: Verify installation
```bash
ls -lh /Users/florentmartin/Sites/bmad-spell-writer/spell-writer/app/src/main/jniLibs/arm64-v8a/*.so
# Should see: libsherpa-onnx-jni.so, libonnxruntime.so, libsherpa-onnx-core.so
```

## Implementation Steps

### 1. Copy Kotlin API Wrapper (15 min)

**Source:** `/Users/florentmartin/Sites/sherpa-onnx/sherpa-onnx/kotlin-api/Tts.kt`

**Target:** Create new package `com.spellwriter.tts.sherpa`

**Files to create:**
- `app/src/main/java/com/spellwriter/tts/sherpa/Tts.kt`

**Changes needed:**
```kotlin
// Change package declaration
package com.spellwriter.tts.sherpa

// Add companion object for native library loading
companion object {
    init {
        System.loadLibrary("sherpa-onnx-jni")
    }
}
```

**Classes included:**
- `OfflineTtsVitsModelConfig` - VITS model configuration
- `OfflineTtsMatchaModelConfig` - Matcha model support (future)
- `OfflineTtsKokoroModelConfig` - Kokoro model support (future)
- `OfflineTtsKittenModelConfig` - Kitten model support (future)
- `OfflineTtsModelConfig` - Container for all model types
- `OfflineTtsConfig` - Main TTS configuration
- `GeneratedAudio` - Audio output wrapper
- `OfflineTts` - Main TTS class with JNI methods

**Keep all native method declarations:**
- `newFromAsset(AssetManager, OfflineTtsConfig): Long`
- `newFromFile(OfflineTtsConfig): Long`
- `generateImpl(Long, String, Int, Float): Array<Any>`
- `generateWithCallbackImpl(Long, String, Int, Float, callback): Array<Any>`
- `getSampleRate(Long): Int`
- `getNumSpeakers(Long): Int`
- `delete(Long)`

### 2. Create Model Configuration System (20 min)

**File:** `app/src/main/java/com/spellwriter/tts/TtsModelConfig.kt`

**Purpose:** Centralize model configuration for each language

```kotlin
package com.spellwriter.tts

import com.spellwriter.data.models.AppLanguage
import com.spellwriter.tts.sherpa.OfflineTtsConfig
import com.spellwriter.tts.sherpa.getOfflineTtsConfig

object TtsModelConfig {
    fun getConfigForLanguage(language: AppLanguage): ModelConfig {
        return when (language) {
            AppLanguage.GERMAN -> ModelConfig(
                modelDir = "vits-piper-de_DE-thorsten-low-int8",
                modelName = "de_DE-thorsten-low.onnx",
                tokensName = "tokens.txt",
                espeakDataPath = "vits-piper-de_DE-thorsten-low-int8/espeak-ng-data"
            )
            AppLanguage.ENGLISH -> ModelConfig(
                modelDir = "vits-piper-en_US-danny-low-int8",
                modelName = "en_US-danny-low.onnx",
                tokensName = "tokens.txt",
                espeakDataPath = "vits-piper-en_US-danny-low-int8/espeak-ng-data"
            )
        }
    }

    data class ModelConfig(
        val modelDir: String,
        val modelName: String,
        val tokensName: String,
        val espeakDataPath: String
    )
}
```

**Extensibility:** To add French, Spanish, etc.:
1. Add to `AppLanguage` enum
2. Download Piper model to assets/
3. Add case to `getConfigForLanguage()`

### 3. Refactor AudioManager (90-120 min)

**File:** `app/src/main/java/com/spellwriter/audio/AudioManager.kt`

**Key changes:**

#### Replace imports:
```kotlin
// Remove:
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

// Add:
import android.media.AudioTrack
import android.media.AudioFormat
import android.media.AudioAttributes
import com.spellwriter.tts.sherpa.OfflineTts
import com.spellwriter.tts.TtsModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
```

#### Replace TTS initialization:
```kotlin
// Remove TextToSpeech initialization (lines 39-58)
// Add:
private var tts: OfflineTts? = null
private var track: AudioTrack? = null

private fun initializeTTS() {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            // Copy espeak-ng-data to external storage (sherpa needs file paths)
            val externalDir = copyEspeakDataToExternal()

            // Get model config
            val modelConfig = TtsModelConfig.getConfigForLanguage(language)

            // Build sherpa-onnx config
            val config = getOfflineTtsConfig(
                modelDir = modelConfig.modelDir,
                modelName = modelConfig.modelName,
                acousticModelName = "",
                vocoder = "",
                voices = "",
                lexicon = "",
                dataDir = "$externalDir/${modelConfig.espeakDataPath}",
                dictDir = "",
                ruleFsts = "",
                ruleFars = "",
                numThreads = 2,
                isKitten = false
            )

            // Initialize OfflineTts from assets
            tts = OfflineTts(context.assets, config)

            // Initialize AudioTrack
            initializeAudioTrack()

            _isTTSReady.value = true
            Log.d(TAG, "Sherpa-ONNX TTS ready: ${language}")
        } catch (e: Exception) {
            Log.e(TAG, "TTS init failed", e)
            _isTTSReady.value = false
        }
    }
}
```

#### Add AudioTrack setup:
```kotlin
private fun initializeAudioTrack() {
    val sampleRate = tts?.sampleRate() ?: 22050
    val bufLength = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    )

    val attr = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()

    val format = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .setSampleRate(sampleRate)
        .build()

    track = AudioTrack(
        attr, format, bufLength,
        AudioTrack.MODE_STREAM,
        android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
    )
    track?.play()
}
```

#### Replace speakWord() implementation:
```kotlin
fun speakWord(
    word: String,
    onStart: () -> Unit,
    onDone: () -> Unit,
    onError: () -> Unit
) {
    if (word.isEmpty() || !_isTTSReady.value || tts == null) {
        Log.w(TAG, "TTS not ready or no word")
        onError()
        return
    }

    viewModelScope.launch(Dispatchers.IO) {
        try {
            // Prepare audio track
            track?.pause()
            track?.flush()
            track?.play()

            // Update state
            withContext(Dispatchers.Main) {
                _isSpeaking.value = true
                onStart()
            }

            // Generate with streaming callback
            tts!!.generateWithCallback(
                text = word,
                sid = 0,  // Speaker ID (0 for single-speaker models)
                speed = 0.9f,  // Match existing speech rate
                callback = { samples ->
                    track?.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                    1  // Return 1 to continue, 0 to stop
                }
            )

            // Small delay for final samples to play
            delay(100)

            withContext(Dispatchers.Main) {
                _isSpeaking.value = false
                onDone()
            }

            Log.d(TAG, "Word spoken: $word")
        } catch (e: Exception) {
            Log.e(TAG, "TTS generation failed", e)
            withContext(Dispatchers.Main) {
                _isSpeaking.value = false
                onError()
            }
        }
    }
}
```

#### Add espeak-ng-data copying utility:
```kotlin
/**
 * Copy espeak-ng-data from assets to external storage.
 * sherpa-onnx requires file paths, not AssetManager.
 * Only copies once per language.
 */
private suspend fun copyEspeakDataToExternal(): String {
    return withContext(Dispatchers.IO) {
        val externalDir = context.getExternalFilesDir(null)!!.absolutePath
        val modelConfig = TtsModelConfig.getConfigForLanguage(language)
        val targetDir = File("$externalDir/${modelConfig.espeakDataPath}")

        if (targetDir.exists()) {
            Log.d(TAG, "espeak-ng-data exists, skipping copy")
            return@withContext externalDir
        }

        Log.d(TAG, "Copying espeak-ng-data...")
        copyAssetsRecursive(modelConfig.espeakDataPath)
        Log.d(TAG, "espeak-ng-data copied to $targetDir")
        externalDir
    }
}

private fun copyAssetsRecursive(path: String) {
    val assets = context.assets.list(path) ?: emptyArray()

    if (assets.isEmpty()) {
        // Leaf file
        copyAssetFile(path)
    } else {
        // Directory
        val targetDir = File("${context.getExternalFilesDir(null)}/$path")
        targetDir.mkdirs()

        for (asset in assets) {
            copyAssetsRecursive("$path/$asset")
        }
    }
}

private fun copyAssetFile(filename: String) {
    context.assets.open(filename).use { input ->
        val targetFile = File("${context.getExternalFilesDir(null)}/$filename")
        targetFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
```

#### Update release() method:
```kotlin
fun release() {
    track?.stop()
    track?.release()
    track = null
    tts?.free()  // Release native memory
    tts = null
    soundManager.release()
    Log.d(TAG, "Audio resources released")
}
```

### 4. Update Imports in AudioManager (5 min)

Add coroutines support for Dispatchers.IO:
```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
```

Add coroutineScope field:
```kotlin
private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
```

Update release() to cancel scope:
```kotlin
fun release() {
    coroutineScope.cancel()
    track?.stop()
    // ... rest of cleanup
}
```

### 5. No Changes Needed

**These files require NO modification:**
- `MainActivity.kt` - AudioManager creation flow stays the same
- `GameScreen.kt` - AudioManager injection unchanged
- `GameViewModel.kt` - Uses same AudioManager API
- `build.gradle.kts` - Native libs loaded via jniLibs, no Gradle dependency
- UI components - Receive state via StateFlow, no direct coupling

## Critical Implementation Notes

### Threading Model
- **TTS Init:** Dispatchers.IO (blocking file I/O)
- **Audio Generation:** Dispatchers.IO (CPU-intensive)
- **AudioTrack Playback:** Background thread via generateWithCallback
- **State Updates:** Main thread (withContext)

### Memory Management
- **Native Memory:** Call `tts.free()` in release()
- **AudioTrack:** Call `stop()` and `release()`
- **CoroutineScope:** Cancel on AudioManager.release()

### Error Handling
- TTS init failure → `isTTSReady = false`, log error
- Generation failure → Call `onError()`, reset `isSpeaking`
- Missing models → Exception caught, graceful degradation
- Language switch → Release old AudioManager, create new

### Asset Requirements
**Already available:**
- ✅ `vits-piper-de_DE-thorsten-low-int8/de_DE-thorsten-low.onnx` (18MB)
- ✅ `vits-piper-de_DE-thorsten-low-int8/tokens.txt`
- ✅ `vits-piper-de_DE-thorsten-low-int8/espeak-ng-data/` (120+ files)
- ✅ `vits-piper-en_US-danny-low-int8/en_US-danny-low.onnx` (18MB)
- ✅ `vits-piper-en_US-danny-low-int8/tokens.txt`
- ✅ `vits-piper-en_US-danny-low-int8/espeak-ng-data/` (120+ files)

## Verification Steps

### 1. Native Library Loading
```bash
# Build app
./gradlew assembleDebug

# Check APK contains .so files
unzip -l app/build/outputs/apk/debug/app-debug.apk | grep "\.so$"
# Should see: libsherpa-onnx-jni.so, libonnxruntime.so for each arch
```

### 2. TTS Initialization
**Test:** Launch app, click Play button
**Expected:**
- Logs: "Copying espeak-ng-data..." (first launch only)
- Logs: "Sherpa-ONNX TTS ready: GERMAN"
- No errors, navigation to GameScreen

### 3. Word Pronunciation
**Test:** Type letter in GameScreen
**Expected:**
- AudioTrack plays word pronunciation
- Ghost animates (isSpeaking state changes)
- Clear, natural German/English voice

### 4. Language Switching
**Test:** Home → EN → Play → Back → DE → Play
**Expected:**
- English word pronunciation in first session
- German word pronunciation in second session
- Logs show TTS reinitialized with new language

### 5. Performance
**Measure:**
- First word latency: <500ms (espeak-ng-data copy on first launch adds ~2s)
- Subsequent words: <200ms
- Memory increase: ~50MB (models + runtime)

### 6. Error Scenarios
**Test:**
- Launch on device without external storage
- Launch with corrupted model files
- Generate audio for very long text

**Expected:** Graceful degradation, error logged, game continues without audio

## File Summary

### Files to Create:
1. `app/src/main/java/com/spellwriter/tts/sherpa/Tts.kt` - JNI wrapper (copied)
2. `app/src/main/java/com/spellwriter/tts/TtsModelConfig.kt` - Model config

### Files to Modify:
1. `app/src/main/java/com/spellwriter/audio/AudioManager.kt` - Core refactoring

### Files to Download:
1. Native libraries from https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.12.23/sherpa-onnx-v1.12.23-android.tar.bz2

### No Changes Needed:
- MainActivity.kt
- GameScreen.kt
- GameViewModel.kt
- build.gradle.kts
- All UI components

## Architectural Decisions

### 1. Asset-based model loading + File-based espeak-ng-data
**Decision:** Load .onnx models from assets, copy espeak-ng-data to external storage

**Rationale:**
- sherpa-onnx supports AssetManager for model loading
- espeak-ng requires file paths (C library limitation)
- One-time copy acceptable (3MB, 120 files)

### 2. Streaming audio with generateWithCallback
**Decision:** Use callback-based generation instead of buffered

**Rationale:**
- Lower latency (playback starts during generation)
- Lower memory (no full audio buffer)
- Better UX for longer words

### 3. Piper low-quality models
**Decision:** Use existing vits-piper-*-low-int8 models

**Rationale:**
- Already downloaded (36MB total)
- Good quality for single-word pronunciation
- Fast generation (<200ms for short words)
- Low memory footprint

### 4. Keep existing AudioManager API
**Decision:** Preserve StateFlow API and callback signatures

**Rationale:**
- No changes needed in MainActivity/GameScreen/GameViewModel
- Minimal testing surface
- Clean separation of concerns

## Future Extensibility

### Adding New Languages (15 min per language)
1. Download Piper model from https://github.com/k2-fsa/sherpa-onnx/releases/tag/tts-models
2. Extract to `app/src/main/assets/vits-piper-{lang}_{region}-{voice}-low-int8/`
3. Add to `AppLanguage` enum in `AppLanguage.kt`
4. Add case in `TtsModelConfig.getConfigForLanguage()`
5. Add string resources in `values-{lang}/strings.xml`

**Example:** French
```kotlin
AppLanguage.FRENCH -> ModelConfig(
    modelDir = "vits-piper-fr_FR-siwis-low",
    modelName = "fr_FR-siwis-low.onnx",
    tokensName = "tokens.txt",
    espeakDataPath = "vits-piper-fr_FR-siwis-low/espeak-ng-data"
)
```

### Alternative Model Types
**Matcha TTS:** Higher quality, slower generation
```kotlin
// Update TtsModelConfig to use OfflineTtsMatchaModelConfig
acousticModelName = "model.onnx",
vocoder = "vocos-22khz-univ.onnx",
```

**Kokoro/Kitten:** Multi-speaker support
```kotlin
voices = "voices.bin",
sid = speakerIdFromSettings,  // Allow user to select voice
```

### Voice Selection UI (future)
Add settings screen with speaker ID slider:
```kotlin
// In AudioManager
fun setSpeaker(speakerId: Int) {
    // Pass to generateWithCallback
}
```

## Estimated Effort
- **Total:** 2.5-3 hours
- Native library download/setup: 15 min
- Copy Tts.kt wrapper: 15 min
- Create TtsModelConfig: 20 min
- Refactor AudioManager: 90-120 min
- Testing: 30 min

## Risk Mitigation

**Risk:** espeak-ng-data copy fails
**Mitigation:** Check permissions, handle IOException, use app-specific directory

**Risk:** Native library load fails
**Mitigation:** Try-catch System.loadLibrary, log error, set isTTSReady=false

**Risk:** AudioTrack underrun/glitches
**Mitigation:** Use WRITE_BLOCKING mode, sufficient buffer size

**Risk:** First-word latency too high
**Mitigation:** Pre-initialize TTS at MainActivity startup (already doing this)

## Success Criteria
✅ Native libraries load successfully
✅ TTS initializes for both German and English
✅ Words pronounced clearly with natural voice
✅ Speaking animation syncs with audio
✅ Language switching works without crashes
✅ Memory usage acceptable (<100MB increase)
✅ No audio artifacts or stuttering
✅ Game remains playable if TTS fails

## References
- sherpa-onnx releases: https://github.com/k2-fsa/sherpa-onnx/releases
- Android AudioTrack: https://developer.android.com/reference/android/media/AudioTrack
- Reference implementation: /Users/florentmartin/Sites/sherpa-onnx/android/SherpaOnnxTts/
- Piper TTS models: https://github.com/k2-fsa/sherpa-onnx/releases/tag/tts-models
