# Story 2.1: 20-Word Learning Sessions

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a child learning to spell,
I want to complete structured learning sessions with 20 words that are appropriately ordered and give me chances to retry,
So that I have a complete learning experience with proper difficulty progression and multiple opportunities to succeed.

## Acceptance Criteria

**AC1: Session Contains Exactly 20 Words**
```gherkin
Given I start a new star level session
When the session begins
Then the session contains exactly 20 words for completion (FR4.1)
And the words are selected from the appropriate star level word pool
And the session progress shows "0/20" at the start
And the system tracks which words I need to complete
```

**AC2: Word Difficulty Progression**
```gherkin
Given I am in a learning session
When the system presents words to me
Then shorter words are presented first, followed by longer words (FR4.2)
And for Star 1: 3-letter words come before 4-letter words
And for Star 2: 4-letter words come before 5-letter words
And for Star 3: 5-letter words come before 6-letter words
And the word order provides a natural difficulty progression
```

**AC3: Failed Words Return to Pool**
```gherkin
Given I fail to spell a word correctly within a reasonable time
When I struggle with or cannot complete a word
Then that word is returned to the word pool for retry later in the session (FR4.3)
And the word doesn't count as "completed" in my 20-word progress
And I will encounter the failed word again later in the same session
And the system ensures I practice difficult words multiple times
```

**AC4: Progress Tracking**
```gherkin
Given I am working through a 20-word session
When I complete each word successfully
Then the progress counter increments (1/20, 2/20, etc.)
And the progress bar visually updates to reflect my advancement
And completed words are removed from the current session pool
And the system selects the next appropriate word based on difficulty ordering
```

**AC5: Failed Word Retry Logic**
```gherkin
Given I have failed words in my retry pool
When I reach the end of the initial word sequence
Then the system presents my failed words for retry attempts
And failed words follow the same difficulty ordering (shorter first)
And I must successfully complete all 20 unique words before session ends
And the session doesn't end until I've mastered all 20 words
```

**AC6: Session Completion**
```gherkin
Given I successfully complete all 20 unique words
When the session ends
Then the progress bar shows "20/20"
And the system recognizes that I have finished the current star level
And no additional words are presented in this session
And I proceed to star achievement celebrations (Story 2.4)
```

## Tasks / Subtasks

- [x] Implement word retry tracking system (AC: #3, #5)
  - [x] Add `failedWords: List<String>` to GameState to track failed attempts
  - [x] Add `completedWords: MutableSet<String>` in ViewModel to track unique completions
  - [x] Create `onWordFailed()` function to handle failed word attempts
  - [x] Implement failed word return logic: add word back to active pool
  - [x] Ensure failed words maintain difficulty ordering (shorter first) via `insertWordByLength()`

- [x] Enhance word pool management (AC: #1, #2, #4)
  - [x] Verify current `loadWordsForStar()` logic creates 20-word pool correctly
  - [x] Ensure word selection uses difficulty-ordered approach (short → long) via `groupBy + toSortedMap`
  - [x] Add validation: exactly 20 unique words per session
  - [x] Track completed words via `completedWords` Set (unique tracking)
  - [x] Implement "next word" selection that respects ordering via `remainingWords.firstOrNull()`

- [x] Create session completion detection (AC: #6)
  - [x] Add completion check: `completedWords.size >= 20`
  - [x] Trigger session complete state when all 20 words done via `sessionComplete = true`
  - [x] Add `LaunchedEffect` in GameScreen to trigger `onStarComplete` callback (Story 2.4 integration)
  - [x] Ensure progress bar shows "20/20" at completion
  - [x] Block additional word presentation after completion via empty `remainingWords`

- [x] Implement failure/timeout trigger (AC: #3)
  - [x] Create `onWordFailed()` public function as integration hook
  - [ ] _Timeout tracking (8s, 20s) deferred to Story 3.2_ - prepared integration point only
  - [x] `onWordFailed()` triggers `triggerFailureAnimation()` for visual feedback
  - [x] Return word to pool with appropriate positioning via `insertWordByLength()`
  - [x] Prepare integration point for Story 3.2 timeout animations

- [x] Update GameViewModel state management (AC: #1-6)
  - [x] Add `remainingWords: List<String>` to GameState for active word pool
  - [x] Add `failedWords: List<String>` to GameState for retry tracking
  - [x] Update word completion to select from `remainingWords.firstOrNull()`
  - [x] Implement word retry logic via `insertWordByLength()` in word selection
  - [x] Ensure StateFlow updates trigger UI recomposition

- [x] Write comprehensive tests (AC: All)
  - [x] Unit test: Session initializes with 20 words (WordPoolTest)
  - [x] Unit test: Words ordered by length (short → long) (WordPoolTest - 5 tests)
  - [x] Unit test: `insertWordByLength` maintains difficulty ordering (5 tests)
  - [x] Unit test: Progress increments only on successful completion
  - [x] Unit test: Session completes only when all 20 unique words done
  - [x] Unit test: Failed word retry tracking state validation
  - [ ] _Integration test: Full 20-word session flow_ - requires instrumentation testing
  - [ ] _Integration test: Failed word appears again later_ - requires instrumentation testing
  - [ ] _Integration test: Progress bar updates accurately_ - requires instrumentation testing

## Dev Notes

### Architecture Patterns & Constraints

**Technology Stack (MANDATORY):**
- **Language:** Kotlin (null-safe, modern syntax)
- **UI Framework:** Jetpack Compose (declarative UI from Stories 1.1-1.5)
- **Architecture Pattern:** MVVM with GameViewModel (established in Story 1.4)
- **State Management:** StateFlow for reactive session state updates
- **Data Management:** WordRepository for word pool access
- **Build System:** Gradle with Kotlin DSL
- **Testing:** JUnit 4 + Compose Testing (established pattern from Stories 1.2-1.5)

**MVVM Session Management Pattern:**
```kotlin
// Story 2.1 enhances existing GameViewModel from Stories 1.4-1.5
class GameViewModel(
    context: Context,
    private val starNumber: Int,
    private val isReplaySession: Boolean
) : ViewModel() {

    // Existing from Story 1.4-1.5
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // NEW for Story 2.1: Session word tracking
    private val _remainingWords = MutableStateFlow<List<String>>(emptyList())
    val remainingWords: StateFlow<List<String>> = _remainingWords.asStateFlow()

    private val _failedWords = MutableStateFlow<List<String>>(emptyList())
    val failedWords: StateFlow<List<String>> = _failedWords.asStateFlow()

    // Internal tracking
    private val completedWords = mutableListOf<String>()
    private val attemptedWords = mutableListOf<String>()

    // NEW: Start session with 20 words
    private fun startNewSession(star: Int) {
        val allWords = WordRepository.getWordsForStar(star)
        val (shortWords, longWords) = allWords

        // Difficulty ordering: short words first, then long words
        val orderedWords = (shortWords.shuffled() + longWords.shuffled())
            .take(20)  // Ensure exactly 20 words

        _remainingWords.value = orderedWords
        _failedWords.value = emptyList()
        completedWords.clear()
        attemptedWords.clear()

        _gameState.value = _gameState.value.copy(
            currentWord = orderedWords.first(),
            wordsCompleted = 0,
            wordPool = orderedWords,
            typedLetters = ""
        )
    }

    // NEW: Failed word handling
    fun onWordFailed(word: String) {
        // Add to failed words list
        _failedWords.value = _failedWords.value + word

        // Add back to remaining words (for retry)
        // Position based on length to maintain ordering
        val currentRemaining = _remainingWords.value.toMutableList()
        val insertPosition = currentRemaining.indexOfFirst { it.length > word.length }
        if (insertPosition == -1) {
            currentRemaining.add(word)  // Add at end if longest
        } else {
            currentRemaining.add(insertPosition, word)
        }
        _remainingWords.value = currentRemaining

        // Load next word
        loadNextWord()
    }

    // ENHANCED: Word completion logic
    private fun onWordComplete(word: String) {
        // Mark as completed
        completedWords.add(word)
        attemptedWords.add(word)

        // Remove from failed words if it was there
        _failedWords.value = _failedWords.value.filter { it != word }

        // Update progress
        val newWordsCompleted = completedWords.size
        _gameState.value = _gameState.value.copy(
            wordsCompleted = newWordsCompleted,
            typedLetters = ""
        )

        // Check session completion
        if (allWordsCompleted()) {
            onSessionComplete()
        } else {
            // Load next word after delay
            viewModelScope.launch {
                delay(1500L)
                loadNextWord()
            }
        }
    }

    // NEW: Check if all 20 words completed
    private fun allWordsCompleted(): Boolean {
        return completedWords.size >= 20
    }

    // NEW: Session completion
    private fun onSessionComplete() {
        // Prepare for star completion (Story 2.4 will handle animations)
        // For now, just mark session complete
        _gameState.value = _gameState.value.copy(
            sessionComplete = true  // Need to add to GameState
        )
    }

    // ENHANCED: Load next word (priority: failed words first)
    private fun loadNextWord() {
        val remaining = _remainingWords.value.toMutableList()

        // Remove current word from remaining
        remaining.remove(_gameState.value.currentWord)
        _remainingWords.value = remaining

        if (remaining.isNotEmpty()) {
            // Next word is first in remaining list (maintains ordering)
            _gameState.value = _gameState.value.copy(
                currentWord = remaining.first(),
                typedLetters = ""
            )
        } else if (!allWordsCompleted()) {
            // Should not happen - but safety check
            Log.e("GameViewModel", "No remaining words but session not complete!")
        }
    }
}
```

**Word Retry Logic Architecture:**
1. **Failed Word Detection**: When failure condition met (timeout or explicit failure)
2. **Return to Pool**: Add failed word back to `remainingWords` at appropriate position
3. **Maintain Ordering**: Insert based on word length to preserve short → long progression
4. **Track Separately**: `failedWords` list tracks which words need extra practice
5. **Completion Check**: Session complete only when all 20 unique words in `completedWords`

### File Structure Requirements

**Project Organization (Building on Stories 1.1-1.5):**
```
app/src/main/java/com/spellwriter/
├── MainActivity.kt                   ← NO CHANGES
├── ui/
│   ├── theme/                        ← NO CHANGES
│   ├── screens/
│   │   ├── HomeScreen.kt             ← NO CHANGES
│   │   └── GameScreen.kt             ← MINOR: May need sessionComplete handling
│   └── components/
│       ├── Ghost.kt                  ← NO CHANGES
│       ├── Grimoire.kt               ← NO CHANGES
│       ├── StarProgress.kt           ← NO CHANGES
│       ├── SpellKeyboard.kt          ← NO CHANGES
│       └── WorldProgressRow.kt       ← NO CHANGES
├── data/
│   ├── models/
│   │   ├── GhostExpression.kt        ← NO CHANGES
│   │   ├── GameState.kt              ← ENHANCE (add sessionComplete, remaining words)
│   │   ├── WordPool.kt               ← NO CHANGES
│   │   ├── Progress.kt               ← NO CHANGES
│   │   └── World.kt                  ← NO CHANGES
│   └── repository/
│       └── WordRepository.kt         ← VERIFY (word selection logic correct)
├── audio/
│   └── SoundManager.kt               ← NO CHANGES
└── viewmodel/
    └── GameViewModel.kt              ← ENHANCE (retry logic, session tracking)

app/src/test/java/com/spellwriter/
└── viewmodel/
    └── GameViewModelTest.kt          ← ENHANCE (session management tests)
```

**Critical Implementation Order:**
1. Update GameState data model with sessionComplete and retry tracking
2. Implement `onWordFailed()` in GameViewModel
3. Enhance `loadNextWord()` to check failed words first
4. Implement `allWordsCompleted()` session completion check
5. Add word retry positioning logic (maintain length ordering)
6. Update word completion flow to check for session completion
7. Write comprehensive unit tests for retry logic
8. Integration test full 20-word session with failures

### Component Implementation Details

**1. GameState.kt Enhancement:**
```kotlin
// app/src/main/java/com/spellwriter/data/models/GameState.kt
data class GameState(
    val currentWord: String = "",
    val typedLetters: String = "",
    val wordsCompleted: Int = 0,
    val sessionStars: Int = 0,  // Always 0 until Story 2.4
    val wordPool: List<String> = emptyList(),

    // NEW for Story 2.1
    val sessionComplete: Boolean = false,  // Marks 20-word completion
    val remainingWords: List<String> = emptyList(),  // Words not yet completed
    val failedWords: List<String> = emptyList()  // Words that need retry
)
```

**2. Word Retry Positioning Logic:**
```kotlin
// Insert failed word at correct position to maintain length ordering
private fun insertFailedWord(word: String, remaining: List<String>): List<String> {
    val mutableRemaining = remaining.toMutableList()

    // Find insertion point: first word longer than failed word
    val insertIndex = mutableRemaining.indexOfFirst { it.length > word.length }

    return if (insertIndex == -1) {
        // No words longer - add at end
        mutableRemaining + word
    } else {
        // Insert before first longer word
        mutableRemaining.add(insertIndex, word)
        mutableRemaining
    }
}
```

**3. Session Completion Detection:**
```kotlin
// Check if all 20 unique words completed
private fun allWordsCompleted(): Boolean {
    return completedWords.size >= 20 && completedWords.toSet().size == 20
}
```

**4. Word Selection Logic:**
```kotlin
// Enhanced word selection respecting difficulty ordering
private fun loadNextWord() {
    val remaining = _gameState.value.remainingWords

    if (remaining.isEmpty()) {
        if (allWordsCompleted()) {
            onSessionComplete()
        }
        return
    }

    // Next word is first in remaining (maintains short → long ordering)
    val nextWord = remaining.first()

    _gameState.value = _gameState.value.copy(
        currentWord = nextWord,
        typedLetters = "",
        remainingWords = remaining.drop(1)  // Remove from remaining
    )
}
```

### Previous Story Intelligence

**From Story 1.4 (Core Word Gameplay):**
- **GameViewModel established** with StateFlow pattern for reactive state
- **Word completion logic** already exists in `onWordComplete()`
- **Word pool management** via WordRepository with star-based selection
- **Current limitation**: No retry mechanism - words only attempted once
- **Delay pattern**: Uses `viewModelScope.launch { delay(1500L) }` for word transitions
- **Testing pattern**: Comprehensive unit tests with edge case coverage

**From Story 1.5 (Ghost Character System):**
- **StateFlow pattern** proven successful for UI reactivity
- **Coroutine Job cancellation** pattern for timing logic
- **Auto-reset logic** using viewModelScope.launch + delay pattern
- **Rapid input handling** via Job cancellation - similar pattern needed for word retry
- **Testing approach**: Unit tests + integration tests documented

**Key Learnings to Apply:**
1. **Extend GameViewModel** - Add retry tracking StateFlows, don't replace existing
2. **Use StateFlow pattern** - `remainingWords` and `failedWords` as StateFlows
3. **Coroutine timing** - Use viewModelScope.launch for delayed word loading
4. **Immutable state updates** - GameState.copy() pattern for all state changes
5. **TDD throughout** - Follow established RED-GREEN-REFACTOR cycle
6. **Integration with existing logic** - Enhance `onWordComplete()`, don't replace

### Critical Gap Analysis

**Current Implementation (from Story 1.4):**
- ✅ Session contains 20 words (FR4.1) - IMPLEMENTED
- ✅ Short then long ordering (FR4.2) - IMPLEMENTED via `shortWords + longWords`
- ❌ Failed words retry (FR4.3) - **MISSING** - Critical gap for Story 2.1
- ✅ Progress tracking (FR4.4) - IMPLEMENTED
- ❌ Internal word tracking (FR4.5) - PARTIAL - tracks completed but not failed

**Architecture Review (from architecture.md):**
- Session Management section (lines 191-210): Mentions planned retry logic but not implemented
- FR-04 compliance: 4/5 implemented, retry mechanism is the main gap
- DataStore persistence: Dependency exists but not implemented (will be Story 2.3)

**Implementation Gaps to Fill:**
1. **Word Retry Mechanism** - Core requirement for Story 2.1
2. **Failed Word Tracking** - Separate list from completed words
3. **Word Re-insertion Logic** - Maintain difficulty ordering when word fails
4. **Session Completion Check** - Verify 20 unique words, not just 20 attempts
5. **Failure Trigger** - Integration point for Story 3.2 timeout system

### Testing Requirements

**Unit Tests (app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt):**
```kotlin
class GameViewModelSessionTest {

    @Test
    fun startNewSession_contains20Words() {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        val gameState = viewModel.gameState.value
        assertEquals(20, gameState.wordPool.size)
        assertEquals(0, gameState.wordsCompleted)
    }

    @Test
    fun startNewSession_wordsOrderedByLength() {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        val wordPool = viewModel.gameState.value.wordPool

        // First 10 should be 3-letter (Star 1), next 10 should be 4-letter
        val firstHalf = wordPool.take(10)
        val secondHalf = wordPool.drop(10)

        assertTrue(firstHalf.all { it.length == 3 })
        assertTrue(secondHalf.all { it.length == 4 })
    }

    @Test
    fun onWordFailed_returnsWordToPool() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)
        val initialWord = viewModel.gameState.value.currentWord

        // Fail the current word
        viewModel.onWordFailed(initialWord)

        // Word should be in failed list
        assertTrue(viewModel.failedWords.value.contains(initialWord))

        // Word should be back in remaining pool
        assertTrue(viewModel.remainingWords.value.contains(initialWord))
    }

    @Test
    fun onWordFailed_maintainsLengthOrdering() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        // Complete several short words to get to long words
        repeat(5) {
            val currentWord = viewModel.gameState.value.currentWord
            // Complete word...
        }

        // Fail a long word
        val longWord = viewModel.gameState.value.currentWord  // Should be 4-letter
        viewModel.onWordFailed(longWord)

        // Failed long word should be inserted after short words
        val remaining = viewModel.remainingWords.value
        val failedWordIndex = remaining.indexOf(longWord)
        val wordsBeforeFailed = remaining.take(failedWordIndex)

        // All words before failed word should be shorter or equal length
        assertTrue(wordsBeforeFailed.all { it.length <= longWord.length })
    }

    @Test
    fun wordCompletion_incrementsProgress() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        assertEquals(0, viewModel.gameState.value.wordsCompleted)

        // Complete one word
        val word = viewModel.gameState.value.currentWord
        word.forEach { letter ->
            viewModel.onLetterTyped(letter)
        }

        // Wait for word completion delay
        advanceTimeBy(1500L)

        assertEquals(1, viewModel.gameState.value.wordsCompleted)
    }

    @Test
    fun sessionComplete_onlyWhen20UniqueWordsCompleted() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        // Complete 20 unique words
        repeat(20) {
            val word = viewModel.gameState.value.currentWord
            // Complete word...
            // Advance time...
        }

        assertTrue(viewModel.gameState.value.sessionComplete)
        assertEquals(20, viewModel.gameState.value.wordsCompleted)
    }

    @Test
    fun sessionNotComplete_with19WordsAnd1Failed() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)

        // Complete 19 words
        repeat(19) {
            // Complete word...
        }

        // Fail 1 word
        viewModel.onWordFailed(viewModel.gameState.value.currentWord)

        assertFalse(viewModel.gameState.value.sessionComplete)
        assertTrue(viewModel.remainingWords.value.isNotEmpty())
    }

    @Test
    fun loadNextWord_selectsFirstFromRemainingPool() = runTest {
        val viewModel = GameViewModel(context, starNumber = 1, isReplaySession = false)
        val initialRemaining = viewModel.remainingWords.value

        // Complete current word
        val completedWord = viewModel.gameState.value.currentWord
        // ... complete letters

        // Wait for loadNextWord
        advanceTimeBy(1500L)

        // Next word should be first from remaining
        assertEquals(initialRemaining[1], viewModel.gameState.value.currentWord)
    }
}
```

**Integration Tests (app/src/androidTest/):**
```kotlin
class SessionManagementIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completeFullSession_shows20WordsProgress() {
        // Start game
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        // Should show 0/20
        composeTestRule.onNodeWithText("0/20").assertExists()

        // Complete 1 word (need to know test word)
        // Type letters...

        // Should show 1/20
        composeTestRule.onNodeWithText("1/20").assertExists()
    }

    @Test
    fun failedWord_appearsAgainLater() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.waitForIdle()

        val firstWord = "CAT"  // Assume test fixture

        // Fail the word (timeout or explicit failure)
        // ... trigger failure

        // Continue through several words
        repeat(5) {
            // Complete words...
        }

        // Failed word should appear again
        // Verify currentWord == firstWord at some point
    }
}
```

### Performance Considerations

**Word Pool Management:**
- Initial pool: 20 words selected from larger set (60 words total)
- Memory: ~20 strings × 6 chars avg = 120 bytes (negligible)
- StateFlow updates: Immediate, no performance impact
- Word insertion: O(n) where n ≤ 20 (acceptable)

**Session Tracking:**
- `completedWords` list: Max 20 items
- `failedWords` list: Variable, but typically < 5 items
- `remainingWords` list: Decreases from 20 to 0
- Total memory: < 1KB for all session tracking

**UI Responsiveness:**
- StateFlow recomposition: < 16ms (single frame)
- Progress bar updates: Immediate via Compose recomposition
- Target: All state updates < 100ms (NFR1.3) - Easily met

### Project Structure Notes

**Alignment with Unified Project Structure:**
- GameViewModel.kt in viewmodel/ folder (established Story 1.4)
- GameState.kt in data/models/ folder (established Story 1.4)
- WordRepository.kt in data/repository/ folder (established Story 1.4)
- Test structure mirrors source (viewmodel tests in test/viewmodel/)

**No Detected Conflicts:**
- Extends existing GameViewModel, doesn't replace
- Enhances GameState with additional fields
- Uses established StateFlow pattern from Stories 1.4-1.5
- Follows MVVM architecture consistently

### References

**Source Documents:**
- [Epics: Story 2.1 - 20-Word Learning Sessions](file:///Users/florentmartin/Sites/bmad-spell-writer/_bmad-output/planning-artifacts/epics.md#story-21-20-word-learning-sessions) (lines 399-448)
- [Architecture: Session Management](file:///Users/florentmartin/Sites/bmad-spell-writer/_bmad-output/planning-artifacts/architecture.md#data-persistence-strategy) (lines 189-210)
- [Architecture: FR-04 Compliance](file:///Users/florentmartin/Sites/bmad-spell-writer/_bmad-output/planning-artifacts/architecture.md#fr-04-session-management-5-requirements---status-45-implemented-) (lines 277-285)
- [Story 1.4: Core Word Gameplay](file:///Users/florentmartin/Sites/bmad-spell-writer/_bmad-output/implementation-artifacts/1-4-core-word-gameplay.md) - Word completion patterns
- [Story 1.5: Ghost Character System](file:///Users/florentmartin/Sites/bmad-spell-writer/_bmad-output/implementation-artifacts/1-5-ghost-character-system.md) - StateFlow patterns

**Functional Requirements:**
- FR4.1: Each star level contains 20 words
- FR4.2: Present shorter words first, then longer words
- FR4.3: Failed words return to pool for retry later
- FR4.4: Session completes when all 20 words correctly written
- FR4.5: Track words internally for adaptive learning (future)

**Non-Functional Requirements:**
- NFR1.3: Letter feedback < 100ms (applies to state updates)
- NFR3.1: Progress saved after each word (Story 2.3 will implement)
- NFR3.5: 100% offline functionality (no network needed)

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All unit tests pass (30 total: 17 GameViewModelTest, 13 WordPoolTest)
- No regression failures in existing tests
- Build compiles successfully with minor warnings (unused onBackPress parameter, deprecated member)

### Completion Notes List

1. **GameState Enhanced (AC1, AC3-6)**: Added `sessionComplete`, `remainingWords`, and `failedWords` fields to GameState data class to track session state
2. **WordPool Difficulty Ordering (AC2)**: Modified `getWordsForStar()` to return words ordered by length (short→long) using groupBy + toSortedMap + flatMap pattern, with shuffling within each length group
3. **Word Retry System (AC3, AC5)**: Implemented `onWordFailed()` function that:
   - Adds failed word to `failedWords` tracking list
   - Inserts word back into `remainingWords` at correct position via `insertWordByLength()`
   - Maintains difficulty ordering when words are retried
4. **Session Completion (AC6)**: Enhanced `onWordCompleted()` to:
   - Track unique completed words using `completedWords` Set
   - Check for session completion (20 unique words)
   - Set `sessionComplete = true` and clear remaining pools
   - Prepared integration point for Story 2.4 celebrations
5. **State Management (AC1-6)**: Updated `loadWordsForStar()` to initialize all session tracking fields correctly
6. **Comprehensive Tests**: Added 7 new unit tests for GameState session tracking, 5 new tests for WordPool difficulty ordering
7. **Code Review Fixes (2026-01-16)**:
   - Removed dead `allWordsCompleted()` function
   - Made `insertWordByLength()` internal for testability
   - Added `LaunchedEffect` in GameScreen to trigger `onStarComplete` callback on session complete
   - Added 7 additional unit tests for `insertWordByLength()` algorithm verification
   - Corrected task claims to accurately reflect implementation (removed false `attemptedWords` claim)
   - Marked integration tests as pending (require instrumentation testing)

### File List

**Modified:**
- spell-writer/app/src/main/java/com/spellwriter/data/models/GameState.kt - Added sessionComplete, remainingWords, failedWords fields
- spell-writer/app/src/main/java/com/spellwriter/data/models/WordPool.kt - Implemented difficulty ordering (short→long)
- spell-writer/app/src/main/java/com/spellwriter/viewmodel/GameViewModel.kt - Added onWordFailed(), insertWordByLength(), enhanced onWordCompleted() and loadWordsForStar()
- spell-writer/app/src/main/java/com/spellwriter/ui/screens/GameScreen.kt - Added LaunchedEffect for sessionComplete callback trigger (AC6)
- spell-writer/app/src/test/java/com/spellwriter/viewmodel/GameViewModelTest.kt - Added 17 tests (10 original + 7 code review fixes)
- spell-writer/app/src/test/java/com/spellwriter/data/models/WordPoolTest.kt - Added 5 difficulty ordering tests

## Change Log

- 2026-01-16: Code review fixes applied
  - Fixed C2: Removed dead `allWordsCompleted()` function
  - Fixed M1: Added `LaunchedEffect` in GameScreen to trigger `onStarComplete` on session complete
  - Fixed H2/H3: Added 7 new unit tests for `insertWordByLength()` algorithm
  - Fixed C1/C3: Updated task claims to accurately reflect implementation
  - Fixed M3: Added GameScreen.kt to File List
  - All 30 unit tests pass (17 GameViewModelTest, 13 WordPoolTest)
  - Story status: review → done
- 2026-01-16: Story implementation completed by dev-story workflow
  - Implemented 20-word learning sessions with retry logic (AC1-AC6)
  - Added sessionComplete, remainingWords, failedWords to GameState
  - Implemented word difficulty ordering (short→long) in WordPool
  - Created onWordFailed() with length-based insertion for retry
  - Enhanced onWordCompleted() with session completion detection
  - All 23 unit tests pass (10 GameViewModelTest, 13 WordPoolTest)
  - Story status: in-progress → review
- 2026-01-15: Story created by create-story workflow
  - Extracted Story 2.1 requirements from epics.md (lines 399-448)
  - Analyzed architecture.md for session management patterns and FR-04 compliance
  - Integrated learnings from Story 1.4 (GameViewModel, word completion) and Story 1.5 (StateFlow patterns)
  - Identified critical gap: retry mechanism for failed words (FR4.3) not implemented
  - Designed word retry logic with length-based insertion to maintain difficulty ordering
  - Created comprehensive implementation guide with StateFlow-based session tracking
  - Provided detailed testing requirements covering retry logic and session completion
  - All 6 acceptance criteria documented with Gherkin format
  - Complete dev notes with code examples for retry mechanism, session management
  - Story status: backlog → ready-for-dev
