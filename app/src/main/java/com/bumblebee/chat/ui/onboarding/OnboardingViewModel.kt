package com.bumblebee.chat.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val displayName: String = "",
    val selectedEmoji: String = "\uD83D\uDC1D",
    val selectedColor: Long = 0xFF9B7EDE,
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun updateDisplayName(name: String) {
        if (name.length <= 20) {
            _uiState.update {
                it.copy(
                    displayName = name,
                    isValid = name.trim().length >= 2
                )
            }
        }
    }

    fun selectEmoji(emoji: String) {
        _uiState.update { it.copy(selectedEmoji = emoji) }
    }

    fun selectColor(color: Long) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun completeOnboarding() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userPreferencesRepository.completeOnboarding(
                name = state.displayName.trim(),
                emoji = state.selectedEmoji,
                color = state.selectedColor
            )
            _uiState.update { it.copy(isLoading = false, isComplete = true) }
        }
    }
}
