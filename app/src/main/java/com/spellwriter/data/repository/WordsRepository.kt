package com.spellwriter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

class WordsRepository(private val context: Context) {
    // Extension property for DataStore instance
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "spell_writer_words"
    )

    private object PreferencesLanguages {
        val EN = intPreferencesKey("english")
        val DE = intPreferencesKey("german")
    }
}
