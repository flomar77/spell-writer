package com.spellwriter.utils

import android.content.Context
import android.util.Log
import kotlin.random.Random

/**
 * Utility for selecting random GIF rewards from assets folder.
 *
 * GIFs are stored in `/assets/gifs/` directory and randomly selected
 * when a star is earned. Handles missing or empty folders gracefully.
 */
object GifSelector {
    private const val TAG = "GifSelector"
    private const val GIFS_FOLDER = "gifs"

    /**
     * Select a random GIF file path from the assets/gifs folder.
     *
     * @param context Application context for AssetManager access
     * @return GIF file path relative to assets folder (e.g., "gifs/cat1.gif"),
     *         or null if no GIF files found or folder doesn't exist
     */
    fun selectRandomGif(context: Context): String? {
        return try {
            // List all files in the gifs folder
            val allFiles = context.assets.list(GIFS_FOLDER) ?: run {
                Log.w(TAG, "GIF folder '$GIFS_FOLDER' not found in assets")
                return null
            }

            // Filter for .gif files (case-insensitive)
            val gifFiles = allFiles.filter { file ->
                file.endsWith(".gif", ignoreCase = true)
            }

            // Check if any GIF files were found
            if (gifFiles.isEmpty()) {
                Log.w(TAG, "No GIF files found in $GIFS_FOLDER folder")
                return null
            }

            // Randomly select one GIF file
            val selectedFile = gifFiles[Random.nextInt(gifFiles.size)]
            val selectedPath = "$GIFS_FOLDER/$selectedFile"

            Log.d(TAG, "Selected GIF: $selectedPath (from ${gifFiles.size} options)")
            selectedPath

        } catch (e: Exception) {
            // Handle any I/O errors (folder doesn't exist, permission issues, etc.)
            Log.e(TAG, "Error selecting GIF from $GIFS_FOLDER folder", e)
            null
        }
    }
}
