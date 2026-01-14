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
 * Game Screen stub for Story 1.1 - Minimal placeholder.
 * This screen is only a stub to enable navigation testing.
 * Full game screen implementation will be done in Story 1.3.
 *
 * @param onBackPress Callback for back navigation (handled by MainActivity for now)
 * @param modifier Optional modifier for the screen
 */
@Composable
fun GameScreen(
    onBackPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Game Screen\n(Coming in Story 1.3)",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
