package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Singleton DataStore provider to ensure only one instance per file.
 *
 * DataStore requires singleton access - multiple instances accessing the same file
 * will throw IllegalStateException. This object provides centralized DataStore creation.
 *
 * Uses extension properties on Context to ensure DataStore delegates are created only once.
 */
private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spell_writer_progress"
)

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spell_writer_session"
)

object DataStoreProvider {
    /**
     * Get the progress DataStore instance.
     */
    fun getProgressDataStore(context: Context): DataStore<Preferences> {
        return context.progressDataStore
    }

    /**
     * Get the session DataStore instance.
     */
    fun getSessionDataStore(context: Context): DataStore<Preferences> {
        return context.sessionDataStore
    }
}
