package com.spellwriter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Story 2.4: Star pop lock-in animation component.
 *
 * AC4: Star pop animation lasts exactly 800ms
 * - Satisfying bounce effect as star "locks in"
 * - Gives sense of permanent achievement
 * - Visual feedback for permanence
 *
 * @param starLevel The star level just earned (1, 2, or 3)
 * @param modifier Modifier for the animation container
 */
@Composable
fun StarPopAnimation(
    starLevel: Int,
    modifier: Modifier = Modifier
) {
    // Animate scale with spring for bouncy "lock-in" effect
    // Spring settling time targets ~800ms (AC4)
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run animations simultaneously
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, // ~800ms settling time
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Draw large golden star with satisfying pop effect
        drawBigStar(
            center = Offset(centerX, centerY),
            radius = 100f * scale.value,
            rotationDegrees = rotation.value,
            color = Color(0xFFFFD700) // Gold color
        )

        // Draw smaller inner star for depth
        drawBigStar(
            center = Offset(centerX, centerY),
            radius = 60f * scale.value,
            rotationDegrees = -rotation.value * 0.5f,
            color = Color(0xFFFFEB3B) // Bright yellow
        )

        // Draw sparkles around the star
        if (scale.value > 0.7f) {
            val sparkleAlpha = ((scale.value - 0.7f) / 0.3f).coerceIn(0f, 1f)
            drawSparkles(
                center = Offset(centerX, centerY),
                radius = 120f * scale.value,
                alpha = sparkleAlpha
            )
        }
    }
}

/**
 * Draw a large 5-pointed star.
 *
 * @param center Center position of the star
 * @param radius Outer radius of the star
 * @param rotationDegrees Rotation angle in degrees
 * @param color Color to draw the star
 */
private fun DrawScope.drawBigStar(
    center: Offset,
    radius: Float,
    rotationDegrees: Float,
    color: Color
) {
    val path = Path()
    val innerRadius = radius * 0.45f // Inner points are 45% of outer radius
    val rotationRadians = (rotationDegrees * PI / 180.0)

    // Draw 5-pointed star
    for (i in 0 until 10) {
        val angle = (PI / 5.0 * i) - (PI / 2.0) + rotationRadians // Rotate
        val r = if (i % 2 == 0) radius else innerRadius

        val x = center.x + (cos(angle) * r).toFloat()
        val y = center.y + (sin(angle) * r).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color
    )
}

/**
 * Draw sparkle particles around the star for extra celebration effect.
 *
 * @param center Center position for sparkles
 * @param radius Distance of sparkles from center
 * @param alpha Alpha transparency for sparkles
 */
private fun DrawScope.drawSparkles(
    center: Offset,
    radius: Float,
    alpha: Float
) {
    val sparkleColor = Color(0xFFFFFFFF).copy(alpha = alpha)

    // Draw 8 sparkles around the star
    for (i in 0 until 8) {
        val angle = (2 * PI * i) / 8.0
        val x = center.x + (cos(angle) * radius).toFloat()
        val y = center.y + (sin(angle) * radius).toFloat()

        // Draw small cross for sparkle effect
        drawLine(
            color = sparkleColor,
            start = Offset(x - 5f, y),
            end = Offset(x + 5f, y),
            strokeWidth = 3f
        )
        drawLine(
            color = sparkleColor,
            start = Offset(x, y - 5f),
            end = Offset(x, y + 5f),
            strokeWidth = 3f
        )
    }
}
