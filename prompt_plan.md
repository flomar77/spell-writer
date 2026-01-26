# TDD Prompt Plan: Hint Letters After 5 Consecutive Incorrect Attempts

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

- [ ] 27. [TEST] Write tests for hint letter logic in GameViewModel that verify:
  - Counter increments on incorrect letter
  - Counter resets to 0 on correct letter
  - Hint shows (hintState is set) after 5 consecutive failures
  - Hint contains correct letter and position
  - Counter resets after showing hint
  - Position out of bounds is handled safely

- [ ] 28. [IMPL] Add consecutiveFailuresAtCurrentPosition variable to GameViewModel

- [ ] 29. [IMPL] Update handleIncorrectLetter() to increment counter and trigger hint at 5 failures

- [ ] 30. [IMPL] Update handleCorrectLetter() to reset counter to 0

- [ ] 31. [IMPL] Add showHintLetter() method with bounds checking and state update

- [ ] 32. [CHECK] Run tests and verify hint triggering logic works correctly

- [ ] 33. [COMMIT] Commit with message `feat: add consecutive failure tracking and hint triggering logic` if user agreed

### Feature: Hint auto-clear after timeout

- [ ] 34. [TEST] Write tests for hint auto-clear that verify:
  - Hint clears after 2000ms delay
  - clearHintLetter() sets hintState to null
  - Multiple rapid hints don't cause state corruption

- [ ] 35. [IMPL] Add clearHintLetter() method to GameViewModel

- [ ] 36. [IMPL] Update showHintLetter() to launch coroutine with 2000ms delay then clear

- [ ] 37. [CHECK] Run tests with coroutine timing verification

- [ ] 38. [COMMIT] Commit with message `feat: auto-clear hint letter after 2 second display` if user agreed

### Feature: Hint clearing on word transitions

- [ ] 39. [TEST] Write tests for hint state cleanup that verify:
  - Hint clears when word completes
  - Hint clears when word fails
  - Counter resets on word transitions
  - No hint persists across words

- [ ] 40. [IMPL] Update onWordCompleted() to clear hintState and reset counter

- [ ] 41. [IMPL] Update onWordFailed() to clear hintState and reset counter

- [ ] 42. [CHECK] Run full ViewModel test suite

- [ ] 43. [COMMIT] Commit with message `feat: clear hint state on word completion and failure` if user agreed

## Phase 3: UI Implementation

### Feature: Grimoire hint letter display

- [ ] 44. [TEST] Write UI tests for Grimoire hint display that verify:
  - Hint letter displays at correct position
  - Hint letter has grey color with 60% alpha
  - Hint doesn't interfere with typed letters
  - Display length extends to include hint position
  - AnimatedVisibility triggers for hints

- [ ] 45. [IMPL] Add hintState parameter to Grimoire composable signature

- [ ] 46. [IMPL] Update letter display logic to handle both typed letters and hint letters

- [ ] 47. [IMPL] Add AnimatedVisibility with fadeIn/fadeOut for hint letters

- [ ] 48. [IMPL] Apply grey color with alpha to hint letter text

- [ ] 49. [CHECK] Run UI tests and verify visual rendering

- [ ] 50. [COMMIT] Commit with message `feat: add grey hint letter display in Grimoire with fade animations` if user agreed

### Feature: GameScreen integration

- [ ] 51. [TEST] Write integration tests that verify:
  - Grimoire receives hintState from gameState
  - Hint appears after 5 wrong letters
  - Hint displays in grey
  - Hint disappears after timeout
  - Typed letters work normally alongside hints

- [ ] 52. [IMPL] Update Grimoire call in GameScreen to pass gameState.hintState

- [ ] 53. [CHECK] Run full integration test suite

- [ ] 54. [COMMIT] Commit with message `feat: integrate hint state from ViewModel to Grimoire display` if user agreed

## Phase 4: Edge Cases and Polish

### Feature: Edge case handling

- [ ] 55. [TEST] Write edge case tests that verify:
  - Position out of bounds doesn't crash
  - Rapid typing doesn't corrupt state
  - Multiple hints at same position work correctly
  - Word change during hint display doesn't leak state

- [ ] 56. [IMPL] Add/verify bounds checking in showHintLetter()

- [ ] 57. [IMPL] Ensure state transitions are atomic and safe

- [ ] 58. [CHECK] Run all edge case tests

- [ ] 59. [COMMIT] Commit with message `fix: add bounds checking and state safety for hint letters` if user agreed

### Feature: Final polish and verification

- [ ] 60. [CLEANUP] Add missing imports (fadeOut animation)

- [ ] 61. [CLEANUP] Verify all logging statements are appropriate

- [ ] 62. [CHECK] Run complete test suite (`./gradlew test` and `./gradlew connectedAndroidTest`)

- [ ] 63. [COMMIT] Commit with message `chore: polish hint letter implementation` if user agreed

## Phase 5: Manual Testing and Documentation

- [ ] 64. [VERIFY] Manual device/emulator testing:
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

- [ ] 65. [DOCS] Update inline code comments to document hint feature

- [ ] 66. [COMMIT] Final commit with message `docs: document hint letter feature in code comments` if needed

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
