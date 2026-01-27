# TDD Prompt Plan: SpellWriter Features

## Table of Contents
1. [Hint Letters After 5 Consecutive Incorrect Attempts](#feature-1-hint-letters) ✅ COMPLETED
2. [Move TTS Initialization to HomeScreen](#feature-2-tts-initialization) 🚧 IN PROGRESS

---

# Feature 1: Hint Letters After 5 Consecutive Incorrect Attempts

## Phase 1: Data Model Setup

### Feature: HintState data class

- [x] 22. [TEST] Write tests for HintState data class that verify:
  - HintState holds letter (Char) and positionIndex (Int)
  - Can be created with valid values
  - Equals/hashCode work correctly for state comparisons

- [x] 23. [IMPL] Add HintState data class to GameState.kt with letter and positionIndex properties

- [x] 24. [IMPL] Add hintState: HintState? field to GameState data class with default null

- [x] 25. [CHECK] Run full test suite and verify GameState compilation

- [x] 26. [COMMIT] Commit with message `feat: add HintState model to GameState for hint letter tracking` if user agreed

## Phase 2: ViewModel Logic

### Feature: Consecutive failure tracking and hint triggering

- [x] 27. [TEST] Write tests for hint letter logic in GameViewModel that verify:
  - Counter increments on incorrect letter
  - Counter resets to 0 on correct letter
  - Hint shows (hintState is set) after 5 consecutive failures
  - Hint contains correct letter and position
  - Counter resets after showing hint
  - Position out of bounds is handled safely

- [x] 28. [IMPL] Add consecutiveFailuresAtCurrentPosition variable to GameViewModel

- [x] 29. [IMPL] Update handleIncorrectLetter() to increment counter and trigger hint at 5 failures

- [x] 30. [IMPL] Update handleCorrectLetter() to reset counter to 0

- [x] 31. [IMPL] Add showHintLetter() method with bounds checking and state update

- [x] 32. [CHECK] Run tests and verify hint triggering logic works correctly

- [x] 33. [COMMIT] Commit with message `feat: add consecutive failure tracking and hint triggering logic` if user agreed

### Feature: Hint auto-clear after timeout

- [x] 34. [TEST] Write tests for hint auto-clear that verify:
  - Hint clears after 2000ms delay
  - clearHintLetter() sets hintState to null
  - Multiple rapid hints don't cause state corruption

- [x] 35. [IMPL] Add clearHintLetter() method to GameViewModel

- [x] 36. [IMPL] Update showHintLetter() to launch coroutine with 2000ms delay then clear

- [x] 37. [CHECK] Run tests with coroutine timing verification

- [x] 38. [COMMIT] Commit with message `feat: auto-clear hint letter after 2 second display` if user agreed

### Feature: Hint clearing on word transitions

- [x] 39. [TEST] Write tests for hint state cleanup that verify:
  - Hint clears when word completes
  - Hint clears when word fails
  - Counter resets on word transitions
  - No hint persists across words

- [x] 40. [IMPL] Update onWordCompleted() to clear hintState and reset counter

- [x] 41. [IMPL] Update onWordFailed() to clear hintState and reset counter

- [x] 42. [CHECK] Run full ViewModel test suite

- [x] 43. [COMMIT] Commit with message `feat: clear hint state on word completion and failure` if user agreed

## Phase 3: UI Implementation

### Feature: Grimoire hint letter display

- [x] 44. [TEST] Write UI tests for Grimoire hint display that verify:
  - Hint letter displays at correct position
  - Hint letter has grey color with 60% alpha
  - Hint doesn't interfere with typed letters
  - Display length extends to include hint position
  - AnimatedVisibility triggers for hints

- [x] 45. [IMPL] Add hintState parameter to Grimoire composable signature

- [x] 46. [IMPL] Update letter display logic to handle both typed letters and hint letters

- [x] 47. [IMPL] Add AnimatedVisibility with fadeIn/fadeOut for hint letters

- [x] 48. [IMPL] Apply grey color with alpha to hint letter text

- [x] 49. [CHECK] Run UI tests and verify visual rendering

- [x] 50. [COMMIT] Commit with message `feat: add grey hint letter display in Grimoire with fade animations` if user agreed

### Feature: GameScreen integration

- [x] 51. [TEST] Write integration tests that verify:
  - Grimoire receives hintState from gameState
  - Hint appears after 5 wrong letters
  - Hint displays in grey
  - Hint disappears after timeout
  - Typed letters work normally alongside hints

- [x] 52. [IMPL] Update Grimoire call in GameScreen to pass gameState.hintState

- [x] 53. [CHECK] Run full integration test suite

- [x] 54. [COMMIT] Commit with message `feat: integrate hint state from ViewModel to Grimoire display` if user agreed

## Phase 4: Edge Cases and Polish

### Feature: Edge case handling

- [x] 55. [TEST] Write edge case tests that verify:
  - Position out of bounds doesn't crash
  - Rapid typing doesn't corrupt state
  - Multiple hints at same position work correctly
  - Word change during hint display doesn't leak state

- [x] 56. [IMPL] Add/verify bounds checking in showHintLetter()

- [x] 57. [IMPL] Ensure state transitions are atomic and safe

- [x] 58. [CHECK] Run all edge case tests

- [x] 59. [COMMIT] Commit with message `fix: add bounds checking and state safety for hint letters` if user agreed

### Feature: Final polish and verification

- [x] 60. [CLEANUP] Add missing imports (fadeOut animation)

- [x] 61. [CLEANUP] Verify all logging statements are appropriate

- [x] 62. [CHECK] Run complete test suite (`./gradlew test` and `./gradlew connectedAndroidTest`)

- [x] 63. [COMMIT] Commit with message `chore: polish hint letter implementation` if user agreed

## Phase 5: Manual Testing and Documentation

- [x] 64. [VERIFY] Manual device/emulator testing:
  - Start game with word "CAT"
  - Type 'Z' five times → grey 'C' appears
  - Verify fade in animation (300ms)
  - Verify hint displays for ~2 seconds
  - Verify fade out animation (500ms)
  - Type 'C' (correct) → counter resets
  - Type 'Z' four times → no hint
  - Type fifth 'Z' → grey 'A' appears
  - Complete word → hint clears
  - Test at different positions (0, 1, 2+)
  - Verify no crashes or memory leaks

- [x] 65. [DOCS] Update inline code comments to document hint feature

- [x] 66. [COMMIT] Final commit with message `docs: document hint letter feature in code comments` if needed

## Summary

Total prompts: 66 (45 new for hint feature)
- Data Model: 5 prompts
- ViewModel Logic: 17 prompts
- UI Implementation: 11 prompts
- Edge Cases: 6 prompts
- Manual Testing: 3 prompts
- Final verification: 3 prompts

## Feature Completion Criteria

✅ Unit tests pass for:
- HintState model
- Consecutive failure tracking
- Hint triggering at 5 failures
- Counter reset on correct letter
- Hint auto-clear after 2s
- Hint clearing on word transitions

✅ UI tests pass for:
- Grey hint letter display
- Correct position rendering
- Fade animations
- Integration with typed letters

✅ Manual verification confirms:
- Visual appearance (grey with fade)
- Timing (2s display)
- Counter behavior
- Edge cases handled

---

# Feature 2: TTS Initialization to HomeScreen

## Overview
Move TTS initialization from GameViewModel to HomeScreen with loading indicator, error handling, and AudioManager lifecycle management at MainActivity level.

## Phase 1: String Resources Setup

- [ ] 67. [IMPL] Add TTS loading and error string resources
  - Add `home_tts_loading` and `home_tts_error` to values/strings.xml
  - Add German translations to values-de/strings.xml
  - Verify strings compile and are accessible

- [ ] 68. [CHECK] Build verification
  - Run `./gradlew compileDebugKotlin`
  - Verify no compilation errors

- [ ] 69. [COMMIT] Commit string resources
  - Review all changes
  - Commit: `feat: add TTS initialization string resources for loading and error states`

---

## Phase 2: LanguageSwitcher Component Update

- [ ] 70. [TEST] Write tests for LanguageSwitcher enabled/disabled states
  - Test button enabled by default
  - Test buttons disabled when enabled=false parameter passed
  - Test click events blocked when disabled
  - Test visual state (color) changes when disabled

- [ ] 71. [IMPL] Add enabled parameter to LanguageSwitcher
  - Add `enabled: Boolean = true` parameter to LanguageSwitcher composable
  - Add `enabled: Boolean = true` parameter to LanguageButton composable
  - Pass enabled to Button's enabled property in both language buttons
  - Combine with existing isSelected logic

- [ ] 72. [CHECK] Run LanguageSwitcher tests
  - Verify all new tests pass
  - Verify existing functionality unchanged

- [ ] 73. [COMMIT] Commit LanguageSwitcher changes
  - Review changes
  - Commit: `feat: add enabled parameter to LanguageSwitcher for loading state control`

---

## Phase 3: GameViewModel AudioManager Injection

- [ ] 74. [TEST] Write tests for GameViewModel with injected AudioManager
  - Test GameViewModel with null audioManager (game works without audio)
  - Test GameViewModel with valid audioManager (TTS functions work)
  - Test isTTSReady returns false when audioManager is null
  - Test speakCurrentWord handles null audioManager gracefully
  - Test audio playback (success/error sounds) with null audioManager

- [ ] 75. [IMPL] Modify GameViewModel to accept AudioManager parameter
  - Add `audioManager: AudioManager? = null` constructor parameter
  - Remove line 114: `private val audioManager = AudioManager(context, _currentLanguage.value)`
  - Make audioManager a constructor property
  - Update `isTTSReady` exposure to handle null:
    ```kotlin
    val isTTSReady: StateFlow<Boolean> = audioManager?.isTTSReady
        ?: MutableStateFlow(false).asStateFlow()
    ```
  - Verify all audioManager usages handle null (safe calls already in place)

- [ ] 76. [CHECK] Run GameViewModel tests
  - Verify all new tests pass
  - Verify existing tests still pass with default null parameter
  - Test game flow works without AudioManager

- [ ] 77. [COMMIT] Commit GameViewModel changes
  - Review changes
  - Commit: `refactor: convert GameViewModel to accept injected AudioManager instead of creating internally`

---

## Phase 4: GameScreen AudioManager Parameter

- [ ] 78. [TEST] Write tests for GameScreen with AudioManager parameter
  - Test GameScreen renders with null audioManager
  - Test GameScreen passes audioManager to GameViewModel
  - Test GameViewModel receives correct audioManager instance
  - Test remember() key includes audioManager for proper recomposition

- [ ] 79. [IMPL] Add audioManager parameter to GameScreen
  - Add `audioManager: AudioManager? = null` parameter to GameScreen composable
  - Update GameViewModel instantiation (line ~70):
    ```kotlin
    val viewModel = remember(starNumber, isReplaySession, audioManager) {
        GameViewModel(
            context = context,
            starNumber = starNumber,
            isReplaySession = isReplaySession,
            progressRepository = progressRepository,
            initialProgress = currentProgress,
            audioManager = audioManager
        )
    }
    ```

- [ ] 80. [CHECK] Run GameScreen tests
  - Verify all new tests pass
  - Verify GameScreen renders correctly with and without audioManager
  - Test navigation flow unchanged

- [ ] 81. [COMMIT] Commit GameScreen changes
  - Review changes
  - Commit: `feat: add audioManager parameter to GameScreen for dependency injection`

---

## Phase 5: HomeScreen Loading UI

- [ ] 82. [TEST] Write tests for HomeScreen loading states
  - Test loading indicator appears when isTTSInitializing=true
  - Test loading text displays correct string resource
  - Test LinearProgressIndicator renders during loading
  - Test Play button disabled when isTTSInitializing=true
  - Test Play button enabled when isTTSInitializing=false
  - Test error message displays when ttsError is not null
  - Test error message hidden when ttsError is null
  - Test LanguageSwitcher receives enabled=false during loading

- [ ] 83. [IMPL] Add loading UI to HomeScreen
  - Add parameters: `isTTSInitializing: Boolean = false`, `ttsError: String? = null`
  - Add loading indicator after WorldProgressRow (before Play button):
    ```kotlin
    if (isTTSInitializing) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_tts_loading),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    ```
  - Add error message display below loading indicator
  - Update Play button: `enabled = !isTTSInitializing`
  - Update LanguageSwitcher: `LanguageSwitcher(onLanguageChanged, enabled = !isTTSInitializing)`

- [ ] 84. [CHECK] Run HomeScreen tests
  - Verify all loading UI tests pass
  - Verify button states change correctly
  - Verify error messages display correctly
  - Preview HomeScreen in different loading states

- [ ] 85. [COMMIT] Commit HomeScreen changes
  - Review changes
  - Commit: `feat: add TTS loading indicator and error handling to HomeScreen`

---

## Phase 6: MainActivity TTS Initialization Logic

- [ ] 86. [TEST] Write tests for MainActivity TTS initialization
  - Test audioManager state starts as null
  - Test initializeTTS creates AudioManager with correct language
  - Test initializeTTS sets isTTSInitializing=true during init
  - Test initializeTTS sets isTTSInitializing=false after success
  - Test initializeTTS navigates to GameScreen after success
  - Test initializeTTS handles timeout (5s) and shows error
  - Test initializeTTS prevents double-click (guard clause)
  - Test audioManager reused on second Play click (no re-init)
  - Test language change releases AudioManager and resets state
  - Test DisposableEffect releases AudioManager on app dispose

- [ ] 87. [IMPL] Add TTS initialization state to MainActivity
  - Add state variables in SpellWriterApp composable (after currentScreen, selectedStar):
    ```kotlin
    var audioManager by remember { mutableStateOf<AudioManager?>(null) }
    var isTTSInitializing by remember { mutableStateOf(false) }
    var ttsError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    ```

- [ ] 88. [IMPL] Add initializeTTS function in SpellWriterApp
  - Create function with Context and language parameters
  - Add double-click guard: `if (isTTSInitializing) return`
  - Set isTTSInitializing=true, ttsError=null
  - Convert language string to AppLanguage enum
  - Create AudioManager with language
  - Launch coroutine with 5s timeout using withTimeoutOrNull
  - Collect isTTSReady flow, navigate on success
  - Handle timeout: set error message, navigate anyway
  - Set selectedStar before navigation

- [ ] 89. [IMPL] Update HomeScreen callbacks
  - Pass isTTSInitializing and ttsError to HomeScreen
  - Update onPlayClick:
    - Check if audioManager?.isTTSReady?.value == true
    - If yes: navigate immediately
    - If no: call initializeTTS()
  - Update onStarClick with same logic (set selectedStar)
  - Update onLanguageChanged:
    - Release audioManager with audioManager?.release()
    - Reset audioManager to null
    - Reset isTTSInitializing to false
    - Reset ttsError to null
    - Update language and reload progress

- [ ] 90. [IMPL] Update GameScreen navigation
  - Pass audioManager to GameScreen
  - Keep audioManager in memory on onBackPress (don't release)
  - Keep audioManager in memory on onStarComplete (don't release)

- [ ] 91. [IMPL] Add cleanup on dispose
  - Add DisposableEffect(Unit) in SpellWriterApp
  - In onDispose: audioManager?.release()

- [ ] 92. [CHECK] Run MainActivity integration tests
  - Verify all TTS initialization tests pass
  - Test full flow: Home → Play → Loading → Game
  - Test replay flow: Game → Home → Play (immediate)
  - Test language change flow
  - Test error handling with TTS disabled

- [ ] 93. [COMMIT] Commit MainActivity changes
  - Review all changes
  - Commit: `feat: implement TTS initialization at MainActivity level with loading state management`

---

## Phase 7: Integration Testing

- [ ] 94. [TEST] Write end-to-end integration tests
  - Test complete flow: Launch → Play → Loading → Game → Word spoken
  - Test replay flow: Game → Home → Play → Immediate navigation
  - Test language change: EN → DE → Play → German TTS
  - Test star replay: Click star → Loading → Game
  - Test TTS failure: Timeout → Error message → Game without audio
  - Test double-click prevention during loading
  - Test language change during loading (cancel + reset)

- [ ] 95. [CHECK] Run full test suite
  - Unit tests: `./gradlew test`
  - Instrumented tests: `./gradlew connectedAndroidTest`
  - Verify all tests pass
  - Check code coverage for new code paths

- [ ] 96. [COMMIT] Commit integration tests
  - Review test coverage
  - Commit: `test: add end-to-end integration tests for TTS initialization flow`

---

## Phase 8: Manual Testing & Edge Cases

- [ ] 97. [MANUAL] Test on real device - Initial play flow
  - Launch app
  - Click Play button
  - Verify loading indicator appears immediately
  - Verify "Preparing voice..." displays
  - Verify progress bar animates
  - Verify game loads after 0.5-2s
  - Verify word is spoken automatically

- [ ] 98. [MANUAL] Test on real device - Replay flow
  - Complete game session
  - Return to Home
  - Click Play again
  - Verify immediate navigation (no loading)
  - Verify AudioManager reused

- [ ] 99. [MANUAL] Test on real device - Language change
  - Initialize TTS (English)
  - Change to German
  - Click Play
  - Verify loading appears again
  - Verify German voice used
  - Verify German UI text displayed

- [ ] 100. [MANUAL] Test on real device - Star replay
  - Earn at least one star
  - Click star icon
  - Verify same loading behavior
  - Verify correct star level loads

- [ ] 101. [MANUAL] Test on real device - Error handling
  - Disable TTS in device settings (or use emulator without TTS)
  - Click Play
  - Verify error message after 5s timeout
  - Verify game still loads and works
  - Verify no crashes

- [ ] 102. [MANUAL] Test on real device - Button states
  - Click Play to start loading
  - Observe Play button (should be greyed out)
  - Try clicking Play again (should be ignored)
  - Observe language buttons (should be greyed out)
  - Try clicking language button (should be ignored)

- [ ] 103. [MANUAL] Test on real device - Language change during loading
  - Click Play to start TTS init
  - Immediately click language button during loading
  - Verify loading stops
  - Verify AudioManager released
  - Verify language changes
  - Click Play again
  - Verify new loading starts

- [ ] 104. [MANUAL] Test on real device - Background/foreground
  - Click Play to start loading
  - Switch to another app (background)
  - Return to app (foreground)
  - Verify state preserved correctly
  - Verify initialization completes or shows error

---

## Phase 9: Polish & Documentation

- [ ] 105. [REFACTOR] Code cleanup and optimization
  - Review all new code for clarity
  - Add/update KDoc comments
  - Remove any debug logging
  - Verify error handling is comprehensive
  - Check for memory leaks (coroutine cleanup)

- [ ] 106. [CHECK] Build and lint verification
  - Run `./gradlew compileDebugKotlin`
  - Run `./gradlew lint`
  - Fix any warnings or errors
  - Verify app builds successfully

- [ ] 107. [COMMIT] Commit refactoring and cleanup
  - Review all polish changes
  - Commit: `refactor: cleanup TTS initialization code and add documentation`

---

## Phase 10: Final Review & Acceptance

- [ ] 108. [REVIEW] Verify all acceptance criteria
  - AC1: Loading indicator displays correctly ✓
  - AC2: Buttons disabled during loading ✓
  - AC3: Automatic navigation on success ✓
  - AC4: AudioManager reused on replay ✓
  - AC5: Language change resets state ✓
  - AC6: TTS failure handled gracefully ✓
  - AC7: Double-click prevention works ✓
  - AC8: Localization correct (EN/DE) ✓

- [ ] 109. [REVIEW] Verify all edge cases handled
  - E1: Language change during loading ✓
  - E2: Star replay button ✓
  - E3: App backgrounded during init ✓
  - E4: Back navigation from game ✓
  - E5: TTS not supported on device ✓
  - E6: Memory pressure / cleanup ✓

- [ ] 110. [CHECK] Final build verification
  - Clean build: `./gradlew clean build`
  - Run full test suite
  - Test on multiple devices (if available)
  - Verify no regressions in existing features

- [ ] 111. [COMMIT] Final commit with comprehensive message
  - Review entire feature implementation
  - Commit message:
    ```
    feat: move TTS initialization to HomeScreen with loading indicator

    - TTS now initializes on Play button click instead of in GameViewModel
    - Loading indicator shows "Preparing voice..." during initialization
    - Play and language buttons disabled during loading
    - AudioManager kept in memory at MainActivity level for reuse
    - Error message displayed if TTS fails, game still playable
    - Language change releases AudioManager and forces re-initialization
    - Double-click prevention during initialization
    - Full localization support (English/German)

    Fixes race condition on slow devices where word would not speak
    automatically on GameScreen load.

    Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
    ```

---

## Feature 2 Completion Checklist

### All Phases Complete
- [ ] Phase 1: String Resources
- [ ] Phase 2: LanguageSwitcher
- [ ] Phase 3: GameViewModel
- [ ] Phase 4: GameScreen
- [ ] Phase 5: HomeScreen
- [ ] Phase 6: MainActivity
- [ ] Phase 7: Integration Testing
- [ ] Phase 8: Manual Testing
- [ ] Phase 9: Polish
- [ ] Phase 10: Final Review

### Success Metrics
- [ ] No TTS race conditions on slow devices
- [ ] Clear user feedback during initialization
- [ ] Faster subsequent play sessions (no re-init)
- [ ] Graceful degradation without audio
- [ ] No memory leaks or resource issues
- [ ] All tests passing (unit + integration + manual)
- [ ] Build successful with no warnings
- [ ] Feature accepted by product owner
