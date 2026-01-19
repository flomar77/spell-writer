package com.spellwriter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.sin

/**
 * Story 2.4: Dragon fly-through animation component.
 *
 * AC2: Dragon fly-through animation plays for exactly 2000ms
 * - Vibrant and magical (the main color moment in the app)
 * - Flies across or around the screen in a satisfying pattern
 * - 60fps performance
 *
 * AC3: Progressive dragon size based on star level
 * - Star 1: Small dragon (cute and encouraging)
 * - Star 2: Medium dragon (more impressive)
 * - Star 3: Large dragon (magnificent achievement)
 *
 * @param starLevel The star level just earned (1, 2, or 3)
 * @param modifier Modifier for the animation container
 */
@Composable
fun DragonAnimation(
    starLevel: Int,
    modifier: Modifier = Modifier
) {
    // Dragon size based on star level (AC3)
    val dragonSize = when (starLevel) {
        1 -> 100f  // Small - cute and encouraging
        2 -> 150f  // Medium - more impressive
        3 -> 200f  // Large - magnificent achievement
        else -> 100f
    }

    // Animate from 0f to 1f over exactly 2000ms (AC2)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000) // FR5.5: Exactly 2000ms
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animationProgress.value

        // Calculate dragon position (flies left to right with sine wave)
        val x = size.width * progress
        val y = (size.height / 2f) + sin(progress * 8f) * (size.height / 6f)

        // Fade in at start, fade out at end
        val alpha = when {
            progress < 0.1f -> progress * 10f
            progress > 0.9f -> (1f - progress) * 10f
            else -> 1f
        }

        // Draw vibrant dragon
        drawDragon(
            position = Offset(x, y),
            size = dragonSize,
            alpha = alpha,
            progress = progress
        )
    }
}

/**
 * Draw a stylized dragon with vibrant gradient colors.
 * The dragon is the "main color moment" in the app (AC2).
 *
 * @param position Center position of the dragon
 * @param size Size of the dragon
 * @param alpha Alpha transparency (for fade in/out)
 * @param progress Animation progress for wing flapping
 */
private fun DrawScope.drawDragon(
    position: Offset,
    size: Float,
    alpha: Float,
    progress: Float
) {
    // Vibrant gradient colors for magical effect
    val dragonColors = listOf(
        Color(0xFFFF6B6B).copy(alpha = alpha), // Red
        Color(0xFFFFD93D).copy(alpha = alpha), // Gold
        Color(0xFF6BCB77).copy(alpha = alpha), // Green
        Color(0xFF4D96FF).copy(alpha = alpha)  // Blue
    )

    val gradient = Brush.linearGradient(
        colors = dragonColors,
        start = Offset(position.x - size, position.y - size),
        end = Offset(position.x + size, position.y + size)
    )

    // Draw dragon body
    val bodyPath = Path().apply {
        // Body (elongated oval)
        val bodyWidth = size * 0.8f
        val bodyHeight = size * 0.5f
        moveTo(position.x - bodyWidth / 2, position.y)
        cubicTo(
            position.x - bodyWidth / 2, position.y - bodyHeight / 2,
            position.x + bodyWidth / 2, position.y - bodyHeight / 2,
            position.x + bodyWidth / 2, position.y
        )
        cubicTo(
            position.x + bodyWidth / 2, position.y + bodyHeight / 2,
            position.x - bodyWidth / 2, position.y + bodyHeight / 2,
            position.x - bodyWidth / 2, position.y
        )
        close()
    }

    drawPath(bodyPath, gradient, style = Fill)

    // Draw head (circle at front)
    val headRadius = size * 0.3f
    drawCircle(
        brush = gradient,
        radius = headRadius,
        center = Offset(position.x + size * 0.5f, position.y - size * 0.1f)
    )

    // Draw wings (flapping based on progress)
    val wingFlap = sin(progress * 20f) * 0.3f + 0.7f // Flap between 0.7 and 1.0
    val wingHeight = size * 0.6f * wingFlap

    val leftWingPath = Path().apply {
        moveTo(position.x - size * 0.2f, position.y)
        quadraticTo(
            position.x - size * 0.6f, position.y - wingHeight,
            position.x - size * 0.4f, position.y - size * 0.2f
        )
        close()
    }

    val rightWingPath = Path().apply {
        moveTo(position.x + size * 0.2f, position.y)
        quadraticTo(
            position.x + size * 0.6f, position.y - wingHeight,
            position.x + size * 0.4f, position.y - size * 0.2f
        )
        close()
    }

    drawPath(leftWingPath, gradient, style = Fill, alpha = alpha * 0.8f)
    drawPath(rightWingPath, gradient, style = Fill, alpha = alpha * 0.8f)

    // Draw tail (triangular behind body)
    val tailPath = Path().apply {
        moveTo(position.x - size * 0.4f, position.y)
        lineTo(position.x - size * 0.8f, position.y - size * 0.2f)
        lineTo(position.x - size * 0.8f, position.y + size * 0.2f)
        close()
    }

    drawPath(tailPath, gradient, style = Fill, alpha = alpha * 0.9f)
}
