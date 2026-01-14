package com.spellwriter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.R
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.ui.components.Ghost

/**
 * Home Screen for Story 1.1 - Simple welcoming interface.
 * Displays app title, ghost character, instructions, and play button.
 * No progress tracking or star system (those are added in future stories).
 *
 * @param onPlayClick Callback when user taps the PLAY button to start the game
 * @param modifier Optional modifier for the screen
 */
@Composable
fun HomeScreen(
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // App Title
        Text(
            text = stringResource(R.string.home_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )

        // Ghost Character with NEUTRAL expression
        Ghost(
            expression = GhostExpression.NEUTRAL,
            modifier = Modifier.size(80.dp)
        )

        // Instruction Text
        Text(
            text = stringResource(R.string.home_instruction),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        // PLAY Button with accessibility
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)  // Exceeds 48dp minimum per WCAG 2.1
                .semantics {
                    // Accessibility: contentDescription automatically provided by Text
                },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.home_play),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
