# Plan: Hint Letters After 5 Consecutive Incorrect Attempts

## Objective
Briefly show hint letter (grey, fade in and **[create-prompt-plan.md](.claude/commands/my/create-prompt-plan.md)**out) at current position after 5 consecutive incorrect attempts to help young kids.

## Implementation Strategy

**State Management:**
- Add `HintState(letter: Char, positionIndex: Int)` to GameState
- Track `consecutiveFailuresAtCurrentPosition: Int` in ViewModel (transient)
- Reset counter on: correct letter typed, hint shown, word changes

**UI Display:**
- Grimoire accepts `hintState: HintState?` parameter
- Hint displays as grey letter at correct position
- Animation: 300ms fade-in, 2000ms display, 500ms fade-out, auto-clear

**Logic Flow:**
1. Incorrect letter → increment counter
2. Counter reaches 5 → show hint, reset counter
3. Correct letter → reset counter
4. Hint auto-clears after 2000ms

## Changes

### 1. GameState.kt
**File:** `app/src/main/java/com/spellwriter/data/models/GameState.kt`

Add HintState data class (after line 15):
```kotlin
data class HintState(
    val letter: Char,
    val positionIndex: Int
)
```

Add field to GameState (around line 28):
```kotlin
data class GameState(
    // ... existing fields ...
    val hintState: HintState? = null
)
```

### 2. GameViewModel.kt
**File:** `app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt`

Add tracking variable (after line 92):
```kotlin
private var consecutiveFailuresAtCurrentPosition = 0
```

Update `handleIncorrectLetter()` (around line 204-212):
```kotlin
private fun handleIncorrectLetter(letter: Char) {
    Log.d(TAG, "Incorrect letter: $letter (expected: ${_gameState.value.currentWord[_gameState.value.typedLetters.length]})")

    wordPerformanceTracker.recordIncorrectAttempt()
    consecutiveFailuresAtCurrentPosition++

    if (consecutiveFailuresAtCurrentPosition >= 5) {
        showHintLetter()
    }

    setGhostExpression(GhostExpression.UNHAPPY)
    audioManager.playError()
}
```

Update `handleCorrectLetter()` to reset counter (around line 183):
```kotlin
private fun handleCorrectLetter(letter: Char) {
    Log.d(TAG, "Correct letter: $letter")

    wordPerformanceTracker.recordCorrectAttempt()
    consecutiveFailuresAtCurrentPosition = 0  // NEW

    // ... rest of existing code
}
```

Add new methods:
```kotlin
private fun showHintLetter() {
    val currentWord = _gameState.value.currentWord
    val currentPosition = _gameState.value.typedLetters.length

    if (currentPosition >= currentWord.length) {
        Log.w(TAG, "Cannot show hint - position out of bounds")
        return
    }

    val hintLetter = currentWord[currentPosition]
    consecutiveFailuresAtCurrentPosition = 0

    _gameState.update {
        it.copy(hintState = HintState(hintLetter, currentPosition))
    }

    viewModelScope.launch {
        delay(2000L)
        clearHintLetter()
    }
}

private fun clearHintLetter() {
    _gameState.update { it.copy(hintState = null) }
}
```

Update `onWordCompleted()` (around line 298):
```kotlin
viewModelScope.launch {
    delay(WORD_COMPLETE_DISPLAY_DELAY_MS)

    consecutiveFailuresAtCurrentPosition = 0  // NEW

    _gameState.update {
        it.copy(
            // ... existing fields ...
            hintState = null  // NEW
        )
    }
}
```

Update `onWordFailed()` (around line 357):
```kotlin
_gameState.update {
    it.copy(
        // ... existing fields ...
        hintState = null  // NEW
    )
}
consecutiveFailuresAtCurrentPosition = 0  // NEW
```

### 3. Grimoire.kt
**File:** `app/src/main/java/com/spellwriter/ui/components/Grimoire.kt`

Add import:
```kotlin
import com.spellwriter.data.models.HintState
import androidx.compose.animation.fadeOut
```

Update signature (line 38):
```kotlin
@Composable
fun Grimoire(
    typedLetters: String,
    hintState: HintState? = null,  // NEW
    modifier: Modifier = Modifier
)
```

Replace letter display section (lines 66-88):
```kotlin
} else {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val displayLength = if (hintState != null) {
            maxOf(typedLetters.length, hintState.positionIndex + 1)
        } else {
            typedLetters.length
        }

        repeat(displayLength) { index ->
            val isHintPosition = hintState != null && index == hintState.positionIndex
            val isTypedPosition = index < typedLetters.length

            when {
                isTypedPosition -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = typedLetters[index].toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
                isHintPosition -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 500))
                    ) {
                        Text(
                            text = hintState.letter.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = Color.Gray.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
            }
        }
    }
}
```

### 4. GameScreen.kt
**File:** `app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt`

Update Grimoire call (around line 159):
```kotlin
Grimoire(
    typedLetters = gameState.typedLetters,
    hintState = gameState.hintState,  // NEW
    modifier = Modifier.fillMaxWidth()
)
```

## Testing

**Manual Testing:**
1. Run app, start game with word "CAT"
2. Type 'Z' five times (wrong letter)
3. Verify grey 'C' appears and fades in/out after ~2 seconds
4. Type 'C' (correct) - verify counter reset
5. Type 'Z' four times at position 2 - no hint should show
6. Type fifth 'Z' - grey 'A' should appear
7. Complete/fail word - verify hint clears

**Unit Tests (create new file):**
`app/src/test/java/com/spellwriter/viewmodel/HintLetterBehaviorTest.kt`
- Hint shows after 5 failures
- Auto-clears after 2s
- Counter resets on correct letter
- Counter resets after hint shown
- Hint clears on word change
- Tracks correct position

**UI Tests (create new file):**
`app/src/androidTest/java/com/spellwriter/HintLetterUITest.kt`
- Hint displays at correct position
- Grey color differentiation
- Integration with typed letters

## Verification Checklist

- [ ] Compile succeeds
- [ ] Unit tests pass (hint logic)
- [ ] UI tests pass (grey letter display)
- [ ] Manual: 5 wrong attempts → hint shows (grey)
- [ ] Manual: Hint fades in (300ms) and out (500ms)
- [ ] Manual: Hint auto-clears after ~2s
- [ ] Manual: Counter resets on correct letter
- [ ] Manual: Hint clears when word changes
- [ ] Manual: Works at different positions (0, 1, 2+)
- [ ] Manual: No crashes or memory leaks

## Edge Cases

1. **Position out of bounds** - Guard in `showHintLetter()` prevents crash
2. **Word changes during hint** - Cleared in word transition logic
3. **Rapid typing** - Counter tracks per-position, resets appropriately
4. **Multiple hints same position** - Counter resets after hint, can trigger again

## Risk
Low-Medium - Multiple file changes but isolated feature. Existing validation logic unchanged. Clean state management pattern.
