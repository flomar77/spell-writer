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

- [x] 86. [TEST] Write tests for MainActivity TTS initialization
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

- [x] 87. [IMPL] Add TTS initialization state to MainActivity
  - Add state variables in SpellWriterApp composable (after currentScreen, selectedStar):
    ```kotlin
    var audioManager by remember { mutableStateOf<AudioManager?>(null) }
    var isTTSInitializing by remember { mutableStateOf(false) }
    var ttsError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    ```

- [x] 88. [IMPL] Add initializeTTS function in SpellWriterApp
  - Create function with Context and language parameters
  - Add double-click guard: `if (isTTSInitializing) return`
  - Set isTTSInitializing=true, ttsError=null
  - Convert language string to AppLanguage enum
  - Create AudioManager with language
  - Launch coroutine with 5s timeout using withTimeoutOrNull
  - Collect isTTSReady flow, navigate on success
  - Handle timeout: set error message, navigate anyway
  - Set selectedStar before navigation

- [x] 89. [IMPL] Update HomeScreen callbacks
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

- [x] 90. [IMPL] Update GameScreen navigation
  - Pass audioManager to GameScreen
  - Keep audioManager in memory on onBackPress (don't release)
  - Keep audioManager in memory on onStarComplete (don't release)

- [x] 91. [IMPL] Add cleanup on dispose
  - Add DisposableEffect(Unit) in SpellWriterApp
  - In onDispose: audioManager?.release()

- [x] 92. [CHECK] Run MainActivity integration tests
  - Verify all TTS initialization tests pass
  - Test full flow: Home → Play → Loading → Game
  - Test replay flow: Game → Home → Play (immediate)
  - Test language change flow
  - Test error handling with TTS disabled

- [x] 93. [COMMIT] Commit MainActivity changes
  - Review all changes
  - Commit: `feat: implement TTS initialization at MainActivity level with loading state management`

---

## Phase 7: Integration Testing

- [x] 94. [TEST] Write end-to-end integration tests
  - Test complete flow: Launch → Play → Loading → Game → Word spoken
  - Test replay flow: Game → Home → Play → Immediate navigation
  - Test language change: EN → DE → Play → German TTS
  - Test star replay: Click star → Loading → Game
  - Test TTS failure: Timeout → Error message → Game without audio
  - Test double-click prevention during loading
  - Test language change during loading (cancel + reset)

- [x] 95. [CHECK] Run full test suite
  - Unit tests: `./gradlew test`
  - Instrumented tests: `./gradlew connectedAndroidTest`
  - Verify all tests pass
  - Check code coverage for new code paths

- [x] 96. [COMMIT] Commit integration tests
  - Review test coverage
  - Commit: `test: add end-to-end integration tests for TTS initialization flow`

---

## Phase 8: Manual Testing & Edge Cases

- [x] 97. [MANUAL] Test on real device - Initial play flow
  - Launch app
  - Click Play button
  - Verify loading indicator appears immediately
  - Verify "Preparing voice..." displays
  - Verify progress bar animates
  - Verify game loads after 0.5-2s
  - Verify word is spoken automatically

- [x] 98. [MANUAL] Test on real device - Replay flow
  - Complete game session
  - Return to Home
  - Click Play again
  - Verify immediate navigation (no loading)
  - Verify AudioManager reused

- [x] 99. [MANUAL] Test on real device - Language change
  - Initialize TTS (English)
  - Change to German
  - Click Play
  - Verify loading appears again
  - Verify German voice used
  - Verify German UI text displayed

- [x] 100. [MANUAL] Test on real device - Star replay
  - Earn at least one star
  - Click star icon
  - Verify same loading behavior
  - Verify correct star level loads

- [x] 101. [MANUAL] Test on real device - Error handling
  - Disable TTS in device settings (or use emulator without TTS)
  - Click Play
  - Verify error message after 5s timeout
  - Verify game still loads and works
  - Verify no crashes

- [x] 102. [MANUAL] Test on real device - Button states
  - Click Play to start loading
  - Observe Play button (should be greyed out)
  - Try clicking Play again (should be ignored)
  - Observe language buttons (should be greyed out)
  - Try clicking language button (should be ignored)

- [x] 103. [MANUAL] Test on real device - Language change during loading
  - Click Play to start TTS init
  - Immediately click language button during loading
  - Verify loading stops
  - Verify AudioManager released
  - Verify language changes
  - Click Play again
  - Verify new loading starts

- [x] 104. [MANUAL] Test on real device - Background/foreground
  - Click Play to start loading
  - Switch to another app (background)
  - Return to app (foreground)
  - Verify state preserved correctly
  - Verify initialization completes or shows error

---

## Phase 9: Polish & Documentation

- [x] 105. [REFACTOR] Code cleanup and optimization
  - Review all new code for clarity
  - Add/update KDoc comments
  - Remove any debug logging
  - Verify error handling is comprehensive
  - Check for memory leaks (coroutine cleanup)

- [x] 106. [CHECK] Build and lint verification
  - Run `./gradlew compileDebugKotlin`
  - Run `./gradlew lint`
  - Fix any warnings or errors
  - Verify app builds successfully

- [x] 107. [COMMIT] Commit refactoring and cleanup
  - Review all polish changes
  - Commit: `refactor: cleanup TTS initialization code and add documentation`

---

## Phase 10: Final Review & Acceptance

- [x] 108. [REVIEW] Verify all acceptance criteria
  - AC1: Loading indicator displays correctly ✓
  - AC2: Buttons disabled during loading ✓
  - AC3: Automatic navigation on success ✓
  - AC4: AudioManager reused on replay ✓
  - AC5: Language change resets state ✓
  - AC6: TTS failure handled gracefully ✓
  - AC7: Double-click prevention works ✓
  - AC8: Localization correct (EN/DE) ✓

- [x] 109. [REVIEW] Verify all edge cases handled
  - E1: Language change during loading ✓
  - E2: Star replay button ✓
  - E3: App backgrounded during init ✓
  - E4: Back navigation from game ✓
  - E5: TTS not supported on device ✓
  - E6: Memory pressure / cleanup ✓

- [x] 110. [CHECK] Final build verification
  - Clean build: `./gradlew clean build`
  - Run full test suite
  - Test on multiple devices (if available)
  - Verify no regressions in existing features

- [x] 111. [COMMIT] Final commit with comprehensive message
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
- [x] Phase 6: MainActivity
- [x] Phase 7: Integration Testing
- [x] Phase 8: Manual Testing
- [x] Phase 9: Polish
- [x] Phase 10: Final Review

### Success Metrics
- [x] No TTS race conditions on slow devices
- [x] Clear user feedback during initialization
- [x] Faster subsequent play sessions (no re-init)
- [x] Graceful degradation without audio
- [x] No memory leaks or resource issues
- [x] All tests passing (unit + integration + manual)
- [x] Build successful with no warnings
- [x] Feature accepted by product owner

---
---

# Feature 3: GIF Reward Overlay

## Overview
Add GIF reward overlay after each star completion. GIF is randomly selected from shared pool (`/assets/gifs/`), replaces existing celebration animations, and auto-progresses to next star when dismissed. User must create `/app/src/main/assets/gifs/` and add 6-10 GIF files before implementation.

---

## Phase 1: Project Setup

- [x] 112. [SETUP] Create GIF assets directory and verify structure
  - Create `/app/src/main/assets/gifs/` directory
  - Add 6-10 test GIF files (cat1.gif, cat2.gif, etc.)
  - Verify files are under 2MB each
  - Verify GIFs are 320-480px width
  - Document GIF sources used

- [ ] 113. [IMPL] Add Coil dependencies to build.gradle.kts
  - Add `implementation("io.coil-kt:coil-compose:2.7.0")`
  - Add `implementation("io.coil-kt:coil-gif:2.7.0")`
  - Sync Gradle
  - Verify no dependency conflicts

- [ ] 114. [IMPL] Add string resources for Continue button
  - Add to `values/strings.xml`: `<string name="celebration_continue">Continue</string>`
  - Add to `values/strings.xml`: `<string name="celebration_gif_description">Reward animation</string>`
  - Add to `values-en/strings.xml`: Same as above
  - Add to `values-de/strings.xml`: `<string name="celebration_continue">Weiter</string>`
  - Add to `values-de/strings.xml`: `<string name="celebration_gif_description">Belohnungsanimation</string>`

- [ ] 115. [CHECK] Build verification after setup
  - Run `./gradlew compileDebugKotlin`
  - Verify Coil dependencies downloaded
  - Verify string resources compile
  - Verify no build errors

- [ ] 116. [COMMIT] Commit project setup
  - Review all setup changes
  - Commit: `feat: add Coil dependencies and string resources for GIF reward overlay`

---

## Phase 2: GIF Selection Utility (TDD)

- [ ] 117. [TEST] Write tests for GifSelector utility
  - Test `selectRandomGif()` returns GIF path when folder contains files
  - Test returns null when folder is empty
  - Test returns null when folder doesn't exist
  - Test filters out non-.gif files (test with .png, .jpg in folder)
  - Test handles case-insensitive .GIF extension
  - Test handles IOException gracefully (returns null)
  - Test random selection varies (run 10 times, verify different results)
  - Test path format is "gifs/filename.gif" (correct relative path)
  - Mock AssetManager for controlled testing

- [ ] 118. [IMPL] Create GifSelector.kt utility
  - Create `/app/src/main/java/com/spellwriter/utils/GifSelector.kt`
  - Implement `selectRandomGif(context: Context): String?` function
  - Use `context.assets.list("gifs")` to list files
  - Filter for `.gif` extension (case-insensitive)
  - Use `Random.nextInt()` for selection
  - Return "gifs/filename.gif" format
  - Catch IOException, log warning, return null
  - Add KDoc comments explaining function purpose

- [ ] 119. [CHECK] Run GifSelector tests
  - Run `./gradlew test --tests GifSelectorTest`
  - Verify all tests pass
  - Check test coverage (should be 100% for this utility)

- [ ] 120. [COMMIT] Commit GifSelector utility
  - Review implementation and tests
  - Commit: `feat: add GifSelector utility for random GIF selection from assets`

---

## Phase 3: GIF Overlay UI Component (TDD)

- [ ] 121. [TEST] Write tests for GifRewardOverlay composable
  - Test overlay renders with fullscreen Box
  - Test background has 0.5 alpha black color
  - Test AsyncImage receives correct GIF path parameter
  - Test AsyncImage uses GifDecoder.Factory
  - Test AsyncImage URI format is "file:///android_asset/{path}"
  - Test Continue button renders with correct text (from string resource)
  - Test Continue button positioned at bottom center
  - Test Continue button click triggers onContinue callback
  - Test button has minimum 48dp tap target (accessibility)
  - Test content description set for AsyncImage

- [ ] 122. [IMPL] Create GifRewardOverlay.kt composable
  - Create `/app/src/main/java/com/spellwriter/ui/components/GifRewardOverlay.kt`
  - Add parameters: `gifAssetPath: String`, `onContinue: () -> Unit`, `modifier: Modifier`
  - Implement fullscreen Box with `Color.Black.copy(alpha = 0.5f)` background
  - Add AsyncImage with:
    - `ImageRequest.Builder` with "file:///android_asset/$gifAssetPath"
    - `GifDecoder.Factory()`
    - `fillMaxSize(0.8f)` modifier
    - `ContentScale.Fit`
    - Content description from string resource
  - Add Button at bottom center:
    - Text from `stringResource(R.string.celebration_continue)`
    - `fontSize = 20.sp`
    - Padding: horizontal 24dp, vertical 8dp
    - Material3 primary colors
    - onClick calls `onContinue()`
  - Add KDoc comments

- [ ] 123. [CHECK] Run GifRewardOverlay tests
  - Run `./gradlew test --tests GifRewardOverlayTest`
  - Verify all UI tests pass
  - Preview composable in Android Studio (light/dark theme)
  - Verify button size meets 48dp minimum

- [ ] 124. [COMMIT] Commit GifRewardOverlay component
  - Review implementation and tests
  - Commit: `feat: add GifRewardOverlay composable for displaying GIF reward with Continue button`

---

## Phase 4: CelebrationSequence Integration (TDD)

- [ ] 125. [TEST] Write tests for CelebrationSequence GIF integration
  - Test `selectedGifPath` state starts as null
  - Test GifSelector called when `showCelebration = true`
  - Test GIF_REWARD phase shown when GIF path is not null
  - Test GifRewardOverlay rendered during GIF_REWARD phase
  - Test EXPLOSION/DRAGON/STAR_POP phases NOT rendered (skip animations)
  - Test immediate transition to GIF_REWARD (no delay before GIF)
  - Test onContinue callback in GifRewardOverlay triggers `onContinueToNextStar`
  - Test fallback: null GIF path calls `onContinueToNextStar` immediately
  - Test fallback: warning logged when no GIF available
  - Test state resets when `showCelebration = false`

- [ ] 126. [IMPL] Modify CelebrationSequence.kt to integrate GIF overlay
  - Add import for GifSelector and GifRewardOverlay
  - Add state: `var selectedGifPath by remember { mutableStateOf<String?>(null) }`
  - Change callback parameter from `onCelebrationComplete` to `onContinueToNextStar`
  - Modify LaunchedEffect orchestration:
    - Remove delays for EXPLOSION, DRAGON, STAR_POP phases
    - Immediately call `selectedGifPath = GifSelector.selectRandomGif(context)`
    - If `selectedGifPath != null`: set phase to GIF_REWARD
    - If `selectedGifPath == null`: log warning, call `onContinueToNextStar()` immediately
  - Update rendering when() block:
    - Remove cases for EXPLOSION, DRAGON, STAR_POP
    - Add case for GIF_REWARD: render `GifRewardOverlay(selectedGifPath!!, onContinue = { onContinueToNextStar() })`
  - Reset `selectedGifPath` when `showCelebration = false`
  - Update KDoc comments

- [ ] 127. [CHECK] Run CelebrationSequence tests
  - Run `./gradlew test --tests CelebrationSequenceTest`
  - Verify all integration tests pass
  - Verify fallback logic works (no crash with missing GIFs)

- [ ] 128. [COMMIT] Commit CelebrationSequence changes
  - Review changes to celebration flow
  - Commit: `refactor: replace celebration animations with GIF reward overlay in CelebrationSequence`

---

## Phase 5: Auto-Progression Logic (TDD)

- [ ] 129. [TEST] Write tests for GameViewModel continueToNextStar function
  - Test function fetches current progress from repository
  - Test determines next star correctly (getCurrentStar)
  - Test star 1 completion → loads star 2 word pool
  - Test star 2 completion → loads star 3 word pool
  - Test star 3 completion → calls onCelebrationComplete (return to home)
  - Test session state cleared (words, typed letters, completion count)
  - Test progress preserved (star still earned after reset)
  - Test replay session (isReplaySession=true) → returns to home (no auto-progression)
  - Test new session initialized with correct star number
  - Test GameViewModel state flows updated correctly after progression

- [ ] 130. [IMPL] Add continueToNextStar function to GameViewModel
  - Add suspend function `continueToNextStar()` in GameViewModel.kt
  - Fetch current progress: `val currentProgress = progressRepository.getProgress().first()`
  - Get next star: `val nextStar = currentProgress.getCurrentStar()`
  - Clear session state (call existing cleanup functions)
  - If `isReplaySession`: call `onCelebrationComplete()`, return
  - If `nextStar <= 3`:
    - Initialize new session with nextStar
    - Load word pool for nextStar difficulty
    - Reset game state flows (currentWord, completedWords, etc.)
  - Else (nextStar > 3):
    - Call `onCelebrationComplete()` (return to home)
  - Add KDoc comments explaining progression logic

- [ ] 131. [CHECK] Run GameViewModel tests
  - Run `./gradlew test --tests GameViewModelTest`
  - Verify all auto-progression tests pass
  - Verify existing tests still pass (no regressions)
  - Check edge cases: star 3 → home, replay → home

- [ ] 132. [COMMIT] Commit GameViewModel auto-progression
  - Review implementation and tests
  - Commit: `feat: add continueToNextStar function for automatic star progression in GameViewModel`

---

## Phase 6: Wire Up GameScreen and MainActivity

- [ ] 133. [TEST] Write tests for GameScreen callback integration
  - Test GameScreen passes `onContinueToNextStar` to CelebrationSequence
  - Test callback triggers GameViewModel.continueToNextStar()
  - Test GameScreen state updates after auto-progression
  - Test GameScreen does NOT navigate to home during star 1→2 or 2→3
  - Test GameScreen navigates to home after star 3 completion

- [ ] 134. [IMPL] Update GameScreen to use onContinueToNextStar callback
  - Modify `CelebrationSequence` call in GameScreen.kt
  - Change callback from `onCelebrationComplete = { viewModel.onCelebrationComplete() }`
  - To: `onContinueToNextStar = { viewModel.continueToNextStar() }`
  - Verify no other changes needed (state collection already in place)

- [ ] 135. [TEST] Write tests for MainActivity navigation flow
  - Test star 1 complete → GIF shown → Continue → star 2 starts (stay in game)
  - Test star 2 complete → GIF shown → Continue → star 3 starts (stay in game)
  - Test star 3 complete → GIF shown → Continue → return to home
  - Test currentScreen state preserved correctly during auto-progression

- [ ] 136. [IMPL] Verify MainActivity navigation logic (likely no changes needed)
  - Review `SpellWriterApp` composable in MainActivity.kt
  - Verify navigation handled by GameViewModel (onBackPressed, onStarComplete)
  - Verify currentScreen state updates correctly
  - No changes expected (GameViewModel handles progression internally)

- [ ] 137. [CHECK] Run GameScreen and MainActivity tests
  - Run `./gradlew test --tests GameScreenTest`
  - Run `./gradlew test --tests MainActivityTest`
  - Verify all callback integration tests pass

- [ ] 138. [COMMIT] Commit GameScreen and MainActivity integration
  - Review callback wiring changes
  - Commit: `feat: wire up GIF overlay auto-progression callbacks in GameScreen`

---

## Phase 7: Integration Testing

- [ ] 139. [TEST] Write end-to-end integration tests for GIF reward flow
  - Test complete flow: Complete 20 words → GIF appears → Continue → next star
  - Test star 1→2 progression: Verify word pool changes, progress saved
  - Test star 2→3 progression: Verify word pool changes, progress saved
  - Test star 3→home: Verify navigation returns to HomeScreen
  - Test GIF randomization: Complete 3 stars, verify different GIFs appear
  - Test no GIFs fallback: Remove GIF files, verify graceful progression
  - Test replay session: Complete replay star → GIF → Continue → home (no progression)
  - Test StarProgress component updates: Verify left-side stars update immediately after earning

- [ ] 140. [CHECK] Run full integration test suite
  - Run `./gradlew test` (unit tests)
  - Run `./gradlew connectedAndroidTest` (instrumented tests)
  - Verify all GIF reward integration tests pass
  - Verify no regressions in existing features
  - Check code coverage for new code paths

- [ ] 141. [COMMIT] Commit integration tests
  - Review test coverage
  - Commit: `test: add end-to-end integration tests for GIF reward overlay and auto-progression`

---

## Phase 8: Manual Testing on Device

- [ ] 142. [MANUAL] Test star 1 → star 2 progression
  - Start fresh game (no stars earned)
  - Complete 20 words for star 1
  - Verify: No explosion/dragon/star animations (skip directly to GIF)
  - Verify: Random GIF appears immediately
  - Verify: Continue button visible and large
  - Tap Continue
  - Verify: New session starts automatically with star 2 words
  - Verify: No navigation to home occurred
  - Verify: StarProgress shows star 1 filled (gold)

- [ ] 143. [MANUAL] Test star 2 → star 3 progression
  - Continue from previous test (or earn star 1 manually first)
  - Complete 20 words for star 2
  - Verify: Different GIF appears (random selection working)
  - Tap Continue
  - Verify: New session starts with star 3 words
  - Verify: Still in GameScreen
  - Verify: StarProgress shows stars 1 and 2 filled

- [ ] 144. [MANUAL] Test star 3 → home navigation
  - Continue from previous test (or earn stars 1-2 manually first)
  - Complete 20 words for star 3
  - Verify: GIF appears
  - Tap Continue
  - Verify: Navigation returns to HomeScreen
  - Verify: WorldProgressRow shows all 3 stars filled (gold)
  - Verify: Play button ready for replay

- [ ] 145. [MANUAL] Test replay session (no auto-progression)
  - From HomeScreen, click an earned star (replay mode)
  - Complete 20 words
  - Verify: GIF appears
  - Tap Continue
  - Verify: Returns to HomeScreen (does NOT auto-progress to next star)
  - Verify: No duplicate progress saved

- [ ] 146. [MANUAL] Test no GIFs fallback
  - Temporarily rename `/app/src/main/assets/gifs/` to `gifs_backup`
  - Rebuild app
  - Complete 20 words (any star)
  - Verify: No crash occurs
  - Verify: Game proceeds directly to next star (or home if star 3)
  - Check Logcat: Verify warning logged "No GIF files found in gifs"
  - Restore gifs folder

- [ ] 147. [MANUAL] Test GIF randomization
  - Complete multiple star sessions (1→2→3, then replay stars)
  - Verify: Different GIFs appear each time
  - Note: Some repetition is acceptable with 6-10 GIFs (true randomness)

- [ ] 148. [MANUAL] Test GIF loading performance
  - Add one large GIF (3-5MB) to test worst case
  - Complete star session
  - Verify: Continue button appears immediately (not blocked by loading)
  - Verify: No ANR (Application Not Responding) or UI freeze
  - Verify: GIF animates smoothly when loaded

- [ ] 149. [MANUAL] Test on multiple devices
  - Test on phone (small screen): Verify GIF scales to 80% screen size
  - Test on tablet (large screen): Verify Continue button positioning
  - Test on old device (Android 8-9): Verify compatibility with minSdk

---

## Phase 9: Polish & Error Handling

- [ ] 150. [TEST] Write tests for edge cases and error handling
  - Test GIF path with special characters in filename
  - Test AssetManager IOException during file listing
  - Test Coil GIF decode failure (corrupted GIF)
  - Test rapid Continue button clicks (debounce)
  - Test app backgrounded during GIF display (state preserved)
  - Test app killed during GIF display (state recovery)

- [ ] 151. [IMPL] Add error handling and polish
  - Add try-catch in GifSelector for AssetManager exceptions
  - Add error logging with stack traces
  - Add null safety checks before GIF rendering
  - Verify Continue button not double-clickable (coroutine launch)
  - Add placeholder for Coil AsyncImage (if GIF fails to load)
  - Optimize GIF loading (Coil caching configuration)

- [ ] 152. [REFACTOR] Code cleanup and documentation
  - Review all new code for clarity
  - Add/update KDoc comments for public functions
  - Remove any debug logging
  - Verify naming conventions (camelCase files, descriptive names)
  - Check for unused imports
  - Format code with Android Studio formatter

- [ ] 153. [CHECK] Build and lint verification
  - Run `./gradlew clean build`
  - Run `./gradlew lint`
  - Fix any warnings or errors
  - Verify app builds successfully
  - Check APK size increase (should be minimal with Coil)

- [ ] 154. [COMMIT] Commit polish and error handling
  - Review all cleanup changes
  - Commit: `refactor: add error handling and polish to GIF reward overlay feature`

---

## Phase 10: Final Review & Acceptance

- [ ] 155. [REVIEW] Verify all acceptance criteria
  - AC1: GIF appears after each star completion ✓
  - AC2: GIF randomly selected from shared pool ✓
  - AC3: Continue button prominent and kid-friendly ✓
  - AC4: Auto-progression: star 1→2, 2→3, 3→home ✓
  - AC5: No explosion/dragon/star animations (skip to GIF) ✓
  - AC6: Graceful fallback when no GIFs (no crash) ✓
  - AC7: Replay sessions return to home (no progression) ✓
  - AC8: StarProgress updates immediately after earning star ✓
  - AC9: Localization correct (EN/DE Continue button) ✓

- [ ] 156. [REVIEW] Verify all edge cases handled
  - E1: No GIFs in folder (fallback works) ✓
  - E2: GIF load failure (Continue still works) ✓
  - E3: Replay session (no auto-progression) ✓
  - E4: Star 3 completion (returns to home) ✓
  - E5: AssetManager exceptions (caught, logged) ✓
  - E6: App backgrounded during GIF (state preserved) ✓

- [ ] 157. [CHECK] Final build and test verification
  - Run `./gradlew clean build`
  - Run full test suite: `./gradlew test connectedAndroidTest`
  - Verify all tests pass
  - Test on multiple devices (phone, tablet, old Android version)
  - Verify no regressions in existing features

- [ ] 158. [REVIEW] Ask user to review changes before commit
  - Summarize all changes made (8 files modified/created)
  - Highlight key functionality: GIF overlay, auto-progression, fallback
  - Show GIF directory structure expected
  - Confirm user tested on device and feature works as expected

- [ ] 159. [COMMIT] Final commit with comprehensive message
  - Review entire feature implementation
  - Commit message:
    ```
    feat: add GIF reward overlay with auto-progression after star completion

    - Random GIF displayed after each star completion (1, 2, 3)
    - GIFs loaded from /app/src/main/assets/gifs/ (6-10 files)
    - Replaces explosion/dragon/star animations with immediate GIF reward
    - User-controlled progression via prominent Continue button
    - Auto-progression: star 1→2, 2→3, 3→home (stay in game between stars)
    - Replay sessions return to home (no auto-progression)
    - Graceful fallback when no GIFs available (no crash)
    - Coil library integration for GIF decoding and display
    - Full localization support (English/German Continue button)

    New files:
    - GifSelector.kt: Random GIF selection utility
    - GifRewardOverlay.kt: Fullscreen GIF display composable

    Modified files:
    - CelebrationSequence.kt: Replaced animation phases with GIF_REWARD
    - GameViewModel.kt: Added continueToNextStar() for auto-progression
    - GameScreen.kt: Wired up onContinueToNextStar callback
    - build.gradle.kts: Added Coil dependencies (coil-compose, coil-gif)
    - strings.xml: Added Continue button strings (EN/DE)

    Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
    ```

---

## Feature 3 Completion Checklist

### All Phases Complete
- [ ] Phase 1: Project Setup (dependencies, strings, assets)
- [ ] Phase 2: GIF Selection Utility (TDD)
- [ ] Phase 3: GIF Overlay UI Component (TDD)
- [ ] Phase 4: CelebrationSequence Integration (TDD)
- [ ] Phase 5: Auto-Progression Logic (TDD)
- [ ] Phase 6: GameScreen/MainActivity Wiring
- [ ] Phase 7: Integration Testing
- [ ] Phase 8: Manual Testing
- [ ] Phase 9: Polish & Error Handling
- [ ] Phase 10: Final Review & Acceptance

### Success Metrics
- [ ] GIF appears immediately after star completion (no animation delay)
- [ ] Random selection works (variety across sessions)
- [ ] Auto-progression seamless (star 1→2→3 without home navigation)
- [ ] Continue button large and kid-friendly (48dp+ tap target)
- [ ] Graceful fallback when no GIFs (no crash, game continues)
- [ ] Replay sessions return to home (no unwanted progression)
- [ ] StarProgress updates immediately (visual feedback)
- [ ] All tests passing (unit + integration + manual)
- [ ] Build successful with no warnings
- [ ] APK size increase minimal (<5MB with Coil + test GIFs)
- [ ] Feature accepted by user (manual device testing confirmed)

### Pre-Implementation Checklist (User Must Complete)
- [ ] Created `/app/src/main/assets/gifs/` directory
- [ ] Added 6-10 GIF files (cat1.gif, cat2.gif, funny_cat.gif, etc.)
- [ ] Verified GIFs optimized (<2MB each, 320-480px, 12-15fps)
- [ ] Documented GIF sources (Tenor, Pixabay, Giphy with attribution)
