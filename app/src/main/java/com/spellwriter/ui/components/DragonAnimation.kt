package com.spellwriter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DragonAnimation(
    starLevel: Int, // 1, 2, or 3 - determines dragon size
    onAnimationComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp

    // Dragon size based on star level
    val dragonSize = when (starLevel) {
        1 -> 60.dp
        2 -> 100.dp
        else -> 150.dp
    }

    val fontSize = when (starLevel) {
        1 -> 40.sp
        2 -> 70.sp
        else -> 100.sp
    }

    // Animation: fly from right to left
    val offsetX = remember { Animatable(screenWidthPx.toFloat() + 100f) }

    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = -200f,
            animationSpec = tween(
                durationMillis = when (starLevel) {
                    1 -> 2000
                    2 -> 1500
                    else -> 1200 // Fastest for star 3
                },
                easing = LinearEasing
            )
        )
        onAnimationComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "🐉",
            fontSize = fontSize,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 200) }
                .size(dragonSize)
        )
    }
}
