package com.spellwriter.ui.screens

import LanguageSwitcher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.spellwriter.R
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.data.models.Progress
import com.spellwriter.data.repository.ProgressRepository
import com.spellwriter.ui.components.CelebrationSequence
import com.spellwriter.ui.components.CompletedWordsList
import com.spellwriter.ui.components.Ghost
import com.spellwriter.ui.components.Grimoire
import com.spellwriter.ui.components.SpellKeyboard
import com.spellwriter.ui.components.StarProgress
import com.spellwriter.viewmodel.GameViewModel

/**
 * Game Screen with complete layout and gameplay logic.
 * Story 1.1: Basic stub for navigation testing.
 * Story 1.2: Accepts starNumber and isReplaySession parameters for word selection.
 * Story 1.3: Complete functional layout with all UI components.
 * Story 1.4: Integrated GameViewModel for complete gameplay (AC: All)
 * Story 1.5: Added ghost expression and speaking state management (AC2, AC3, AC4, AC6)
 * Story 2.1: Added session completion detection and callback trigger (AC6)
 * Story 2.3: Added ProgressRepository integration for persistence (AC2, AC4)
 * Story 3.1: Added exit button and confirmation dialog (AC1, AC2, AC3, AC4, AC5)
 *
 * @param starNumber The star level (1, 2, or 3) to play (Story 1.2)
 * @param isReplaySession If true, don't update progress when completing (Story 1.2)
 * @param progressRepository Repository for persisting progress (Story 2.3)
 * @param currentProgress Current progress state (Story 2.3)
 * @param onBackPress Callback for back navigation
 * @param onStarComplete Callback when star is completed, passes completed star number (Story 1.2)
 * @param modifier Optional modifier for the screen
 */
@Composable
fun GameScreen(
    starNumber: Int = 1,
    isReplaySession: Boolean = false,
    progressRepository: ProgressRepository? = null,
    currentProgress: Progress = Progress(),
    onBackPress: () -> Unit = {},
    onStarComplete: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Story 1.4, 2.3: GameViewModel integration with TTS and gameplay logic + persistence
    val context = LocalContext.current
    val viewModel = remember(starNumber, isReplaySession) {
        GameViewModel(
            context = context,
            starNumber = starNumber,
            isReplaySession = isReplaySession,
            progressRepository = progressRepository,
            initialProgress = currentProgress
        )
    }
    val gameState by viewModel.gameState.collectAsState()

    // Story 1.5: Ghost expression and speaking state (AC2, AC3, AC4, AC6)
    val ghostExpression by viewModel.ghostExpression.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    // Story 2.4: Celebration state (AC6, AC7)
    val showCelebration by viewModel.showCelebration.collectAsState()
    val celebrationStarLevel by viewModel.celebrationStarLevel.collectAsState()

    // Story 3.1: Exit dialog and session state (AC2, AC3, AC4, AC5)
    val showExitDialog by viewModel.showExitDialog.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Story 2.1, 2.3: Trigger star completion callback when session completes (AC6)
    LaunchedEffect(gameState.sessionComplete) {
        if (gameState.sessionComplete) {
            onStarComplete?.invoke(starNumber)
        }
    }

    // Story 3.1: Handle session exit navigation (AC5)
    LaunchedEffect(sessionState) {
        if (sessionState == com.spellwriter.data.models.SessionState.EXITED) {
            viewModel.resetSession()
            onBackPress()
        }
    }

    // Story 2.4: Wrap content in Box for celebration overlay (AC6, AC7)
    Box(modifier = modifier.fillMaxSize()) {
        // Base game UI
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top row: Exit button + Progress bar + Ghost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Story 3.1: Exit button (AC1)
                IconButton(
                    onClick = { viewModel.requestExit() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.exit_button_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                // Story 1.5: Ghost with expression and speaking animation (AC2, AC3, AC4, AC6)
                Ghost(
                    expression = ghostExpression,
                    isSpeaking = isSpeaking,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main content area with stars and grimoire
            Row(
                modifier = Modifier.weight(1f)
            ) {
                // Left side: Session stars
                StarProgress(
                    earnedStars = gameState.sessionStars,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Center: Grimoire and completed words
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Grimoire(
                        typedLetters = gameState.typedLetters,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Completed words list under the Grimoire
                    CompletedWordsList(
                        completedWords = gameState.completedWords
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progressbar
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${gameState.wordsCompleted}/20", fontSize = 16.sp)
                        LinearProgressIndicator(
                            progress = (gameState.wordsCompleted / 20f).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio control buttons (AC1, AC2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.speakCurrentWord() },  // AC1: Play word
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play word",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(
                    onClick = { viewModel.speakCurrentWord() },  // AC2: Repeat word
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Replay,
                        contentDescription = "Repeat word",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Native keyboard TextField for letter-by-letter input
            // The TextField value is always bound to validated typedLetters from ViewModel
            TextField(
                value = gameState.typedLetters,
                onValueChange = { newValue ->
                    // Only process additions (when length increases)
                    if (newValue.length > gameState.typedLetters.length) {
                        // Extract the newly typed character and convert to uppercase
                        val newChar = newValue.last().uppercaseChar()
                        // Delegate to existing validation logic in ViewModel
                        viewModel.onLetterTyped(newChar)
                    }
                    // Note: TextField value is always bound to gameState.typedLetters
                    // If letter is incorrect, ViewModel won't update typedLetters,
                    // causing TextField to reset to the validated state
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Story 2.4: Celebration overlay (AC1, AC2, AC3, AC4, AC6, AC7)
        CelebrationSequence(
            showCelebration = showCelebration,
            starLevel = celebrationStarLevel,
            onCelebrationComplete = { viewModel.onCelebrationComplete() }
        )

        // Story 3.1: Exit confirmation dialog (AC2, AC3, AC4, AC5)
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelExit() },
                title = {
                    Text(
                        text = stringResource(R.string.exit_dialog_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.exit_dialog_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.confirmExit()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.exit_dialog_leave))
                    }
                },
                dismissButton = {
                    // Stay button - more prominent to prevent accidental exits (AC3)
                    Button(
                        onClick = { viewModel.cancelExit() }
                    ) {
                        Text(stringResource(R.string.exit_dialog_stay))
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false // Prevent accidental dismissal
                )
            )
        }
    }
}
