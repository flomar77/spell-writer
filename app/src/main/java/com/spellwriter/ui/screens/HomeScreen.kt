package com.spellwriter.ui.screens

import LanguageSwitcher
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
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.components.Ghost
import com.spellwriter.ui.components.WorldProgressRow

/**
 * Home Screen for Stories 1.1 & 1.2 - Welcoming interface with progress tracking.
 * Displays app title, ghost character, instructions, star progress, and play button.
 * Story 1.2 adds progress tracking and star replay functionality.
 *
 * @param progress User progress tracking (Story 1.2)
 * @param onPlayClick Callback when user taps the PLAY button to start the game
 * @param onStarClick Callback when user taps a star to replay that level (Story 1.2)
 * @param onLanguageChanged Callback when language is changed to trigger recomposition
 * @param modifier Optional modifier for the screen
 */
@Composable
fun HomeScreen(
    progress: Progress,
    onPlayClick: () -> Unit,
    onStarClick: (Int) -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LanguageSwitcher(onLanguageChanged = onLanguageChanged)
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

        // Star Progress Display (Story 1.2)
        WorldProgressRow(
            worldName = stringResource(R.string.world_wizard),
            earnedStars = progress.wizardStars,
            onStarClick = onStarClick
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
