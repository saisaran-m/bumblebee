package com.bumblebee.chat.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.repository.ChatRepository
import com.bumblebee.chat.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> = chatRepository.getAllConversations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createHive(name: String, emoji: String, color: Long, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val hiveId = chatRepository.createHive(name, emoji, color)
            onCreated(hiveId)
        }
    }
}
