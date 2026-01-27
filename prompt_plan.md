# Feature 2: TTS Initialization to HomeScreen

## Overview
Move TTS initialization from GameViewModel to HomeScreen with loading indicator, error handling, and AudioManager lifecycle management at MainActivity level.

## Phase 1: String Resources Setup

- [x] 67. [IMPL] Add TTS loading and error string resources
  - Add `home_tts_loading` and `home_tts_error` to values/strings.xml
  - Add German translations to values-de/strings.xml
  - Verify strings compile and are accessible

- [x] 68. [CHECK] Build verification
  - Run `./gradlew compileDebugKotlin`
  - Verify no compilation errors

- [x] 69. [COMMIT] Commit string resources
  - Review all changes
  - Commit: `feat: add TTS initialization string resources for loading and error states`

---

## Phase 2: LanguageSwitcher Component Update

- [x] 70. [TEST] Write tests for LanguageSwitcher enabled/disabled states
  - Test button enabled by default
  - Test buttons disabled when enabled=false parameter passed
  - Test click events blocked when disabled
  - Test visual state (color) changes when disabled

- [x] 71. [IMPL] Add enabled parameter to LanguageSwitcher
  - Add `enabled: Boolean = true` parameter to LanguageSwitcher composable
  - Add `enabled: Boolean = true` parameter to LanguageButton composable
  - Pass enabled to Button's enabled property in both language buttons
  - Combine with existing isSelected logic

- [x] 72. [CHECK] Run LanguageSwitcher tests
  - Verify all new tests pass
  - Verify existing functionality unchanged

- [x] 73. [COMMIT] Commit LanguageSwitcher changes
  - Review changes
  - Commit: `feat: add enabled parameter to LanguageSwitcher for loading state control`

---

## Phase 3: GameViewModel AudioManager Injection

- [x] 74. [TEST] Write tests for GameViewModel with injected AudioManager
  - Test GameViewModel with null audioManager (game works without audio)
  - Test GameViewModel with valid audioManager (TTS functions work)
  - Test isTTSReady returns false when audioManager is null
  - Test speakCurrentWord handles null audioManager gracefully
  - Test audio playback (success/error sounds) with null audioManager

- [x] 75. [IMPL] Modify GameViewModel to accept AudioManager parameter
  - Add `audioManager: AudioManager? = null` constructor parameter
  - Remove line 114: `private val audioManager = AudioManager(context, _currentLanguage.value)`
  - Make audioManager a constructor property
  - Update `isTTSReady` exposure to handle null:
    ```kotlin
    val isTTSReady: StateFlow<Boolean> = audioManager?.isTTSReady
        ?: MutableStateFlow(false).asStateFlow()
    ```
  - Verify all audioManager usages handle null (safe calls already in place)

- [x] 76. [CHECK] Run GameViewModel tests
  - Verify all new tests pass
  - Verify existing tests still pass with default null parameter
  - Test game flow works without AudioManager

- [x] 77. [COMMIT] Commit GameViewModel changes
  - Review changes
  - Commit: `refactor: convert GameViewModel to accept injected AudioManager instead of creating internally`

---

## Phase 4: GameScreen AudioManager Parameter

- [x] 78. [TEST] Write tests for GameScreen with AudioManager parameter
  - Test GameScreen renders with null audioManager
  - Test GameScreen passes audioManager to GameViewModel
  - Test GameViewModel receives correct audioManager instance
  - Test remember() key includes audioManager for proper recomposition

- [x] 79. [IMPL] Add audioManager parameter to GameScreen
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

- [x] 80. [CHECK] Run GameScreen tests
  - Verify all new tests pass
  - Verify GameScreen renders correctly with and without audioManager
  - Test navigation flow unchanged

- [x] 81. [COMMIT] Commit GameScreen changes
  - Review changes
  - Commit: `feat: add audioManager parameter to GameScreen for dependency injection`

---

## Phase 5: HomeScreen Loading UI

- [x] 82. [TEST] Write tests for HomeScreen loading states
  - Test loading indicator appears when isTTSInitializing=true
  - Test loading text displays correct string resource
  - Test LinearProgressIndicator renders during loading
  - Test Play button disabled when isTTSInitializing=true
  - Test Play button enabled when isTTSInitializing=false
  - Test error message displays when ttsError is not null
  - Test error message hidden when ttsError is null
  - Test LanguageSwitcher receives enabled=false during loading

- [x] 83. [IMPL] Add loading UI to HomeScreen
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

- [x] 84. [CHECK] Run HomeScreen tests
  - Verify all loading UI tests pass
  - Verify button states change correctly
  - Verify error messages display correctly
  - Preview HomeScreen in different loading states

- [x] 85. [COMMIT] Commit HomeScreen changes
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
- [x] Phase 1: String Resources
- [x] Phase 2: LanguageSwitcher
- [x] Phase 3: GameViewModel
- [x] Phase 4: GameScreen
- [x] Phase 5: HomeScreen
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
