package com.spellwriter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.spellwriter.data.models.HintState

/**
 * Grimoire (magical book) component that displays typed letters.
 * Story 1.3: Game Screen Layout (basic structure)
 * Story 1.4: Added animated letter display (AC3: fade-in animations)
 * Hint Feature: Displays grey hint letters after 5 consecutive failures
 *
 * Displays letters as the user types them, with a book-like appearance.
 * Shows placeholder text when no letters have been typed yet.
 * AC3: Letters appear with smooth fade-in animation.
 * NFR1.4: Animations run at 60fps.
 *
 * @param typedLetters The correctly typed letters so far
 * @param hintState Optional hint state with letter and position to display as grey hint
 * @param modifier Optional modifier for the component
 */
@Composable
fun Grimoire(
    typedLetters: String,
    hintState: HintState? = null,
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
        if (typedLetters.isEmpty() && hintState == null) {
            Text(
                text = stringResource(R.string.grimoire_placeholder),
                fontSize = 20.sp,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // Story 1.4: Animated letter display with fade-in
            // Hint Feature: Display both typed letters and hint letters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Calculate display length - extend to include hint position if hint is shown
                val displayLength = if (hintState != null) {
                    maxOf(typedLetters.length, hintState.positionIndex + 1)
                } else {
                    typedLetters.length
                }

                repeat(displayLength) { index ->
                    val isHintPosition = hintState != null && index == hintState.positionIndex
                    val isTypedPosition = index < typedLetters.length

                    when {
                        isTypedPosition -> {
                            // Display typed letter (correct letters already entered)
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300))
                            ) {
                                Text(
                                    text = typedLetters[index].toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp,
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                        }
                        isHintPosition -> {
                            // Display hint letter in grey with fade animations
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) {
                                Text(
                                    text = hintState.letter.toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
