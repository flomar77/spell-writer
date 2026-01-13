package com.spellwriter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.R
import com.spellwriter.data.models.GameState
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.ui.components.*
import com.spellwriter.ui.theme.SpellWriterColors

@Composable
fun GameScreen(
    gameState: GameState,
    ghostExpression: GhostExpression,
    earnedStars: Int,
    onPlayClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onKeyPressed: (Char) -> Unit,
    onStarClick: (Int) -> Unit,
    showDragonAnimation: Boolean,
    showStarAnimation: Boolean,
    onDragonAnimationComplete: () -> Unit,
    onStarAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top row: Progress bar + Ghost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress bar
                Column {
                    LinearProgressIndicator(
                        progress = (gameState.wordsCompleted / 20f),
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp),
                        color = SpellWriterColors.StarGold,
                        trackColor = SpellWriterColors.KeyBorder
                    )
                    Text(
                        text = "${gameState.wordsCompleted}/20",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Ghost
                Ghost(
                    expression = ghostExpression,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle section: Stars + Grimoire + Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars (left)
                StarProgress(
                    earnedStars = earnedStars,
                    currentStar = gameState.currentStar,
                    onStarClick = onStarClick,
                    modifier = Modifier.padding(8.dp)
                )

                // Grimoire (center)
                Grimoire(
                    typedLetters = gameState.typedLetters,
                    targetWord = gameState.currentWord,
                    modifier = Modifier.weight(1f)
                )

                // Controls (right)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Play button
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SpellWriterColors.KeyBackground)
                    ) {
                        Text(text = "▶", fontSize = 24.sp)
                    }

                    // Repeat button
                    IconButton(
                        onClick = onRepeatClick,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SpellWriterColors.KeyBackground)
                    ) {
                        Text(text = "🔁", fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keyboard (bottom)
            SpellKeyboard(
                onKeyPressed = onKeyPressed,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Dragon animation overlay
        if (showDragonAnimation) {
            DragonAnimation(
                starLevel = gameState.currentStar,
                onAnimationComplete = onDragonAnimationComplete
            )
        }

        // Star animation overlay
        if (showStarAnimation) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                StarPopAnimation(
                    onAnimationComplete = onStarAnimationComplete
                )
            }
        }
    }
}
