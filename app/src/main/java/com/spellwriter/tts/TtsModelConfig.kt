package com.spellwriter.tts

import com.spellwriter.data.models.AppLanguage

/**
 * Model configuration for sherpa-onnx TTS integration.
 * Provides paths and settings for VITS Piper models.
 */
data class ModelConfig(
    val modelDir: String,
    val modelName: String,
    val tokensPath: String,
    val espeakDataPath: String
)

/**
 * Configuration provider for TTS models based on app language.
 * Maps AppLanguage enum to corresponding VITS Piper model paths.
 */
object TtsModelConfig {

    /**
     * Get model configuration for the specified language.
     *
     * @param language The app language (GERMAN or ENGLISH)
     * @return ModelConfig with paths to model files and espeak-ng-data
     */
    fun getConfigForLanguage(language: AppLanguage): ModelConfig {
        return when (language) {
            AppLanguage.GERMAN -> ModelConfig(
                modelDir = "vits-piper-de_DE-thorsten-low-int8",
                modelName = "de_DE-thorsten-low.onnx",
                tokensPath = "vits-piper-de_DE-thorsten-low-int8/tokens.txt",
                espeakDataPath = "vits-piper-de_DE-thorsten-low-int8/espeak-ng-data"
            )
            AppLanguage.ENGLISH -> ModelConfig(
                modelDir = "vits-piper-en_US-danny-low-int8",
                modelName = "en_US-danny-low.onnx",
                tokensPath = "vits-piper-en_US-danny-low-int8/tokens.txt",
                espeakDataPath = "vits-piper-en_US-danny-low-int8/espeak-ng-data"
            )
        }
    }
}
