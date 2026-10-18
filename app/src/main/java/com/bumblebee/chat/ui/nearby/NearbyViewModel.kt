package com.bumblebee.chat.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.nearby.model.ConnectedPeer
import com.bumblebee.chat.data.nearby.model.ConnectionRequest
import com.bumblebee.chat.data.nearby.model.DiscoveredPeer
import com.bumblebee.chat.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = chatRepository.discoveredPeers
    val connectedPeers: StateFlow<List<ConnectedPeer>> = chatRepository.connectedPeers
    val isScanning: StateFlow<Boolean> = chatRepository.isScanning
    val incomingConnectionRequest: StateFlow<ConnectionRequest?> = chatRepository.incomingConnectionRequest

    fun startScanning() {
        viewModelScope.launch {
            chatRepository.startNearbyServices()
        }
    }

    fun stopScanning() {
        chatRepository.stopNearbyServices()
    }

    fun connectToPeer(endpointId: String) {
        viewModelScope.launch {
            chatRepository.connectToPeer(endpointId)
        }
    }

    fun acceptConnection(endpointId: String) {
        chatRepository.acceptConnection(endpointId)
    }

    fun rejectConnection(endpointId: String) {
        chatRepository.rejectConnection(endpointId)
    }
}
