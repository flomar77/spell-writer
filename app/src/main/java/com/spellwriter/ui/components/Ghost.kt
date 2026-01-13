package com.spellwriter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.spellwriter.data.models.GhostExpression

@Composable
fun Ghost(
    expression: GhostExpression,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with actual drawable resources
    // For now, using placeholder
    /*
    val imageRes = when (expression) {
        GhostExpression.NEUTRAL -> R.drawable.ghost_neutral
        GhostExpression.HAPPY -> R.drawable.ghost_happy
        GhostExpression.UNHAPPY -> R.drawable.ghost_unhappy
        GhostExpression.DEAD -> R.drawable.ghost_dead
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Ghost",
        modifier = modifier.size(100.dp)
    )
    */

    // Placeholder: Box with text showing expression
    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(100.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = when (expression) {
                GhostExpression.NEUTRAL -> "👻"
                GhostExpression.HAPPY -> "👻😊"
                GhostExpression.UNHAPPY -> "👻😟"
                GhostExpression.DEAD -> "👻💀"
            },
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )
    }
}
