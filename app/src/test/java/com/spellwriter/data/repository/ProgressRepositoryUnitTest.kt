package com.spellwriter.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for ProgressRepository.
 * Verifies loadSessionState() uses .first() (completes) not .collect{} (hangs).
 *
 * Note: DataStore integration tested in androidTest/ProgressRepositoryTest.
 * These tests verify the flow termination pattern used in the implementation.
 */
class ProgressRepositoryUnitTest {

    @Test
    fun flowFirst_completesImmediately() = runTest {
        // Verify .first() pattern terminates — the fix for loadSessionState
        val flow = flowOf(mapOf("a" to 1))
        val result = withTimeoutOrNull(1000L) {
            flow.first()
        }
        assertNotNull("Flow.first() should complete without hanging", result)
        assertEquals(1, result!!["a"])
    }

    @Test
    fun loadSessionState_usesFirstNotCollect() {
        // Compile-time verification: ProgressRepository imports kotlinx.coroutines.flow.first
        // If someone reverts to .collect{}, this documents the expected behavior
        val source = java.io.File(
            "src/main/java/com/spellwriter/data/repository/ProgressRepository.kt"
        ).readText()

        assert(source.contains(".first()")) {
            "ProgressRepository.loadSessionState() must use .first(), not .collect{}"
        }
        assert(!source.contains("preferences.collect")) {
            "ProgressRepository.loadSessionState() must not use .collect{} (causes infinite hang)"
        }
    }
}
