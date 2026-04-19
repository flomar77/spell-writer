package com.spellwriter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spellwriter.data.models.MAX_STARS

/**
 * Displays world progress with name and 3 stars showing earned/unearned status.
 * Story 1.2: Star Progress Display
 *
 *@param earnedStars Number of stars earned (0-3)
 * @param onStarClick Callback triggered when an earned star is clicked, receives star number (1-3)
 * @param modifier Optional modifier for the component
 */
@Composable
fun WorldProgressRow(
    earnedStars: Int,
    onStarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(MAX_STARS) { index ->
                val starNumber = index + 1
                val isEarned = starNumber <= earnedStars
                val isStartingStar = (starNumber == 1 && earnedStars == 0)

                StarIcon(
                    starNumber = starNumber,
                    isEarned = isEarned,
                    isStartingStar = isStartingStar,
                    onClick = {
                        if (isEarned) {
                            onStarClick(starNumber)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

