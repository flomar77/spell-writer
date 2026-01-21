---
stepsCompleted: [1, 2, 3, 4]
inputDocuments: []
session_topic: 'Audio-to-writing app for children learning to read and write'
session_goals: 'Help children correctly translate spoken words into written form'
selected_approach: 'ai-recommended'
techniques_used: ['Inner Child Conference']
ideas_generated: [49]
context_file: ''
---

# Brainstorming Session Results

**Facilitator:** Florent
**Date:** 2026-01-11

## Session Overview

**Topic:** Building an educational app where children hear spoken words and practice writing them
**Goals:** Help early learners correctly bridge auditory input to written output
**Core Loop:** Listen → Write → Feedback

---

## Final App Concept: Spell Writer

A minimalist, black-and-white mobile app where children hear words spoken by a friendly ghost and type them on a magical grimoire. Color appears only during magical reward moments.

---

## Key Decisions Summary

| Area | Decision |
|------|----------|
| **Platform** | Tablet/phone app |
| **First mode** | Wizard Mode (others unlockable later) |
| **Design style** | Black & white, color only for magic moments |
| **Input** | In-app QWERTY keyboard, uppercase only |
| **Character** | Ghost with 4 expressions (happy, unhappy, neutral, dead) |
| **Session** | 20 words per star (shorter words first, then longer) |
| **Progression** | 3 stars per mode, 3 stars needed to unlock next mode |
| **Star 1** | 10 × 3-letter + 10 × 4-letter words |
| **Star 2** | 10 × 4-letter + 10 × 5-letter words |
| **Star 3** | 10 × 5-letter + 10 × 6-letter words |
| **Setup** | None - zero friction, just open and play |
| **Failure handling** | Ghost goes "dead" expression, funny moment |
| **Star animation** | Dragon flies R→L (grows bigger each star) |
| **Victory** | Stars explosion (color) |

---

## Screen Layout

```
┌─────────────────────────────────────────┐
│ [████████░░░░░░░░░░] 8/20      👻 Ghost │
│                                (B&W)    │
│                                         │
│  [⭐]     ┌─────────────┐      [ ▶ ]    │
│  [☆]     │  GRIMOIRE   │               │
│  [☆]     │   C A _     │      [ 🔁 ]   │
│           └─────────────┘               │
│              (B&W)                       │
│                                         │
│    [Q][W][E][R][T][Y][U][I][O][P]       │
│    [A][S][D][F][G][H][J][K][L]          │
│    [Z][X][C][V][B][N][M]                │
└─────────────────────────────────────────┘
```

**Layout elements:**
- Progress bar (top) - shows X/20 words completed
- Ghost (top right) - speaks words, reacts with 4 expressions
- Stars (left) - clickable to replay, show progress toward mode completion
- Grimoire (center) - where letters appear magically
- Play button (right) - starts next word
- Repeat button (right) - hear word again
- Keyboard (bottom) - QWERTY, uppercase, basic styling

---

## Core Interaction Flow

```
1. Child taps [ ▶ PLAY ]
2. Ghost says a word
3. Child types letters on keyboard
4. Each correct letter → appears on grimoire + sound + ghost happy
5. Each wrong letter → wobble/fade + ghost unhappy
6. Word complete → progress bar updates + ghost celebrates
7. If stuck too long → ghost goes "dead" (funny)
8. After 20 words → stars explosion + star earned + dragon animation
9. After 3 stars → next mode unlocks
```

---

## All 49 Ideas Generated

### Core Concept
1. **Magic Formula Dictation** - Child writes "spells" (words) to make magic happen
2. **Double Mode Magique** - Mode Oracle (app writes) + Mode Apprenti (child writes)
3. **Progressive Animations** - More correct letters = more magical surprises
4. **Failure Fun** - Wrong answers trigger funny moments, ghost goes "dead"

### Theme & Progression
5. **Theme Worlds** - Multiple universes (Wizard, Pirate, Superhero, Space, Chef)
6. **Unlockable Worlds** - Unlock new themes by mastering words (3 stars required)
7. **Themed Vocabulary** - Each world teaches contextual words
8. **World Unlock Sounds** - Signature audio when unlocking new world

### Parent Experience
9. **Parent Notifications** - Milestone alerts ("Emma unlocked Space Explorer!")
25. **Internal Learning History** - App tracks difficult words invisibly
26. **Natural Help-Seeking** - No skip button; child asks adult for help

### UI/UX Design
10. **Keyboard-Based Letter Entry** - In-app keyboard input
11. **Magical Letter Appearance** - Letters materialize with subtle effects
12. **Per-Letter Audio Feedback** - Satisfying sounds for each correct letter
13. **Repeatable Clear Voice** - Ghost speaks words, replay anytime
14. **Physical Keyboard** - (Revised to in-app keyboard for mobile)
17. **Clean Minimalist Design** - White background, centered grimoire
18. **The Ghost Guide** - Friendly ghost companion speaks the words
19. **Subtle Ambient Soundscape** - Very low background music

### Ghost Character
15. **Wizard Voice Character** - Warm, patient ghost voice
16. **Gentle Wrong Letter Feedback** - Wobble + fade, no harsh rejection
21. **Reactive Silly Ghost** - Ghost reacts to success/failure
29. **Four-Expression Ghost** - Happy, unhappy, neutral, dead (static swaps)

### Session Structure
20. **Play Button Core Loop** - Child controls pace with play button
22. **Repeat Button** - Hear the word again, no penalty
23. **20-Word Progress Bar** - Visual journey to session complete
24. **Random Word Pool with Retry** - Failed words return later in session
27. **Stars Explosion Victory** - Simple celebration on completion

### Simplification Decisions
28. **Animation Complexity Check** - Minimum Viable Magic approach
30. **Basic In-App Keyboard** - Simple letter buttons, function over flair
31. **Right-Side Controls** - Play/Repeat buttons on right, centered
32. **QWERTY Uppercase Keyboard** - Familiar layout, no lowercase

### Adaptive Learning
33. **Simple Onboarding** - Ghost intro, then straight to playing
34. **Adaptive Word Library** - Built-in words organized by difficulty
35. **Zero Setup Launch** - No profiles, no config, just play
36. **Progressive Word Length** - 3-letter → 4-letter → longer words

### Progression System
37. **Mixed Word Length Sessions** - Each session mixes two word lengths
38. **Three-Star Difficulty Per World** - 3 stars to master each world
39. **Sequential Word Length Within Session** - Shorter words first, then longer
40. **Star Pop Celebration** - Star pops into place when earned
41. **Clickable Star Replay** - Tap earned stars to replay that level
42. **Three-Star World Unlock** - Need all 3 stars to unlock next world

### Final Design Decisions
43. **Start at 3-Letter Words** - Skip 2-letter (too abstract)
44. **Three Star Animations Only** - One per star, not per word
45. **Black & White Design, Color for Magic** - B&W base, color = reward
46. **Left-Side Stars** - Stars displayed on left of grimoire
47. **Wizard Mode (Renamed)** - First world is "Wizard Mode"
48. **Dragon Fly-Through Animation** - Dragon flies R→L for star celebration
49. **Progressive Dragon Animation** - Dragon grows bigger with each star

---

## Assets Checklist (for Designer)

### Ghost Character
| Asset | Description | Size | Format | Notes |
|-------|-------------|------|--------|-------|
| ghost_neutral | Default expression | 200x200px | PNG/SVG | B&W, transparent bg |
| ghost_happy | Big smile, celebrating | 200x200px | PNG/SVG | B&W, transparent bg |
| ghost_unhappy | Slight frown, worried | 200x200px | PNG/SVG | B&W, transparent bg |
| ghost_dead | X eyes, tongue out (funny) | 200x200px | PNG/SVG | B&W, transparent bg |

**Ghost style:** Simple, cute, friendly (not scary). Think Casper meets emoji.

### Grimoire
| Asset | Description | Size | Format | Notes |
|-------|-------------|------|--------|-------|
| grimoire | Leather-bound spellbook, open | 400x300px | PNG/SVG | B&W, aged paper look |

**Grimoire style:** Open book view, space in center for letters, subtle leather texture, slightly worn/magical.

### Dragon
| Asset | Description | Size | Format | Notes |
|-------|-------------|------|--------|-------|
| dragon_small | Star 1 dragon | 150x100px | PNG/SVG | COLOR, flying pose |
| dragon_medium | Star 2 dragon | 250x170px | PNG/SVG | COLOR, flying pose |
| dragon_large | Star 3 dragon | 400x270px | PNG/SVG | COLOR, flying pose |

**Dragon style:** Friendly (not scary), same dragon 3 sizes, flying left pose, colorful, small flame trail optional.

### UI Elements
| Asset | Description | Size | Format | Notes |
|-------|-------------|------|--------|-------|
| star_empty | Unearned star | 48x48px | PNG/SVG | B&W outline |
| star_filled | Earned star | 48x48px | PNG/SVG | GOLD color |
| btn_play | Play button icon | 64x64px | PNG/SVG | B&W, triangle |
| btn_repeat | Repeat button icon | 64x64px | PNG/SVG | B&W, circular arrows |
| key_normal | Keyboard key (A-Z) | 48x48px | PNG/SVG | B&W, rounded rect |
| key_pressed | Keyboard key pressed | 48x48px | PNG/SVG | B&W, darker fill |

### Screen Assets
| Asset | Description | Size | Format | Notes |
|-------|-------------|------|--------|-------|
| logo | "SPELL WRITER" text | 300x80px | PNG/SVG | B&W, magical font |
| bg_home | Home background | 1080x1920px | PNG | White/off-white |
| bg_game | Game background | 1080x1920px | PNG | White/off-white |

### Effects (optional - can be code)
| Asset | Description | Notes |
|-------|-------------|-------|
| particle_star | Star particle | Small gold star shape |
| sparkle | Letter appear effect | Small white sparkle |

### Audio Assets
| Asset | Description | Duration | Notes |
|-------|-------------|----------|-------|
| sfx_correct | Correct letter | 0.2s | Soft chime |
| sfx_wrong | Wrong letter | 0.3s | Gentle fizzle |
| sfx_word_complete | Word finished | 0.5s | Small fanfare |
| sfx_star_pop | Star earned | 0.4s | Pop + sparkle |
| sfx_dragon_whoosh | Dragon fly-by | 1.0s | Whoosh sound |
| sfx_dragon_roar | Star 3 dragon | 0.8s | Friendly roar |
| sfx_victory | Stars explosion | 1.5s | Celebration |
| sfx_button | Button tap | 0.1s | Soft click |
| music_ambient | Background music | loop | Very subtle, magical |

### Asset Totals
| Category | Count |
|----------|-------|
| Ghost images | 4 |
| Grimoire | 1 |
| Dragon images | 3 |
| UI elements | 6 |
| Screen assets | 3 |
| Effects | 2 |
| **Visual total** | **19** |
| **Audio total** | **9** |

### Internationalization

**Languages:** German (default), English
**Umlaut input:** Long-press (A→Ä, O→Ö, U→Ü, S→ß)

### Word Lists - English (60 words)

**Star 1: 3-letter words**
CAT, DOG, SUN, HAT, BED, CUP, EGG, PIG, BUS, BOX

**Star 1: 4-letter words**
TREE, FISH, BIRD, CAKE, MOON, BOOK, FROG, STAR, DUCK, BALL

**Star 2: 4-letter words**
BEAR, DOOR, MILK, RAIN, SHOE, BOAT, LION, HAND, NOSE, LAMP

**Star 2: 5-letter words**
APPLE, HORSE, HOUSE, WATER, CHAIR, CLOUD, BREAD, TIGER, PLANT, TRAIN

**Star 3: 5-letter words**
SNAKE, BEACH, LEMON, TRUCK, QUEEN, SHEEP, SPOON, DRESS, MOUSE, STORM

**Star 3: 6-letter words**
RABBIT, GARDEN, ORANGE, CHEESE, FLOWER, MONKEY, BANANA, PENCIL, DRAGON, CASTLE

### Word Lists - German (60 words)

**Star 1: 3-letter words**
OHR, ARM, EIS, HUT, ZUG, TAG, TOR, RAD, ROT, NUS

**Star 1: 4-letter words**
BAUM, HAUS, BALL, BUCH, HUND, MOND, BROT, KOPF, NASE, HAND

**Star 2: 4-letter words**
BEIN, TIER, SOFA, HASE, BERG, EULE, MAUS, GRAS, ROSE, BOOT

**Star 2: 5-letter words**
APFEL, KATZE, MILCH, TISCH, VOGEL, FISCH, PFERD, STUHL, WOLKE, BLUME

**Star 3: 5-letter words**
BIRNE, LAMPE, BIENE, TIGER, SONNE, STERN, HONIG, NACHT, ZEBRA, GABEL

**Star 3: 6-letter words**
ORANGE, BANANE, GARTEN, DRACHE, BUTTER, SCHULE, KUCHEN, FROSCH, STRAND, TOMATE

**Total:** 120 words (60 per language)

---

## MVP Scope (Version 1)

**Include:**
- Wizard Mode only (1 world)
- 3 stars progression
- Ghost with 4 expressions
- Basic keyboard
- Dragon animation (3 sizes)
- Core gameplay loop
- Progress bar
- Stars explosion victory

**Defer to Later:**
- Other worlds (Pirate, Superhero, etc.)
- Parent notifications
- Parent dashboard
- Multiple profiles
- Cross-theme rewards
- Advanced adaptive learning

---

## Tech Stack (Android)

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Animations | Compose Animation APIs |
| Audio | MediaPlayer + TextToSpeech |
| Storage | DataStore or Room |
| Architecture | MVVM |

### Project Structure

```
app/
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── GameScreen.kt
│   │   └── CelebrationScreen.kt
│   ├── components/
│   │   ├── Ghost.kt
│   │   ├── Grimoire.kt
│   │   ├── Keyboard.kt
│   │   ├── StarProgress.kt
│   │   └── DragonAnimation.kt
│   └── theme/
│       └── Theme.kt (B&W + accent colors)
├── data/
│   ├── WordRepository.kt
│   ├── ProgressRepository.kt
│   └── models/
├── audio/
│   └── SoundManager.kt
└── viewmodel/
    └── GameViewModel.kt
```

### Implementation Notes

| Feature | How |
|---------|-----|
| Ghost expressions | 4 drawable images, swap on state |
| Dragon animation | `animateFloatAsState` + translation |
| Star pop | `animateScale` + `animateColor` |
| Stars explosion | Particle library or compose animation |
| Keyboard | Custom Compose grid of buttons |
| Letter appear | `AnimatedVisibility` with fade/scale |
| Ghost voice | Android `TextToSpeech` API |

---

## Open Questions for Later

1. **Ghost voice:** Text-to-speech or recorded voice actor?
2. **Word lists:** Specific words for each level (need to curate)
3. **Language:** English only or multiple languages?
4. **Session interruption:** What if app closes mid-session?
5. **Sound design:** Exact sounds to use
6. **Analytics:** What to track for parent dashboard (future)?

---

## Development Plan

### Phase 1: Project Setup (~4h)
- [ ] 1.1 Create Android project (Kotlin + Compose)
- [ ] 1.2 Set up project structure (screens, components, data)
- [ ] 1.3 Configure theme (B&W colors, typography)
- [ ] 1.4 Add placeholder assets (rectangles for ghost, grimoire)

### Phase 2: Core Data Layer (~6h)
- [ ] 2.1 Create Word model + WordRepository
- [ ] 2.2 Add 60 words as static data
- [ ] 2.3 Create Progress model (stars, current level)
- [ ] 2.4 Set up DataStore for persistence
- [ ] 2.5 Create GameState model (current word, letters typed)

### Phase 3: Home Screen (~6h)
- [ ] 3.1 Build HomeScreen layout
- [ ] 3.2 Add ghost image (placeholder)
- [ ] 3.3 Add logo + instruction text
- [ ] 3.4 Add Play button + navigation
- [ ] 3.5 Add star progress display
- [ ] 3.6 Connect to saved progress

### Phase 4: Game Screen - Layout (~9h)
- [ ] 4.1 Build GameScreen layout structure
- [ ] 4.2 Create Grimoire component
- [ ] 4.3 Create Keyboard component (26 keys)
- [ ] 4.4 Create Ghost component (4 states)
- [ ] 4.5 Create StarProgress component
- [ ] 4.6 Add Play + Repeat buttons
- [ ] 4.7 Add progress bar

### Phase 5: Game Screen - Logic (~9h)
- [ ] 5.1 Create GameViewModel
- [ ] 5.2 Implement word selection (random from pool)
- [ ] 5.3 Implement letter input handling
- [ ] 5.4 Implement correct/wrong letter logic
- [ ] 5.5 Implement word completion logic
- [ ] 5.6 Implement 20-word session tracking
- [ ] 5.7 Implement star earning logic

### Phase 6: Audio (~5.5h)
- [ ] 6.1 Set up SoundManager
- [ ] 6.2 Integrate TextToSpeech for ghost voice
- [ ] 6.3 Add sound effects (correct, wrong, complete)
- [ ] 6.4 Add button tap sounds
- [ ] 6.5 Add ambient music (optional for MVP)

### Phase 7: Animations (~9h)
- [ ] 7.1 Letter appear animation
- [ ] 7.2 Wrong letter wobble + fade
- [ ] 7.3 Ghost expression transitions
- [ ] 7.4 Star pop animation
- [ ] 7.5 Dragon fly-through (3 sizes)
- [ ] 7.6 Stars explosion effect

### Phase 8: Polish & Integration (~10h)
- [ ] 8.1 Replace placeholders with real assets
- [ ] 8.2 End-to-end flow testing
- [ ] 8.3 Fix bugs and edge cases
- [ ] 8.4 Performance optimization
- [ ] 8.5 Final UI polish

### Development Summary
| Phase | Hours |
|-------|-------|
| 1. Setup | 4h |
| 2. Data | 6h |
| 3. Home Screen | 6h |
| 4. Game Layout | 9h |
| 5. Game Logic | 9h |
| 6. Audio | 5.5h |
| 7. Animations | 9h |
| 8. Polish | 10h |
| **Total** | **~58h** |

---

## Future Work
- Brainstorm other worlds (Pirate, Superhero, Space, Chef)
- Design parent dashboard
- Plan adaptive difficulty algorithm
- Add multiple language support

---

**Session Status:** COMPLETE

**Ideas Generated:** 49
**Techniques Used:** Inner Child Conference (primary)
**Techniques Available for Future:** Dream Fusion Laboratory, Pirate Code Brainstorm
