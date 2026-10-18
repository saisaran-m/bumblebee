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
            val existingConv = conversationDao.getConversationById(peerIdArg)
            val targetPeerId = if (existingConv != null && !existingConv.isHive && !existingConv.peerId.isNullOrBlank()) {
                existingConv.peerId
            } else {
                peerIdArg
            }

            val peer = chatRepository.getPeer(targetPeerId)
            val canonicalPeerId = peer?.peerId ?: targetPeerId
            val conv = chatRepository.getOrCreateConversation(canonicalPeerId, peer?.displayName)
            val convId = conv.conversationId

            _uiState.value = _uiState.value.copy(
                conversationId = convId,
                peer = peer ?: chatRepository.getPeer(canonicalPeerId)
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
        val peerId = _uiState.value.peer?.peerId ?: peerIdArg
        _uiState.value = _uiState.value.copy(inputText = "", isSending = true)

        viewModelScope.launch {
            chatRepository.sendMessage(convId, peerId, text)
            _uiState.value = _uiState.value.copy(isSending = false)
        }
    }

    fun sendImage(imageBytes: ByteArray) {
        val convId = _uiState.value.conversationId
        val peerId = _uiState.value.peer?.peerId ?: peerIdArg
        if (convId.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendImage(convId, peerId, imageBytes)
        }
    }

    fun sendVoiceNote(audioBytes: ByteArray, durationMs: Long) {
        val convId = _uiState.value.conversationId
        val peerId = _uiState.value.peer?.peerId ?: peerIdArg
        if (convId.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendVoiceNote(convId, peerId, audioBytes, durationMs)
        }
    }
}
