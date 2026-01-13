package com.spellwriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.spellwriter.data.models.World
import com.spellwriter.data.models.getCurrentStar
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme
import com.spellwriter.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpellWriterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpellWriterApp(gameViewModel)
                }
            }
        }
    }
}

@Composable
fun SpellWriterApp(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val gameState by viewModel.gameState.collectAsState()
    val ghostExpression by viewModel.ghostExpression.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val showDragonAnimation by viewModel.showDragonAnimation.collectAsState()
    val showStarAnimation by viewModel.showStarAnimation.collectAsState()

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                progress = progress,
                onPlayClick = {
                    // Start next incomplete star
                    val world = progress.currentWorld
                    val star = progress.getCurrentStar(world)
                    viewModel.startNewSession(star)
                    currentScreen = Screen.Game
                },
                onStarClick = { world, star ->
                    viewModel.startNewSession(star)
                    currentScreen = Screen.Game
                }
            )
        }
        is Screen.Game -> {
            val earnedStars = when (progress.currentWorld) {
                World.WIZARD -> progress.wizardStars
                World.PIRATE -> progress.pirateStars
            }

            GameScreen(
                gameState = gameState,
                ghostExpression = ghostExpression,
                earnedStars = earnedStars,
                onPlayClick = { viewModel.speakCurrentWord() },
                onRepeatClick = { viewModel.speakCurrentWord() },
                onKeyPressed = { letter -> viewModel.onLetterTyped(letter) },
                onStarClick = { star ->
                    viewModel.startNewSession(star)
                },
                showDragonAnimation = showDragonAnimation,
                showStarAnimation = showStarAnimation,
                onDragonAnimationComplete = {
                    viewModel.onDragonAnimationComplete()
                    // Return home after animations
                    if (!showStarAnimation) {
                        currentScreen = Screen.Home
                    }
                },
                onStarAnimationComplete = {
                    viewModel.onStarAnimationComplete()
                    // Return home after animations
                    if (!showDragonAnimation) {
                        currentScreen = Screen.Home
                    }
                }
            )
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Game : Screen()
}
