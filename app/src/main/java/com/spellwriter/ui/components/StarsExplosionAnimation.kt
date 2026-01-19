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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Story 2.4: Stars explosion animation component.
 *
 * AC1: Stars explosion animation plays for exactly 500ms
 * - Colorful particle explosion contrasting with black/white base design
 * - 60fps performance using Compose Animation APIs
 * - Captures attention and feels celebratory
 *
 * @param modifier Modifier for the animation container
 */
@Composable
fun StarsExplosionAnimation(
    modifier: Modifier = Modifier
) {
    // Generate particles once using remember to avoid recomposition overhead
    val particles = remember { generateParticles(count = 20) }

    // Animate from 0f to 1f over exactly 500ms (AC1)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500) // FR5.4: Exactly 500ms
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animationProgress.value

        particles.forEach { particle ->
            // Calculate particle position based on animation progress
            val position = particle.calculatePosition(progress, size.width, size.height)

            // Fade out as animation progresses
            val alpha = (1f - progress).coerceIn(0f, 1f)

            // Draw star particle
            drawStar(
                center = position,
                radius = particle.size,
                color = particle.color.copy(alpha = alpha)
            )
        }
    }
}

/**
 * Data class representing a single particle in the explosion.
 *
 * @param angle Direction of particle movement in radians
 * @param speed Distance multiplier for particle travel
 * @param size Radius of the star particle
 * @param color Vibrant color for the particle (contrasting with B&W theme)
 */
private data class Particle(
    val angle: Double,
    val speed: Float,
    val size: Float,
    val color: Color
) {
    /**
     * Calculate particle position at given animation progress.
     * Particles explode outward from center.
     */
    fun calculatePosition(progress: Float, canvasWidth: Float, canvasHeight: Float): Offset {
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // Particle travels outward based on progress
        val distance = progress * speed * (canvasWidth.coerceAtLeast(canvasHeight) / 2f)

        val x = centerX + (cos(angle) * distance).toFloat()
        val y = centerY + (sin(angle) * distance).toFloat()

        return Offset(x, y)
    }
}

/**
 * Generate random particles for explosion effect.
 * Uses vibrant colors to contrast with the app's black/white theme (AC1).
 *
 * @param count Number of particles to generate
 * @return List of particles with random properties
 */
private fun generateParticles(count: Int): List<Particle> {
    val random = Random.Default

    // Vibrant colors for magical celebration effect
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Cyan
        Color(0xFFFFE66D), // Yellow
        Color(0xFFFF6BCB), // Pink
        Color(0xFF95E1D3), // Mint
        Color(0xFFF38181), // Coral
        Color(0xFFAA96DA)  // Purple
    )

    return List(count) { i ->
        // Distribute particles evenly in a circle
        val angle = (2 * PI * i) / count + random.nextDouble(-0.2, 0.2)

        Particle(
            angle = angle,
            speed = random.nextFloat() * 0.5f + 0.75f, // 0.75 to 1.25 speed variation
            size = random.nextFloat() * 8f + 12f, // 12dp to 20dp size
            color = colors.random()
        )
    }
}

/**
 * Draw a 5-pointed star shape.
 *
 * @param center Center position of the star
 * @param radius Outer radius of the star
 * @param color Color to draw the star
 */
private fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path()
    val innerRadius = radius * 0.4f // Inner points are 40% of outer radius

    // Draw 5-pointed star
    for (i in 0 until 10) {
        val angle = (PI / 5.0 * i) - (PI / 2.0) // Start from top
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
