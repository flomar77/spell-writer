package com.spellwriter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.ui.theme.SpellWriterColors

@Composable
fun StarProgress(
    earnedStars: Int,
    currentStar: Int,
    onStarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (star in 1..3) {
            Star(
                isEarned = star <= earnedStars,
                isCurrent = star == currentStar,
                onClick = { if (star <= earnedStars) onStarClick(star) },
                isClickable = star <= earnedStars
            )
        }
    }
}

@Composable
private fun Star(
    isEarned: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    isClickable: Boolean,
    modifier: Modifier = Modifier
) {
    var animateScale by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animateScale) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { animateScale = false },
        label = "starScale"
    )

    LaunchedEffect(isEarned) {
        if (isEarned) {
            animateScale = true
        }
    }

    Text(
        text = if (isEarned) "⭐" else "☆",
        fontSize = 32.sp,
        modifier = modifier
            .scale(scale)
            .clickable(enabled = isClickable) { onClick() }
            .padding(4.dp)
    )
}

@Composable
fun StarPopAnimation(
    onAnimationComplete: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { if (scale == 1f) onAnimationComplete() },
        label = "starPopScale"
    )

    LaunchedEffect(Unit) {
        scale = 1f
    }

    Text(
        text = "⭐",
        fontSize = 80.sp,
        modifier = Modifier.scale(animatedScale)
    )
}
