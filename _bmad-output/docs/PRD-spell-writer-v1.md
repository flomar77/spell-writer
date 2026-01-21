# Product Requirements Document (PRD)
## Spell Writer - Educational Writing App for Children

**Version:** 1.1
**Status:** Ready for Development
**Author:** Florent
**Date:** 2026-01-12
**Source:** Brainstorming Session 2026-01-11

---

## 1. Executive Summary

**Spell Writer** is a minimalist educational mobile application designed to help children (ages 5-8) learn to write by listening to spoken words and typing them on a magical grimoire. The app uses a wizard/magic theme to make learning engaging, with a friendly ghost character guiding the child through the experience.

**Core Value Proposition:** Transform the potentially frustrating experience of learning to spell into a magical, playful adventure where mistakes are funny rather than discouraging.

---

## 2. Problem Statement

### The Challenge
Children learning to read and write often struggle with:
- Connecting spoken words (auditory) to written form (visual/motor)
- Maintaining motivation when making spelling mistakes
- Abstract, unengaging learning tools that feel like "work"

### The Opportunity
Create an app that:
- Makes the auditory-to-written connection clear and immediate
- Treats mistakes as part of the fun (not failures)
- Engages children through a magical theme they genuinely enjoy

---

## 3. Goals & Objectives

### Primary Goals
| Goal | Success Metric |
|------|----------------|
| Help children practice spelling through dictation | Child completes 20-word sessions |
| Make learning feel like play | Child voluntarily returns to app |
| Build confidence through positive feedback | Low frustration, high engagement |

### Business Objectives
- Launch MVP with single "Wizard Mode" world
- Validate core gameplay loop with target users
- Establish foundation for future world expansions

---

## 4. Target Audience

### Primary User: The Child
- **Age:** 5-8 years old
- **Stage:** Beginning to learn reading and writing
- **Needs:** Practice spelling, build vocabulary, maintain motivation
- **Context:** Uses tablet/phone at home, possibly with parent nearby

### Secondary User: The Parent
- **Role:** Provides device, monitors progress, assists when stuck
- **Needs:** Trust app is educational, see child enjoying learning
- **Context:** May check progress occasionally, not actively supervising

### User Personas

**Emma (Age 6)**
- Just started learning to write at school
- Loves magical stories and games
- Gets frustrated when she makes mistakes
- Needs: Encouraging feedback, sense of progress

**Lucas (Age 7)**
- Can write basic words but struggles with longer ones
- Competitive, likes earning stars and achievements
- Needs: Clear goals, visible progress, rewards

---

## 5. User Stories

### Epic: Core Gameplay

| ID | As a... | I want to... | So that... | Priority |
|----|---------|--------------|------------|----------|
| US-01 | Child | Hear a word spoken clearly | I know what to write | Must Have |
| US-02 | Child | Type letters on a keyboard | I can write the word | Must Have |
| US-03 | Child | See letters appear on the grimoire | I can see my progress | Must Have |
| US-04 | Child | Know immediately if a letter is correct | I can learn from feedback | Must Have |
| US-05 | Child | Hear the word again | I can try again if I didn't catch it | Must Have |
| US-06 | Child | See a funny animation when I struggle | I don't feel bad about mistakes | Must Have |

**Acceptance Criteria (Core Gameplay):**
- **US-01:** Given game is active, when Play button tapped, then TTS speaks word within 500ms
- **US-02:** Given word is active, when letter key tapped, then letter input is processed within 100ms
- **US-03:** Given correct letter typed, then letter appears on grimoire with fade-in animation
- **US-04:** Given letter typed, then visual feedback (green flash or red wobble) shows within 100ms
- **US-05:** Given word is active, when Repeat button tapped, then same word is spoken again
- **US-06:** Given no correct input for 20 seconds, then failure animation plays and ghost shows "dead" expression

### Epic: Progression

| ID | As a... | I want to... | So that... | Priority |
|----|---------|--------------|------------|----------|
| US-07 | Child | See my progress (X/20 words) | I know how close I am to finishing | Must Have |
| US-08 | Child | Earn stars for completing sessions | I feel accomplished | Must Have |
| US-09 | Child | See a celebration when I earn a star | The achievement feels special | Must Have |
| US-10 | Child | Replay earlier star levels | I can practice words I've learned | Should Have |
| US-11 | Child | Unlock new worlds | I have something to work toward | Future |

**Acceptance Criteria (Progression):**
- **US-07:** Progress bar displays "X/20" and fills proportionally as words are completed
- **US-08:** Given 20 words completed in a star level, then star is marked as earned and persisted
- **US-09:** Given star earned, then dragon animation (2000ms) and star pop animation (800ms) play
- **US-10:** Given star is earned, when star tapped on home screen, then that level can be replayed

### Epic: Accessibility

| ID | As a... | I want to... | So that... | Priority |
|----|---------|--------------|------------|----------|
| US-12 | Child | Start playing immediately without setup | I don't need adult help to begin | Must Have |
| US-13 | Child | Use the app in German or English | I can learn in my language | Must Have |
| US-14 | Child | Type umlauts (Ä, Ö, Ü, ß) easily | I can write German words correctly | Must Have |

**Acceptance Criteria (Accessibility):**
- **US-12:** App launches to Home screen with Play button visible; no login or setup required
- **US-13:** App automatically uses German or English based on device system language
- **US-14:** Given A/O/U/S key long-pressed for 500ms, then umlaut variant (Ä/Ö/Ü/ß) is input

---

## 6. Functional Requirements

### FR-01: Home Screen
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-01.1 | Display app title "SPELL WRITER" | Must Have |
| FR-01.2 | Display ghost character (neutral expression) | Must Have |
| FR-01.3 | Display instruction text explaining the game | Must Have |
| FR-01.4 | Display PLAY button to start game | Must Have |
| FR-01.5 | Display current world with earned stars | Must Have |
| FR-01.6 | Hide locked worlds (don't show greyed out) | Must Have |
| FR-01.7 | Allow tapping earned stars to replay that level | Should Have |
| FR-01.8 | Current star level auto-selected on game start | Must Have |
| FR-01.9 | Replaying completed star does not affect progress | Should Have |

### FR-02: Game Screen Layout
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-02.1 | Display progress bar showing X/20 words completed | Must Have |
| FR-02.2 | Display ghost character (top right) | Must Have |
| FR-02.3 | Display grimoire (center) where letters appear | Must Have |
| FR-02.4 | Display 3 stars (left side) showing level progress | Must Have |
| FR-02.5 | Display Play button to hear next word | Must Have |
| FR-02.6 | Display Repeat button to hear word again | Must Have |
| FR-02.7 | Display QWERTY keyboard (uppercase only) | Must Have |
| FR-02.8 | Keyboard keys minimum touch target: 48dp | Must Have |

### FR-03: Core Gameplay
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-03.1 | When Play tapped, ghost speaks a word using TTS | Must Have |
| FR-03.2 | When Repeat tapped, ghost repeats current word | Must Have |
| FR-03.3 | When correct letter typed, letter appears on grimoire | Must Have |
| FR-03.4 | When correct letter typed, play success sound | Must Have |
| FR-03.5 | When correct letter typed, ghost shows happy expression | Must Have |
| FR-03.6 | When wrong letter typed, letter wobbles and fades | Must Have |
| FR-03.7 | When wrong letter typed, play gentle error sound | Must Have |
| FR-03.8 | When wrong letter typed, ghost shows unhappy expression | Must Have |
| FR-03.9 | When word completed, update progress bar | Must Have |
| FR-03.10 | When word completed, load next word from pool | Must Have |

### FR-04: Session Management
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-04.1 | Each star level contains 20 words | Must Have |
| FR-04.2 | Present shorter words first, then longer words | Must Have |
| FR-04.3 | Failed words return to pool for retry later | Must Have |
| FR-04.4 | Session completes when all 20 words are correctly written | Must Have |
| FR-04.5 | Track words internally for adaptive learning (future) | Should Have |

### FR-05: Star Progression
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-05.1 | Star 1: 10 × 3-letter + 10 × 4-letter words | Must Have |
| FR-05.2 | Star 2: 10 × 4-letter + 10 × 5-letter words | Must Have |
| FR-05.3 | Star 3: 10 × 5-letter + 10 × 6-letter words | Must Have |
| FR-05.4 | On session complete, play stars explosion animation (500ms) | Must Have |
| FR-05.5 | On session complete, play dragon fly-through animation (2000ms) | Must Have |
| FR-05.6 | Dragon size increases with each star (small → medium → large) | Must Have |
| FR-05.7 | Earned star pops into place with animation (800ms) | Must Have |
| FR-05.8 | Star is "earned" when all 20 words in that level are completed | Must Have |
| FR-05.9 | After 3 stars earned, next world unlocks (future) | Future |

### FR-06: Ghost Character
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-06.1 | Ghost has 4 expressions: neutral, happy, unhappy, dead | Must Have |
| FR-06.2 | Ghost speaks words using device TTS | Must Have |
| FR-06.3 | Ghost shows happy expression on correct letter | Must Have |
| FR-06.4 | Ghost shows unhappy expression on wrong letter | Must Have |
| FR-06.5 | Ghost shows "dead" expression for failure animation | Must Have |
| FR-06.6 | Ghost returns to neutral after reaction | Must Have |

### FR-07: Failure Handling
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-07.1 | If no key pressed for 8 seconds, ghost shows encouraging expression | Should Have |
| FR-07.2 | If no correct key pressed for 20 seconds, trigger funny "failure" animation | Should Have |
| FR-07.3 | Ghost shows "dead" expression during failure animation | Should Have |
| FR-07.4 | Failure animation should make child laugh, not feel bad | Should Have |
| FR-07.5 | After failure animation, child can retry the word | Must Have |

### FR-08: Internationalization
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-08.1 | Support German language (default) | Must Have |
| FR-08.2 | Support English language | Must Have |
| FR-08.3 | German word list: 60 words (separate from English) | Must Have |
| FR-08.4 | English word list: 60 words (separate from German) | Must Have |
| FR-08.5 | Long-press on A/O/U/S for Ä/Ö/Ü/ß input | Must Have |
| FR-08.6 | TTS language matches app language | Must Have |
| FR-08.7 | UI strings localized for both languages | Must Have |
| FR-08.8 | App language follows device system language setting | Must Have |
| FR-08.9 | Default to German if system language not supported | Must Have |

### FR-09: Session Controls
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-09.1 | Display Exit button (X icon) on Game Screen top-left | Must Have |
| FR-09.2 | Exit button shows confirmation dialog: "Leave session?" | Must Have |
| FR-09.3 | Confirmation dialog has "Stay" and "Leave" options | Must Have |
| FR-09.4 | On Leave, save current word progress and return to Home | Must Have |
| FR-09.5 | On Stay, dismiss dialog and continue game | Must Have |

### FR-10: Error Handling
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-10.1 | If TTS engine unavailable, show message: "Voice not available" | Must Have |
| FR-10.2 | If TTS fails, allow retry via Repeat button | Must Have |
| FR-10.3 | If device muted, show visual indicator (muted icon) | Should Have |
| FR-10.4 | App must function without crashing if TTS unavailable | Must Have |

---

## 7. Non-Functional Requirements

### NFR-01: Performance
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-01.1 | App launch to playable | < 2 seconds |
| NFR-01.2 | TTS response after tap | < 500ms |
| NFR-01.3 | Letter feedback after keypress | < 100ms |
| NFR-01.4 | Animation frame rate | 60 fps |

### NFR-02: Usability
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-02.1 | Child can start playing without adult help | Zero setup |
| NFR-02.2 | All touch targets | ≥ 48dp minimum |
| NFR-02.3 | Text readability | Clear, large fonts |
| NFR-02.4 | No ads or in-app purchases | Child-safe |

### NFR-03: Reliability
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-03.1 | Progress saved after each completed word | Immediately |
| NFR-03.2 | Progress saved on app backgrounding | Within 100ms |
| NFR-03.3 | On restart, resume from last completed word | Automatic |
| NFR-03.4 | App crash rate | < 0.1% |
| NFR-03.5 | Works offline | 100% functionality |

### NFR-04: Compatibility
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-04.1 | Android version | 8.0+ (API 26+) |
| NFR-04.2 | Screen sizes | Phone and tablet |
| NFR-04.3 | Orientation | Portrait only (MVP) |

### NFR-05: Accessibility
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-05.1 | Touch targets | ≥ 48dp (WCAG 2.1) |
| NFR-05.2 | Text contrast ratio | ≥ 4.5:1 for body text |
| NFR-05.3 | Font sizes | Scalable with system settings |
| NFR-05.4 | Color not sole indicator | Icons + color for feedback |

---

## 8. User Interface Requirements

### UI-01: Design Principles
- **Minimalist:** Black & white base design
- **Color as Reward:** Color appears only during magical moments (stars, dragon, celebrations)
- **Child-Friendly:** Large touch targets, simple navigation, no clutter
- **Magical Theme:** Grimoire, ghost, wizard aesthetic

### UI-02: Screen Specifications

#### Home Screen
```
┌─────────────────────────────────────────┐
│                 👻                       │
│            SPELL WRITER                 │
│                                         │
│    "To win, write the words you        │
│         will hear correctly"            │
│                                         │
│           ┌─────────────┐               │
│           │    PLAY     │               │
│           └─────────────┘               │
│                                         │
│            ⭐☆☆ Wizard                  │
└─────────────────────────────────────────┘
```

#### Game Screen
```
┌─────────────────────────────────────────┐
│ [████████░░░░░░░░░░] 8/20      👻 Ghost │
│                                         │
│  [⭐]     ┌─────────────┐      [ ▶ ]    │
│  [☆]     │  GRIMOIRE   │               │
│  [☆]     │   C A _     │      [ 🔁 ]   │
│           └─────────────┘               │
│                                         │
│    [Q][W][E][R][T][Y][U][I][O][P]       │
│    [A][S][D][F][G][H][J][K][L]          │
│    [Z][X][C][V][B][N][M]                │
└─────────────────────────────────────────┘
```

### UI-03: Asset Requirements

| Category | Assets | Notes |
|----------|--------|-------|
| Ghost | 4 expressions (200×200px) | B&W, PNG/SVG |
| Grimoire | 1 open book (400×300px) | B&W, aged paper |
| Dragon | 3 sizes (small/medium/large) | COLOR, flying pose |
| Stars | 2 states (empty/filled, 48×48px) | B&W / Gold |
| Buttons | Play, Repeat (64×64px) | B&W |
| Keys | Normal, Pressed (48×48px) | B&W |
| Audio | 9 sound effects | See Audio section |

---

## 9. Technical Requirements

### Platform
- **Target:** Android (phone/tablet)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Min SDK:** 26 (Android 8.0)

### Architecture
- **Pattern:** MVVM (Model-View-ViewModel)
- **State Management:** Compose StateFlow
- **Persistence:** DataStore
- **Audio:** TextToSpeech API + MediaPlayer

### Project Structure
```
app/
├── ui/
│   ├── screens/ (HomeScreen, GameScreen)
│   ├── components/ (Ghost, Grimoire, Keyboard, etc.)
│   └── theme/ (Colors, Theme)
├── data/
│   ├── models/ (Word, GameState, Progress)
│   └── repository/ (WordRepository, ProgressRepository)
├── audio/ (SoundManager)
└── viewmodel/ (GameViewModel)
```

---

## 10. Scope Definition

### MVP (Version 1.0) - In Scope
| Feature | Status |
|---------|--------|
| Wizard Mode (1 world) | ✅ Included |
| 3 star levels | ✅ Included |
| 60 German words | ✅ Included |
| 60 English words | ✅ Included |
| Ghost character (4 expressions) | ✅ Included |
| Grimoire + letter animations | ✅ Included |
| Dragon animation (3 sizes) | ✅ Included |
| QWERTY keyboard with umlaut support | ✅ Included |
| TTS for word pronunciation | ✅ Included |
| Progress persistence | ✅ Included |
| Zero-setup launch | ✅ Included |

### Future Versions - Out of Scope for MVP
| Feature | Target Version |
|---------|----------------|
| Pirate World | v1.1 |
| Superhero World | v1.2 |
| Space Explorer World | v1.3 |
| Chef World | v1.4 |
| Parent Dashboard | v2.0 |
| Parent Notifications | v2.0 |
| Multiple Child Profiles | v2.0 |
| Advanced Adaptive Learning | v2.0 |
| iOS Version | v2.0 |

---

## 11. Success Metrics

### Launch Criteria
| Metric | Target |
|--------|--------|
| All MVP features implemented | 100% |
| Crash-free sessions | > 99% |
| TTS works on test devices | 100% |
| Full German + English localization | 100% |

### Post-Launch KPIs
| Metric | Target | Measurement |
|--------|--------|-------------|
| Session completion rate | > 70% | Child finishes 20 words |
| Return rate | > 50% | Child returns within 7 days |
| Stars earned per user | > 2 | Average across users |
| Session duration | 5-10 min | Average time per session |

---

## 12. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| TTS quality varies by device | Medium | Medium | Test on multiple devices; consider pre-recorded audio for v2 |
| Children find it too hard | Low | High | Difficulty tuned through playtesting; failure animations reduce frustration |
| Children find it too easy | Low | Medium | 3-star progression provides challenge; word lists curated for difficulty |
| Animation performance issues | Medium | Low | Simple animations; test on low-end devices |
| German umlaut input confusing | Low | Medium | Visual hint on keys; simple long-press mechanic |

---

## 13. Word Lists

### German Words (60)

**Star 1 (3+4 letters):**
OHR, ARM, EIS, HUT, ZUG, TAG, TOR, RAD, ROT, OPA,
BAUM, HAUS, BALL, BUCH, HUND, MOND, BROT, KOPF, NASE, HAND

**Star 2 (4+5 letters):**
BEIN, TIER, SOFA, HASE, BERG, EULE, MAUS, GRAS, ROSE, BOOT,
APFEL, KATZE, MILCH, TISCH, VOGEL, FISCH, PFERD, HONIG, WOLKE, BLUME

**Star 3 (5+6 letters):**
BIRNE, LAMPE, BIENE, TIGER, SONNE, STERN, REGEN, NACHT, ZEBRA, GABEL,
ORANGE, BANANE, GARTEN, DRACHE, BUTTER, SCHULE, KUCHEN, FROSCH, STRAND, TOMATE

### English Words (60)

**Star 1 (3+4 letters):**
CAT, DOG, SUN, HAT, BED, CUP, EGG, PIG, BUS, BOX,
TREE, FISH, BIRD, CAKE, MOON, BOOK, FROG, STAR, DUCK, BALL

**Star 2 (4+5 letters):**
BEAR, DOOR, MILK, RAIN, SHOE, BOAT, LION, HAND, NOSE, LAMP,
APPLE, HORSE, HOUSE, WATER, CHAIR, CLOUD, BREAD, TIGER, PLANT, TRAIN

**Star 3 (5+6 letters):**
SNAKE, BEACH, LEMON, TRUCK, QUEEN, SHEEP, SPOON, DRESS, MOUSE, STORM,
RABBIT, GARDEN, ORANGE, CHEESE, FLOWER, MONKEY, BANANA, PENCIL, DRAGON, CASTLE

---

## 14. Appendix

### A. Development Estimate
| Phase | Hours |
|-------|-------|
| Project Setup | 4h |
| Core Data Layer | 6h |
| Home Screen | 6h |
| Game Screen Layout | 9h |
| Game Screen Logic | 9h |
| Audio Integration | 5.5h |
| Animations | 9h |
| Polish & Testing | 10h |
| **Total** | **~58h** |

### B. Related Documents
- Brainstorming Session: `_bmad-output/analysis/brainstorming-session-2026-01-11.md`
- Prototype Code: `spell-writer/` directory

### C. Revision History
| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-12 | Florent | Initial PRD from brainstorming session |
| 1.1 | 2026-01-12 | Florent | Fixes from adversarial review: added FR-09 (exit), FR-10 (error handling), acceptance criteria, animation timings, accessibility NFRs, clarified persistence and star earning |

---

**Document Status:** Ready for Development
**Next Steps:** Technical review, then development kickoff
