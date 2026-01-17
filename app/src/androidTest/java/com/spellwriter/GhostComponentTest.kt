package com.spellwriter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.spellwriter.data.models.GhostExpression
import com.spellwriter.ui.components.Ghost
import com.spellwriter.ui.theme.SpellWriterTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Ghost component.
 * Tests Ghost displays correctly with different expressions.
 */
class GhostComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ghost_displaysNeutralExpression() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.NEUTRAL)
            }
        }

        // Assert
        composeTestRule
            .onNodeWithContentDescription("Ghost character with neutral expression")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun ghost_hasCorrectSize() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.NEUTRAL)
            }
        }

        // Assert - Ghost should be 80dp (per architecture)
        val ghostNode = composeTestRule.onNodeWithContentDescription("Ghost character with neutral expression")
        ghostNode.assertExists()
        ghostNode.assertHeightIsAtLeast(80.dp)
        ghostNode.assertWidthIsAtLeast(80.dp)
    }

    @Test
    fun ghost_supportsAccessibility() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.NEUTRAL)
            }
        }

        // Assert - Ghost has semantic description for screen readers
        composeTestRule
            .onNode(hasContentDescription("Ghost character with neutral expression"))
            .assertExists()
    }

    @Test
    fun ghost_displaysHappyExpression() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.HAPPY)
            }
        }

        // Assert - Future implementation test
        composeTestRule
            .onNodeWithContentDescription("Ghost character with happy expression")
            .assertExists()
    }

    @Test
    fun ghost_displaysUnhappyExpression() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.UNHAPPY)
            }
        }

        // Assert - Future implementation test
        composeTestRule
            .onNodeWithContentDescription("Ghost character with unhappy expression")
            .assertExists()
    }

    @Test
    fun ghost_displaysDeadExpression() {
        // Arrange & Act
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(expression = GhostExpression.DEAD)
            }
        }

        // Assert - Future implementation test
        composeTestRule
            .onNodeWithContentDescription("Ghost character with dead expression")
            .assertExists()
    }

    // Story 1.5: TTS-synchronized animation tests

    @Test
    fun ghost_acceptsIsSpeakingParameter() {
        // Arrange & Act - Test that isSpeaking parameter is accepted
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(
                    expression = GhostExpression.NEUTRAL,
                    isSpeaking = false
                )
            }
        }

        // Assert - Ghost still displays correctly
        composeTestRule
            .onNodeWithContentDescription("Ghost character with neutral expression")
            .assertExists()
    }

    @Test
    fun ghost_whileSpeaking_stillDisplaysCorrectExpression() {
        // Arrange & Act - Test that isSpeaking doesn't interfere with expression
        composeTestRule.setContent {
            SpellWriterTheme {
                Ghost(
                    expression = GhostExpression.HAPPY,
                    isSpeaking = true
                )
            }
        }

        // Assert - Expression is still correct while speaking
        composeTestRule
            .onNodeWithContentDescription("Ghost character with happy expression")
            .assertExists()
    }
}
