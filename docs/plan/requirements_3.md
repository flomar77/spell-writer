# Requirements: Move TTS Initialization to HomeScreen

## Objective

Move Text-to-Speech (TTS) initialization from GameViewModel to HomeScreen with loading indicator. Only navigate to GameScreen after TTS is ready. Keep AudioManager in memory between sessions for faster subsequent plays.

## Current State

- TTS initialization happens in GameViewModel constructor (line 114)
- GameScreen waits for TTS ready before speaking word
- Race condition possible on slow devices
- No user feedback during initialization

## Target State

- TTS initialization triggered on Play/Star button click
- Loading indicator displayed at bottom of HomeScreen
- Play button and language switcher disabled during initialization
- AudioManager passed to GameViewModel via constructor
- AudioManager kept in memory at MainActivity level for reuse
- Graceful fallback if TTS fails (show error, allow play without audio)

## Functional Requirements

### FR1: TTS Initialization on Play Button

**Priority:** High

When user clicks Play button or Star replay button:
- If AudioManager already initialized → Navigate immediately
- If AudioManager not initialized → Start initialization, show loading UI

### FR2: Loading Indicator UI

**Priority:** High

Display at bottom of HomeScreen during TTS initialization:
- Text: "Preparing voice..." (localized)
- Animated progress bar (indeterminate)
- Positioned after WorldProgressRow, before Play button

### FR3: Disabled State During Loading

**Priority:** High

During TTS initialization:
- Play button disabled (greyed out)
- Language switcher buttons disabled (greyed out)
- Prevent multiple initialization attempts

### FR4: Error Handling

**Priority:** High

If TTS initialization fails or times out (5 seconds):
- Display error message: "Voice not available. Playing without audio."
- Allow navigation to GameScreen anyway
- Keep null/failed AudioManager (game works without audio)

### FR5: AudioManager Lifecycle

**Priority:** High

- Create AudioManager at MainActivity/SpellWriterApp level
- Initialize lazily on first Play click
- Keep in memory after successful initialization
- Reuse for subsequent Home ↔ Game navigation
- Release on app dispose only

### FR6: Language Change Handling

**Priority:** Medium

When user changes language:
- Release existing AudioManager
- Clear initialization state
- Force re-initialization on next Play click
- Reload progress for new language

### FR7: AudioManager Injection

**Priority:** High

- Pass AudioManager to GameViewModel via constructor
- Make audioManager parameter nullable in GameViewModel
- Update GameScreen to accept and pass audioManager
- Handle null audioManager gracefully in GameViewModel

## Technical Requirements

### TR1: Architecture Changes

**File:** `MainActivity.kt`
- Add state: `audioManager: AudioManager?`
- Add state: `isTTSInitializing: Boolean`
- Add state: `ttsError: String?`
- Add function: `initializeTTS(context, language)`
- Pass audioManager to GameScreen

**File:** `GameViewModel.kt`
- Add constructor parameter: `audioManager: AudioManager? = null`
- Remove line 114: `private val audioManager = AudioManager(...)`
- Handle null audioManager in `isTTSReady` exposure

**File:** `GameScreen.kt`
- Add parameter: `audioManager: AudioManager? = null`
- Pass audioManager to GameViewModel constructor

**File:** `HomeScreen.kt`
- Add parameter: `isTTSInitializing: Boolean = false`
- Add parameter: `ttsError: String? = null`
- Add loading UI component
- Disable Play button during initialization
- Pass initialization state to LanguageSwitcher

**File:** `LanguageSwitcher.kt`
- Add parameter: `enabled: Boolean = true`
- Pass to language buttons

### TR2: TTS Initialization Logic

```kotlin
fun initializeTTS(context: Context, language: String) {
    if (isTTSInitializing) return  // Guard against double-click

    isTTSInitializing = true
    ttsError = null

    // Create AudioManager with language
    audioManager = AudioManager(context, appLanguage)

    // Wait for TTS ready with 5s timeout
    coroutineScope.launch {
        withTimeoutOrNull(5000L) {
            audioManager?.isTTSReady?.collect { ready ->
                if (ready) {
                    isTTSInitializing = false
                    // Navigate to game
                    cancel()
                }
            }
        } ?: run {
            // Timeout - show error but allow play
            isTTSInitializing = false
            ttsError = context.getString(R.string.home_tts_error)
            // Navigate to game anyway
        }
    }
}
```

### TR3: String Resources

**File:** `values/strings.xml`
```xml
<string name="home_tts_loading">Preparing Android voice…</string>
<string name="home_tts_error">Android Voice not available. Playing without audio.</string>
```

**File:** `values-de/strings.xml`
```xml
<string name="home_tts_loading">Android Stimme wird vorbereitet…</string>
<string name="home_tts_error">Android Stimme nicht verfügbar. Spiel ohne Audio.</string>
```

## Acceptance Criteria

### AC1: Loading Indicator Display
- GIVEN user clicks Play button
- WHEN TTS is not yet initialized
- THEN loading indicator appears immediately at bottom
- AND displays "Preparing voice..." text
- AND shows animated progress bar

### AC2: Button Disabled State
- GIVEN TTS initialization is in progress
- WHEN user views HomeScreen
- THEN Play button is greyed out and non-clickable
- AND language switcher buttons are greyed out

### AC3: Automatic Navigation
- GIVEN TTS initialization completes successfully
- WHEN ready state is true
- THEN app automatically navigates to GameScreen
- AND loading indicator disappears

### AC4: AudioManager Reuse
- GIVEN user completed a game session
- WHEN user navigates back to Home and clicks Play again
- THEN navigation is immediate (no loading)
- AND same AudioManager instance is reused

### AC5: Language Change Reset
- GIVEN AudioManager is initialized
- WHEN user changes language
- THEN AudioManager is released
- AND initialization state is reset
- AND next Play click shows loading indicator again

### AC6: TTS Failure Handling
- GIVEN TTS initialization times out after 5s
- WHEN failure occurs
- THEN error message is displayed
- AND user can still navigate to GameScreen
- AND game works without audio

### AC7: Double-Click Prevention
- GIVEN user clicks Play button
- WHEN user rapidly clicks Play again during initialization
- THEN only one initialization process runs
- AND duplicate initializations are ignored

### AC8: Localization
- GIVEN user is in German language mode
- WHEN TTS initializes
- THEN loading text displays "Stimme wird vorbereitet…"
- AND error text displays in German if failure occurs

## Edge Cases

### E1: Language Change During Loading
**Scenario:** User changes language while TTS is initializing
**Expected:** Cancel current initialization, release AudioManager, reset state

### E2: Star Replay Button
**Scenario:** User clicks star replay button instead of Play
**Expected:** Same TTS initialization logic and loading behavior

### E3: App Backgrounded During Init
**Scenario:** User switches apps while TTS initializing
**Expected:** Coroutine continues, state preserved on return

### E4: Back Navigation from Game
**Scenario:** User exits game and returns to Home
**Expected:** AudioManager preserved, no re-initialization needed

### E5: TTS Not Supported on Device
**Scenario:** Device has no TTS engine installed
**Expected:** 5s timeout, error message, game still playable

### E6: Memory Pressure
**Scenario:** System kills app during background
**Expected:** AudioManager cleaned up, fresh initialization on restart

## Non-Functional Requirements

### NFR1: Performance
- TTS initialization typically completes in 0.5-2 seconds
- Maximum timeout: 5 seconds
- No UI blocking during initialization (async)

### NFR2: User Experience
- Immediate visual feedback on Play button click
- Clear loading state indication
- Graceful degradation if TTS unavailable

### NFR3: Memory Management
- AudioManager released on app dispose
- AudioManager released on language change
- No memory leaks from coroutine cleanup

### NFR4: Accessibility
- Loading text screen-reader accessible
- Disabled button states clearly indicated
- Error messages visible and readable

## Verification Steps

### Manual Testing

1. **Initial Play Flow**
   - Launch app → Click Play
   - Verify loading indicator appears
   - Verify "Preparing voice..." text displays
   - Verify game loads after 0.5-2s

2. **Replay Flow**
   - Complete game → Return to Home → Click Play
   - Verify immediate navigation (no loading)

3. **Language Change**
   - Initialize TTS → Change language → Click Play
   - Verify loading indicator appears again
   - Verify correct localized text

4. **Star Replay**
   - Click earned star icon
   - Verify same loading behavior as Play button

5. **Error Handling**
   - Test on device with TTS disabled
   - Verify error message appears after 5s
   - Verify game still loads and works

6. **Double-Click Prevention**
   - Rapidly click Play button multiple times
   - Verify only one initialization runs

7. **Button States**
   - Observe Play button during loading (greyed out)
   - Observe language buttons during loading (greyed out)
   - Try clicking disabled buttons (no effect)

### Build Verification

```bash
./gradlew compileDebugKotlin
```

### Expected Behavior Summary

- ✅ Loading indicator shows immediately on Play
- ✅ Progress bar animates during 0.5-2s initialization
- ✅ Play and language buttons disabled during loading
- ✅ Automatic navigation when TTS ready
- ✅ No loading on subsequent plays
- ✅ Language change forces re-initialization
- ✅ Error message if TTS fails
- ✅ Game playable without TTS

## Critical Files

1. **MainActivity.kt** - AudioManager state, initialization logic
2. **HomeScreen.kt** - Loading UI, disabled states
3. **GameViewModel.kt** - AudioManager injection
4. **GameScreen.kt** - Pass AudioManager to ViewModel
5. **LanguageSwitcher.kt** - Enabled parameter
6. **strings.xml / strings-de.xml** - Loading/error messages

## Dependencies

- Existing AudioManager class
- Existing LanguageManager
- Existing ProgressRepository
- Coroutines (kotlinx.coroutines)
- Compose State management

## Success Metrics

- No TTS race conditions on slow devices
- Clear user feedback during initialization
- Faster subsequent play sessions (no re-init)
- Graceful degradation without audio
- No memory leaks or resource issues
