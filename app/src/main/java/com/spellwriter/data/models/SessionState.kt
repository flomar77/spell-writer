package com.spellwriter.data.models

/**
 * Session state machine for exit flow management.
 * Story 3.1: Session Control & Exit Flow
 *
 * State transitions:
 * - ACTIVE: Normal gameplay, user is actively playing
 * - EXITED: User confirmed exit, triggers navigation to Home
 */
enum class SessionState {
    /**
     * Normal gameplay state.
     * The user is actively playing the game and can interact normally.
     */
    ACTIVE,

    /**
     * User has confirmed exit.
     * This state triggers navigation back to the Home screen.
     * ViewModel should reset to ACTIVE after navigation completes.
     */
    EXITED
}
