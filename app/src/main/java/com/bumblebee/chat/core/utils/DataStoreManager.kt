package com.bumblebee.chat.core.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preferences via Jetpack DataStore.
 * Provides Flow-based getters and suspend setters for all user settings.
 */
@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AVATAR_EMOJI = stringPreferencesKey("user_avatar_emoji")
        val KEY_USER_AVATAR_COLOR = longPreferencesKey("user_avatar_color")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val KEY_LANGUAGE = stringPreferencesKey("language")

        const val DEFAULT_AVATAR_EMOJI = "\uD83D\uDC1D" // 🐝
        const val DEFAULT_AVATAR_COLOR = 0xFF9B7EDEL      // BumblebeePrimary
        const val DEFAULT_LANGUAGE = "en"
    }

    // ── User Name ──────────────────────────────────────────────────────

    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: ""
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    // ── User Avatar Emoji ──────────────────────────────────────────────

    val userAvatarEmoji: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_AVATAR_EMOJI] ?: DEFAULT_AVATAR_EMOJI
    }

    suspend fun setUserAvatarEmoji(emoji: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_AVATAR_EMOJI] = emoji
        }
    }

    // ── User Avatar Color ──────────────────────────────────────────────

    val userAvatarColor: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_USER_AVATAR_COLOR] ?: DEFAULT_AVATAR_COLOR
    }

    suspend fun setUserAvatarColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_AVATAR_COLOR] = color
        }
    }

    // ── User ID ────────────────────────────────────────────────────────

    /**
     * Returns the persisted user ID. Generates and stores a new UUID on first access.
     */
    val userId: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ID] ?: UUID.randomUUID().toString()
    }

    /**
     * Ensures a user ID is persisted. Call this during app startup.
     * Only writes if no ID exists yet.
     */
    suspend fun ensureUserId(): String {
        var generatedId = ""
        dataStore.edit { preferences ->
            if (preferences[KEY_USER_ID] == null) {
                val newId = UUID.randomUUID().toString()
                preferences[KEY_USER_ID] = newId
                generatedId = newId
            } else {
                generatedId = preferences[KEY_USER_ID]!!
            }
        }
        return generatedId
    }

    // ── Onboarding ─────────────────────────────────────────────────────

    val isOnboarded: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_ONBOARDED] ?: false
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_ONBOARDED] = onboarded
        }
    }

    // ── Language ────────────────────────────────────────────────────────

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }
}
