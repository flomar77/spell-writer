package com.spellwriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Black & White theme with color accents for magic moments
object SpellWriterColors {
    // Base colors (B&W)
    val Background = Color.White
    val OnBackground = Color.Black
    val Surface = Color.White
    val OnSurface = Color.Black

    // UI elements
    val KeyBackground = Color(0xFFF5F5F5)
    val KeyBorder = Color(0xFFE0E0E0)
    val KeyPressed = Color(0xFFE0E0E0)

    // Accent colors (for magic moments only)
    val StarGold = Color(0xFFFFD700)
    val StarEmpty = Color(0xFFBDBDBD)
    val DragonColor = Color(0xFF4CAF50)
    val SuccessGreen = Color(0xFF4CAF50)
    val ErrorRed = Color(0xFFE57373)
}

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = SpellWriterColors.Background,
    onBackground = SpellWriterColors.OnBackground,
    surface = SpellWriterColors.Surface,
    onSurface = SpellWriterColors.OnSurface
)

@Composable
fun SpellWriterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
