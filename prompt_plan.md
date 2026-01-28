# Sherpa-ONNX TTS Integration - Prompt Plan

## Overview
Replace Android system TTS with sherpa-onnx offline TTS using Piper models for better voice quality and offline capability.

---

## Phase 1: Prerequisites & Setup

### Step 1: Download Native Libraries
- [X] 1. [SETUP] Download and extract sherpa-onnx native libraries
  - Download sherpa-onnx-v1.12.23-android.tar.bz2 from GitHub releases
  - Extract .so files for arm64-v8a, armeabi-v7a, x86_64, x86
  - Copy to app/src/main/jniLibs/{arch}/ directories
  - Verify files exist: libsherpa-onnx-jni.so, libonnxruntime.so, libsherpa-onnx-core.so

- [X] 2. [CHECK] Verify native library installation
  - Run: `ls -lh app/src/main/jniLibs/arm64-v8a/*.so`
  - Confirm all 3 .so files present in each architecture folder
  - Build test: `./gradlew assembleDebug`
  - Check APK contains libs: `unzip -l app/build/outputs/apk/debug/app-debug.apk | grep "\.so$"`

- [X] 3. [COMMIT] Commit native libraries
  - Review: Native libraries added to jniLibs/
  - Commit: `chore: add sherpa-onnx native libraries for offline TTS`

---

## Phase 2: Kotlin API Wrapper

### Step 2: Copy and Adapt Tts.kt
- [X] 4. [IMPL] Copy sherpa-onnx Kotlin API wrapper
  - Create package: `app/src/main/java/com/spellwriter/tts/sherpa/`
  - Copy `/Users/florentmartin/Sites/sherpa-onnx/sherpa-onnx/kotlin-api/Tts.kt`
  - Change package from `com.k2fsa.sherpa.onnx` to `com.spellwriter.tts.sherpa`
  - Add companion object with `System.loadLibrary("sherpa-onnx-jni")` to OfflineTts class
  - Keep all data classes: OfflineTtsVitsModelConfig, OfflineTtsConfig, GeneratedAudio, OfflineTts
  - Keep all native method declarations unchanged

- [X] 5. [CHECK] Build verification for Tts.kt wrapper
  - Run: `./gradlew compileDebugKotlin`
  - Verify no compilation errors
  - Verify native library loading works (check logs)

- [X] 6. [COMMIT] Commit Tts.kt wrapper
  - Review: New package com.spellwriter.tts.sherpa created
  - Commit: `feat: add sherpa-onnx Kotlin JNI wrapper for TTS integration`

---

## Phase 3: Model Configuration System

### Step 3: Create Model Config
- [X] 7. [TEST] Write tests for TtsModelConfig
  - Test getConfigForLanguage(GERMAN) returns correct model paths
  - Test getConfigForLanguage(ENGLISH) returns correct model paths
  - Test ModelConfig data class contains expected fields
  - Test model directories match assets structure

- [X] 8. [IMPL] Implement TtsModelConfig
  - Create: `app/src/main/java/com/spellwriter/tts/TtsModelConfig.kt`
  - Add getConfigForLanguage() function with switch on AppLanguage
  - German config: modelDir="vits-piper-de_DE-thorsten-low-int8", modelName="de_DE-thorsten-low.onnx"
  - English config: modelDir="vits-piper-en_US-danny-low-int8", modelName="en_US-danny-low.onnx"
  - Add espeakDataPath for espeak-ng-data location
  - Create ModelConfig data class with all paths

- [X] 9. [CHECK] Run TtsModelConfig tests
  - Verify all tests pass
  - Verify config matches asset directory structure

- [X] 10. [COMMIT] Commit model configuration
  - Review: TtsModelConfig created with German and English support
  - Commit: `feat: add TTS model configuration system for multi-language support`

---

## Phase 4: AudioManager Refactoring - Part 1 (Asset Copying)

### Step 4: Asset Copying Utilities
- [X] 11. [TEST] Write tests for espeak-ng-data asset copying
  - Test copyEspeakDataToExternal() creates external directory
  - Test copyAssetsRecursive() handles files and directories correctly
  - Test copy skipped if target directory already exists
  - Test IOException handled gracefully
  - Mock Context.assets and File operations

- [X] 12. [IMPL] Implement asset copying utilities in AudioManager
  - Add imports: java.io.File, kotlinx.coroutines.withContext, Dispatchers
  - Add copyEspeakDataToExternal() suspending function
  - Add copyAssetsRecursive(path: String) helper
  - Add copyAssetFile(filename: String) helper
  - Check if target exists before copying (optimization)
  - Log copy operations for debugging

- [X] 13. [CHECK] Run asset copying tests
  - Verify all tests pass
  - Test on real device: Check external files directory after copy
  - Verify espeak-ng-data files present and readable

- [X] 14. [COMMIT] Commit asset copying utilities
  - Review: Asset copying functions added
  - Commit: `feat: add espeak-ng-data asset copying utilities for sherpa-onnx`

---

## Phase 5: AudioManager Refactoring - Part 2 (TTS Initialization)

### Step 5: Replace TTS Engine
- [X] 15. [TEST] Write tests for sherpa-onnx TTS initialization
  - Test initializeTTS() creates OfflineTts successfully
  - Test isTTSReady becomes true after successful init
  - Test isTTSReady remains false on init failure
  - Test initialization for German language
  - Test initialization for English language
  - Test exception handling (missing models, missing libs)
  - Mock OfflineTts constructor and AssetManager

- [ ] 16. [IMPL] Replace TextToSpeech with OfflineTts in AudioManager
  - Remove imports: android.speech.tts.TextToSpeech, UtteranceProgressListener
  - Add imports: com.spellwriter.tts.sherpa.OfflineTts, com.spellwriter.tts.TtsModelConfig
  - Replace `private var tts: TextToSpeech?` with `private var tts: OfflineTts?`
  - Remove getTTSLocale() function (no longer needed)
  - Refactor initializeTTS() to use sherpa-onnx:
    - Copy espeak-ng-data first
    - Get ModelConfig for language
    - Build OfflineTtsConfig using getOfflineTtsConfig()
    - Create OfflineTts(context.assets, config)
    - Set _isTTSReady.value = true on success
    - Handle exceptions and log errors

- [ ] 17. [CHECK] Run TTS initialization tests
  - Verify all new tests pass
  - Verify old TextToSpeech tests removed/updated
  - Test initialization timing (should be <2s)

- [ ] 18. [COMMIT] Commit TTS initialization refactoring
  - Review: OfflineTts replaces TextToSpeech
  - Commit: `refactor: replace Android TextToSpeech with sherpa-onnx OfflineTts`

---

## Phase 6: AudioManager Refactoring - Part 3 (AudioTrack Playback)

### Step 6: AudioTrack Setup
- [ ] 19. [TEST] Write tests for AudioTrack initialization
  - Test initializeAudioTrack() creates AudioTrack with correct sample rate
  - Test AudioTrack uses ENCODING_PCM_FLOAT
  - Test AudioTrack uses CHANNEL_OUT_MONO
  - Test AudioTrack starts in play state
  - Test sample rate obtained from tts.sampleRate()
  - Mock AudioTrack construction

- [ ] 20. [IMPL] Implement AudioTrack initialization
  - Add imports: android.media.AudioTrack, AudioFormat, AudioAttributes
  - Add `private var track: AudioTrack?` field
  - Implement initializeAudioTrack() function:
    - Get sample rate from tts.sampleRate()
    - Calculate buffer length with getMinBufferSize()
    - Build AudioAttributes for speech/media
    - Build AudioFormat for PCM_FLOAT/MONO
    - Create AudioTrack in MODE_STREAM
    - Call track.play()
  - Call initializeAudioTrack() after OfflineTts creation in initializeTTS()

- [ ] 21. [CHECK] Run AudioTrack tests
  - Verify all tests pass
  - Test on real device: Verify AudioTrack creates without crashes
  - Check logs for sample rate value

- [ ] 22. [COMMIT] Commit AudioTrack setup
  - Review: AudioTrack initialization added
  - Commit: `feat: add AudioTrack setup for PCM audio playback from sherpa-onnx`

---

## Phase 7: AudioManager Refactoring - Part 4 (Speech Synthesis)

### Step 7: Replace speakWord() Implementation
- [ ] 23. [TEST] Write tests for sherpa-onnx speech generation
  - Test speakWord() calls tts.generateWithCallback()
  - Test isSpeaking state changes: false → true → false
  - Test onStart callback fires when generation starts
  - Test onDone callback fires when generation completes
  - Test onError callback fires on exception
  - Test AudioTrack writes samples via callback
  - Test empty word handling
  - Test null TTS handling
  - Mock OfflineTts.generateWithCallback()

- [ ] 24. [IMPL] Refactor speakWord() to use sherpa-onnx
  - Replace UtteranceProgressListener logic
  - Add Dispatchers.IO coroutine launch
  - Prepare AudioTrack: pause(), flush(), play()
  - Set _isSpeaking.value = true, call onStart()
  - Call tts.generateWithCallback() with:
    - text = word
    - sid = 0 (single speaker)
    - speed = 0.9f
    - callback = { samples -> track.write(); return 1 }
  - Add delay(100) for final samples
  - Set _isSpeaking.value = false, call onDone()
  - Wrap in try-catch, call onError() on exception

- [ ] 25. [CHECK] Run speech synthesis tests
  - Verify all tests pass
  - Test on real device: Click Play button
  - Verify word pronunciation works
  - Verify isSpeaking animation syncs
  - Check audio quality

- [ ] 26. [COMMIT] Commit speech synthesis refactoring
  - Review: speakWord() now uses sherpa-onnx generation
  - Commit: `feat: implement streaming audio synthesis with sherpa-onnx generateWithCallback`

---

## Phase 8: AudioManager Refactoring - Part 5 (Cleanup & Coroutines)

### Step 8: Resource Cleanup
- [ ] 27. [TEST] Write tests for resource cleanup
  - Test release() stops AudioTrack
  - Test release() releases AudioTrack
  - Test release() calls tts.free()
  - Test release() nullifies references
  - Test CoroutineScope cancellation on release
  - Test SoundManager.release() still called

- [ ] 28. [IMPL] Update release() and add CoroutineScope
  - Add imports: CoroutineScope, SupervisorJob, Dispatchers, cancel
  - Add field: `private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)`
  - Update release():
    - Call coroutineScope.cancel()
    - Call track?.stop(), track?.release(), track = null
    - Call tts?.free(), tts = null
    - Call soundManager.release()
  - Replace all `viewModelScope.launch` with `coroutineScope.launch`

- [ ] 29. [CHECK] Run cleanup tests
  - Verify all tests pass
  - Test on real device: Language switch (triggers release)
  - Check memory profiler for leaks
  - Verify no crashes on app close

- [ ] 30. [COMMIT] Commit resource cleanup improvements
  - Review: Proper AudioTrack and native memory cleanup
  - Commit: `refactor: improve AudioManager resource cleanup with coroutine scope management`

---

## Phase 9: Integration Testing

### Step 9: End-to-End Tests
- [ ] 31. [TEST] Write end-to-end integration tests
  - Test: Launch app → Play → Word spoken in German
  - Test: Launch app → EN → Play → Word spoken in English
  - Test: Game → Home → Play → AudioManager reused
  - Test: DE → EN switch → Play → English voice
  - Test: isTTSReady changes from false to true
  - Test: isSpeaking syncs with audio playback
  - Test: Multiple words in sequence
  - Test: Error scenarios (missing libs, missing models)

- [ ] 32. [CHECK] Run full integration test suite
  - Run: `./gradlew connectedAndroidTest`
  - Verify all tests pass
  - Test on real device for audio verification
  - Check logcat for sherpa-onnx logs

- [ ] 33. [COMMIT] Commit integration tests
  - Review: Complete test coverage for sherpa-onnx integration
  - Commit: `test: add end-to-end integration tests for sherpa-onnx TTS`

---

## Phase 10: Manual Testing & Verification

### Step 10: Device Testing
- [ ] 34. [MANUAL] Test on real device - German voice
  - Launch app (default German)
  - Click Play button
  - Verify loading indicator
  - Verify German word pronunciation
  - Check voice quality and clarity
  - Verify ghost speaking animation

- [ ] 35. [MANUAL] Test on real device - English voice
  - Switch to English
  - Click Play
  - Verify English word pronunciation
  - Compare voice quality with German
  - Verify correct accent (US English)

- [ ] 36. [MANUAL] Test on real device - Language switching
  - Start session in German
  - Return to Home
  - Switch to English
  - Click Play
  - Verify English voice used
  - Verify no German audio remnants
  - Check memory usage

- [ ] 37. [MANUAL] Test on real device - Performance
  - Measure first word latency (should be <500ms after first launch)
  - Measure subsequent word latency (should be <200ms)
  - Check memory usage (should be +~50MB)
  - Monitor CPU during synthesis
  - Verify no audio stuttering or glitches

- [ ] 38. [MANUAL] Test on real device - Error handling
  - Test with missing espeak-ng-data (delete from external storage)
  - Test with insufficient storage space
  - Verify error logged but no crash
  - Verify game still playable without audio

- [ ] 39. [MANUAL] Test on emulator - Architecture compatibility
  - Test on x86_64 emulator
  - Test on arm64-v8a emulator
  - Verify native libraries load correctly
  - Verify audio playback works

---

## Phase 11: Performance Optimization

### Step 11: Optimization
- [ ] 40. [REFACTOR] Optimize espeak-ng-data copying
  - Add check to skip copy if files already exist
  - Add progress logging for long operations
  - Consider zip compression for faster copy
  - Test optimization on slow device

- [ ] 41. [REFACTOR] Optimize AudioTrack buffer size
  - Experiment with buffer size multipliers
  - Balance latency vs glitch prevention
  - Test on various devices
  - Document optimal settings

- [ ] 42. [CHECK] Performance benchmarks
  - Measure initialization time
  - Measure generation time per word
  - Measure memory footprint
  - Compare with old TextToSpeech baseline
  - Document improvements

- [ ] 43. [COMMIT] Commit performance optimizations
  - Review: Optimizations applied
  - Commit: `perf: optimize asset copying and AudioTrack buffering for sherpa-onnx`

---

## Phase 12: Documentation & Polish

### Step 12: Code Documentation
- [ ] 44. [REFACTOR] Add KDoc comments
  - Document TtsModelConfig.getConfigForLanguage()
  - Document AudioManager.initializeTTS()
  - Document AudioManager.speakWord() with sherpa-onnx details
  - Document asset copying functions
  - Document threading model
  - Document native memory management

- [ ] 45. [REFACTOR] Update README with sherpa-onnx info
  - Add sherpa-onnx section explaining integration
  - Document native library requirements
  - Document model files and assets
  - Add troubleshooting section
  - Link to sherpa-onnx documentation

- [ ] 46. [CHECK] Code quality verification
  - Run: `./gradlew lint`
  - Fix any warnings
  - Run: `./gradlew detekt` (if configured)
  - Verify code formatting consistent
  - Review for dead code or TODOs

- [ ] 47. [COMMIT] Commit documentation and polish
  - Review: Documentation complete
  - Commit: `docs: add comprehensive documentation for sherpa-onnx TTS integration`

---

## Phase 13: Final Verification & Acceptance

### Step 13: Final Checks
- [ ] 48. [CHECK] Build verification
  - Clean build: `./gradlew clean build`
  - Verify no warnings or errors
  - Check APK size increase (expected: ~50MB)
  - Verify all architectures included

- [ ] 49. [CHECK] Test suite verification
  - Run all unit tests: `./gradlew test`
  - Run all instrumented tests: `./gradlew connectedAndroidTest`
  - Verify 100% pass rate
  - Check code coverage (should be >80% for new code)

- [ ] 50. [REVIEW] Acceptance criteria verification
  - ✅ Native libraries load successfully
  - ✅ TTS initializes for German and English
  - ✅ Words pronounced clearly with natural voice
  - ✅ Speaking animation syncs with audio
  - ✅ Language switching works without crashes
  - ✅ Memory usage acceptable (<100MB increase)
  - ✅ No audio artifacts or stuttering
  - ✅ Game playable if TTS fails
  - ✅ Extensible for more languages

- [ ] 51. [COMMIT] Final commit
  - Review entire implementation
  - Commit message:
    ```
    feat: replace Android TTS with sherpa-onnx offline TTS using Piper models

    Major improvements:
    - Replaced Android TextToSpeech with sherpa-onnx OfflineTts
    - Integrated VITS Piper models for German and English
    - Implemented AudioTrack for PCM audio playback
    - Added streaming audio synthesis with generateWithCallback
    - Proper native memory management with tts.free()
    - espeak-ng-data copying from assets to external storage
    - Multi-language support with TtsModelConfig
    - Graceful error handling and fallback

    Technical details:
    - Native libs: libsherpa-onnx-jni.so, libonnxruntime.so
    - Models: vits-piper-de_DE-thorsten-low-int8, vits-piper-en_US-danny-low-int8
    - Sample rate: 22050 Hz (Piper default)
    - Threading: Dispatchers.IO for synthesis, AudioTrack callback for playback
    - Memory: ~50MB increase (models + runtime)

    Benefits:
    - Better voice quality (neural TTS)
    - Fully offline (no dependency on system TTS)
    - Consistent across all devices
    - Extensible for additional languages

    Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
    ```

---

## Phase 14: Future Extensibility Planning

### Step 14: Language Addition Guide
- [ ] 52. [DOCS] Create language addition guide
  - Document step-by-step process
  - Example: Adding French support
  - Include model download URLs
  - Include AppLanguage enum update
  - Include TtsModelConfig update
  - Include asset directory structure

- [ ] 53. [DOCS] Document alternative model types
  - Matcha TTS integration guide
  - Kokoro multi-speaker integration
  - Model conversion process
  - Performance trade-offs

---

## Completion Checklist

### Prerequisites
- [ ] Native libraries downloaded and installed
- [ ] Build successful with native libs

### Core Implementation
- [ ] Tts.kt wrapper copied and adapted
- [ ] TtsModelConfig implemented
- [ ] AudioManager refactored for sherpa-onnx
- [ ] Asset copying utilities working
- [ ] AudioTrack playback functional
- [ ] Resource cleanup implemented

### Testing
- [ ] Unit tests passing (100%)
- [ ] Integration tests passing (100%)
- [ ] Manual device testing complete
- [ ] Performance benchmarks acceptable

### Quality
- [ ] Code documented (KDoc)
- [ ] README updated
- [ ] Lint warnings resolved
- [ ] Code review complete

### Acceptance
- [ ] All success criteria met
- [ ] Voice quality acceptable
- [ ] Performance acceptable
- [ ] Memory usage acceptable
- [ ] Feature approved

---

## Notes

**Key Files:**
- `/Users/florentmartin/Sites/sherpa-onnx/sherpa-onnx/kotlin-api/Tts.kt` - Source for JNI wrapper
- `app/src/main/java/com/spellwriter/audio/AudioManager.kt` - Main refactoring target
- `app/src/main/assets/vits-piper-*` - Model directories

**Critical Decisions:**
- Use streaming callback for lower latency
- Copy espeak-ng-data to external storage (file path requirement)
- Maintain existing AudioManager API contract
- 2 threads for TTS synthesis (balance of speed/CPU)

**Risks & Mitigations:**
- espeak-ng-data copy failure → Check permissions, use app-specific dir
- Native library load failure → Try-catch, graceful degradation
- AudioTrack underrun → Use WRITE_BLOCKING, sufficient buffer
- First-word latency → Pre-initialize TTS (already doing)
