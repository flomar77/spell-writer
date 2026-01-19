package com.spellwriter.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spellwriter.data.models.GhostExpression
import kotlin.math.sin

/**
 * Ghost character component.
 * Story 1.1: Basic structure with NEUTRAL expression
 * Story 1.4: Added HAPPY and UNHAPPY expressions for gameplay feedback (AC3, AC4)
 * Story 1.5: Added AnimatedContent transitions and TTS-synchronized speaking animation (AC2, AC6)
 *
 * @param expression The emotional state of the ghost
 * @param isSpeaking Whether the ghost is currently speaking (triggers bounce animation)
 * @param modifier Modifier for the component (default size should be 80dp per architecture)
 */
@Composable
fun Ghost(
    expression: GhostExpression,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Story 1.5: Expression to emoji mapping
    // Story 3.2: Added ENCOURAGING expression (AC1)
    val emoji = when (expression) {
        GhostExpression.NEUTRAL -> "👻"     // Default expression
        GhostExpression.HAPPY -> "😊"       // AC3: Correct letter feedback (warm, encouraging)
        GhostExpression.UNHAPPY -> "😔"     // AC4: Incorrect letter feedback (gentle, not scary)
        GhostExpression.DEAD -> "💀"        // AC5: Failure animation (humorous, cartoonish)
        GhostExpression.ENCOURAGING -> "🤗" // Story 3.2: Gentle encouragement after 8s timeout
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .semantics {
                contentDescription = "Ghost character with ${expression.name.lowercase()} expression"
            },
        contentAlignment = Alignment.Center
    ) {
        // Story 1.5: AnimatedContent for smooth expression transitions (AC6)
        AnimatedContent(
            targetState = emoji,
            transitionSpec = {
                // 150ms fade transition for snappy feedback
                fadeIn(animationSpec = tween(150)) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            label = "Ghost Expression Animation"
        ) { currentEmoji ->
            Text(
                text = currentEmoji,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    // Story 1.5: Subtle bounce animation when speaking (AC2)
                    .graphicsLayer {
                        if (isSpeaking) {
                            val scale = 1.0f + (sin(System.currentTimeMillis() / 200.0) * 0.05f).toFloat()
                            scaleX = scale
                            scaleY = scale
                        }
                    }
            )
        }
    }
}
