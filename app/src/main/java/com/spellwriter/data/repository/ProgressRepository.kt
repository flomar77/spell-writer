package com.spellwriter.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.spellwriter.data.models.Progress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class ProgressRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> = DataStoreProvider.getProgressDataStore(context)

    private object PreferencesKeys {
        val STARS = intPreferencesKey("stars")
    }

    val progressFlow: Flow<Progress> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Progress(stars = preferences[PreferencesKeys.STARS] ?: 0)
        }

    suspend fun saveProgress(progress: Progress) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STARS] = progress.stars
        }
    }

    suspend fun clearAllProgress() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.STARS)
        }
        Log.d("ProgressRepository", "All progress cleared")
    }
}
