package com.spellwriter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.R

/**
 * Grimoire (magical book) component that displays typed letters.
 * Story 1.3: Game Screen Layout (basic structure)
 * Story 1.4: Added animated letter display (AC3: fade-in animations)
 *
 * Displays letters as the user types them, with a book-like appearance.
 * Shows placeholder text when no letters have been typed yet.
 * AC3: Letters appear with smooth fade-in animation.
 * NFR1.4: Animations run at 60fps.
 */
@Composable
fun Grimoire(
    typedLetters: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)  // Wider than tall (book shape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (typedLetters.isEmpty()) {
            Text(
                text = stringResource(R.string.grimoire_placeholder),
                fontSize = 20.sp,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // Story 1.4: Animated letter display with fade-in
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                typedLetters.forEach { letter ->
                    // AC3: Fade-in animation for each letter (300ms)
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
            }
        }
    }
}
