# TDD Prompt Plan: Native Keyboard Implementation

## Phase 1: Project Setup

- [x] 1. [SETUP] Review existing test infrastructure and ensure Compose UI testing dependencies are configured. Verify `./gradlew test` runs successfully.

## Phase 2: Core Features

### Feature: Letter-by-letter validation with native keyboard input


- [x] 2. [TEST] Write tests for native keyboard input handler that verify:
  - When a correct letter is typed, it is accepted and added to typedLetters
  - When an incorrect letter is typed, it is rejected and typedLetters remains unchanged
  - The onLetterTyped callback is invoked with the new character

- [ ] 3. [IMPL] Implement the native keyboard input handling logic in GameScreen to intercept TextField value changes and delegate to existing onLetterTyped validation

- [ ] 4. [CHECK] Run full test suite (`./gradlew test`) and commit changes with message `feat: add native keyboard input handling with letter validation`

### Feature: Backspace prevention

- [ ] 5. [TEST] Write tests for backspace prevention that verify:
  - When user attempts to delete a character, typedLetters remains unchanged
  - TextField value is always bound to the validated typedLetters state
  - Only additions (new characters) are processed, deletions are ignored

- [ ] 6. [IMPL] Implement backspace prevention by comparing new TextField value length with current typedLetters and only processing when length increases

- [ ] 7. [CHECK] Run full test suite and commit changes with message `feat: prevent backspace from deleting validated letters`

### Feature: Disable autocorrect and suggestions

- [ ] 8. [TEST] Write tests for TextField keyboard configuration that verify:
  - KeyboardOptions has autoCorrect set to false
  - KeyboardCapitalization is set to Characters (uppercase)
  - ImeAction is configured appropriately

- [ ] 9. [IMPL] Configure TextField with proper KeyboardOptions to disable autocorrect, suggestions, and predictive text while enabling uppercase input

- [ ] 10. [CHECK] Run full test suite and commit changes with message `feat: configure native keyboard to disable autocorrect and suggestions`

## Phase 3: Integration

- [ ] 11. [TEST] Write integration tests that verify the complete flow:
  - Native keyboard appears when game starts
  - Typing correct letters shows them in Grimoire and triggers happy Ghost
  - Typing incorrect letters triggers unhappy Ghost without showing in Grimoire
  - Word completion advances to next word
  - Full session can be completed with native keyboard

- [ ] 12. [IMPL] Replace SpellKeyboard composable with configured TextField in GameScreen, ensuring all existing feedback mechanisms (Ghost, sounds, Grimoire) work correctly

- [ ] 13. [CHECK] Run full test suite and commit changes with message `feat: replace custom SpellKeyboard with native keyboard TextField`

## Phase 4: Polish

- [ ] 14. [CLEANUP] Remove SpellKeyboard.kt file and any unused imports/references. Update any documentation referencing the custom keyboard.

- [ ] 15. [CHECK] Run full test suite and commit changes with message `chore: remove deprecated SpellKeyboard component`

- [ ] 16. [VERIFY] Manual verification on device/emulator:
  - Native keyboard appears
  - Correct letter → Grimoire + happy Ghost
  - Incorrect letter → no display + unhappy Ghost
  - Backspace → no effect
  - No autocorrect suggestions
  - Word completion works
  - Run `./gradlew test` one final time

## Summary

Total prompts: 16
- Setup: 1
- Test prompts: 5
- Implementation prompts: 5
- Check/commit prompts: 5
- Cleanup: 1
- Final verification: 1
