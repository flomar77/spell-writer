package com.spellwriter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 *
 * @param starNumber The star level (1, 2, or 3) to play (Story 1.2)
 * @param isReplaySession If true, don't update progress when completing (Story 1.2)
 * @param onBackPress Callback for back navigation
 * @param onStarComplete Callback when star is completed, passes completed star number (Story 1.2)
 * @param modifier Optional modifier for the screen
 */
@Composable
fun GameScreen(
    starNumber: Int = 1,
    isReplaySession: Boolean = false,
    onBackPress: () -> Unit = {},
    onStarComplete: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Story 1.4: GameViewModel integration with TTS and gameplay logic
    val context = LocalContext.current
    val viewModel = remember {
        GameViewModel(
            context = context,
            starNumber = starNumber,
            isReplaySession = isReplaySession
        )
    }
    val gameState by viewModel.gameState.collectAsState()

    // Story 1.5: Ghost expression and speaking state (AC2, AC3, AC4, AC6)
    val ghostExpression by viewModel.ghostExpression.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    // Story 2.1: Trigger star completion callback when session completes (AC6)
    LaunchedEffect(gameState.sessionComplete) {
        if (gameState.sessionComplete) {
            onStarComplete?.invoke(starNumber)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top row: Progress bar + Ghost
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress indicator
            Column(modifier = Modifier.weight(1f)) {
                Text("${gameState.wordsCompleted}/20", fontSize = 16.sp)
                LinearProgressIndicator(
                    progress = (gameState.wordsCompleted / 20f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
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

            // Center: Grimoire
            Grimoire(
                typedLetters = gameState.typedLetters,
                modifier = Modifier.weight(1f)
            )
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

        // Keyboard at bottom (AC3, AC4)
        SpellKeyboard(
            onLetterClick = { letter ->
                // Story 1.4: Integrated gameplay logic
                viewModel.onLetterTyped(letter[0])
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
