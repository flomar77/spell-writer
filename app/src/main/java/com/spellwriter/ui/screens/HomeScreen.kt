package com.spellwriter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.R
import com.spellwriter.ui.components.LanguageSwitcher
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.Progress
import com.spellwriter.ui.components.Ghost
import com.spellwriter.ui.components.WorldProgressRow
import com.spellwriter.viewmodel.GameViewModel
import com.spellwriter.viewmodel.HomeViewModel

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
 * @param isTTSInitializing Whether TTS is currently initializing (shows loading UI)
 * @param ttsError Error message if TTS initialization failed, null if successful
 */
@Composable
fun HomeScreen(
    progress: Progress,
    onPlayClick: () -> Unit,
    onStarClick: (Int) -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    isTTSInitializing: Boolean = false,
    ttsError: String? = null
) {
//    val context = LocalContext.current
//    val viewModel = HomeViewModel(
//        context = context
//
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LanguageSwitcher(
            onLanguageChanged = onLanguageChanged,
            enabled = !isTTSInitializing
        )
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

        // Loading indicator and error message
        if (isTTSInitializing) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_tts_loading),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (ttsError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = ttsError,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        // PLAY Button with accessibility
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),  // Exceeds 48dp minimum per WCAG 2.1
            enabled = !isTTSInitializing,
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
