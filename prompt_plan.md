# TDD Prompt Plan: Native Keyboard Implementation

## Phase 1: Project Setup

- [x] 1. [SETUP] Review existing test infrastructure and ensure Compose UI testing dependencies are configured. Verify `./gradlew test` runs successfully.

## Phase 2: Core Features

### Feature: Letter-by-letter validation with native keyboard input


- [x] 2. [TEST] Write tests for native keyboard input handler that verify:
  - When a correct letter is typed, it is accepted and added to typedLetters
  - When an incorrect letter is typed, it is rejected and typedLetters remains unchanged
  - The onLetterTyped callback is invoked with the new character

- [x] 3. [IMPL] Implement the native keyboard input handling logic in GameScreen to intercept TextField value changes and delegate to existing onLetterTyped validation

- [x] 4. [CHECK] Run full test suite (`./gradlew test`) and ask user to review changes

- [x] 5. [COMMIT] Commit changes with message `feat: add native keyboard input handling with letter validation` if user agreed in step above

### Feature: Backspace prevention

- [x] 6. [TEST] Write tests for backspace prevention that verify:
  - When user attempts to delete a character, typedLetters remains unchanged
  - TextField value is always bound to the validated typedLetters state
  - Only additions (new characters) are processed, deletions are ignored

- [x] 7. [IMPL] Implement backspace prevention by comparing new TextField value length with current typedLetters and only processing when length increases

- [x] 8. [CHECK] Run full test suite and ask user to review changes

- [x] 9. [COMMIT] Commit changes with message `feat: prevent backspace from deleting validated letters` if user agreed in step above

### Feature: Disable autocorrect and suggestions

- [x] 10. [TEST] Write tests for TextField keyboard configuration that verify:
  - KeyboardOptions has autoCorrect set to false
  - KeyboardCapitalization is set to Characters (uppercase)
  - ImeAction is configured appropriately

- [x] 11. [IMPL] Configure TextField with proper KeyboardOptions to disable autocorrect, suggestions, and predictive text while enabling uppercase input

- [x] 12. [CHECK] Run full test suite and ask user to review changes

- [x] 13. [COMMIT] Commit changes with message `feat: configure native keyboard to disable autocorrect and suggestions` if user agreed in step above

## Phase 3: Integration

- [x] 14. [TEST] Write integration tests that verify the complete flow:
  - Native keyboard appears when game starts
  - Typing correct letters shows them in Grimoire and triggers happy Ghost
  - Typing incorrect letters triggers unhappy Ghost without showing in Grimoire
  - Word completion advances to next word
  - Full session can be completed with native keyboard

- [x] 15. [IMPL] Replace SpellKeyboard composable with configured TextField in GameScreen, ensuring all existing feedback mechanisms (Ghost, sounds, Grimoire) work correctly

- [x] 16. [CHECK] Run full test suite and ask user to review changes

- [x] 17. [COMMIT] Commit changes with message `feat: replace custom SpellKeyboard with native keyboard TextField` if user agreed in step above

## Phase 4: Polish

- [ ] 18. [CLEANUP] Remove any unused imports/references. Update any documentation referencing the custom keyboard.

- [ ] 19. [CHECK] Run full test suite and ask user to review changes

- [ ] 20. [COMMIT] Commit changes with message `chore: remove deprecated SpellKeyboard component` if user agreed in step above

- [ ] 21. [VERIFY] Manual verification on device/emulator:
  - Native keyboard appears
  - Correct letter → Grimoire + happy Ghost
  - Incorrect letter → no display + unhappy Ghost
  - Backspace → no effect
  - No autocorrect suggestions
  - Word completion works
  - Run `./gradlew test` one final time

## Summary

Total prompts: 21
- Setup: 1
- Test prompts: 5
- Implementation prompts: 5
- Check prompts: 5
- Commit prompts: 4
- Cleanup: 1
- Final verification: 1
