package com.spellwriter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays a list of completed words under the Grimoire.
 * Shows only the last 10 words to avoid clutter.
 * Uses small grey font to not distract the child learning.
 *
 * @param completedWords List of words the user has completed
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompletedWordsList(
    completedWords: List<String>,
    modifier: Modifier = Modifier
) {
    if (completedWords.isEmpty()) return

    val wordsToShow = completedWords.takeLast(10)

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        wordsToShow.forEachIndexed { index, word ->
            Text(
                fontSize = 14.sp,
                text = if (index < wordsToShow.size - 1) "$word  " else word,
                color = Color.Gray
            )
        }
    }
}
