package com.spellwriter

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.WordPool
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.data.repository.WordsRepository
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme

/**
 * MainActivity for Stories 1.1, 1.2, and 2.3 - Navigation and progress management.
 * Story 1.1: Basic screen navigation between Home and Game screens.
 * Story 1.2: Adds Progress state management and star replay functionality.
 * Story 2.3: Adds DataStore persistence and lifecycle-aware saving.
 */
class MainActivity : ComponentActivity() {

    // Story 2.3: ProgressRepository for persistence (AC4, AC6)
    private lateinit var progressRepository: ProgressRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Story 2.3: Initialize repository (AC4)
        progressRepository = ProgressRepository(this)

        // Initialize WordsRepository and inject into WordPool
        val wordsRepository = WordsRepository(applicationContext)
        WordPool.repository = wordsRepository

        // Story 2.3: Add lifecycle observer for persistence (AC6, NFR3.2)
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Save will be called from GameScreen/ViewModel if active
                // This is handled in Task 6
            }
        })

        setContent {
            SpellWriterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpellWriterApp(progressRepository)
                }
            }
        }
    }


    // Recreate the activity to apply the new locale
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}

/**
 * Main app composable that handles navigation and progress state.
 * Story 1.1: Simple sealed class navigation.
 * Story 1.2: Adds Progress and selectedStar state management for star replay.
 * Story 2.3: Loads progress from DataStore and persists star completion (AC4, AC5).
 */
@Composable
fun SpellWriterApp(progressRepository: ProgressRepository) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedStar by remember { mutableStateOf<Int?>(null) }  // Story 1.2: for replay

    // Story 2.3: Load progress from repository (AC4, NFR3.3)
    val progress by progressRepository.progressFlow.collectAsState(initial = Progress())

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                progress = progress,  // Story 1.2, 2.3
                onPlayClick = {
                    selectedStar = null  // Auto-select current star
                    currentScreen = Screen.Game
                },
                onStarClick = { starNumber ->  // Story 1.2
                    selectedStar = starNumber  // Replay specific star
                    currentScreen = Screen.Game
                }
            )
        }

        is Screen.Game -> {
            GameScreen(
                starNumber = selectedStar ?: progress.getCurrentStar(),  // Story 1.2
                isReplaySession = selectedStar != null,  // Story 1.2
                progressRepository = progressRepository,  // Story 2.3
                currentProgress = progress,  // Story 2.3
                onBackPress = {
                    currentScreen = Screen.Home
                },
                onStarComplete = { completedStar ->  // Story 1.2, 2.3
                    // Star completion now handled by GameViewModel persistence
                    // Just navigate back to home
                    currentScreen = Screen.Home
                }
            )
        }
    }
}

/**
 * Sealed class representing navigation screens.
 * Story 1.1 implements Home and Game.
 * Future stories may add additional screens.
 */
sealed class Screen {
    object Home : Screen()
    object Game : Screen()
}

