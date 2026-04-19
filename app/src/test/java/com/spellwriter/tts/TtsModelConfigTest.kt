package com.spellwriter.tts

import com.spellwriter.data.models.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TtsModelConfigTest {

    @Test
    fun getConfigForLanguage_german_returnsCorrectModelPaths() {
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.GERMAN)

        assertNotNull(config)
        assertEquals("vits-piper-de_DE-thorsten-medium", config.modelDir)
        assertEquals("de_DE-thorsten-medium.onnx", config.modelName)
        assertEquals("vits-piper-de_DE-thorsten-medium/tokens.txt", config.tokensPath)
    }

    @Test
    fun getConfigForLanguage_english_returnsCorrectModelPaths() {
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.ENGLISH)

        assertNotNull(config)
        assertEquals("vits-piper-en_GB-alan-low-int8", config.modelDir)
        assertEquals("en_GB-alan-low.onnx", config.modelName)
        assertEquals("vits-piper-en_GB-alan-low-int8/tokens.txt", config.tokensPath)
    }

    @Test
    fun modelConfig_containsExpectedFields() {
        val config = TtsModelConfig.getConfigForLanguage(AppLanguage.GERMAN)

        assertNotNull(config.modelDir)
        assertNotNull(config.modelName)
        assertNotNull(config.tokensPath)
    }
}
