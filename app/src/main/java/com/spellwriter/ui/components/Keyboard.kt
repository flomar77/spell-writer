package com.spellwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellwriter.ui.theme.SpellWriterColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QWERTY_ROWS = listOf(
    listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
    listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
    listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
)

// Long-press mappings for German umlauts
private val UMLAUT_MAP = mapOf(
    'A' to 'Ä',
    'O' to 'Ö',
    'U' to 'Ü',
    'S' to 'ß'
)

@Composable
fun SpellKeyboard(
    onKeyPressed: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QWERTY_ROWS.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { letter ->
                    KeyButton(
                        letter = letter,
                        umlaut = UMLAUT_MAP[letter],
                        onTap = { onKeyPressed(letter) },
                        onLongPress = { UMLAUT_MAP[letter]?.let { onKeyPressed(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    letter: Char,
    umlaut: Char?,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressed) SpellWriterColors.KeyPressed
                else SpellWriterColors.KeyBackground
            )
            .border(
                width = 1.dp,
                color = SpellWriterColors.KeyBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val longPressJob = scope.launch {
                            delay(500) // Long press threshold
                            if (umlaut != null) {
                                onLongPress()
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        if (longPressJob.isActive) {
                            longPressJob.cancel()
                            onTap()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = letter.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            if (umlaut != null) {
                Text(
                    text = umlaut.toString(),
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
