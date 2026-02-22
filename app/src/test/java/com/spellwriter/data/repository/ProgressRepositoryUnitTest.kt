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
 *
 * Note: DataStore integration tested in androidTest/ProgressRepositoryTest.
 * Session state methods (loadSessionState, saveSessionState, clearSessionState)
 * removed — session persistence is now solely owned by SessionRepository.
 */
class ProgressRepositoryUnitTest {

    @Test
    fun flowFirst_completesImmediately() = runTest {
        // Verify .first() pattern terminates (general Flow contract test)
        val flow = flowOf(mapOf("a" to 1))
        val result = withTimeoutOrNull(1000L) {
            flow.first()
        }
        assertNotNull("Flow.first() should complete without hanging", result)
        assertEquals(1, result!!["a"])
    }
}
