package com.spellwriter.ui.components

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import com.spellwriter.data.models.CelebrationPhase
import com.spellwriter.utils.GifSelector

/**
 * Celebration sequence orchestrator with GIF reward overlay.
 *
 * Displays a random GIF reward from assets after star completion.
 * Replaces previous animation sequence (explosion/dragon/star) with
 * immediate GIF display for faster, more direct feedback.
 *
 * Flow:
 * 1. Star earned → GIF selected randomly from assets/gifs/
 * 2. GIF displayed in fullscreen overlay with Continue button
 * 3. User taps Continue → progression to next star or home
 *
 * @param showCelebration Whether to show the celebration sequence
 * @param starLevel The star level just earned (1, 2, or 3)
 * @param onContinueToNextStar Callback when user clicks Continue (triggers auto-progression)
 * @param modifier Modifier for the celebration overlay
 */
@Composable
fun CelebrationSequence(
    showCelebration: Boolean,
    starLevel: Int,
    onContinueToNextStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var celebrationPhase by remember { mutableStateOf(CelebrationPhase.NONE) }
    var selectedGifPath by remember { mutableStateOf<String?>(null) }

    // Orchestrate the celebration sequence - skip animations, go straight to GIF
    LaunchedEffect(showCelebration) {
        if (showCelebration && celebrationPhase == CelebrationPhase.NONE) {
            // Select random GIF from assets
            selectedGifPath = GifSelector.selectRandomGif(context)

            if (selectedGifPath != null) {
                // Show GIF reward overlay
                celebrationPhase = CelebrationPhase.GIF_REWARD
                // User controls progression by tapping Continue button
            } else {
                // No GIF available - skip overlay and proceed
                Log.w("CelebrationSequence", "No GIF found for star $starLevel - skipping reward overlay")
                celebrationPhase = CelebrationPhase.COMPLETE
                onContinueToNextStar()
            }
        }
    }

    // Reset phase when celebration is dismissed
    LaunchedEffect(showCelebration) {
        if (!showCelebration) {
            celebrationPhase = CelebrationPhase.NONE
            selectedGifPath = null
        }
    }

    // Render current celebration phase
    if (showCelebration) {
        when (celebrationPhase) {
            CelebrationPhase.GIF_REWARD -> {
                selectedGifPath?.let { path ->
                    GifRewardOverlay(
                        gifAssetPath = path,
                        onContinue = {
                            celebrationPhase = CelebrationPhase.COMPLETE
                            onContinueToNextStar()
                        }
                    )
                }
            }
            CelebrationPhase.NONE, CelebrationPhase.COMPLETE,
            CelebrationPhase.EXPLOSION, CelebrationPhase.DRAGON, CelebrationPhase.STAR_POP -> {
                // No animation rendered (old phases kept for compatibility but not used)
            }
        }
    }
}
