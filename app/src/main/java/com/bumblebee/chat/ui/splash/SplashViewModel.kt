package com.bumblebee.chat.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashNavigationEvent {
    data object GoToOnboarding : SplashNavigationEvent()
    data object GoToMain : SplashNavigationEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isOnboarded = userPreferencesRepository.isOnboarded.first()
            if (isOnboarded) {
                _navigationEvent.emit(SplashNavigationEvent.GoToMain)
            } else {
                _navigationEvent.emit(SplashNavigationEvent.GoToOnboarding)
            }
        }
    }
}
