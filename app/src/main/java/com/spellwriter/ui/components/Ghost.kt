package com.spellwriter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spellwriter.data.models.GhostExpression

/**
 * Ghost character component for Story 1.1.
 * Currently implements NEUTRAL expression only using emoji placeholder.
 * Future stories will add drawable resources for all expressions.
 *
 * @param expression The emotional state of the ghost (only NEUTRAL implemented in Story 1.1)
 * @param modifier Modifier for the component (default size should be 80dp per architecture)
 */
@Composable
fun Ghost(
    expression: GhostExpression,
    modifier: Modifier = Modifier
) {
    // Story 1.1: Placeholder implementation using emoji
    // Story 1.5 will replace with actual drawable resources
    Box(
        modifier = modifier
            .size(80.dp)
            .semantics {
                contentDescription = "Ghost character with ${expression.name.lowercase()} expression"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (expression) {
                GhostExpression.NEUTRAL -> "👻"
                GhostExpression.HAPPY -> "👻😊"    // Future implementation
                GhostExpression.UNHAPPY -> "👻😟"  // Future implementation
                GhostExpression.DEAD -> "👻💀"     // Future implementation
            },
            style = MaterialTheme.typography.displayLarge
        )
    }
}
