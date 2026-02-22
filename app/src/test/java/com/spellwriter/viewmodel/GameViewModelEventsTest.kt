package com.spellwriter.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.spellwriter.data.models.Progress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for one-shot event behavior in GameViewModel.
 * Verifies Channel-based events are consumed once and not redelivered.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], instrumentedPackages = ["androidx.loader.content"])
class GameViewModelEventsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun createViewModel(
        starNumber: Int = 1,
        isReplaySession: Boolean = false
    ): GameViewModel {
        return GameViewModel(
            context = context,
            starNumber = starNumber,
            isReplaySession = isReplaySession,
            progressRepository = null,
            sessionRepository = null,
            initialProgress = Progress()
        )
    }

    @Test
    fun navigationEvent_doesNotEmit_onInit() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<Unit>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvents.collect { events.add(Unit) }
        }

        advanceUntilIdle()
        assertTrue("Navigation should not emit on init", events.isEmpty())
        job.cancel()
    }

    @Test
    fun navigationEvent_firesOnce_notRedeliveredOnResubscribe() = runTest {
        val viewModel = createViewModel(isReplaySession = true)

        // Trigger navigation
        viewModel.continueToNextStar()
        advanceUntilIdle()

        // First subscriber collects it
        val firstEvents = mutableListOf<Unit>()
        val job1 = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvents.collect { firstEvents.add(Unit) }
        }
        advanceUntilIdle()
        job1.cancel()

        assertEquals("First subscriber should get the event", 1, firstEvents.size)

        // Second subscriber should NOT get the same event
        val secondEvents = mutableListOf<Unit>()
        val job2 = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvents.collect { secondEvents.add(Unit) }
        }
        advanceUntilIdle()
        job2.cancel()

        assertTrue("Second subscriber should not get redelivered event", secondEvents.isEmpty())
    }

    @Test
    fun audioEvent_emitsOnTrigger() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<Unit>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.audioEvents.collect { events.add(Unit) }
        }

        advanceUntilIdle()
        // Init triggers one audio event (loadWordsForStar calls triggerAudioPlayback)
        assertTrue("Audio event should emit after init", events.isNotEmpty())
        job.cancel()
    }

    @Test
    fun playButton_debounce_blocksWithin500ms() {
        val viewModel = createViewModel()

        // First click should succeed
        assertTrue("First click should be allowed", viewModel.onPlayButtonClicked())

        // Immediate second click should be blocked
        assertFalse("Second click within 500ms should be blocked", viewModel.onPlayButtonClicked())
    }

    @Test
    fun playButton_debounce_allowsAfter500ms() {
        val viewModel = createViewModel()

        viewModel.onPlayButtonClicked()

        // Simulate time passing (set last click to 600ms ago)
        viewModel.resetPlayDebounce()

        assertTrue("Click after debounce period should be allowed", viewModel.onPlayButtonClicked())
    }

    @Test
    fun replayButton_debounce_independent() {
        val viewModel = createViewModel()

        // Play button clicked
        assertTrue("Play click should be allowed", viewModel.onPlayButtonClicked())

        // Replay should still work (independent debounce)
        assertTrue("Replay click should be allowed independently", viewModel.onReplayButtonClicked())
    }
}
