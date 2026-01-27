package com.spellwriter

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import com.spellwriter.data.models.LanguageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spellwriter.audio.AudioManager
import com.spellwriter.data.models.AppLanguage
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.WordPool
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.data.repository.WordsRepository
import com.spellwriter.ui.screens.GameScreen
import com.spellwriter.ui.screens.HomeScreen
import com.spellwriter.ui.theme.SpellWriterTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

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
}

/**
 * Main app composable that handles navigation and progress state.
 * Story 1.1: Simple sealed class navigation.
 * Story 1.2: Adds Progress and selectedStar state management for star replay.
 * Story 2.3: Loads progress from DataStore and persists star completion (AC4, AC5).
 */
@Composable
fun SpellWriterApp(progressRepository: ProgressRepository) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedStar by remember { mutableStateOf<Int?>(null) }  // Story 1.2: for replay

    // TTS initialization state management
    var audioManager by remember { mutableStateOf<AudioManager?>(null) }
    var isTTSInitializing by remember { mutableStateOf(false) }
    var ttsError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Track current language for locale-aware context
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }

    // Create locale-wrapped context
    val localizedContext = remember(currentLanguage) {
        createLocaleContext(context, currentLanguage)
    }

    // Story 2.3: Load progress from repository (AC4, NFR3.3)
    val progress by progressRepository.progressFlow.collectAsState(initial = Progress())

    // TTS initialization function
    fun initializeTTS(starNumber: Int? = null) {
        // Guard clause: prevent double-click
        if (isTTSInitializing) return

        // Reset state and start initialization
        isTTSInitializing = true
        ttsError = null

        // Convert language string to AppLanguage enum
        val appLanguage = when (currentLanguage) {
            "de" -> AppLanguage.GERMAN
            "en" -> AppLanguage.ENGLISH
            else -> AppLanguage.ENGLISH // fallback
        }

        // Create AudioManager
        audioManager = AudioManager(localizedContext, appLanguage)

        // Launch coroutine with timeout
        coroutineScope.launch {
            val isReady = withTimeoutOrNull(5000L) {
                // Wait for TTS to be ready
                audioManager?.isTTSReady?.first { it }
            }

            if (isReady == true) {
                // Success: navigate to game
                isTTSInitializing = false
                selectedStar = starNumber
                currentScreen = Screen.Game
            } else {
                // Timeout: show error but still navigate
                ttsError = localizedContext.getString(R.string.home_tts_error)
                isTTSInitializing = false
                selectedStar = starNumber
                currentScreen = Screen.Game
            }
        }
    }

    // Cleanup AudioManager on app dispose
    DisposableEffect(Unit) {
        onDispose {
            audioManager?.release()
        }
    }

    // Provide the localized context to all composables
    CompositionLocalProvider(LocalContext provides localizedContext) {
        when (currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    progress = progress,  // Story 1.2, 2.3
                    onPlayClick = {
                        // Check if TTS is already ready
                        if (audioManager?.isTTSReady?.value == true) {
                            // Navigate immediately
                            selectedStar = null
                            currentScreen = Screen.Game
                        } else {
                            // Initialize TTS first
                            initializeTTS(starNumber = null)
                        }
                    },
                    onStarClick = { starNumber ->  // Story 1.2
                        // Check if TTS is already ready
                        if (audioManager?.isTTSReady?.value == true) {
                            // Navigate immediately
                            selectedStar = starNumber
                            currentScreen = Screen.Game
                        } else {
                            // Initialize TTS first
                            initializeTTS(starNumber = starNumber)
                        }
                    },
                    onLanguageChanged = { newLanguage ->
                        // Release existing AudioManager
                        audioManager?.release()
                        audioManager = null
                        isTTSInitializing = false
                        ttsError = null

                        // Update language state to trigger recomposition with new locale
                        currentLanguage = newLanguage
                    },
                    isTTSInitializing = isTTSInitializing,
                    ttsError = ttsError
                )
            }

            is Screen.Game -> {
                GameScreen(
                    starNumber = selectedStar ?: progress.getCurrentStar(),  // Story 1.2
                    isReplaySession = selectedStar != null,  // Story 1.2
                    progressRepository = progressRepository,  // Story 2.3
                    currentProgress = progress,  // Story 2.3
                    audioManager = audioManager,  // Pass AudioManager for TTS
                    onBackPress = {
                        // Keep audioManager in memory for reuse
                        currentScreen = Screen.Home
                    },
                    onStarComplete = { completedStar ->  // Story 1.2, 2.3
                        // Star completion now handled by GameViewModel persistence
                        // Keep audioManager in memory for reuse
                        currentScreen = Screen.Home
                    }
                )
            }
        }
    }
}

/**
 * Creates a context wrapper with the specified locale.
 * This allows string resources to be loaded in the correct language.
 */
fun createLocaleContext(context: Context, languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)

    return context.createConfigurationContext(configuration)
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

