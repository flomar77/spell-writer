package com.spellwriter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Alphabetical keyboard component for spelling game.
 * Story 1.5: Alphabetical Keyboard Layout (Bug Fix: Responsive sizing)
 *
 * Displays a 3-row alphabetical keyboard (A-Z) with uppercase letters only.
 * Keys dynamically size to fit screen width while maintaining accessibility.
 */
@Composable
fun SpellKeyboard(
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Calculate key size based on available width
        // Row 1 has 9 keys, so we use that as the maximum
        val spacingDp = 4.dp
        val horizontalPadding = 16.dp
        val numKeysInLongestRow = 9
        val totalSpacing = spacingDp * (numKeysInLongestRow - 1) + horizontalPadding * 2
        val availableWidth = maxWidth - totalSpacing
        val keySize = (availableWidth / numKeysInLongestRow).coerceIn(36.dp, 48.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: ABCDEFGHI (9 keys)
            KeyboardRow(
                letters = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I"),
                onLetterClick = onLetterClick,
                keySize = keySize,
                spacing = spacingDp
            )

            // Row 2: JKLMNOPQR (9 keys)
            KeyboardRow(
                letters = listOf("J", "K", "L", "M", "N", "O", "P", "Q", "R"),
                onLetterClick = onLetterClick,
                keySize = keySize,
                spacing = spacingDp
            )

            // Row 3: STUVWXYZ (8 keys)
            KeyboardRow(
                letters = listOf("S", "T", "U", "V", "W", "X", "Y", "Z"),
                onLetterClick = onLetterClick,
                keySize = keySize,
                spacing = spacingDp
            )
        }
    }
}

/**
 * Single row of keyboard letters with dynamic sizing.
 */
@Composable
private fun KeyboardRow(
    letters: List<String>,
    onLetterClick: (String) -> Unit,
    keySize: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
    ) {
        letters.forEach { letter ->
            KeyButton(
                letter = letter,
                onClick = { onLetterClick(letter) },
                size = keySize
            )
        }
    }
}

/**
 * Individual keyboard key button with responsive sizing.
 * Dynamically sizes between 36dp-48dp to fit screen while maintaining accessibility.
 */
@Composable
private fun KeyButton(
    letter: String,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(size)
            .height(size),
        contentPadding = PaddingValues(0.dp)  // Remove default padding for compact keys
    ) {
        Text(
            text = letter,
            fontSize = (size.value * 0.375).sp,  // Scale font size with button size
            fontWeight = FontWeight.Bold
        )
    }
}
