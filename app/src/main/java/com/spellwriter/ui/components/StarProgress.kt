package com.spellwriter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Session star progress component for game screen.
 * Story 1.3: Game Screen Layout
 *
 * Displays 3 stars vertically showing session progress (0-3 stars earned).
 * Different from WorldProgressRow - this shows current session progress,
 * not overall world progress.
 */
@Composable
fun StarProgress(
    completedStars: Int,  // 0-3 stars completed (persistent progress)
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { index ->
            val starNumber = 3 - index  // Display from top to bottom: 3, 2, 1
            val isCompleted = starNumber <= completedStars

            Icon(
                imageVector = if (isCompleted) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Session star $starNumber${if (isCompleted) " completed" else ""}",
                tint = if (isCompleted) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(40.dp)  // Smaller than home screen stars (56dp)
            )
        }
    }
}