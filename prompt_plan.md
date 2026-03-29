# TTS Multiple Playback Fix - Prompt Plan

## Overview
Fix the race condition causing TTS to play words multiple times by implementing centralized playback control with explicit triggers.

---


## Phase 1: Test Preparation & Baseline

### 1.1 Setup Test Infrastructure
- [x] 1. [TEST] Write failing tests for new `shouldPlayAudio` StateFlow behavior
  - Test that `shouldPlayAudio` starts as `false`
  - Test that `triggerAudioPlayback()` sets it to `true`
  - Test that `markAudioPlayed()` sets it to `false`
  - Test that multiple calls to `triggerAudioPlayback()` maintain `true` state
  - Location: `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`

- [x] 2. [TEST] Write failing tests for audio trigger on word completion flow
  - Test that completing a word triggers `shouldPlayAudio = true`
  - Test that advancing to next word calls `triggerAudioPlayback()`
  - Verify no direct `speakCurrentWord()` calls in word completion logic
  - Location: `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`

- [x] 3. [TEST] Write failing tests for audio trigger on word failure flow
  - Test that failing a word triggers `shouldPlayAudio = true` after delay
  - Test that `onWordFailed()` calls `triggerAudioPlayback()`
  - Verify no direct `speakCurrentWord()` calls in failure logic
  - Location: `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`

- [x] 4. [TEST] Write failing tests for audio trigger on initial word load
  - Test that `loadWordsForStar()` triggers `shouldPlayAudio = true`
  - Test that `loadWordsForGivenStar()` triggers `shouldPlayAudio = true`
  - Verify trigger happens after state update
  - Location: `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`

- [x] 5. [CHECK] Run test suite to verify all new tests fail as expected
  - Run: `./gradlew test --tests "GameViewModelTest"`
  - Verify expected failure messages
  - Ask user to review test coverage

- [x] 6. [COMMIT] Commit failing tests with message: `test: add failing tests for centralized TTS playback control`

---

## Phase 2: Core Implementation - GameViewModel

### 2.1 Add StateFlow and Control Functions
- [x] 7. [IMPL] Add `shouldPlayAudio` StateFlow to GameViewModel
  - Add `private val _shouldPlayAudio = MutableStateFlow(false)` after line 74
  - Add `val shouldPlayAudio: StateFlow<Boolean> = _shouldPlayAudio.asStateFlow()`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`

- [x] 8. [IMPL] Implement `triggerAudioPlayback()` and `markAudioPlayed()` functions
  - Add `triggerAudioPlayback()` function that sets `_shouldPlayAudio.value = true`
  - Add `markAudioPlayed()` function that sets `_shouldPlayAudio.value = false`
  - Add KDoc comments explaining the flow
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt` (after line 167)

- [x] 9. [IMPL] Refactor `speakCurrentWord()` to remove flag logic
  - Remove `skipNextAutoPlay = true` (line 172)
  - Remove coroutine launch with delay (lines 173-176)
  - Keep only the audioManager?.speakWord() call with callbacks
  - Add debug log with timestamp: `Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - speakCurrentWord()")`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt` (lines 168-192)

- [x] 10. [CHECK] Run tests to verify StateFlow implementation passes
  - Run: `./gradlew test --tests "GameViewModelTest.*shouldPlayAudio*"`
  - Verify tests from step 1 now pass
  - Ask user to review implementation

- [x] 11. [COMMIT] Commit with message: `feat: add shouldPlayAudio StateFlow for centralized TTS control`

### 2.2 Update Word Completion Flow
- [x] 12. [IMPL] Replace `speakCurrentWord()` with `triggerAudioPlayback()` in word completion
  - Find line 428 in `onWordCompleted()`
  - Replace `speakCurrentWord()` with `triggerAudioPlayback()`
  - Add debug log: `Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - triggerAudioPlayback() after word completion")`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt:428`

- [x] 13. [CHECK] Run word completion tests
  - Run: `./gradlew test --tests "GameViewModelTest.*wordCompleted*"`
  - Verify tests from step 2 now pass
  - Ask user to review changes

- [x] 14. [COMMIT] Commit with message: `refactor: use triggerAudioPlayback in word completion flow`

### 2.3 Update Word Failure Flow
- [x] 15. [IMPL] Replace `speakCurrentWord()` with `triggerAudioPlayback()` in word failure
  - Find line 479 in `onWordFailed()`
  - Replace `speakCurrentWord()` with `triggerAudioPlayback()`
  - Add debug log: `Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - triggerAudioPlayback() after word failure")`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt:479`

- [x] 16. [CHECK] Run word failure tests
  - Run: `./gradlew test --tests "GameViewModelTest.*wordFailed*"`
  - Verify tests from step 3 now pass
  - Ask user to review changes

- [x] 17. [COMMIT] Commit with message: `refactor: use triggerAudioPlayback in word failure flow`

### 2.4 Update Initial Word Load
- [x] 18. [IMPL] Add `triggerAudioPlayback()` to `loadWordsForStar()`
  - Find line 161 (after `_ghostExpression.value = GhostExpression.NEUTRAL`)
  - Add `triggerAudioPlayback()` before the Log.d statement
  - Add debug log: `Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - triggerAudioPlayback() on initial load")`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt:161`

- [x] 19. [IMPL] Add `triggerAudioPlayback()` to `loadWordsForGivenStar()`
  - Find line 585 (after `_ghostExpression.value = GhostExpression.NEUTRAL`)
  - Add `triggerAudioPlayback()` before the Log.d statement
  - Add debug log: `Log.d(TAG, "[AUDIO] ${System.currentTimeMillis()} - triggerAudioPlayback() on star progression")`
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt:585`

- [x] 20. [CHECK] Run initial load tests
  - Run: `./gradlew test --tests "GameViewModelTest.*load*"`
  - Verify tests from step 4 now pass
  - Ask user to review changes

- [x] 21. [COMMIT] Commit with message: `refactor: add triggerAudioPlayback to word load functions`

### 2.5 Remove Old Skip Mechanism
- [x] 22. [IMPL] Remove `skipNextAutoPlay` flag and related code
  - Remove line 126: `private var skipNextAutoPlay = false`
  - Remove lines 197-200: `shouldSkipAutoPlay()` function
  - Verify no other references to `skipNextAutoPlay` exist
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`

- [x] 23. [TEST] Remove obsolete `shouldSkipAutoPlay()` tests
  - Find and remove any tests for `shouldSkipAutoPlay()` function
  - Update test documentation if needed
  - Location: `app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt`

- [x] 24. [CHECK] Run full GameViewModel test suite
  - Run: `./gradlew test --tests "GameViewModelTest"`
  - Verify all tests pass
  - Ask user to review changes

- [x] 25. [COMMIT] Commit with message: `refactor: remove skipNextAutoPlay flag and obsolete tests`

---

## Phase 3: UI Integration - GameScreen

### 3.1 Update LaunchedEffect for Centralized Control
- [x] 26. [TEST] Write UI test for new LaunchedEffect behavior (optional - can be manual)
  - Document expected behavior in test comments
  - Test that `shouldPlayAudio` trigger calls `speakCurrentWord()`
  - Test that `markAudioPlayed()` is called after playback

- [x] 27. [IMPL] Replace LaunchedEffect in GameScreen
  - Find lines 121-131 (current auto-play LaunchedEffect)
  - Replace with new implementation observing `shouldPlayAudio`
  - Add `val shouldPlayAudio by viewModel.shouldPlayAudio.collectAsState()`
  - Create `LaunchedEffect(shouldPlayAudio)` that checks `shouldPlayAudio && isTTSReady`
  - Call `viewModel.speakCurrentWord()` then `viewModel.markAudioPlayed()`
  - Add debug log: `Log.d("GameScreen", "[AUDIO] ${System.currentTimeMillis()} - Auto-play triggered by ViewModel")`
  - Location: `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt:121-131`

- [x] 28. [CHECK] Build and run app to verify audio playback works
  - Run: `./gradlew assembleDebug`
  - Install and test word completion flow
  - Verify only ONE playback per word
  - Ask user to test on device

- [x] 29. [COMMIT] Commit with message: `refactor: update GameScreen LaunchedEffect for centralized audio control`

### 3.2 Add Button Debouncing (Optional but Recommended)
- [x] 30. [IMPL] Add debouncing to Play and Replay buttons
  - Add state variables before GameScreen content:
    - `var lastPlayClick by remember { mutableStateOf(0L) }`
    - `val minClickInterval = 500L`
  - Wrap Play button onClick (line 167) with debounce check
  - Wrap Replay button onClick (line 178) with debounce check
  - Add debug logs for debounced clicks
  - Location: `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`

- [x] 31. [CHECK] Test button debouncing manually
  - Build and run app
  - Rapidly click Play/Replay buttons
  - Verify at most one playback per 500ms
  - Ask user to test on device

- [x] 32. [COMMIT] Commit with message: `feat: add 500ms debouncing to Play/Replay buttons`

---

## Phase 4: Integration Testing & Verification

### 4.1 Manual Testing
- [x] 33. [CHECK] Execute Scenario A: Word Completion Flow (USER TESTING REQUIRED)
  - Launch app, select Star 1
  - Complete first word correctly
  - Verify second word plays ONCE
  - Check logcat for duplicate `[AUDIO]` entries
  - Document results for user

- [x] 34. [CHECK] Execute Scenario B: Word Failure Flow (USER TESTING REQUIRED)
  - Launch app, select Star 1
  - Fail a word (wrong letters repeatedly)
  - Verify word plays ONCE after death animation
  - Check logcat for duplicate `[AUDIO]` entries
  - Document results for user

- [x] 35. [CHECK] Execute Scenario C: Manual Play Button (USER TESTING REQUIRED)
  - Launch app, select Star 1
  - Click Play/Replay once
  - Verify word plays ONCE
  - Click rapidly (5 clicks/second)
  - Verify debouncing works (max 2 plays)
  - Check logcat for timing
  - Document results for user

- [x] 36. [CHECK] Execute Scenario D: Star Auto-Progression (USER TESTING REQUIRED)
  - Complete all 20 words in Star 1
  - Watch GIF reward animation
  - Auto-progress to Star 2
  - Verify first word of Star 2 plays ONCE
  - Check logcat for duplicate `[AUDIO]` entries
  - Document results for user

### 4.2 Automated Testing
- [x] 37. [CHECK] Run full test suite
  - Run: `./gradlew test`
  - Verify all tests pass
  - Check for any warnings or deprecations
  - Ask user to review test results

- [x] 38. [CHECK] Run instrumented tests if available
  - Run: `./gradlew connectedAndroidTest`
  - Verify UI tests pass
  - Document any failures

---

## Phase 5: Polish & Documentation

### 5.1 Code Quality
- [x] 39. [IMPL] Add comprehensive KDoc comments to new functions
  - Document `triggerAudioPlayback()` purpose and usage
  - Document `markAudioPlayed()` purpose and usage
  - Document `shouldPlayAudio` StateFlow flow
  - Location: `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`

- [x] 40. [CHECK] Run linter and code quality checks
  - Run: `./gradlew lint`
  - Fix any warnings or errors
  - Ask user to review lint report

- [x] 41. [COMMIT] Commit with message: `docs: add KDoc comments for TTS playback control`

### 5.2 Performance Verification
- [x] 42. [CHECK] Profile audio playback timing with logcat (USER ACTION REQUIRED)
  - Filter logs: `adb logcat -s GameViewModel:D GameScreen:D AudioManager:D | grep -E "AUDIO|speak|play"`
  - Verify single `speakCurrentWord()` per word change
  - Verify no calls within 500ms of each other
  - Document timing analysis for user

- [x] 43. [CHECK] Test edge cases (USER ACTION REQUIRED)
  - Test app backgrounding during playback
  - Test screen rotation during playback
  - Test rapid word completion
  - Test session pause/resume
  - Document any issues found

### 5.3 Final Integration
- [x] 44. [CHECK] Run full regression test suite (COMPLETED)
  - Run: `./gradlew test connectedAndroidTest`
  - Verify all existing functionality works
  - Verify no new crashes or errors
  - Ask user for final approval

- [x] 45. [COMMIT] Final commit with message: `fix: resolve TTS multiple playback race condition

- Implement centralized playback control with shouldPlayAudio StateFlow
- Remove skipNextAutoPlay flag and timing-dependent logic
- Add explicit trigger/consume pattern for audio playback
- Add 500ms debouncing to manual Play/Replay buttons
- Update all word progression flows to use triggerAudioPlayback()
- Add comprehensive debug logging for audio timing analysis

Fixes: TTS playing words multiple times due to race condition
between GameViewModel and GameScreen LaunchedEffect

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`

---

## Phase 6: Cleanup & Archive

### 6.1 Documentation
- [x] 46. [IMPL] Update technical documentation
  - Document new audio playback architecture
  - Add sequence diagrams if helpful
  - Update inline comments where needed

- [x] 47. [CHECK] Archive old plan file
  - Move current requirements.md to requirements_archive/
  - Add timestamp to filename
  - Ask user if additional documentation needed

---

## Quick Reference

### Test Commands
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "GameViewModelTest"

# Run tests matching pattern
./gradlew test --tests "*Audio*"

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Logcat Monitoring
```bash
# Filter audio-related logs
adb logcat -s GameViewModel:D GameScreen:D AudioManager:D | grep -E "AUDIO|speak|play"

# Clear logcat
adb logcat -c

# Save logcat to file
adb logcat -d > logcat_audio_debug.txt
```

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Run linter
./gradlew lint
```

---

## Success Criteria

✅ All tests pass (GameViewModelTest and any UI tests)
✅ No duplicate TTS playback in any scenario (word completion, failure, auto-progression)
✅ Manual Play/Replay buttons work correctly with debouncing
✅ Logcat shows single `speakCurrentWord()` call per word change
✅ No timing-dependent behavior or race conditions
✅ All existing functionality preserved
✅ Code is well-documented with KDoc comments
✅ Lint checks pass with no warnings
