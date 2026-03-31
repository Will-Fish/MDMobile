package com.example.mdmobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mdmobile.data.model.ThemeMode
import com.example.mdmobile.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DEFAULT_FOLDER = stringPreferencesKey("default_folder")
        private val FONT_SIZE = intPreferencesKey("font_size")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val themeMode = when (preferences[THEME_MODE] ?: "auto") {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.AUTO
            }
            val defaultFolder = preferences[DEFAULT_FOLDER]
            val fontSize = preferences[FONT_SIZE] ?: 16

            UserPreferences(
                themeMode = themeMode,
                defaultFolder = defaultFolder,
                fontSize = fontSize
            )
        }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.AUTO -> "auto"
            }
        }
    }

    suspend fun updateDefaultFolder(folderPath: String?) {
        context.dataStore.edit { preferences ->
            if (folderPath != null) {
                preferences[DEFAULT_FOLDER] = folderPath
            } else {
                preferences.remove(DEFAULT_FOLDER)
            }
        }
    }

    suspend fun updateFontSize(fontSize: Int) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = fontSize
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}