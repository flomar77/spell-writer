# Story 2.2: Progressive Difficulty System

Status: review

## Story

As a child learning to spell,
I want each star level to have appropriately challenging words that get progressively longer,
So that I can build my spelling skills gradually from simple words to more complex ones.

## Acceptance Criteria

1. **AC1 (FR5.1)**: Star 1 sessions contain exactly 10 three-letter words and 10 four-letter words
   - Three-letter words include: "OHR", "ARM", "EIS", "HAT", "CAT", "SUN" (German/English)
   - Four-letter words include: "BAUM", "HAUS", "BALL", "TREE", "FISH", "BIRD" (German/English)
   - All words are age-appropriate and within a child's vocabulary

2. **AC2 (FR5.2)**: Star 2 sessions contain exactly 10 four-letter words and 10 five-letter words
   - Four-letter words include: "BEIN", "TIER", "BEAR", "DOOR", "MILK" (German/English)
   - Five-letter words include: "APFEL", "KATZE", "APPLE", "HORSE", "HOUSE" (German/English)
   - Difficulty represents a clear step up from Star 1

3. **AC3 (FR5.3)**: Star 3 sessions contain exactly 10 five-letter words and 10 six-letter words
   - Five-letter words include: "BIRNE", "LAMPE", "SNAKE", "BEACH", "LEMON" (German/English)
   - Six-letter words include: "ORANGE", "BANANE", "RABBIT", "GARDEN", "CHEESE" (German/English)
   - Represents the most challenging level for World 1

4. **AC4**: Each star level provides appropriate challenge increase
   - Star 1 focuses on basic letter recognition and short words
   - Star 2 introduces longer words while building confidence
   - Star 3 challenges with most complex spellings

5. **AC5**: Words are chosen randomly with variety
   - Words don't appear in the exact same order every time
   - Word selection ensures variety while maintaining difficulty progression
   - Both German and English word pools follow the same length progression rules

6. **AC6**: Difficulty progression feels natural
   - Within a session, shorter words are presented before longer words (Story 2.1)
   - Word length progression supports learning confidence

## Tasks / Subtasks

- [x] Task 1: Verify word pool data structure (AC: 1, 2, 3)
  - [x] Audit existing WordPool.kt word lists for correct length distribution
  - [x] Confirm German star 1 has exactly 10 x 3-letter + 10 x 4-letter words
  - [x] Confirm German star 2 has exactly 10 x 4-letter + 10 x 5-letter words
  - [x] Confirm German star 3 has exactly 10 x 5-letter + 10 x 6-letter words
  - [x] Confirm English follows same pattern for all 3 stars
  - **NOTE**: Already verified in existing implementation

- [x] Task 2: Add comprehensive word length validation tests (AC: 1, 2, 3, 5)
  - [x] Test Star 1 German: exactly 10 x 3-letter + 10 x 4-letter words
  - [x] Test Star 1 English: exactly 10 x 3-letter + 10 x 4-letter words
  - [x] Test Star 2 German: exactly 10 x 4-letter + 10 x 5-letter words
  - [x] Test Star 2 English: exactly 10 x 4-letter + 10 x 5-letter words
  - [x] Test Star 3 German: exactly 10 x 5-letter + 10 x 6-letter words
  - [x] Test Star 3 English: exactly 10 x 5-letter + 10 x 6-letter words

- [x] Task 3: Add word randomization tests (AC: 5)
  - [x] Test that multiple calls to getWordsForStar() return different orderings
  - [x] Test that randomization occurs within length groups (not across)
  - [x] Test that both languages use same shuffling logic

- [x] Task 4: Add difficulty progression validation tests (AC: 4, 6)
  - [x] Test that returned word list maintains non-decreasing length order
  - [x] Test that shorter words appear before longer words in session
  - [x] Test progression characteristics across star levels

- [x] Task 5: Add explicit word length validation in WordPool (AC: 1, 2, 3)
  - [x] Add compile-time or init-time assertion to validate word counts per length
  - [x] Add documentation comments specifying expected distribution
  - [x] Consider adding `validateWordPool()` function for debugging

## Dev Notes

### Implementation Analysis

**Good News**: The WordPool.kt implementation ALREADY contains the correct word distribution:
- Star 1: 10 x 3-letter + 10 x 4-letter words (lines 12-17, 34-39)
- Star 2: 10 x 4-letter + 10 x 5-letter words (lines 19-24, 41-46)
- Star 3: 10 x 5-letter + 10 x 6-letter words (lines 26-31, 48-53)

The difficulty ordering was implemented in Story 2.1:
```kotlin
return wordList
    .groupBy { it.length }
    .toSortedMap()
    .flatMap { (_, words) -> words.shuffled() }
```

**Primary work for this story**: Comprehensive test coverage to validate and protect this structure.

### Key Architecture Patterns

- **WordPool**: Singleton object providing static word lists [Source: architecture.md#Repository Layer]
- **Immutable data**: Word lists are private vals, only accessed via `getWordsForStar()`
- **Locale-based selection**: Language determined by Locale.getDefault().language

### Testing Standards

- Tests go in: `app/src/test/java/com/spellwriter/data/models/WordPoolTest.kt`
- Use JUnit 4 assertions
- Follow existing test naming pattern: `functionName_scenario_expectedResult`
- No Android dependencies needed (pure Kotlin tests)

### Project Structure Notes

- **WordPool location**: `app/src/main/java/com/spellwriter/data/models/WordPool.kt`
- **Test location**: `app/src/test/java/com/spellwriter/data/models/WordPoolTest.kt`
- Follows MVVM architecture - WordPool is data layer

### Relevant Context from Story 2.1

Story 2.1 already implemented:
- `remainingWords` and `failedWords` tracking in GameState
- Difficulty ordering (short→long) via `groupBy + toSortedMap + flatMap`
- Shuffling within length groups for variety

Story 2.2 builds on this by ensuring the underlying data is correct and well-tested.

### Test Categories

1. **Data Validation Tests**: Verify word counts per length are exactly as specified
2. **Randomization Tests**: Verify shuffling behavior
3. **Progression Tests**: Verify output maintains difficulty order
4. **Language Parity Tests**: Verify German and English follow same rules

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 2.2: Progressive Difficulty System]
- [Source: _bmad-output/planning-artifacts/architecture.md#Repository Layer]
- [Source: spell-writer/app/src/main/java/com/spellwriter/data/models/WordPool.kt]
- [Source: spell-writer/app/src/test/java/com/spellwriter/data/models/WordPoolTest.kt]
- [Source: PRD FR5.1, FR5.2, FR5.3 - Word length requirements per star]

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All 34 WordPoolTest tests pass (100% success rate)
- Build successful with no errors
- Init-time validation runs on WordPool object creation

### Completion Notes List

- **Task 2**: Added comprehensive word length validation tests for all star/language combinations. Tests verify exact counts: 10 short + 10 long words per star level.
- **Task 3**: Added randomization tests verifying multiple calls return different orderings, shuffling occurs within length groups only, and both languages use same logic.
- **Task 4**: Added difficulty progression tests validating non-decreasing length order across all stars/languages, star level progression (avg length increases), and shorter-before-longer session order.
- **Task 5**: Added init-time validation via `validateWordPool()` function that runs on WordPool initialization. Added KDoc comments documenting word distribution requirements per PRD FR5.1-5.3.

### File List

- `spell-writer/app/src/test/java/com/spellwriter/data/models/WordPoolTest.kt` - Added 16 new tests for word length validation, randomization, and progression
- `spell-writer/app/src/main/java/com/spellwriter/data/models/WordPool.kt` - Added init-time validation, validateWordPool() function, and documentation

### Change Log

- 2026-01-17: Story 2.2 implementation complete - Added comprehensive test coverage for progressive difficulty system and init-time word pool validation
