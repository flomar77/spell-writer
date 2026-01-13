package com.spellwriter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.R
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.data.models.Progress
import com.spellwriter.data.models.World
import com.spellwriter.data.models.isWorldUnlocked
import com.spellwriter.ui.components.Ghost
import com.spellwriter.ui.theme.SpellWriterColors

@Composable
fun HomeScreen(
    progress: Progress,
    onPlayClick: () -> Unit,
    onStarClick: (World, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Ghost
        Ghost(
            expression = GhostExpression.NEUTRAL,
            modifier = Modifier.size(120.dp)
        )

        // Title
        Text(
            text = stringResource(R.string.home_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Instruction
        Text(
            text = stringResource(R.string.home_instruction),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Play Button
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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

        // World Progress
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wizard World (always visible)
            WorldProgressRow(
                worldName = stringResource(R.string.world_wizard),
                stars = progress.wizardStars,
                isUnlocked = true,
                onStarClick = { star -> onStarClick(World.WIZARD, star) }
            )

            // Pirate World (only if unlocked)
            if (progress.isWorldUnlocked(World.PIRATE)) {
                WorldProgressRow(
                    worldName = stringResource(R.string.world_pirate),
                    stars = progress.pirateStars,
                    isUnlocked = true,
                    onStarClick = { star -> onStarClick(World.PIRATE, star) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WorldProgressRow(
    worldName: String,
    stars: Int,
    isUnlocked: Boolean,
    onStarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Stars
        Row {
            for (i in 1..3) {
                Text(
                    text = if (i <= stars) "⭐" else "☆",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        // World name
        Text(
            text = worldName,
            fontSize = 18.sp
        )
    }
}
