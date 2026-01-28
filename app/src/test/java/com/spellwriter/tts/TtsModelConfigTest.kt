package com.spellwriter.tts

import com.spellwriter.data.models.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for TtsModelConfig.
 * Tests model configuration for German and English language support.
 */
class TtsModelConfigTest {

    @Test
    fun getConfigForLanguage_german_returnsCorrectModelPaths() {
        // Act
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.GERMAN)

        // Assert
        assertNotNull(config)
        assertEquals("vits-piper-de_DE-thorsten-low-int8", config.modelDir)
        assertEquals("de_DE-thorsten-low.onnx", config.modelName)
    }

    @Test
    fun getConfigForLanguage_english_returnsCorrectModelPaths() {
        // Act
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.ENGLISH)

        // Assert
        assertNotNull(config)
        assertEquals("vits-piper-en_US-danny-low-int8", config.modelDir)
        assertEquals("en_US-danny-low.onnx", config.modelName)
    }

    @Test
    fun modelConfig_containsExpectedFields() {
        // Act
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.GERMAN)

        // Assert - verify ModelConfig has all required fields
        assertNotNull(config.modelDir)
        assertNotNull(config.modelName)
        assertNotNull(config.tokensPath)
        assertNotNull(config.espeakDataPath)
    }

    @Test
    fun modelConfig_german_matchesAssetStructure() {
        // Act
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.GERMAN)

        // Assert - verify paths match expected asset structure
        assertEquals("vits-piper-de_DE-thorsten-low-int8", config.modelDir)
        assertEquals("de_DE-thorsten-low.onnx", config.modelName)
        assertEquals("vits-piper-de_DE-thorsten-low-int8/tokens.txt", config.tokensPath)
        assertEquals("vits-piper-de_DE-thorsten-low-int8/espeak-ng-data", config.espeakDataPath)
    }

    @Test
    fun modelConfig_english_matchesAssetStructure() {
        // Act
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.ENGLISH)

        // Assert - verify paths match expected asset structure
        assertEquals("vits-piper-en_US-danny-low-int8", config.modelDir)
        assertEquals("en_US-danny-low.onnx", config.modelName)
        assertEquals("vits-piper-en_US-danny-low-int8/tokens.txt", config.tokensPath)
        assertEquals("vits-piper-en_US-danny-low-int8/espeak-ng-data", config.espeakDataPath)
    }
}
