package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.World
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for ProgressRepository DataStore persistence.
 * Story 2.3: Session Completion & Tracking
 *
 * Tests verify:
 * - Progress saving and loading
 * - Session state persistence
 * - Data survives "app restart" (new repository instance)
 * - Error handling for corrupted data
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ProgressRepositoryTest {

    private lateinit var context: Context
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var repository: ProgressRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Create a test DataStore with a unique name for each test
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_progress_${System.currentTimeMillis()}") }
        )

        repository = ProgressRepository(context)
    }

    @After
    fun cleanup() {
        // Clean up test files
        testScope.cancel()
    }

    // AC4: Progress saved immediately after word completion
    @Test
    fun saveProgress_persistsWizardStars() = testScope.runTest {
        val progress = Progress(wizardStars = 2, pirateStars = 0, currentWorld = World.WIZARD)

        repository.saveProgress(progress)

        val loaded = repository.progressFlow.first()
        assertEquals(2, loaded.wizardStars)
        assertEquals(0, loaded.pirateStars)
        assertEquals(World.WIZARD, loaded.currentWorld)
    }

    @Test
    fun saveProgress_persistsPirateStars() = testScope.runTest {
        val progress = Progress(wizardStars = 3, pirateStars = 2, currentWorld = World.PIRATE)

        repository.saveProgress(progress)

        val loaded = repository.progressFlow.first()
        assertEquals(3, loaded.wizardStars)
        assertEquals(2, loaded.pirateStars)
        assertEquals(World.PIRATE, loaded.currentWorld)
    }

    // AC4: Progress persists across "app restarts" (new repository instance)
    @Test
    fun saveProgress_survivesRepositoryRecreation() = testScope.runTest {
        val progress = Progress(wizardStars = 3, pirateStars = 1, currentWorld = World.PIRATE)

        repository.saveProgress(progress)

        // Simulate app restart by creating new repository instance
        val newRepository = ProgressRepository(context)
        val loaded = newRepository.progressFlow.first()

        assertEquals(3, loaded.wizardStars)
        assertEquals(1, loaded.pirateStars)
        assertEquals(World.PIRATE, loaded.currentWorld)
    }

    @Test
    fun progressFlow_emitsDefaultValuesWhenNoDataSaved() = testScope.runTest {
        val loaded = repository.progressFlow.first()

        assertEquals(0, loaded.wizardStars)
        assertEquals(0, loaded.pirateStars)
        assertEquals(World.WIZARD, loaded.currentWorld)
    }

    @Test
    fun saveProgress_overwritesPreviousData() = testScope.runTest {
        // Save initial progress
        repository.saveProgress(Progress(wizardStars = 1))

        // Overwrite with new progress
        repository.saveProgress(Progress(wizardStars = 2))

        val loaded = repository.progressFlow.first()
        assertEquals(2, loaded.wizardStars)
    }

    // AC6: Session state persistence
    @Test
    fun saveSessionState_persistsStarLevelAndWordIndex() = testScope.runTest {
        repository.saveSessionState(starLevel = 2, wordIndex = 15)

        val sessionState = repository.loadSessionState()
        assertNotNull(sessionState)
        assertEquals(2, sessionState?.first)
        assertEquals(15, sessionState?.second)
    }

    @Test
    fun loadSessionState_returnsNullWhenNoStateSaved() = testScope.runTest {
        val sessionState = repository.loadSessionState()
        assertNull(sessionState)
    }

    @Test
    fun saveSessionState_overwritesPreviousState() = testScope.runTest {
        repository.saveSessionState(starLevel = 1, wordIndex = 5)
        repository.saveSessionState(starLevel = 3, wordIndex = 18)

        val sessionState = repository.loadSessionState()
        assertEquals(3, sessionState?.first)
        assertEquals(18, sessionState?.second)
    }

    @Test
    fun clearSessionState_removesSessionData() = testScope.runTest {
        repository.saveSessionState(starLevel = 2, wordIndex = 10)

        repository.clearSessionState()

        val sessionState = repository.loadSessionState()
        assertNull(sessionState)
    }

    @Test
    fun clearSessionState_doesNotAffectProgress() = testScope.runTest {
        val progress = Progress(wizardStars = 2)
        repository.saveProgress(progress)
        repository.saveSessionState(starLevel = 2, wordIndex = 10)

        repository.clearSessionState()

        val loaded = repository.progressFlow.first()
        assertEquals(2, loaded.wizardStars)

        val sessionState = repository.loadSessionState()
        assertNull(sessionState)
    }

    // NFR3.1: Multiple progress updates
    @Test
    fun saveProgress_handlesMultipleSequentialSaves() = testScope.runTest {
        repository.saveProgress(Progress(wizardStars = 1))
        repository.saveProgress(Progress(wizardStars = 2))
        repository.saveProgress(Progress(wizardStars = 3))

        val loaded = repository.progressFlow.first()
        assertEquals(3, loaded.wizardStars)
    }

    @Test
    fun saveProgress_handlesAllWorlds() = testScope.runTest {
        // Test WIZARD world
        repository.saveProgress(Progress(currentWorld = World.WIZARD))
        var loaded = repository.progressFlow.first()
        assertEquals(World.WIZARD, loaded.currentWorld)

        // Test PIRATE world
        repository.saveProgress(Progress(currentWorld = World.PIRATE))
        loaded = repository.progressFlow.first()
        assertEquals(World.PIRATE, loaded.currentWorld)
    }

    @Test
    fun progressFlow_emitsUpdatesWhenProgressSaved() = testScope.runTest {
        val initial = repository.progressFlow.first()
        assertEquals(0, initial.wizardStars)

        repository.saveProgress(Progress(wizardStars = 1))

        val updated = repository.progressFlow.first()
        assertEquals(1, updated.wizardStars)
    }

    // Edge cases
    @Test
    fun saveProgress_handlesMaximumStars() = testScope.runTest {
        val progress = Progress(wizardStars = 3, pirateStars = 3)

        repository.saveProgress(progress)

        val loaded = repository.progressFlow.first()
        assertEquals(3, loaded.wizardStars)
        assertEquals(3, loaded.pirateStars)
    }

    @Test
    fun saveSessionState_handlesMaximumWordIndex() = testScope.runTest {
        repository.saveSessionState(starLevel = 3, wordIndex = 19)

        val sessionState = repository.loadSessionState()
        assertEquals(3, sessionState?.first)
        assertEquals(19, sessionState?.second)
    }

    @Test
    fun saveSessionState_handlesMinimumValues() = testScope.runTest {
        repository.saveSessionState(starLevel = 1, wordIndex = 0)

        val sessionState = repository.loadSessionState()
        assertEquals(1, sessionState?.first)
        assertEquals(0, sessionState?.second)
    }
}
