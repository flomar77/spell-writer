package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellwriter.data.models.MAX_STARS
import com.spellwriter.data.models.Progress
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

        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_progress_${System.currentTimeMillis()}") }
        )

        repository = ProgressRepository(context)
    }

    @After
    fun cleanup() {
        testScope.cancel()
    }

    @Test
    fun saveProgress_persistsStars() = testScope.runTest {
        val progress = Progress(stars = 2)

        repository.saveProgress(progress)

        val loaded = repository.progressFlow.first()
        assertEquals(2, loaded.stars)
    }

    @Test
    fun saveProgress_survivesRepositoryRecreation() = testScope.runTest {
        val progress = Progress(stars = MAX_STARS)

        repository.saveProgress(progress)

        val newRepository = ProgressRepository(context)
        val loaded = newRepository.progressFlow.first()

        assertEquals(MAX_STARS, loaded.stars)
    }

    @Test
    fun progressFlow_emitsDefaultValuesWhenNoDataSaved() = testScope.runTest {
        val loaded = repository.progressFlow.first()

        assertEquals(0, loaded.stars)
    }

    @Test
    fun saveProgress_overwritesPreviousData() = testScope.runTest {
        repository.saveProgress(Progress(stars = 1))
        repository.saveProgress(Progress(stars = 2))

        val loaded = repository.progressFlow.first()
        assertEquals(2, loaded.stars)
    }

    @Test
    fun saveProgress_handlesMultipleSequentialSaves() = testScope.runTest {
        repository.saveProgress(Progress(stars = 1))
        repository.saveProgress(Progress(stars = 2))
        repository.saveProgress(Progress(stars = 3))

        val loaded = repository.progressFlow.first()
        assertEquals(3, loaded.stars) // intermediate value, not max
    }

    @Test
    fun progressFlow_emitsUpdatesWhenProgressSaved() = testScope.runTest {
        val initial = repository.progressFlow.first()
        assertEquals(0, initial.stars)

        repository.saveProgress(Progress(stars = 1))

        val updated = repository.progressFlow.first()
        assertEquals(1, updated.stars)
    }

    @Test
    fun saveProgress_handlesMaximumStars() = testScope.runTest {
        val progress = Progress(stars = MAX_STARS)

        repository.saveProgress(progress)

        val loaded = repository.progressFlow.first()
        assertEquals(MAX_STARS, loaded.stars)
    }
}
