package com.bumblebee.chat.data.nearby

import android.content.Context
import android.util.Log
import com.bumblebee.chat.core.constants.AppConstants
import com.bumblebee.chat.data.nearby.model.ConnectedPeer
import com.bumblebee.chat.data.nearby.model.ConnectionRequest
import com.bumblebee.chat.data.nearby.model.DiscoveredPeer
import com.bumblebee.chat.data.nearby.model.NearbyPacket
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "NearbyConnManager"
    private val client: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _connectedPeers = MutableStateFlow<List<ConnectedPeer>>(emptyList())
    val connectedPeers: StateFlow<List<ConnectedPeer>> = _connectedPeers.asStateFlow()

    private val _incomingConnectionRequest = MutableStateFlow<ConnectionRequest?>(null)
    val incomingConnectionRequest: StateFlow<ConnectionRequest?> = _incomingConnectionRequest.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<Pair<String, NearbyPacket>>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<Pair<String, NearbyPacket>> = _incomingPackets.asSharedFlow()

    private val _peerDisconnectedEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val peerDisconnectedEvents: SharedFlow<String> = _peerDisconnectedEvents.asSharedFlow()

    // Endpoint payload callback
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes() ?: return
                val packet = NearbyPacket.fromByteArray(bytes) ?: return
                scope.launch {
                    _incomingPackets.emit(Pair(endpointId, packet))
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Can be used for progress indicators in Phase 3
        }
    }

    // Connection lifecycle callback
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            val (name, emoji, color, userId) = parseEndpointInfo(connectionInfo.endpointName)
            val digits = connectionInfo.authenticationDigits ?: "0000"

            _incomingConnectionRequest.value = ConnectionRequest(
                endpointId = endpointId,
                endpointName = name,
                authenticationDigits = digits,
                avatarEmoji = emoji,
                avatarColor = color,
                userId = userId
            )
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (_incomingConnectionRequest.value?.endpointId == endpointId) {
                _incomingConnectionRequest.value = null
            }

            if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                val discovered = _discoveredPeers.value.find { it.endpointId == endpointId }
                val connectedPeer = ConnectedPeer(
                    endpointId = endpointId,
                    displayName = discovered?.displayName ?: "Nearby Bee",
                    avatarEmoji = discovered?.avatarEmoji ?: "\uD83D\uDC1D",
                    avatarColor = discovered?.avatarColor ?: 0xFF9B7EDE,
                    userId = discovered?.userId ?: ""
                )
                _connectedPeers.update { list -> (list.filter { it.endpointId != endpointId } + connectedPeer) }
                Log.d(tag, "Successfully connected to endpoint: $endpointId")
            } else {
                Log.w(tag, "Connection failed to endpoint: $endpointId code: ${resolution.status.statusCode}")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(tag, "Disconnected from endpoint: $endpointId")
            _connectedPeers.update { list -> list.filter { it.endpointId != endpointId } }
            scope.launch {
                _peerDisconnectedEvents.emit(endpointId)
            }
        }
    }

    // Endpoint discovery callback
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val (name, emoji, color, userId) = parseEndpointInfo(info.endpointName)
            val peer = DiscoveredPeer(
                endpointId = endpointId,
                displayName = name,
                avatarEmoji = emoji,
                avatarColor = color,
                userId = userId
            )
            _discoveredPeers.update { list -> (list.filter { it.endpointId != endpointId } + peer) }
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredPeers.update { list -> list.filter { it.endpointId != endpointId } }
        }
    }

    fun startAdvertising(displayName: String, emoji: String, color: Long, userId: String) {
        val endpointName = encodeEndpointInfo(displayName, emoji, color, userId)
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startAdvertising(
            endpointName,
            AppConstants.SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            _isAdvertising.value = true
            Log.d(tag, "Started advertising as $endpointName")
        }.addOnFailureListener { e ->
            _isAdvertising.value = false
            Log.e(tag, "Failed to start advertising", e)
        }
    }

    fun stopAdvertising() {
        client.stopAdvertising()
        _isAdvertising.value = false
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startDiscovery(
            AppConstants.SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            _isDiscovering.value = true
            Log.d(tag, "Started discovery")
        }.addOnFailureListener { e ->
            _isDiscovering.value = false
            Log.e(tag, "Failed to start discovery", e)
        }
    }

    fun stopDiscovery() {
        client.stopDiscovery()
        _isDiscovering.value = false
        _discoveredPeers.value = emptyList()
    }

    fun requestConnection(endpointId: String, localDisplayName: String, emoji: String, color: Long, userId: String) {
        val localEndpointName = encodeEndpointInfo(localDisplayName, emoji, color, userId)
        client.requestConnection(
            localEndpointName,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener {
            Log.d(tag, "Requested connection to $endpointId")
        }.addOnFailureListener { e ->
            Log.e(tag, "Failed to request connection to $endpointId", e)
        }
    }

    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener {
                Log.d(tag, "Accepted connection with $endpointId")
            }.addOnFailureListener { e ->
                Log.e(tag, "Failed to accept connection with $endpointId", e)
            }
        _incomingConnectionRequest.value = null
    }

    fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId)
        _incomingConnectionRequest.value = null
    }

    fun disconnect(endpointId: String) {
        client.disconnectFromEndpoint(endpointId)
        _connectedPeers.update { list -> list.filter { it.endpointId != endpointId } }
    }

    fun stopAll() {
        stopAdvertising()
        stopDiscovery()
        client.stopAllEndpoints()
        _connectedPeers.value = emptyList()
        _discoveredPeers.value = emptyList()
        _incomingConnectionRequest.value = null
    }

    fun sendPacket(endpointId: String, packet: NearbyPacket) {
        val payload = Payload.fromBytes(packet.toByteArray())
        client.sendPayload(endpointId, payload).addOnFailureListener { e ->
            Log.e(tag, "Failed to send packet to $endpointId", e)
        }
    }

    fun broadcastPacket(packet: NearbyPacket) {
        val connected = _connectedPeers.value
        if (connected.isEmpty()) return
        val payload = Payload.fromBytes(packet.toByteArray())
        client.sendPayload(connected.map { it.endpointId }, payload)
    }

    private fun encodeEndpointInfo(name: String, emoji: String, color: Long, userId: String): String {
        return "$name|$emoji|${color.toString(16)}|$userId"
    }

    private fun parseEndpointInfo(raw: String): Tuple4<String, String, Long, String> {
        val parts = raw.split("|")
        return if (parts.size >= 4) {
            val name = parts[0]
            val emoji = parts[1]
            val color = parts[2].toLongOrNull(16) ?: 0xFF9B7EDE
            val userId = parts[3]
            Tuple4(name, emoji, color, userId)
        } else {
            Tuple4(raw, "\uD83D\uDC1D", 0xFF9B7EDE, "")
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
