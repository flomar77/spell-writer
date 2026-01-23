package com.spellwriter.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
fun StarIcon(
    starNumber: Int,
    isEarned: Boolean,
    isStartingStar: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
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
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = if (isStartingStar) "Star $starNumber (start here)" else "Star $starNumber (locked)",
                tint = Color.Gray,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}