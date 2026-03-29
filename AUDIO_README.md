# Audio & TTS Architecture

## Overview

Spell Writer uses **offline text-to-speech** powered by [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) with [VITS Piper](https://github.com/rhasspy/piper) voice models. No internet connection is required for audio playback.

---

## Components

### 1. Native Libraries — `app/src/main/jniLibs/`

Two `.so` files are required per ABI (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`):

| File | Purpose |
|------|---------|
| `libonnxruntime.so` | ONNX Runtime — runs the neural network |
| `libsherpa-onnx-jni.so` | JNI bridge — exposes sherpa-onnx to Kotlin |

These are copied directly into `jniLibs/` — no Maven/Gradle dependency needed.
Source: https://github.com/k2-fsa/sherpa-onnx/tree/master/android

### 2. Voice Models — `app/src/main/assets/`

Two VITS Piper models are bundled (one per language):

| Language | Directory | Model file |
|----------|-----------|-----------|
| German | `vits-piper-de_DE-thorsten-low-int8/` | `de_DE-thorsten-low.onnx` |
| English | `vits-piper-en_US-danny-low-int8/` | `en_US-danny-low.onnx` |

Each model directory also contains:
- `tokens.txt` — phoneme token vocabulary
- `espeak-ng-data/` — phoneme data directory (copied to external storage at runtime)

> ⚠️ **The `.onnx` and `tokens.txt` files are gitignored** due to their size.
> They must be added manually before building. See [Downloading Models](#downloading-models) below.

Model source: https://huggingface.co/rhasspy/piper-voices

### 3. Kotlin Integration

| File | Role |
|------|------|
| `com/spellwriter/tts/TtsModelConfig.kt` | Maps `AppLanguage` → model paths |
| `com/spellwriter/audio/AudioManager.kt` | Initializes `OfflineTts`, manages `AudioTrack`, exposes `isSpeaking`/`isTTSReady` StateFlows |

---

## How It Works

```
AppLanguage
    └─▶ TtsModelConfig.getConfigForLanguage()
            └─▶ AudioManager.initializeTTS()
                    ├─ copies espeak-ng-data → external storage (file path required by sherpa-onnx)
                    ├─ builds OfflineTtsConfig via getOfflineTtsConfig()
                    └─ creates OfflineTts(assetManager, config)
                            └─▶ AudioManager.speakWord(word)
                                    ├─ tts.generate(text, sid=0, speed=0.9f) → FloatArray samples
                                    └─ AudioTrack.write(samples, WRITE_BLOCKING)
```

- TTS is initialized **once at HomeScreen level** (`MainActivity`) and reused across game sessions
- Initialization is async (IO dispatcher) with a 5-second timeout
- If init fails or times out, the game continues without audio
- `isTTSReady: StateFlow<Boolean>` and `isSpeaking: StateFlow<Boolean>` are observed by the UI for loading states and lip-sync animations

---

## Downloading Models

The `.onnx` model files are too large for git. Download them from Hugging Face and place them in the correct asset directories:

### German — Thorsten (low quality, int8 quantized)

1. Go to: https://huggingface.co/rhasspy/piper-voices/tree/main/de/de_DE/thorsten/low
2. Download `de_DE-thorsten-low.onnx` and `de_DE-thorsten-low.onnx.json`
3. Place in: `app/src/main/assets/vits-piper-de_DE-thorsten-low-int8/`
4. Rename the `.json` to `tokens.txt` if needed (check `TtsModelConfig.kt` for exact expected filenames)

### English — Danny (low quality, int8 quantized)

1. Go to: https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_US/danny/low
2. Download `en_US-danny-low.onnx` and `en_US-danny-low.onnx.json`
3. Place in: `app/src/main/assets/vits-piper-en_US-danny-low-int8/`

> The int8 quantized variants are used to reduce model size and improve inference speed on device.

---

## Getting Native Libraries

Download pre-built `.so` files from the sherpa-onnx GitHub releases:

1. Go to: https://github.com/k2-fsa/sherpa-onnx/releases
2. Find the latest release and download the Android JNI archive
3. Extract `libonnxruntime.so` and `libsherpa-onnx-jni.so` for each ABI
4. Place them in the corresponding `app/src/main/jniLibs/<abi>/` directories

Reference Android TTS example: https://github.com/k2-fsa/sherpa-onnx/tree/master/android/SherpaOnnxTts

---

## Known Issues

- **App crashes on game start if model files are missing** — sherpa-onnx aborts natively (SIGABRT) when it cannot find the `.onnx` file. This cannot be caught with a try/catch.
- `espeak-ng-data` must be copied to external storage at runtime because sherpa-onnx requires file system paths (not Android `AssetManager` paths) for this data.
- The `espeak-ng-data` copy is skipped if the target directory already exists (optimization on subsequent launches).