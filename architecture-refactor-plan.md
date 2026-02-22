# SpellWriter Architecture Refactor — TDD Plan

## Context
Architecture review found issues across 6 severity clusters. This plan fixes them using TDD (red-green-refactor), ordered by dependency.

**Decisions made:**
- DI: Deferred — screen rotation support not needed now
- Loading state: Show spinner (GameUiState.Loading)
- androidTest impact: `GifRewardIntegrationTest` uses `showCelebration` as composable param (safe). `ProgressRepositoryTest` uses `loadSessionState()` heavily (must update in Cluster 4/6).

## Dependency Graph

```
Cluster 0 (Critical Fixes) ......... DONE (already implemented)
Cluster 1 (DI/Lifecycle) ........... SKIPPED (no rotation support needed)
    |
Cluster 2 (One-shot Events)
    |
Cluster 3 (State Consolidation)
    |
Cluster 4 (Persistence Overlap)
    |
Cluster 5 (WordPool/WordRepo)
    |
Cluster 6 (Dead Code)
```

---

## ~~Cluster 0: Quick Critical Fixes~~ DONE

All items previously implemented:
- `GameConstants` values correct, tests exist and pass
- `loadSessionState()` already uses `.first()`
- `GameScreen` already uses `GameConstants.WORDS_PER_SESSION` constant

---

## ~~Cluster 1: DI / Lifecycle~~ SKIPPED

Screen rotation survival not a priority. `remember {}` ViewModel creation acceptable for now.

---

## Cluster 2: One-shot Events (~6 new tests, ~12 updated)

### RED: Write failing tests

1. **New `GameViewModelEventsTest.kt`** (Robolectric + StandardTestDispatcher)
   - `navigationEvent_firesOnce_notRedeliveredOnResubscribe()`
   - `navigationEvent_doesNotEmit_onInit()`
   - `audioEvent_emitsOnTrigger_consumedAfterRead()`
   - `playButton_debounce_blocksWithin500ms()`
   - `playButton_debounce_allowsAfter500ms()`
   - `replayButton_debounce_independent()`

### GREEN: Implement

a. **`GameViewModel.kt`**:
   - Replace `_shouldNavigateHome: MutableStateFlow<Boolean>` → `Channel<NavigationEvent>(Channel.BUFFERED)` + `receiveAsFlow()`
   - Replace `_shouldPlayAudio: MutableStateFlow<Boolean>` → `Channel<Unit>(Channel.CONFLATED)`
   - Remove `markAudioPlayed()` (no longer needed)
   - Add `onPlayButtonClicked()` / `onReplayButtonClicked()` with timestamp debounce

b. **`GameScreen.kt`**:
   - Replace `collectAsState()` for navigation/audio with `LaunchedEffect { events.collect {} }`
   - Remove `lastPlayClick` / `minClickInterval` state vars
   - Wire buttons to `viewModel.onPlayButtonClicked()` / `onReplayButtonClicked()`

### UPDATE existing tests
- `shouldPlayAudio_initialState_isFalse` → `audioEvents_emitsNothing_initially`
- `triggerAudioPlayback_setsStateToTrue` → channel emission test
- `markAudioPlayed_resetsStateToFalse` → delete
- All `wordCompletion_triggersShouldPlayAudio` tests → rewrite with channel collect
- `initialWordLoad_triggersShouldPlayAudio` → rewrite
- `wordFailure_triggersShouldPlayAudio` → rewrite
- Navigation tests referencing `shouldNavigateHome.value` → collect from channel

### Files
- Modify: `GameViewModel.kt`, `GameScreen.kt`
- New: `GameViewModelEventsTest.kt`
- Update: `GameViewModelTest.kt` (~12 tests)

---

## ~~Cluster 3: State Consolidation~~ TRIMMED

Sealed class consolidation dropped — individual StateFlows work fine with Compose and avoid unnecessary over-recomposition. Only the `confirmExit()` fix applied.

### Fix: `confirmExit()` suspend → non-suspend
- `GameViewModel.kt`: Change `suspend fun confirmExit()` → `fun confirmExit()` using `viewModelScope.launch` internally
- `GameScreen.kt`: Remove `coroutineScope.launch { viewModel.confirmExit() }` wrapper

---

## ~~Cluster 4: Persistence Overlap~~ DONE

### RED: Write failing tests

1. **New `DataStoreProviderTest.kt`** (Robolectric)
   - `wordsDataStore_returnsSameInstance()`
   - `progressDataStore_returnsSameInstance()`
   - `sessionDataStore_returnsSameInstance()`

2. **`SessionRepositoryTest.kt`** additions (unit test)
   - `sessionRepo_clear_doesNotAffectProgress()`
   - `sessionRepo_isOnlySessionStatePersister()`

### GREEN: Implement

a. **`DataStoreProvider.kt`**: Add `wordsDataStore` singleton (same pattern as existing progress/session stores)

b. **`WordsRepository.kt`**: Remove inline `Context.dataStore` extension property. Use `DataStoreProvider.wordsDataStore`

c. **`ProgressRepository.kt`**: Remove `saveSessionState()` and `clearSessionState()` — session persistence owned solely by `SessionRepository`

d. **`GameViewModel.kt`**: Remove call to `progressRepository.saveSessionState()` in `onWordCompleted()`

### UPDATE androidTest
- **`ProgressRepositoryTest.kt`** (androidTest): Remove/update tests that call `loadSessionState()`, `saveSessionState()`, `clearSessionState()`. These methods are being removed. (~8 test methods to delete or rewrite to test remaining ProgressRepository methods)

### Files
- Modify: `DataStoreProvider.kt`, `WordsRepository.kt`, `ProgressRepository.kt`, `GameViewModel.kt`
- Update: `app/src/androidTest/.../ProgressRepositoryTest.kt`

---

## ~~Cluster 5: WordPool / WordRepository Responsibilities~~ DONE

### RED: Write failing tests

1. **`WordPoolTest.kt`** updates/additions
   - `staticWords_star1_en_returns20Words()`
   - `staticWords_star1_de_returns20Words()`
   - `shuffleByLength_maintainsGrouping()`
   - `wordPool_hasNoRepositoryDependency()` (reflection: no `repository` field)

2. **`WordRepositoryTest.kt`** additions
   - `getWordsForStar_usesCachedWords_whenAvailable()`
   - `getWordsForStar_fallsBackToStatic_onTimeout()`
   - `getTTSLocale_notPresent()` (compile-time: method removed)

3. **New `LanguageManagerTest.kt`** (unit test, `data/repository/` package)
   - `getCurrentLanguage_returnsDefault_whenNoPref()`

### GREEN: Implement

a. **`WordPool.kt`**: Remove `lateinit var repository`, remove `suspend fun getWordsForStar()`. Keep only static data: `getStaticWords()`, `shuffleByLength()`, `validateWordPool()`

b. **`WordRepository.kt`**: Absorb `getWordsForStar()` orchestration from WordPool. Accept `WordsRepository` as constructor param. Remove `getTTSLocale()` (duplicate of AudioManager)

c. **`LanguageManager.kt`**: Move from `data/models/` to `data/repository/`. Update all imports.

d. **`MainActivity.kt`**: Remove `WordPool.repository = wordsRepository` (no longer needed)

### Files
- Modify: `WordPool.kt`, `WordRepository.kt`, `LanguageManager.kt` (move), `MainActivity.kt`
- Update: `WordPoolTest.kt`, `WordRepositoryTest.kt`
- New: `LanguageManagerTest.kt`

---

## ~~Cluster 6: Dead Code Cleanup~~ DONE

### Deleted
- `HomeViewModel.kt` — never instantiated
- Dead imports `GameViewModel` + `HomeViewModel` from `HomeScreen.kt`
- `WordsRepository.fetchAndCacheNewWords()` — only caller was HomeViewModel
- `WordsRepository.getAllLength()` — private, never called
- Unused `AppLanguage` + `getSystemLanguage` imports from `WordsRepository`

### NOT dead (plan was wrong)
- `LanguageViewModel.kt` — actively used in `LanguageSwitcher.kt`

### Already handled in earlier clusters
- `ProgressRepository.loadSessionState()` — removed in Cluster 4
- `WordRepository.getTTSLocale()` — removed in Cluster 5

### Remaining tech debt
- Legacy `_shouldPlayAudio` / `_shouldNavigateHome` StateFlows in GameViewModel (kept for ~15 existing tests; remove when tests migrated to Channel-based assertions)

---

## Verification

After each cluster:
1. `./gradlew test` — all unit tests pass
2. `./gradlew connectedAndroidTest` (if device available) — all instrumentation tests pass
3. Manual smoke test: launch app, play one star, verify word count, progress bar, TTS, exit flow

After all clusters:
1. Full test suite green
2. No compiler warnings for unused code
3. App launches and completes a full game session without regression

## Test Count Summary

| Cluster | Status | New Tests | Updated Tests | Deleted Tests |
|---------|--------|-----------|---------------|---------------|
| 0 | DONE | — | — | — |
| 1 | SKIPPED | — | — | — |
| 2 | TODO | 6 | ~12 | ~3 |
| 3 | TODO | 7 | ~15 | 0 |
| 4 | TODO | 5 | ~8 (androidTest) | ~5 |
| 5 | TODO | 8 | ~5 | 0 |
| 6 | TODO | 0 | ~2 | ~3 |
| **Total** | | **26** | **~42** | **~11** |
