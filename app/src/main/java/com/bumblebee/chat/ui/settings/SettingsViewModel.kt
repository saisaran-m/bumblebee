package com.bumblebee.chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.crypto.CryptoManager
import com.bumblebee.chat.data.repository.ChatRepository
import com.bumblebee.chat.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String = "",
    val avatarEmoji: String = "\uD83D\uDC1D",
    val avatarColor: Long = 0xFF9B7EDE,
    val userId: String = "",
    val language: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val chatRepository: ChatRepository,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.displayName,
        userPreferencesRepository.avatarEmoji,
        userPreferencesRepository.avatarColor,
        userPreferencesRepository.userId,
        userPreferencesRepository.language
    ) { name, emoji, color, id, lang ->
        SettingsUiState(
            displayName = name,
            avatarEmoji = emoji,
            avatarColor = color,
            userId = id,
            language = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateDisplayName(newName: String) {
        if (newName.isNotBlank() && newName.length <= 20) {
            viewModelScope.launch {
                userPreferencesRepository.updateDisplayName(newName.trim())
            }
        }
    }

    fun updateAvatar(emoji: String, color: Long) {
        viewModelScope.launch {
            userPreferencesRepository.updateAvatar(emoji, color)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(lang)
        }
    }

    fun panicWipe(onWipeComplete: () -> Unit) {
        viewModelScope.launch {
            chatRepository.panicWipeData()
            cryptoManager.panicPurgeKeys()
            onWipeComplete()
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            chatRepository.resetAllDataAndLogout()
            onComplete()
        }
    }
}
