package com.spellwriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme

/**
 * MainActivity for Stories 1.1 & 1.2 - Navigation and progress management.
 * Story 1.1: Basic screen navigation between Home and Game screens.
 * Story 1.2: Adds Progress state management and star replay functionality.
 * No ViewModel yet - just state management with mutableStateOf.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpellWriterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpellWriterApp()
                }
            }
        }
    }
}

/**
 * Main app composable that handles navigation and progress state.
 * Story 1.1: Simple sealed class navigation.
 * Story 1.2: Adds Progress and selectedStar state management for star replay.
 */
@Composable
fun SpellWriterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var progress by remember { mutableStateOf(Progress()) }  // Story 1.2
    var selectedStar by remember { mutableStateOf<Int?>(null) }  // Story 1.2: for replay

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                progress = progress,  // Story 1.2
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
                onBackPress = {
                    currentScreen = Screen.Home
                },
                onStarComplete = { completedStar ->  // Story 1.2
                    // Update progress when star completed (only if not replay)
                    if (selectedStar == null) {
                        // Validate completedStar is in valid range (1-3)
                        val validatedStar = completedStar.coerceIn(1, 3)
                        progress = progress.copy(
                            wizardStars = maxOf(progress.wizardStars, validatedStar)
                        )
                    }
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

