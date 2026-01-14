package com.spellwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
 * Story 1.3: Game Screen Layout
 *
 * Displays letters as the user types them, with a book-like appearance.
 * Shows placeholder text when no letters have been typed yet.
 */
@Composable
fun Grimoire(
    typedLetters: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)  // Wider than tall (book shape)
            .border(
                width = 3.dp,
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
            Text(
                text = typedLetters,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}
