package com.spellwriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme

/**
 * MainActivity for Story 1.1 - Simple navigation foundation.
 * Implements basic screen navigation between Home and Game screens.
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
 * Main app composable that handles navigation between screens.
 * Uses simple sealed class for screen state (MVVM pattern foundation).
 */
@Composable
fun SpellWriterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                onPlayClick = {
                    currentScreen = Screen.Game
                }
            )
        }
        is Screen.Game -> {
            GameScreen(
                onBackPress = {
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

