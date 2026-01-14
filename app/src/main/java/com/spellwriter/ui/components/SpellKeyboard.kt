package com.spellwriter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QWERTY keyboard component for spelling game.
 * Story 1.3: Game Screen Layout
 *
 * Displays a 3-row QWERTY keyboard with uppercase letters only.
 * Each key has minimum 48dp touch target for child accessibility.
 */
@Composable
fun SpellKeyboard(
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: QWERTYUIOP
        KeyboardRow(
            letters = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            onLetterClick = onLetterClick
        )

        // Row 2: ASDFGHJKL
        KeyboardRow(
            letters = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            onLetterClick = onLetterClick
        )

        // Row 3: ZXCVBNM
        KeyboardRow(
            letters = listOf("Z", "X", "C", "V", "B", "N", "M"),
            onLetterClick = onLetterClick
        )
    }
}

/**
 * Single row of keyboard letters.
 */
@Composable
private fun KeyboardRow(
    letters: List<String>,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        letters.forEach { letter ->
            KeyButton(
                letter = letter,
                onClick = { onLetterClick(letter) }
            )
        }
    }
}

/**
 * Individual keyboard key button.
 */
@Composable
private fun KeyButton(
    letter: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(48.dp),  // Minimum 48dp touch target (WCAG 2.1)
        contentPadding = PaddingValues(0.dp)  // Remove default padding for compact keys
    ) {
        Text(
            text = letter,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
