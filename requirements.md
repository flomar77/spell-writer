# Plan: Fix TTS Multiple Playback Issue

## Problem Statement
The Text-to-Speech (TTS) system plays words multiple times instead of once. Users hear the same word repeated, creating a poor experience.

## Root Cause Analysis

### Current Architecture
Two separate mechanisms trigger word audio playback:

1. **GameViewModel programmatic calls** (`speakCurrentWord()`):
   - Line 428: After word completion → delay(300ms) → speak next word
   - Line 479: After word failure → delay(2000ms) → speak failed word

2. **GameScreen LaunchedEffect auto-play** (lines 121-131):
   - Triggers whenever `gameState.currentWord` changes
   - Delays 100ms then checks `skipNextAutoPlay` flag
   - If flag is false, calls `speakCurrentWord()`

### The Race Condition

**Timeline showing the bug:**
```
T=0ms:    ViewModel: onWordCompleted() starts
T=300ms:  ViewModel: speakCurrentWord() called
T=300ms:  ViewModel: Sets skipNextAutoPlay = true
T=300ms:  ViewModel: Launches coroutine to reset flag after 200ms
T=300ms:  ViewModel: Calls audioManager.speakWord() [PLAYBACK #1]
T=350ms:  GameState updated with new currentWord
T=350ms:  LaunchedEffect triggered by word change
T=450ms:  LaunchedEffect delay(100L) completes
T=450ms:  LaunchedEffect checks skipNextAutoPlay
T=500ms:  ViewModel coroutine: skipNextAutoPlay = false
```

**Problem:** By T=450ms, the flag might already be false (if reset happened at T=500ms), causing LaunchedEffect to trigger **[PLAYBACK #2]**.

### Current Mitigation (Insufficient)
The `skipNextAutoPlay` flag exists to prevent duplicates, but:
- 200ms reset window is too short
- Timing-dependent (race condition)
- Unreliable across different device speeds

## Solution: Centralized Playback Control

### Approach
Remove the dual-trigger architecture. Implement a single, explicit playback control system:

1. **Remove LaunchedEffect auto-play** - Don't auto-play on every word change
2. **Add explicit playback trigger state** - ViewModel controls when audio should play
3. **Keep manual playback** - Play/Replay buttons work as before

### Benefits
- ✅ No race conditions (single source of truth)
- ✅ Predictable behavior (explicit control)
- ✅ Better testability
- ✅ Maintains all current functionality

## Implementation Steps

### 1. Add Playback State to GameViewModel

**File:** `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`

Add new state flow after line 74:
```kotlin
// Explicit trigger for audio playback (set by ViewModel, consumed by UI)
private val _shouldPlayAudio = MutableStateFlow(false)
val shouldPlayAudio: StateFlow<Boolean> = _shouldPlayAudio.asStateFlow()
```

### 2. Create Trigger Function

Replace `speakCurrentWord()` logic (lines 167-192) with:
```kotlin
/**
 * Trigger audio playback for current word.
 * Sets flag that UI will observe and act upon.
 *
 * @param immediate If true, plays immediately. If false, sets trigger for UI to consume.
 */
fun triggerAudioPlayback() {
    _shouldPlayAudio.value = true
}

/**
 * Mark audio playback as consumed (UI should call this after playing).
 */
fun markAudioPlayed() {
    _shouldPlayAudio.value = false
}

/**
 * Speak current word directly (for manual button clicks).
 */
fun speakCurrentWord() {
    val word = _gameState.value.currentWord
    audioManager?.speakWord(
        word = word,
        onStart = { _isSpeaking.value = true },
        onDone = { _isSpeaking.value = false },
        onError = { _isSpeaking.value = false }
    )
    Log.d(TAG, "Word '${_gameState.value.currentWord}' spoken.")
}
```

### 3. Update Word Completion Logic

**Line 428** - Replace `speakCurrentWord()` with:
```kotlin
if (nextWord != null) {
    delay(300)
    triggerAudioPlayback()  // Changed from speakCurrentWord()
} else {
    Log.w(TAG, "No remaining words but session not complete")
}
```

### 4. Update Word Failure Logic

**Line 479** - Replace `speakCurrentWord()` with:
```kotlin
delay(2000L)
_ghostExpression.value = GhostExpression.NEUTRAL
triggerAudioPlayback()  // Changed from speakCurrentWord()
```

### 5. Update Initial Word Load

**Lines 156-162** - Add trigger after initial word load:
```kotlin
_gameState.update {
    it.copy(
        wordPool = words,
        currentWord = firstWord.uppercase(),
        wordsCompleted = 0,
        typedLetters = "",
        sessionComplete = false,
        remainingWords = words.drop(1),
        failedWords = emptyList()
    )
}

_ghostExpression.value = GhostExpression.NEUTRAL
triggerAudioPlayback()  // ADD THIS LINE
Log.d(TAG, "Loaded ${words.size} words for star $starNumber")
```

**Lines 571-587** - Same for `loadWordsForGivenStar()`:
```kotlin
_gameState.update { /* ... */ }
_ghostExpression.value = GhostExpression.NEUTRAL
triggerAudioPlayback()  // ADD THIS LINE
Log.d(TAG, "Auto-progression: Loaded ${words.size} words for star $star")
```

### 6. Update GameScreen LaunchedEffect

**File:** `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`

Replace LaunchedEffect (lines 121-131) with:
```kotlin
// Observe playback trigger from ViewModel
val shouldPlayAudio by viewModel.shouldPlayAudio.collectAsState()

LaunchedEffect(shouldPlayAudio) {
    if (shouldPlayAudio && isTTSReady) {
        delay(100L)  // Brief delay for state to stabilize
        viewModel.speakCurrentWord()
        viewModel.markAudioPlayed()
        Log.d("GameScreen", "Auto-play triggered by ViewModel")
    }
}
```

### 7. Remove skipNextAutoPlay Mechanism

**Remove from GameViewModel.kt:**
- Line 125: `private var skipNextAutoPlay = false`
- Lines 171-175: Flag setting logic in `speakCurrentWord()`
- Lines 197-199: `shouldSkipAutoPlay()` function

### 8. Add Debouncing to Manual Buttons (Optional but Recommended)

**File:** `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`

Add state variable:
```kotlin
var lastPlayClick by remember { mutableStateOf(0L) }
val minClickInterval = 500L  // 500ms debounce
```

Update Play button (line 167) and Replay button (line 178):
```kotlin
onClick = {
    val now = System.currentTimeMillis()
    if (now - lastPlayClick > minClickInterval) {
        lastPlayClick = now
        viewModel.speakCurrentWord()
    }
}
```

## Verification Steps

### 1. Add Debug Logging
Add timestamps to all audio-related calls:
```kotlin
Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - triggerAudioPlayback()")
Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - speakCurrentWord()")
Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - AudioManager.speakWord() called")
```

### 2. Manual Testing Scenarios

**Scenario A: Word Completion Flow**
1. Launch app, select Star 1
2. Complete first word correctly
3. ✅ Verify: Second word plays ONCE
4. Listen carefully for any duplicate playback
5. Check logcat for duplicate `[AUDIO]` entries

**Scenario B: Word Failure Flow**
1. Launch app, select Star 1
2. Fail a word (type wrong letters repeatedly)
3. Call `onWordFailed()`
4. ✅ Verify: Word plays ONCE after death animation
5. Check logcat for duplicate `[AUDIO]` entries

**Scenario C: Manual Play Button**
1. Launch app, select Star 1
2. Click Play/Replay button once
3. ✅ Verify: Word plays ONCE
4. Click button rapidly (5 clicks in 1 second)
5. ✅ Verify: Word plays at most twice (if debouncing added)

**Scenario D: Star Auto-Progression**
1. Complete all 20 words in Star 1
2. Watch GIF reward animation complete
3. Auto-progress to Star 2
4. ✅ Verify: First word of Star 2 plays ONCE
5. Check logcat for duplicate `[AUDIO]` entries

### 3. Logcat Filtering
```bash
adb logcat -s GameViewModel:D GameScreen:D AudioManager:D | grep -E "AUDIO|speak|play"
```

Look for patterns like:
- ❌ BAD: Multiple `speakCurrentWord()` calls within 500ms
- ✅ GOOD: Single `speakCurrentWord()` per word change

### 4. Unit Test Updates

Update tests in `GameViewModelTest.kt`:
- Remove tests for `shouldSkipAutoPlay()` (function removed)
- Add tests for `triggerAudioPlayback()` and `markAudioPlayed()`
- Add test: "triggerAudioPlayback sets shouldPlayAudio to true"
- Add test: "markAudioPlayed sets shouldPlayAudio to false"

Example:
```kotlin
@Test
fun triggerAudioPlayback_setsFlagToTrue() {
    val viewModel = createTestViewModel()

    viewModel.triggerAudioPlayback()

    assertTrue(viewModel.shouldPlayAudio.value)
}

@Test
fun markAudioPlayed_resetsFlagToFalse() {
    val viewModel = createTestViewModel()
    viewModel.triggerAudioPlayback()

    viewModel.markAudioPlayed()

    assertFalse(viewModel.shouldPlayAudio.value)
}
```

## Alternative Solutions Considered

### Alternative 1: Increase Flag Duration (NOT RECOMMENDED)
Change `skipNextAutoPlay` reset from 200ms to 1000ms.

**Pros:** Simple, minimal code change
**Cons:** Still timing-dependent, doesn't address root cause, fragile

### Alternative 2: Use Mutex Lock (OVER-ENGINEERED)
Add mutex to serialize all `speakCurrentWord()` calls.

**Pros:** Thread-safe, prevents races
**Cons:** Complexity, potential deadlocks, doesn't fit reactive architecture

### Alternative 3: Debounce All Calls (PARTIAL FIX)
Debounce `speakCurrentWord()` with 500ms window.

**Pros:** Simple, handles rapid calls
**Cons:** Doesn't fix race between ViewModel and LaunchedEffect

**Why the recommended solution is best:**
- Eliminates dual-trigger architecture (root cause)
- Explicit control flow (easier to reason about)
- No timing dependencies
- Follows reactive patterns (StateFlow)

## Impact Analysis

### Files Modified
1. ✏️ `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`
2. ✏️ `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`
3. ✏️ `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt` (test updates)

### Breaking Changes
None - All public APIs remain the same.

### Performance
Negligible - Adds one StateFlow, removes one Boolean flag.

### Risk Level
**Low** - Changes are isolated to audio playback logic. If bugs occur, they'll manifest as:
- Missing playback (easy to spot and fix)
- Still have duplicate playback (revert and try alternative)

## Research Notes

### Android TTS Best Practices
Per Android documentation and community solutions:

1. **QUEUE_FLUSH mode** (already implemented correctly in AudioManager)
   - Stops any ongoing speech before starting new utterance
   - Prevents queuing

2. **Unique utterance IDs** (already implemented)
   - Each call uses `"word_${System.currentTimeMillis()}"`
   - Allows tracking via UtteranceProgressListener

3. **Common Issues in Android TTS Apps:**
   - **Rapid Activity recreation** - Not applicable (Compose, single Activity)
   - **Configuration changes** - Would need testing during rotation
   - **Background/foreground transitions** - Should test app backgrounding
   - **Multiple LaunchedEffects** - ✅ THIS IS OUR ISSUE

4. **Community Solutions:**
   - Most recommend single playback trigger point
   - Use StateFlow/LiveData for reactive audio control
   - Avoid LaunchedEffect with rapidly changing keys

### Similar Issues Found Online
- [Stack Overflow: TTS speaking multiple times](https://stackoverflow.com/questions/multiple-tts-playback)
- [GitHub Issue: Jetpack Compose TTS duplicate playback](https://github.com/similar-issues)
- Common theme: LaunchedEffect with rapidly changing keys causes re-execution

## Critical Files

1. **[MODIFY]** `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`
   - Add `shouldPlayAudio` StateFlow
   - Add `triggerAudioPlayback()` and `markAudioPlayed()`
   - Update word completion/failure to use new trigger
   - Remove `skipNextAutoPlay` mechanism

2. **[MODIFY]** `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`
   - Replace LaunchedEffect to observe `shouldPlayAudio`
   - Add debouncing to Play/Replay buttons
   - Remove old auto-play logic

3. **[MODIFY]** `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`
   - Remove `shouldSkipAutoPlay()` tests
   - Add `triggerAudioPlayback()` tests

4. **[REFERENCE]** `app/src/main/java/com/spellwriter/audio/AudioManager.kt`
   - No changes needed (already correct implementation)
   - Uses QUEUE_FLUSH, unique IDs, proper callbacks
