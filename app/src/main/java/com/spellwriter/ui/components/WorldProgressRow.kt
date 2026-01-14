package com.spellwriter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays world progress with name and 3 stars showing earned/unearned status.
 * Story 1.2: Star Progress Display
 *
 * @param worldName Name of the world (e.g., "Wizard World")
 * @param earnedStars Number of stars earned (0-3)
 * @param onStarClick Callback triggered when an earned star is clicked, receives star number (1-3)
 * @param modifier Optional modifier for the component
 */
@Composable
fun WorldProgressRow(
    worldName: String,
    earnedStars: Int,
    onStarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // World name text
        Text(
            text = worldName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3 stars in a row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) { index ->
                val starNumber = index + 1
                val isEarned = starNumber <= earnedStars
                val isStartingStar = (starNumber == 1 && earnedStars == 0)  // AC4: Indicate starting point

                StarIcon(
                    starNumber = starNumber,
                    isEarned = isEarned,
                    isStartingStar = isStartingStar,
                    onClick = {
                        if (isEarned) {
                            onStarClick(starNumber)
                        }
                    },
                    modifier = Modifier.size(56.dp)  // Exceeds 48dp minimum per WCAG 2.1
                )
            }
        }
    }
}

/**
 * Individual star icon with clickable state.
 * Displays filled gold star for earned, outlined gray star for unearned.
 * AC4: Starting star (Star 1 when none earned) uses distinct color to indicate starting point.
 *
 * @param starNumber The star number (1, 2, or 3)
 * @param isEarned Whether this star has been earned
 * @param isStartingStar Whether this is Star 1 with no stars earned (AC4)
 * @param onClick Click handler
 * @param modifier Optional modifier
 */
@Composable
private fun StarIcon(
    starNumber: Int,
    isEarned: Boolean,
    isStartingStar: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = isEarned,  // Only earned stars are clickable
        modifier = modifier
    ) {
        if (isEarned) {
            // Filled star for earned
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star $starNumber (earned)",
                tint = Color(0xFFFFD700),  // Gold color
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Outline star for unearned
            // AC4: Starting star uses blue tint to indicate it's the starting point
            val unearnedTint = if (isStartingStar) Color(0xFF4A90E2) else Color.Gray
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = if (isStartingStar) "Star $starNumber (start here)" else "Star $starNumber (locked)",
                tint = unearnedTint,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
