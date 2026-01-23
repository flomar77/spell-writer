package com.spellwriter.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for timeout behavior constants and logic.
 * Story 3.2: Timeout tracking for encouragement animation.
 *
 * Full integration tests requiring coroutine time control are in instrumentation tests.
 */
class TimeoutBehaviorTest {

    @Test
    fun timeoutConstants_areSetCorrectly() {
        assertEquals(
            "Encouragement timeout should be 8 seconds",
            8_000L,
            GameViewModel.ENCOURAGEMENT_TIMEOUT_MS
        )

        assertEquals(
            "Timer tick should be 1 second",
            1_000L,
            GameViewModel.TIMER_TICK_MS
        )
    }
}
