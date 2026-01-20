package com.spellwriter.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.spellwriter.R

/**
 * Sound manager for game audio effects.
 * Handles success and error sound playback using MediaPlayer.
 * Story 1.4: Core Word Gameplay
 *
 * @param context Application context for loading sound resources
 */
class SoundManager(private val context: Context) {
    private var successPlayer: MediaPlayer? = null
    private var errorPlayer: MediaPlayer? = null

    init {
        loadSounds()
    }

    /**
     * Load sound effects from resources.
     * Handles missing sound files gracefully.
     */
    private fun loadSounds() {
        try {
            // Load success sound
            successPlayer = try {
                MediaPlayer.create(context, R.raw.success)
            } catch (e: Exception) {
                Log.w(TAG, "Success sound not found - continuing without sound", e)
                null
            }

            // Load error sound
            errorPlayer = try {
                MediaPlayer.create(context, R.raw.error)
            } catch (e: Exception) {
                Log.w(TAG, "Error sound not found - continuing without sound", e)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sounds", e)
        }
    }

    /**
     * Play success sound effect.
     * Called when player types a correct letter.
     */
    fun playSuccess() {
        successPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()  // Prepare after stop for replay
                }
                it.seekTo(0)
                it.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing success sound", e)
            }
        }
    }

    /**
     * Play error sound effect.
     * Called when player types an incorrect letter.
     * Uses gentle, non-discouraging sound per AC4.
     */
    fun playError() {
        errorPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()  // Prepare after stop for replay
                }
                it.seekTo(0)
                it.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing error sound", e)
            }
        }
    }

    /**
     * Release MediaPlayer resources.
     * Must be called in ViewModel.onCleared() to prevent memory leaks.
     */
    fun release() {
        try {
            successPlayer?.release()
            errorPlayer?.release()
            successPlayer = null
            errorPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing sound resources", e)
        }
    }

    companion object {
        private const val TAG = "SoundManager"
    }
}
