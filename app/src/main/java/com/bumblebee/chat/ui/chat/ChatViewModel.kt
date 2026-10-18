package com.bumblebee.chat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.local.dao.ConversationDao
import com.bumblebee.chat.data.repository.ChatRepository
import com.bumblebee.chat.domain.model.Message
import com.bumblebee.chat.domain.model.Peer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversationId: String = "",
    val peer: Peer? = null,
    val inputText: String = "",
    val isSending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val conversationDao: ConversationDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val peerIdArg: String = checkNotNull(savedStateHandle["peerId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            val peer = chatRepository.getPeer(peerIdArg)
            var conv = conversationDao.getConversationByPeerId(peerIdArg)
            val convId = conv?.conversationId ?: java.util.UUID.randomUUID().toString()

            _uiState.value = _uiState.value.copy(
                conversationId = convId,
                peer = peer
            )

            // Mark conversation as read
            chatRepository.markConversationAsRead(convId)

            // Observe messages
            chatRepository.getMessages(convId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val convId = _uiState.value.conversationId
        _uiState.value = _uiState.value.copy(inputText = "", isSending = true)

        viewModelScope.launch {
            chatRepository.sendMessage(convId, peerIdArg, text)
            _uiState.value = _uiState.value.copy(isSending = false)
        }
    }
}
