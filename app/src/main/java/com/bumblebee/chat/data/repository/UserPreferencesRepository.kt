package com.bumblebee.chat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_EMOJI = stringPreferencesKey("avatar_emoji")
        val AVATAR_COLOR = longPreferencesKey("avatar_color")
        val USER_ID = stringPreferencesKey("user_id")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val isOnboarded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_ONBOARDED] ?: false
    }

    val displayName: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DISPLAY_NAME] ?: ""
    }

    val avatarEmoji: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.AVATAR_EMOJI] ?: "\uD83D\uDC1D"
    }

    val avatarColor: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.AVATAR_COLOR] ?: 0xFF9B7EDE
    }

    val userId: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.USER_ID] ?: ""
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "en"
    }

    suspend fun completeOnboarding(name: String, emoji: String, color: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDED] = true
            prefs[Keys.DISPLAY_NAME] = name
            prefs[Keys.AVATAR_EMOJI] = emoji
            prefs[Keys.AVATAR_COLOR] = color
            if (prefs[Keys.USER_ID].isNullOrEmpty()) {
                prefs[Keys.USER_ID] = generateUserId()
            }
        }
    }

    suspend fun updateDisplayName(name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = name
        }
    }

    suspend fun updateAvatar(emoji: String, color: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.AVATAR_EMOJI] = emoji
            prefs[Keys.AVATAR_COLOR] = color
        }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun resetProfile() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun generateUserId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "BB-" + (1..8).map { chars.random() }.joinToString("")
    }
}
