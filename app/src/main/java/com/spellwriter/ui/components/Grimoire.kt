package com.spellwriter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Grimoire(
    typedLetters: String,
    targetWord: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(300.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 3.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color(0xFFFAF3E0)) // Aged paper color
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            targetWord.forEachIndexed { index, targetChar ->
                val isTyped = index < typedLetters.length
                val typedChar = typedLetters.getOrNull(index)

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn()
                ) {
                    Text(
                        text = if (isTyped) typedChar.toString() else "_",
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Serif,
                        color = if (isTyped) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
