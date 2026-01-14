package com.spellwriter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Game Screen for Stories 1.1 & 1.2 - Minimal placeholder with star level awareness.
 * Story 1.1: Basic stub for navigation testing.
 * Story 1.2: Accepts starNumber and isReplaySession parameters for future word selection.
 * Full game screen implementation will be done in Story 1.3.
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Game Screen - Star $starNumber\n${if (isReplaySession) "(Replay Mode)" else "(Progression Mode)"}\n(Full gameplay coming in Story 1.3)",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
