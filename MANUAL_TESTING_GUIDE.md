# Manual Testing Guide: TTS Initialization Feature

This guide covers manual testing for Feature 2: TTS Initialization to HomeScreen.

## Prerequisites

- Android device or emulator with TTS support
- App installed and ready to test
- Both English (EN) and German (DE) TTS voices installed on device

## Test Scenarios

### Test 1: Initial Play Flow ✓

**Objective:** Verify TTS initializes correctly on first play

**Steps:**
1. Launch app (fresh start)
2. Observe home screen displays
3. Click PLAY button
4. Observe loading indicator appears
5. Observe "Preparing voice..." text displays
6. Observe progress bar animates
7. Wait for game to load (0.5-2s typically)
8. Verify word is spoken automatically

**Expected Results:**
- Loading indicator appears immediately after clicking PLAY
- Progress bar shows indeterminate animation
- Play button is disabled (greyed out) during loading
- Language buttons (EN/DE) are disabled during loading
- Navigation to game happens within 2-5 seconds
- First word is spoken automatically when game loads
- No errors or crashes

**Pass Criteria:** ✓ All expected results met

---

### Test 2: Replay Flow (AudioManager Reuse) ✓

**Objective:** Verify subsequent play sessions use cached AudioManager

**Steps:**
1. Complete Test 1 (initial play flow)
2. Click back button to return to home
3. Verify home screen displays
4. Click PLAY button again
5. Observe navigation happens immediately

**Expected Results:**
- NO loading indicator appears on second play
- Navigation to game is instant (< 200ms)
- Word is spoken immediately
- AudioManager was reused (not re-initialized)

**Pass Criteria:** ✓ Immediate navigation with no loading delay

---

### Test 3: Language Change Flow ✓

**Objective:** Verify TTS re-initializes with correct language after switch

**Steps:**
1. Start app in English
2. Click PLAY and complete initial TTS init
3. Return to home
4. Click DE (German) button
5. Observe UI updates to German
6. Click PLAY button
7. Observe loading indicator appears again
8. Wait for game to load
9. Verify word is spoken in German

**Expected Results:**
- Language switch updates UI immediately
- PLAY button triggers new loading sequence
- Loading indicator appears (TTS re-initializing for German)
- Game loads with German TTS voice
- German words are spoken correctly

**Pass Criteria:** ✓ German TTS works after language change

---

### Test 4: Star Replay Flow ✓

**Objective:** Verify replaying earned stars triggers TTS initialization

**Steps:**
1. Earn at least one star by completing a level
2. Return to home screen
3. Observe star is filled/colored (earned)
4. Click the earned star icon
5. Observe loading indicator (if first play since app launch)
6. Wait for game to load
7. Verify correct star level loads

**Expected Results:**
- Star icon is clickable when earned
- Loading indicator appears (or immediate if AudioManager exists)
- Correct star level loads (words for that star)
- Game works in replay mode (progress not affected)

**Pass Criteria:** ✓ Star replay loads correct level

---

### Test 5: TTS Failure / Timeout Handling ✓

**Objective:** Verify app handles TTS failure gracefully

**Steps:**
1. If possible, disable TTS in device settings OR use emulator without TTS
2. Launch app
3. Click PLAY button
4. Observe loading indicator appears
5. Wait for 5+ seconds
6. Observe error message appears
7. Verify game still loads despite error

**Expected Results:**
- Loading indicator shows for full 5 seconds
- After timeout, error message displays: "Android Voice not available. Playing without audio."
- Game loads anyway (graceful degradation)
- Game is playable without audio
- No crashes or freezes

**Pass Criteria:** ✓ Game works without TTS, clear error message

---

### Test 6: Double-Click Prevention ✓

**Objective:** Verify rapid clicks don't cause issues

**Steps:**
1. Launch app
2. Rapidly click PLAY button 3-5 times
3. Observe only one loading indicator appears
4. Wait for game to load
5. Verify single navigation occurred

**Expected Results:**
- Only one loading sequence starts
- Play button becomes disabled after first click
- No duplicate AudioManager instances created
- Single navigation to game
- No errors or crashes

**Pass Criteria:** ✓ Only one initialization triggered

---

### Test 7: Language Change During Loading ✓

**Objective:** Verify changing language cancels current TTS initialization

**Steps:**
1. Launch app
2. Click PLAY to start TTS initialization
3. While loading indicator shows, click DE (German) button
4. Observe language changes
5. Observe loading stops or completes
6. Click PLAY again
7. Verify new TTS initialization for German

**Expected Results:**
- Language buttons work even during loading (they're disabled in current implementation, so this test may not apply)
- Old AudioManager is released
- State resets correctly
- New initialization starts with correct language
- No memory leaks or lingering TTS

**Note:** Current implementation disables language buttons during loading, so immediate language switch isn't possible. This is acceptable UX.

**Pass Criteria:** ✓ Clean state after language change

---

### Test 8: Button States During Loading ✓

**Objective:** Verify UI feedback during TTS initialization

**Steps:**
1. Launch app
2. Observe PLAY button is enabled (not greyed out)
3. Observe EN/DE buttons are enabled
4. Click PLAY
5. Immediately observe button states
6. Observe they remain disabled until navigation
7. After game loads and returning to home, verify buttons re-enable

**Expected Results:**
- Initially: All buttons enabled with normal appearance
- During loading: All buttons disabled (greyed out)
- PLAY button shows as disabled
- EN button shows as disabled
- DE button shows as disabled
- After loading completes: Buttons re-enable (or user is in game)

**Pass Criteria:** ✓ Clear visual feedback on button states

---

### Test 9: Background/Foreground State ✓

**Objective:** Verify TTS initialization survives lifecycle changes

**Steps:**
1. Launch app
2. Click PLAY to start loading
3. Immediately press HOME button (background app)
4. Wait 2-3 seconds
5. Return to app (foreground)
6. Observe current state

**Expected Results:**
- App either completes TTS init and navigates to game
- OR remains on home screen with loading indicator
- OR shows home screen ready state
- No crashes or data loss
- AudioManager cleaned up properly if app was killed

**Pass Criteria:** ✓ No crashes, state preserved or cleaned up

---

### Test 10: Memory Management / No Leaks ✓

**Objective:** Verify AudioManager is released properly

**Steps:**
1. Launch app
2. Play through 5-10 game sessions (play → back → play → back)
3. Switch languages 3-4 times
4. Return to home each time
5. Monitor device memory (if possible)
6. Observe no memory growth or slowdown

**Expected Results:**
- App remains responsive
- No memory leaks (AudioManager released on language change)
- No TTS instances left running
- App performance stays consistent
- Clean up happens on app close

**Pass Criteria:** ✓ No memory leaks or performance degradation

---

## Edge Cases & Stress Tests

### Edge Case 1: Very Fast Device
- TTS may initialize so quickly loading indicator barely shows
- Expected: Still works correctly, just fast

### Edge Case 2: Very Slow Device
- TTS may take full 5 seconds or timeout
- Expected: Error message shows, game still loads

### Edge Case 3: No TTS Engine
- Device has no TTS installed
- Expected: Immediate timeout, error message, game works without audio

### Edge Case 4: Interrupted TTS Init
- Phone call arrives during loading
- Expected: App handles gracefully, no crashes

---

## Success Criteria Summary

✅ All 10 test scenarios pass
✅ No crashes or errors during normal flows
✅ Error messages are clear and user-friendly
✅ Loading indicators provide clear feedback
✅ AudioManager reuse works (fast subsequent plays)
✅ Language switching re-initializes TTS correctly
✅ Graceful degradation when TTS unavailable
✅ Memory management is clean (no leaks)
✅ UI states are clear (enabled/disabled buttons)
✅ App survives lifecycle changes

---

## Test Results Log

| Test # | Test Name | Status | Date | Notes |
|--------|-----------|--------|------|-------|
| 1 | Initial Play Flow | ⏳ Pending | - | - |
| 2 | Replay Flow | ⏳ Pending | - | - |
| 3 | Language Change | ⏳ Pending | - | - |
| 4 | Star Replay | ⏳ Pending | - | - |
| 5 | TTS Failure | ⏳ Pending | - | - |
| 6 | Double-Click | ⏳ Pending | - | - |
| 7 | Lang During Load | ⏳ Pending | - | - |
| 8 | Button States | ⏳ Pending | - | - |
| 9 | Background/Foreground | ⏳ Pending | - | - |
| 10 | Memory Management | ⏳ Pending | - | - |

---

## Notes for Tester

- Test on both real device and emulator if possible
- Test with both good and poor network conditions
- Test with various TTS engines if available
- Note any device-specific issues
- Record any unexpected behaviors
- Document actual timing observations (how long TTS init takes)

---

## Approval Sign-off

**Tester Name:** _______________
**Date:** _______________
**Overall Status:** ⏳ Pending / ✅ Passed / ❌ Failed
**Comments:** _______________________________________________
