---
stepsCompleted: [1, 2, 3]
inputDocuments:
  - "/Users/florentmartin/Sites/bmad-test/_bmad-output/docs/PRD-spell-writer-v1.md"
  - "/Users/florentmartin/Sites/bmad-test/_bmad-output/planning-artifacts/architecture.md"
  - "/Users/florentmartin/Sites/bmad-test/_bmad-output/docs/PRD-review-findings.md"
---

# bmad-test - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bmad-test, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

**FR1: Home Screen (9 requirements)**
- FR1.1: Display app title "SPELL WRITER"
- FR1.2: Display ghost character (neutral expression)
- FR1.3: Display instruction text explaining the game
- FR1.4: Display PLAY button to start game
- FR1.5: Display current world with earned stars
- FR1.6: Hide locked worlds (don't show greyed out)
- FR1.7: Allow tapping earned stars to replay that level
- FR1.8: Current star level auto-selected on game start
- FR1.9: Replaying completed star does not affect progress

**FR2: Game Screen Layout (8 requirements)**
- FR2.1: Display progress bar showing X/20 words completed
- FR2.2: Display ghost character (top right)
- FR2.3: Display grimoire (center) where letters appear
- FR2.4: Display 3 stars (left side) showing level progress
- FR2.5: Display Play button to hear next word
- FR2.6: Display Repeat button to hear word again
- FR2.7: Display QWERTY keyboard (uppercase only)
- FR2.8: Keyboard keys minimum touch target: 48dp

**FR3: Core Gameplay (10 requirements)**
- FR3.1: When Play tapped, ghost speaks a word using TTS
- FR3.2: When Repeat tapped, ghost repeats current word
- FR3.3: When correct letter typed, letter appears on grimoire
- FR3.4: When correct letter typed, play success sound
- FR3.5: When correct letter typed, ghost shows happy expression
- FR3.6: When wrong letter typed, letter wobbles and fades
- FR3.7: When wrong letter typed, play gentle error sound
- FR3.8: When wrong letter typed, ghost shows unhappy expression
- FR3.9: When word completed, update progress bar
- FR3.10: When word completed, load next word from pool

**FR4: Session Management (5 requirements)**
- FR4.1: Each star level contains 20 words
- FR4.2: Present shorter words first, then longer words
- FR4.3: Failed words return to pool for retry later
- FR4.4: Session completes when all 20 words correctly written
- FR4.5: Track words internally for adaptive learning (future)

**FR5: Star Progression (9 requirements)**
- FR5.1: Star 1: 10 × 3-letter + 10 × 4-letter words
- FR5.2: Star 2: 10 × 4-letter + 10 × 5-letter words
- FR5.3: Star 3: 10 × 5-letter + 10 × 6-letter words
- FR5.4: On session complete, play stars explosion animation (500ms)
- FR5.5: On session complete, play dragon fly-through animation (2000ms)
- FR5.6: Dragon size increases with each star (small → medium → large)
- FR5.7: Earned star pops into place with animation (800ms)
- FR5.8: Star is "earned" when all 20 words in that level completed
- FR5.9: After 3 stars earned, next world unlocks (future)

**FR6: Ghost Character (6 requirements)**
- FR6.1: Ghost has 4 expressions: neutral, happy, unhappy, dead
- FR6.2: Ghost speaks words using device TTS
- FR6.3: Ghost shows happy expression on correct letter
- FR6.4: Ghost shows unhappy expression on wrong letter
- FR6.5: Ghost shows "dead" expression for failure animation
- FR6.6: Ghost returns to neutral after reaction

**FR7: Failure Handling (5 requirements)**
- FR7.1: If no key pressed for 8 seconds, ghost shows encouraging expression
- FR7.2: If no correct key pressed for 20 seconds, trigger funny "failure" animation
- FR7.3: Ghost shows "dead" expression during failure animation
- FR7.4: Failure animation should make child laugh, not feel bad
- FR7.5: After failure animation, child can retry the word

**FR8: Internationalization (9 requirements)**
- FR8.1: Support German language (default)
- FR8.2: Support English language
- FR8.3: German word list: 60 words (separate from English)
- FR8.4: English word list: 60 words (separate from German)
- FR8.5: Long-press on A/O/U/S for Ä/Ö/Ü/ß input
- FR8.6: TTS language matches app language
- FR8.7: UI strings localized for both languages
- FR8.8: App language follows device system language setting
- FR8.9: Default to German if system language not supported

**FR9: Session Controls (5 requirements)**
- FR9.1: Display Exit button (X icon) on Game Screen top-left
- FR9.2: Exit button shows confirmation dialog: "Leave session?"
- FR9.3: Confirmation dialog has "Stay" and "Leave" options
- FR9.4: On Leave, save current word progress and return to Home
- FR9.5: On Stay, dismiss dialog and continue game

**FR10: Error Handling (4 requirements)**
- FR10.1: If TTS engine unavailable, show message: "Voice not available"
- FR10.2: If TTS fails, allow retry via Repeat button
- FR10.3: If device muted, show visual indicator (muted icon)
- FR10.4: App must function without crashing if TTS unavailable

### NonFunctional Requirements

**NFR1: Performance Requirements (4 requirements)**
- NFR1.1: App launch to playable < 2 seconds
- NFR1.2: TTS response after tap < 500ms
- NFR1.3: Letter feedback after keypress < 100ms
- NFR1.4: Animation frame rate 60 fps

**NFR2: Usability Requirements (4 requirements)**
- NFR2.1: Child can start playing without adult help (Zero setup)
- NFR2.2: All touch targets ≥ 48dp minimum
- NFR2.3: Text readability - Clear, large fonts
- NFR2.4: No ads or in-app purchases (Child-safe)

**NFR3: Reliability Requirements (5 requirements)**
- NFR3.1: Progress saved after each completed word (Immediately)
- NFR3.2: Progress saved on app backgrounding (Within 100ms)
- NFR3.3: On restart, resume from last completed word (Automatic)
- NFR3.4: App crash rate < 0.1%
- NFR3.5: Works offline - 100% functionality

**NFR4: Compatibility Requirements (3 requirements)**
- NFR4.1: Android version 8.0+ (API 26+)
- NFR4.2: Screen sizes - Phone and tablet
- NFR4.3: Orientation - Portrait only (MVP)

**NFR5: Accessibility Requirements (4 requirements)**
- NFR5.1: Touch targets ≥ 48dp (WCAG 2.1)
- NFR5.2: Text contrast ratio ≥ 4.5:1 for body text
- NFR5.3: Font sizes scalable with system settings
- NFR5.4: Color not sole indicator - Icons + color for feedback

### Additional Requirements

**Technical Architecture Requirements (from Architecture Document):**
- Modern Android native stack: Kotlin + Jetpack Compose + MVVM architecture pattern
- StateFlow for reactive state management and real-time UI updates
- DataStore for lightweight persistence (no complex database)
- Project structure: ui/, data/, audio/, viewmodel/ folders
- Material3 theming foundation with custom educational components
- Memory efficiency for low-end devices and battery optimization
- Portrait orientation only for MVP scope

**Integration Requirements:**
- Android TextToSpeech API with locale-based voice configuration
- Android MediaPlayer integration for sound effects
- System language detection with automatic German/English switching
- TTS locale matching (German TTS for German words, English TTS for English words)
- Proper ViewModel lifecycle management for long educational sessions

**Quality and Error Handling (from PRD Review Critical Issues):**
- Define specific timeouts: 8 seconds for encouraging expression, 20 seconds for failure animation
- Enhanced TTS failure handling with graceful degradation to keyboard-only mode
- Session exit capability with confirmation dialog to prevent child entrapment
- Device mute state detection and visual indicator display
- Immediate progress saving after each word completion (NFR-03.1 compliance)

**Content Corrections (from PRD Review):**
- Fix "NUS" word error in German word list → replace with valid 3-letter word
- Implement word list validation for educational accuracy
- Animation timing specifications: Dragon animation 2000ms, Star pop 800ms, Stars explosion 500ms

**Language and Localization:**
- Automatic system language detection on app launch
- Fallback to German when system language not supported (FR-08.9)
- Language indicator display in UI for user awareness
- Umlaut input via long-press mechanics (Ä/Ö/Ü/ß support)

### FR Coverage Map

**Epic 1 - Core Learning Experience:**
- FR1.1-1.9: Home screen functionality (title, ghost, play button, stars display)
- FR2.1-2.8: Game screen layout (progress bar, grimoire, keyboard, controls)
- FR3.1-3.10: Core gameplay mechanics (TTS, letter input, feedback, word completion)
- FR6.1-6.6: Ghost character expressions and reactions

**Epic 2 - Session Management & Progress:**
- FR4.1-4.5: Session management (20 words, difficulty progression, retry logic)
- FR5.1-5.9: Star progression system (word length progression, animations, achievements)

**Epic 3 - Enhanced User Experience:**
- FR7.1-7.5: Failure handling (timeouts, encouraging feedback, retry mechanisms)
- FR8.1-8.9: Internationalization (German/English, TTS locales, umlaut input)
- FR9.1-9.5: Session controls (exit button, confirmation dialog, save/resume)
- FR10.1-10.4: Error handling (TTS fallbacks, mute detection, crash prevention)

## Epic List

### Epic 1: Core Learning Experience
Children can play the basic spelling game - hear words, type letters, get immediate feedback, and complete words successfully. This epic delivers the foundational educational gameplay loop that makes the app immediately useful and engaging.
**FRs covered:** FR1.1-1.9, FR2.1-2.8, FR3.1-3.10, FR6.1-6.6

### Epic 2: Session Management & Progress
Children can complete full learning sessions (20 words), see their progress, earn stars, and experience magical celebrations. This epic adds motivation and structured learning progression on top of the core gameplay.
**FRs covered:** FR4.1-4.5, FR5.1-5.9

### Epic 3: Enhanced User Experience
Children and parents have a robust, accessible, multilingual learning experience with proper error handling and device compatibility. This epic ensures the app works reliably across different devices, languages, and usage scenarios.
**FRs covered:** FR7.1-7.5, FR8.1-8.9, FR9.1-9.5, FR10.1-10.4

## Epic 1: Core Learning Experience

Children can play the basic spelling game - hear words, type letters, get immediate feedback, and complete words successfully. This epic delivers the foundational educational gameplay loop that makes the app immediately useful and engaging.

### Story 1.1: Home Screen Foundation

As a child learning to spell,
I want to see a welcoming home screen with clear instructions and a play button,
So that I can easily understand what the app does and start playing immediately.

**Acceptance Criteria:**

**Given** the app launches for the first time
**When** I open the application
**Then** I see the app title "SPELL WRITER" prominently displayed
**And** I see a friendly ghost character with neutral expression
**And** I see instruction text explaining "To win, write the words you will hear correctly"
**And** I see a large, accessible PLAY button to start the game
**And** all elements are clearly visible and properly sized for child interaction (≥48dp touch targets)

**Given** I am on the home screen
**When** I tap the PLAY button
**Then** the game screen loads within 2 seconds (NFR1.1)
**And** I am taken to the active game interface

### Story 1.2: Star Progress Display

As a child learning to spell,
I want to see my progress with earned stars and available worlds on the home screen,
So that I can track my achievements and choose which level to play or replay.

**Acceptance Criteria:**

**Given** I am on the home screen
**When** the screen loads
**Then** I see the current world displayed with its star progression
**And** I see 3 stars for the current world showing my progress (earned vs unearned)
**And** earned stars are visually distinct from unearned stars
**And** locked worlds are hidden and not shown (FR1.6)

**Given** I have earned at least one star in the current world
**When** I tap on an earned star
**Then** I can replay that specific star level (FR1.7)
**And** the game starts with words appropriate for that star level
**And** replaying does not affect my existing progress (FR1.9)

**Given** I start a new game session
**When** I tap the PLAY button without selecting a specific star
**Then** the game automatically selects my current star level (FR1.8)
**And** I continue from where I left off in my progression

**Given** I have not yet earned any stars
**When** I view the home screen
**Then** all stars appear unearned but Star 1 is available to play
**And** the interface clearly indicates Star 1 is the starting point

### Story 1.3: Game Screen Layout

As a child learning to spell,
I want to see a clear, organized game screen with all the controls I need,
So that I can easily interact with the spelling game and understand my progress.

**Acceptance Criteria:**

**Given** I start a game session from the home screen
**When** the game screen loads
**Then** I see a progress bar at the top showing "X/20 words completed" (FR2.1)
**And** I see the ghost character positioned in the top right corner (FR2.2)
**And** I see the grimoire (magical book) in the center where letters will appear (FR2.3)
**And** I see 3 stars on the left side showing my current session progress (FR2.4)
**And** all elements are properly sized and positioned for child interaction

**Given** I am on the game screen
**When** I look at the control buttons
**Then** I see a Play button (▶) to hear the next word (FR2.5)
**And** I see a Repeat button (🔁) to hear the word again (FR2.6)
**And** both buttons are easily accessible with minimum 48dp touch targets (FR2.8)
**And** button icons are clear and intuitive for children

**Given** I need to type letters
**When** I look at the keyboard area
**Then** I see a QWERTY keyboard layout with uppercase letters only (FR2.7)
**And** all keyboard keys have minimum 48dp touch targets for child accessibility (FR2.8)
**And** keys are clearly labeled and properly spaced
**And** the keyboard is positioned where children can easily reach it

**Given** I am on the game screen
**When** I observe the overall layout
**Then** all UI elements fit properly on both phone and tablet screens (NFR4.2)
**And** the interface maintains portrait orientation (NFR4.3)
**And** text contrast meets accessibility requirements (≥4.5:1 ratio, NFR5.2)

### Story 1.4: Core Word Gameplay

As a child learning to spell,
I want to hear spoken words and type them with immediate feedback,
So that I can learn to spell through an engaging, interactive experience.

**Acceptance Criteria:**

**Given** I am on the game screen with a word ready
**When** I tap the Play button
**Then** the ghost speaks a word using text-to-speech within 500ms (FR3.1, NFR1.2)
**And** the TTS uses the appropriate language locale (German or English)
**And** the word is spoken clearly and at child-appropriate speed

**Given** I need to hear the word again
**When** I tap the Repeat button
**Then** the ghost repeats the exact same current word (FR3.2)
**And** the audio playback timing meets the same 500ms requirement

**Given** I hear a word and start typing
**When** I press a correct letter on the keyboard
**Then** the letter appears on the grimoire with a smooth fade-in animation (FR3.3)
**And** a success sound plays immediately (FR3.4)
**And** the ghost shows a happy expression (FR3.5)
**And** the letter feedback appears within 100ms of keypress (NFR1.3)

**Given** I type an incorrect letter
**When** I press a wrong letter on the keyboard
**Then** the letter wobbles and fades away without appearing permanently (FR3.6)
**And** a gentle error sound plays (not harsh or discouraging) (FR3.7)
**And** the ghost shows an unhappy expression (FR3.8)
**And** the feedback is immediate (within 100ms) (NFR1.3)

**Given** I have typed all letters of a word correctly
**When** I complete the word
**Then** the progress bar updates to show one more word completed (FR3.9)
**And** the system loads the next word from the current star's word pool (FR3.10)
**And** the grimoire clears and prepares for the next word
**And** all animations run at 60fps for smooth experience (NFR1.4)

**Given** the game needs to provide audio feedback
**When** any sound plays (success, error, TTS)
**Then** the app functions correctly even if TTS is unavailable
**And** the game continues to work in offline mode (NFR3.5)

### Story 1.5: Ghost Character System

As a child learning to spell,
I want to interact with a responsive ghost character that shows different expressions,
So that I receive immediate, engaging visual feedback that makes learning fun and encouraging.

**Acceptance Criteria:**

**Given** I am interacting with the spelling game
**When** I observe the ghost character
**Then** the ghost displays one of four distinct expressions: neutral, happy, unhappy, or dead (FR6.1)
**And** each expression is visually clear and appropriate for children
**And** the ghost is positioned consistently in the top-right area of the game screen

**Given** I need to hear a word spoken
**When** the ghost speaks a word
**Then** the ghost uses the device's text-to-speech engine (FR6.2)
**And** the ghost's mouth or expression animates slightly during speech
**And** the speech is synchronized with appropriate ghost expressions

**Given** I type a correct letter
**When** the letter is accepted by the system
**Then** the ghost immediately shows a happy expression (FR6.3)
**And** the happy expression is clearly positive and encouraging
**And** the expression change happens within 100ms of the correct input

**Given** I type an incorrect letter
**When** the letter is rejected by the system
**Then** the ghost immediately shows an unhappy expression (FR6.4)
**And** the unhappy expression is disappointed but not scary or harsh
**And** the expression conveys gentle correction, not punishment

**Given** a failure animation is triggered (future functionality)
**When** the failure state activates
**Then** the ghost shows a "dead" expression (FR6.5)
**And** the dead expression is humorous rather than frightening
**And** the expression fits the magical, playful theme

**Given** the ghost shows any non-neutral expression
**When** 500ms passes after the reaction
**Then** the ghost automatically returns to neutral expression (FR6.6)
**And** the transition back to neutral is smooth and natural
**And** the ghost is ready to react to the next interaction

**Given** the ghost character system is active
**When** multiple interactions happen in quick succession
**Then** each expression change is clear and doesn't interfere with gameplay
**And** the ghost expressions enhance the learning experience without causing distraction

## Epic 2: Session Management & Progress

Children can complete full learning sessions (20 words), see their progress, earn stars, and experience magical celebrations. This epic adds motivation and structured learning progression on top of the core gameplay.

### Story 2.1: 20-Word Learning Sessions

As a child learning to spell,
I want to complete structured learning sessions with 20 words that are appropriately ordered and give me chances to retry,
So that I have a complete learning experience with proper difficulty progression and multiple opportunities to succeed.

**Acceptance Criteria:**

**Given** I start a new star level session
**When** the session begins
**Then** the session contains exactly 20 words for completion (FR4.1)
**And** the words are selected from the appropriate star level word pool
**And** the session progress shows "0/20" at the start
**And** the system tracks which words I need to complete

**Given** I am in a learning session
**When** the system presents words to me
**Then** shorter words are presented first, followed by longer words (FR4.2)
**And** for Star 1: 3-letter words come before 4-letter words
**And** for Star 2: 4-letter words come before 5-letter words
**And** for Star 3: 5-letter words come before 6-letter words
**And** the word order provides a natural difficulty progression

**Given** I fail to spell a word correctly within a reasonable time
**When** I struggle with or cannot complete a word
**Then** that word is returned to the word pool for retry later in the session (FR4.3)
**And** the word doesn't count as "completed" in my 20-word progress
**And** I will encounter the failed word again later in the same session
**And** the system ensures I practice difficult words multiple times

**Given** I am working through a 20-word session
**When** I complete each word successfully
**Then** the progress counter increments (1/20, 2/20, etc.)
**And** the progress bar visually updates to reflect my advancement
**And** completed words are removed from the current session pool
**And** the system selects the next appropriate word based on difficulty ordering

**Given** I have failed words in my retry pool
**When** I reach the end of the initial word sequence
**Then** the system presents my failed words for retry attempts
**And** failed words follow the same difficulty ordering (shorter first)
**And** I must successfully complete all 20 unique words before session ends
**And** the session doesn't end until I've mastered all 20 words

**Given** I am in the middle of a learning session
**When** I interact with the session system
**Then** my progress is tracked reliably and accurately
**And** word selection logic works consistently
**And** the retry mechanism functions smoothly without confusion

### Story 2.2: Progressive Difficulty System

As a child learning to spell,
I want each star level to have appropriately challenging words that get progressively longer,
So that I can build my spelling skills gradually from simple words to more complex ones.

**Acceptance Criteria:**

**Given** I am playing Star 1 level
**When** I start a 20-word session
**Then** the session contains exactly 10 three-letter words and 10 four-letter words (FR5.1)
**And** the three-letter words include words like "OHR", "ARM", "EIS", "HAT", "CAT", "SUN"
**And** the four-letter words include words like "BAUM", "HAUS", "BALL", "TREE", "FISH", "BIRD"
**And** all words are age-appropriate and within a child's vocabulary

**Given** I am playing Star 2 level
**When** I start a 20-word session
**Then** the session contains exactly 10 four-letter words and 10 five-letter words (FR5.2)
**And** the four-letter words include words like "BEIN", "TIER", "BEAR", "DOOR", "MILK"
**And** the five-letter words include words like "APFEL", "KATZE", "APPLE", "HORSE", "HOUSE"
**And** the difficulty represents a clear step up from Star 1

**Given** I am playing Star 3 level
**When** I start a 20-word session
**Then** the session contains exactly 10 five-letter words and 10 six-letter words (FR5.3)
**And** the five-letter words include words like "BIRNE", "LAMPE", "SNAKE", "BEACH", "LEMON"
**And** the six-letter words include words like "ORANGE", "BANANE", "RABBIT", "GARDEN", "CHEESE"
**And** this represents the most challenging level for the current world

**Given** I am progressing through different star levels
**When** I compare the word difficulty across stars
**Then** each star level provides an appropriate challenge increase
**And** Star 1 focuses on building basic letter recognition and short words
**And** Star 2 introduces longer words while building confidence
**And** Star 3 challenges me with the most complex spellings

**Given** I am in any star level session
**When** the system selects words for my session
**Then** words are chosen randomly from the appropriate length pools for that star
**And** I don't get the same 20 words in the exact same order every time
**And** the word selection ensures variety while maintaining difficulty progression
**And** both German and English word pools follow the same length progression rules

**Given** I complete words of different lengths within a session
**When** I encounter the progression from shorter to longer words
**Then** the difficulty increase feels natural and manageable
**And** I can clearly see my spelling abilities improving through the progression
**And** the word length progression supports my learning confidence

### Story 2.3: Session Completion & Tracking

As a child learning to spell,
I want the game to accurately track my progress and clearly indicate when I've completed a session,
So that I know when I've succeeded and can see my learning achievements.

**Acceptance Criteria:**

**Given** I am working through a 20-word learning session
**When** I successfully complete all 20 unique words
**Then** the session is marked as complete (FR4.4)
**And** the progress bar shows "20/20"
**And** the system recognizes that I have finished the current star level
**And** no additional words are presented in this session

**Given** I complete all 20 words in a star level session
**When** the session ends successfully
**Then** I earn that star permanently (FR5.8)
**And** the star is marked as "earned" in my progress data
**And** the earned star is visually distinct from unearned stars on the home screen
**And** my star achievement is saved immediately to prevent data loss (NFR3.1)

**Given** I am progressing through words in a session
**When** I complete each word successfully
**Then** the system tracks my completion internally (FR4.5)
**And** completed words are recorded for future reference
**And** the tracking includes which words I found easy or difficult
**And** this data is stored for potential future adaptive learning features

**Given** I complete a session and earn a star
**When** the achievement is processed
**Then** my progress is automatically saved to device storage (NFR3.1)
**And** the save occurs immediately after the 20th word completion
**And** my progress is preserved even if the app closes unexpectedly
**And** the star earning is permanent and cannot be lost

**Given** I have earned a star in any level
**When** I return to the home screen after session completion
**Then** my newly earned star is displayed correctly
**And** the next star level becomes available (if applicable)
**And** I can replay the completed star level without affecting my progress
**And** my overall world progression is accurately reflected

**Given** I am in the middle of a session but need to exit
**When** the app closes or I exit the session
**Then** my partial progress within the current session is noted
**And** the session can be resumed from an appropriate point
**And** completed words within the session are not lost
**And** the system maintains session integrity for proper restart

**Given** the tracking system is recording my learning data
**When** I interact with words of varying difficulty
**Then** the system internally logs my performance patterns
**And** timing data for word completion is captured
**And** error patterns are noted for future learning optimization
**And** this data foundation supports future adaptive features

### Story 2.4: Star Achievement & Celebrations

As a child learning to spell,
I want to experience magical celebrations when I earn stars,
So that my achievements feel rewarding and motivate me to continue learning.

**Acceptance Criteria:**

**Given** I complete all 20 words in a star level session
**When** the session ends and I earn a star
**Then** a stars explosion animation plays for exactly 500ms (FR5.4)
**And** the explosion effect is colorful and magical, contrasting with the black/white base design
**And** the animation captures attention and feels celebratory
**And** the stars explosion runs at 60fps for smooth visual experience (NFR1.4)

**Given** the stars explosion animation completes
**When** the initial celebration finishes
**Then** a dragon fly-through animation plays for exactly 2000ms (FR5.5)
**And** the dragon animation is vibrant and magical (the main color moment in the app)
**And** the dragon flies across or around the screen in a satisfying pattern
**And** the dragon animation maintains 60fps performance throughout

**Given** I earn different stars within the same world
**When** I achieve Star 1, Star 2, or Star 3
**Then** the dragon size increases with each star level: small → medium → large (FR5.6)
**And** Star 1 shows a small dragon that's cute and encouraging
**And** Star 2 shows a medium dragon that's more impressive
**And** Star 3 shows a large, magnificent dragon that feels like a major achievement

**Given** the dragon animation completes
**When** the celebration sequence finishes
**Then** the earned star pops into place with an animation lasting exactly 800ms (FR5.7)
**And** the star pop animation is satisfying and gives a sense of permanent achievement
**And** the star visually "locks in" to show it's now permanently earned
**And** the star pop effect has appropriate sound or visual feedback

**Given** I have earned all 3 stars in the current world (Wizard World)
**When** I complete Star 3 and the celebrations finish
**Then** the foundation is prepared for future world unlocking (FR5.9)
**And** the system tracks that I'm ready for the next world (future functionality)
**And** my progress shows complete mastery of the current world
**And** the achievement feels like a significant milestone

**Given** any celebration animation is playing
**When** I observe the magical effects
**Then** all animations run smoothly without frame drops or glitches
**And** the celebration sequence feels seamless from explosion to dragon to star pop
**And** the timing creates a satisfying, memorable reward experience
**And** colors and effects align with the magical grimoire theme

**Given** I earn a star and celebrations play
**When** the entire celebration sequence completes
**Then** I return to a normal state where I can continue playing or return to home
**And** my progress is fully saved and the star achievement is permanent
**And** the celebration doesn't interfere with my ability to continue learning
**And** I feel motivated to work toward the next star level

## Epic 3: Enhanced User Experience

Children and parents have a robust, accessible, multilingual learning experience with proper error handling and device compatibility. This epic ensures the app works reliably across different devices, languages, and usage scenarios.

### Story 3.1: Session Control & Exit Flow

As a child learning to spell,
I want to be able to exit a learning session safely when I need to stop,
So that I'm not trapped in the game and my progress is saved when I leave.

**Acceptance Criteria:**

**Given** I am on the game screen during a learning session
**When** I look at the top-left corner of the screen
**Then** I see an Exit button with a clear "X" icon (FR9.1)
**And** the Exit button has a minimum 48dp touch target for easy access
**And** the button is positioned where children can easily reach it
**And** the "X" icon is clearly visible and universally understood

**Given** I am in the middle of a learning session and want to leave
**When** I tap the Exit button
**Then** a confirmation dialog immediately appears asking "Leave session?" (FR9.2)
**And** the dialog pauses the current session without losing my progress
**And** the dialog is clearly worded for child comprehension
**And** the game state is preserved while the dialog is open

**Given** the exit confirmation dialog is displayed
**When** I see the dialog options
**Then** I see two clear buttons: "Stay" and "Leave" (FR9.3)
**And** the "Stay" button is more prominent to prevent accidental exits
**And** both buttons are appropriately sized (≥48dp) for child interaction
**And** the button labels are clear and age-appropriate

**Given** I decide I want to continue playing
**When** I tap the "Stay" button in the confirmation dialog
**Then** the dialog dismisses immediately (FR9.5)
**And** I return to the exact game state I was in before tapping Exit
**And** the current word, progress, and session continue unchanged
**And** no progress or data is lost from the interruption

**Given** I decide I want to leave the session
**When** I tap the "Leave" button in the confirmation dialog
**Then** my current word progress is immediately saved to prevent data loss (FR9.4)
**And** I am returned to the Home screen within 2 seconds
**And** my completed words within the session are preserved
**And** I can resume the session later from an appropriate point

**Given** I have partially completed a session and exited
**When** I return to the game later
**Then** my progress is accurately restored
**And** words I completed before exiting are not repeated unnecessarily
**And** the session can continue logically from where I left off
**And** my star progress and overall achievements remain intact

**Given** I am using the exit functionality
**When** I interact with the exit flow multiple times
**Then** the system reliably saves my progress each time (NFR3.2)
**And** no data corruption or loss occurs from repeated exits
**And** the exit flow works consistently across different devices
**And** the child safety concern of being "trapped in sessions" is completely resolved

### Story 3.2: Failure Handling & Timeouts

As a child learning to spell,
I want the game to gently encourage me when I'm struggling and make failures fun rather than frustrating,
So that I stay motivated to keep trying even when words are difficult.

**Acceptance Criteria:**

**Given** I am on the game screen with a word active
**When** I don't press any key for 8 seconds
**Then** the ghost shows an encouraging expression to motivate me (FR7.1)
**And** the encouraging expression is warm, supportive, and child-friendly
**And** the ghost might nod or gesture in a way that says "you can do it"
**And** the encouragement doesn't interrupt my thinking process

**Given** I am struggling with a word and making incorrect attempts
**When** I haven't pressed a correct key for 20 seconds
**Then** a funny "failure" animation is triggered (FR7.2)
**And** the animation is designed to make me laugh rather than feel bad
**And** the failure state is treated as part of the fun, not as punishment
**And** the 20-second timer resets with each correct letter I type

**Given** the funny failure animation is triggered
**When** the animation plays
**Then** the ghost shows a "dead" expression as part of the humor (FR7.3)
**And** the "dead" expression is cartoonish and silly, not scary
**And** the ghost might "faint" dramatically or show tongue sticking out
**And** the dead expression fits the magical, playful theme of the game

**Given** I am experiencing the failure animation
**When** I watch the animation sequence
**Then** the animation is designed to make me laugh, not feel discouraged (FR7.4)
**And** the failure is treated as a funny moment in the magical adventure
**And** the animation might include silly sounds or visual effects
**And** the overall tone is playful and maintains my learning confidence

**Given** the failure animation completes
**When** the funny sequence finishes
**Then** I can immediately retry the same word (FR7.5)
**And** the word is repeated using TTS so I can hear it again
**And** the grimoire clears and resets for my next attempt
**And** no progress is lost - the word remains in my current session

**Given** I retry a word after a failure animation
**When** I attempt the word again
**Then** the system treats it as a fresh attempt with full encouragement
**And** the ghost returns to normal expressions and reactions
**And** I get the same positive feedback for correct letters as always
**And** the retry doesn't count against me or create any negative consequences

**Given** I encounter multiple timeouts or failures in a session
**When** the encouragement and failure systems activate repeatedly
**Then** the encouragement remains consistent and supportive
**And** failure animations stay fun without becoming repetitive or annoying
**And** the system maintains my motivation to continue learning
**And** I never feel punished or frustrated by the timeout mechanisms

**Given** the timeout and failure systems are active
**When** I'm playing through various difficulty levels
**Then** the encouragement timing feels appropriate for my age and skill level
**And** the 8-second and 20-second timeouts work well for child attention spans
**And** the failure handling helps me learn that mistakes are part of learning
**And** I feel supported throughout my spelling journey

### Story 3.3: Language Support & Switching

As a child learning to spell in my native language,
I want the app to automatically use my device's language and provide proper support for German or English spelling,
So that I can learn in the language I'm most comfortable with and spell words correctly including special characters.

**Acceptance Criteria:**

**Given** I launch the app for the first time
**When** the app initializes
**Then** it automatically detects my device's system language setting (FR8.8)
**And** if my system language is German, the app uses German as the learning language
**And** if my system language is English, the app uses English as the learning language
**And** if my system language is neither German nor English, the app defaults to English (FR8.9)

**Given** the app is running in German language mode
**When** I start any learning session
**Then** the system uses the complete German word list with 60 unique words (FR8.1, FR8.3)
**And** words include proper German vocabulary like "BAUM", "KATZE", "ORANGE"
**And** all word lists follow the corrected version (replacing "NUS" with a valid word)
**And** TTS uses German locale for proper pronunciation (FR8.6)

**Given** the app is running in English language mode
**When** I start any learning session
**Then** the system uses the complete English word list with 60 unique words (FR8.2, FR8.4)
**And** words include proper English vocabulary like "TREE", "HOUSE", "APPLE"
**And** English words are completely separate from the German word pool
**And** TTS uses English locale for proper pronunciation (FR8.6)

**Given** I am playing in German mode and need to type German characters
**When** I long-press on the A, O, U, or S keys for 500ms
**Then** the system inputs the appropriate umlaut character (Ä, Ö, Ü, ß) (FR8.5)
**And** the long-press mechanic is intuitive and responsive
**And** visual feedback shows the umlaut options during long-press
**And** German words requiring umlauts can be spelled correctly

**Given** the app is running in any supported language
**When** I interact with the user interface
**Then** all UI text is properly localized for that language (FR8.7)
**And** German mode shows German text: "SPELL WRITER", instructions in German
**And** English mode shows English text: "SPELL WRITER", instructions in English
**And** button labels, messages, and help text match the selected language

**Given** I am using the app with TTS functionality
**When** words are spoken by the ghost character
**Then** the TTS engine uses the correct language locale matching the app language (FR8.6)
**And** German words are pronounced with German TTS voice
**And** English words are pronounced with English TTS voice
**And** pronunciation is clear and appropriate for children learning to spell

**Given** I want to see which language the app is currently using
**When** I am on the home screen
**Then** there is a subtle language indicator showing "Deutsch" or "English"
**And** the indicator is visible but not distracting from the main interface
**And** I can understand which language mode is currently active
**And** the language setting persists across app sessions

**Given** I switch between German and English on my device
**When** I change my system language setting and restart the app
**Then** the app respects the new language choice
**And** word lists, TTS, and UI automatically update to match
**And** my learning progress is maintained separately for each language
**And** the language switching works reliably across different devices

**Given** the language system is active across all app features
**When** I use any part of the app (home, game, progress, etc.)
**Then** the language consistency is maintained throughout
**And** no English text appears in German mode (and vice versa)
**And** the complete language experience feels native and appropriate
**And** children can learn confidently in their chosen language

### Story 3.4: Error Handling & Device Robustness

As a child learning to spell on any device,
I want the app to work reliably even when there are audio problems or technical issues,
So that I can always continue learning regardless of my device's limitations.

**Acceptance Criteria:**

**Given** I am using a device where the TTS engine is unavailable or not installed
**When** I try to use the audio features of the game
**Then** a clear message displays: "Voice not available - use keyboard only" (FR10.1)
**And** the message is child-friendly and not alarming or technical
**And** the game continues to function in a visual-only mode
**And** I can still complete words by seeing them displayed instead of hearing them

**Given** the TTS engine fails during gameplay
**When** I tap the Play or Repeat buttons
**Then** I can retry the audio by using the Repeat button (FR10.2)
**And** the system attempts to reinitialize the TTS engine
**And** if TTS remains unavailable, the fallback message is shown
**And** the retry mechanism doesn't cause crashes or freeze the app

**Given** my device is muted or has volume turned off
**When** I am playing the game
**Then** a visual indicator shows that the device is muted (FR10.3)
**And** a small mute icon appears near the audio controls
**And** the indicator helps me understand why I can't hear words
**And** the game continues to work in silent mode with visual cues only

**Given** I am using a device with limited TTS support or audio issues
**When** any audio-related problems occur
**Then** the app continues to function without crashing (FR10.4)
**And** no error causes the app to close unexpectedly
**And** audio failures are handled gracefully with appropriate fallbacks
**And** I can always complete my learning session even without sound

**Given** the app is running in fallback mode due to audio issues
**When** I play the game without TTS
**Then** the current word is displayed visually on screen
**And** I can see the word I need to spell clearly
**And** the typing and feedback systems work exactly the same as with audio
**And** the ghost character still provides visual feedback and expressions

**Given** I am using the app on various devices with different capabilities
**When** I encounter different audio or technical limitations
**Then** the error handling adapts appropriately to each situation
**And** older devices with limited TTS still provide a good experience
**And** newer devices with full audio work optimally
**And** the app maintains educational value regardless of device limitations

**Given** TTS errors occur during extended use
**When** I play multiple sessions over time
**Then** the error handling remains consistent and reliable
**And** temporary audio issues don't permanently break the TTS functionality
**And** the app attempts to restore audio capabilities when possible
**And** my learning progress is never interrupted by technical problems

**Given** I am a parent checking that the app works reliably
**When** I observe my child using the app across different scenarios
**Then** the app handles all common device issues gracefully
**And** my child can always continue learning even with technical problems
**And** error messages are informative but not frightening for children
**And** the app maintains its educational effectiveness in all circumstances