package com.spellwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.spellwriter.data.models.CelebrationPhase
import kotlinx.coroutines.delay

/**
 * Story 2.4: Celebration sequence orchestrator.
 * Coordinates the sequential celebration animations:
 * 1. Stars explosion (500ms)
 * 2. Dragon fly-through (2000ms)
 * 3. Star pop (800ms)
 *
 * AC6: Smooth animation flow with seamless transitions
 * AC7: Post-celebration state management
 *
 * @param showCelebration Whether to show the celebration sequence
 * @param starLevel The star level just earned (1, 2, or 3)
 * @param onCelebrationComplete Callback when the entire sequence finishes
 * @param modifier Modifier for the celebration overlay
 */
@Composable
fun CelebrationSequence(
    showCelebration: Boolean,
    starLevel: Int,
    onCelebrationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var celebrationPhase by remember { mutableStateOf(CelebrationPhase.NONE) }

    // Orchestrate the celebration sequence with precise timing
    LaunchedEffect(showCelebration) {
        if (showCelebration && celebrationPhase == CelebrationPhase.NONE) {
            // Phase 1: Stars explosion (500ms) - AC1
            celebrationPhase = CelebrationPhase.EXPLOSION
            delay(500) // FR5.4: Exactly 500ms

            // Phase 2: Dragon fly-through (2000ms) - AC2, AC3
            celebrationPhase = CelebrationPhase.DRAGON
            delay(2000) // FR5.5: Exactly 2000ms

            // Phase 3: Star pop (800ms) - AC4
            celebrationPhase = CelebrationPhase.STAR_POP
            delay(800) // FR5.7: Exactly 800ms

            // Phase 4: Complete - AC7
            celebrationPhase = CelebrationPhase.COMPLETE
            onCelebrationComplete()
        }
    }

    // Reset phase when celebration is dismissed
    LaunchedEffect(showCelebration) {
        if (!showCelebration) {
            celebrationPhase = CelebrationPhase.NONE
        }
    }

    // Render current celebration phase
    // Semi-transparent overlay to intercept touches during celebration
    if (showCelebration) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // Dim background
        ) {
            when (celebrationPhase) {
                CelebrationPhase.EXPLOSION -> StarsExplosionAnimation()
                CelebrationPhase.DRAGON -> DragonAnimation(starLevel = starLevel)
                CelebrationPhase.STAR_POP -> StarPopAnimation(starLevel = starLevel)
                CelebrationPhase.NONE, CelebrationPhase.COMPLETE -> {
                    // No animation rendered
                }
            }
        }
    }
}
