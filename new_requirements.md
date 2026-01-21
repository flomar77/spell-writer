# Requirements

## Keyboard

Replace the custom SpellKeyboard composable with a TextField using the device's built-in keyboard while preserving existing validation behavior.

### Requirements (confirmed)

- Show only correct letters (ignore incorrect visually)
- No backspace allowed (preserve validated letters)
- Disable autocorrect, suggestions, predictive text
- Letter-by-letter validation with immediate feedback

### Verification

1. Run app on device/emulator
2. Start spelling session
3. Verify native keyboard appears
4. Type correct letter → appears in Grimoire, Ghost happy
5. Type incorrect letter → does NOT appear, Ghost unhappy
6. Try backspace → no effect (letters remain)
7. Verify no autocorrect suggestions appear
8. Complete word → advances to next
9. Run existing tests: ./gradlew test    