package com.bumblebee.chat.data.repository

import android.content.Context
import android.util.Base64
import com.bumblebee.chat.data.local.dao.ConversationDao
import com.bumblebee.chat.data.local.dao.MessageDao
import com.bumblebee.chat.data.local.dao.PeerDao
import com.bumblebee.chat.data.local.entity.ConversationEntity
import com.bumblebee.chat.data.local.entity.MessageEntity
import com.bumblebee.chat.data.local.entity.PeerEntity
import com.bumblebee.chat.data.nearby.NearbyConnectionManager
import com.bumblebee.chat.data.nearby.model.ConnectedPeer
import com.bumblebee.chat.data.nearby.model.DiscoveredPeer
import com.bumblebee.chat.data.nearby.model.NearbyPacket
import com.bumblebee.chat.domain.model.Conversation
import com.bumblebee.chat.domain.model.Message
import com.bumblebee.chat.domain.model.MessageStatus
import com.bumblebee.chat.domain.model.MessageType
import com.bumblebee.chat.domain.model.Peer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nearbyManager: NearbyConnectionManager,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = nearbyManager.discoveredPeers
    val connectedPeers: StateFlow<List<ConnectedPeer>> = nearbyManager.connectedPeers
    val isScanning: StateFlow<Boolean> = nearbyManager.isDiscovering
    val incomingConnectionRequest = nearbyManager.incomingConnectionRequest

    init {
        observeIncomingPackets()
        observePeerConnections()
    }

    private fun observeIncomingPackets() {
        scope.launch {
            nearbyManager.incomingPackets.collect { (endpointId, packet) ->
                when (packet.type) {
                    NearbyPacket.PacketType.MESSAGE -> handleIncomingMessage(endpointId, packet)
                    NearbyPacket.PacketType.HIVE_MESSAGE -> handleIncomingMessage(endpointId, packet)
                    NearbyPacket.PacketType.SOS_BUZZ -> handleIncomingMessage(endpointId, packet)
                    NearbyPacket.PacketType.ACK_DELIVERED -> handleAckDelivered(packet.messageId)
                    NearbyPacket.PacketType.ACK_READ -> handleAckRead(packet.messageId)
                    NearbyPacket.PacketType.HANDSHAKE -> handleHandshake(endpointId, packet)
                    NearbyPacket.PacketType.DISCONNECT -> handlePeerDisconnect(endpointId)
                    NearbyPacket.PacketType.HUM -> {
                        // Ephemeral typing indicator handled via active connection events
                    }
                }
            }
        }
    }

    private fun observePeerConnections() {
        scope.launch {
            nearbyManager.peerConnectedEvents.collect { peer ->
                val peerId = peer.userId.ifBlank { peer.endpointId }
                val existing = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(peer.endpointId)
                val updated = (existing ?: PeerEntity(
                    peerId = peerId,
                    displayName = peer.displayName,
                    avatarEmoji = peer.avatarEmoji,
                    avatarColor = peer.avatarColor
                )).copy(
                    peerId = peerId,
                    displayName = peer.displayName,
                    avatarEmoji = peer.avatarEmoji,
                    avatarColor = peer.avatarColor,
                    endpointId = peer.endpointId,
                    isConnected = true,
                    isNearby = true,
                    lastSeen = System.currentTimeMillis()
                )
                peerDao.insertOrUpdate(updated)

                getOrCreateConversation(peerId, peer.displayName)

                // Transmit immediate bidirectional HANDSHAKE packet so both sides have user IDs
                val localUserId = userPreferencesRepository.userId.first()
                val localName = userPreferencesRepository.displayName.first()
                val localEmoji = userPreferencesRepository.avatarEmoji.first()
                val localColor = userPreferencesRepository.avatarColor.first()
                val handshake = NearbyPacket(
                    type = NearbyPacket.PacketType.HANDSHAKE,
                    senderId = localUserId,
                    senderName = localName,
                    senderEmoji = localEmoji,
                    senderColor = localColor
                )
                nearbyManager.sendPacket(peer.endpointId, handshake)
            }
        }

        scope.launch {
            nearbyManager.connectedPeers.collect { connectedList ->
                connectedList.forEach { peer ->
                    val peerId = peer.userId.ifBlank { peer.endpointId }
                    val existing = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(peer.endpointId)
                    val updated = (existing ?: PeerEntity(
                        peerId = peerId,
                        displayName = peer.displayName,
                        avatarEmoji = peer.avatarEmoji,
                        avatarColor = peer.avatarColor
                    )).copy(
                        peerId = peerId,
                        displayName = peer.displayName,
                        avatarEmoji = peer.avatarEmoji,
                        avatarColor = peer.avatarColor,
                        endpointId = peer.endpointId,
                        isConnected = true,
                        isNearby = true,
                        lastSeen = System.currentTimeMillis()
                    )
                    peerDao.insertOrUpdate(updated)
                    getOrCreateConversation(peerId, peer.displayName)
                }
            }
        }

        scope.launch {
            nearbyManager.peerDisconnectedEvents.collect { endpointId ->
                peerDao.disconnectByEndpoint(endpointId)
            }
        }
    }

    suspend fun getOrCreateConversation(peerId: String, peerName: String? = null): ConversationEntity {
        var conv = conversationDao.getConversationByPeerId(peerId)
        if (conv == null) {
            val peer = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(peerId)
            if (peer != null) {
                if (peer.peerId != peerId) {
                    conv = conversationDao.getConversationByPeerId(peer.peerId)
                }
                if (conv == null && peer.endpointId != null && peer.endpointId != peerId) {
                    conv = conversationDao.getConversationByPeerId(peer.endpointId)
                }
            }
        }
        if (conv == null) {
            val newConv = ConversationEntity(
                conversationId = UUID.randomUUID().toString(),
                peerId = peerId,
                isHive = false,
                lastMessage = if (peerName != null) "Connected with $peerName" else null,
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 0
            )
            conversationDao.insertOrUpdate(newConv)
            return newConv
        }
        return conv
    }

    private fun saveReceivedImage(messageId: String, base64Data: String): String? {
        return try {
            val imagesDir = File(context.cacheDir, "bumblebee_nectar")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val file = File(imagesDir, "img_${messageId}.jpg")
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun handleIncomingMessage(endpointId: String, packet: NearbyPacket) {
        val peerId = packet.senderId.ifBlank { endpointId }

        // 1. Ensure peer exists in DB
        val existingPeer = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(endpointId)
        val peer = (existingPeer ?: PeerEntity(
            peerId = peerId,
            displayName = packet.senderName,
            avatarEmoji = packet.senderEmoji,
            avatarColor = packet.senderColor
        )).copy(
            peerId = peerId,
            displayName = packet.senderName,
            avatarEmoji = packet.senderEmoji,
            avatarColor = packet.senderColor,
            endpointId = endpointId,
            isConnected = true,
            isNearby = true,
            lastSeen = System.currentTimeMillis()
        )
        peerDao.insertOrUpdate(peer)

        // 2. Ensure conversation exists
        val conv = getOrCreateConversation(peerId, packet.senderName)
        val convId = conv.conversationId

        // 3. Process media if attached
        var localMediaUri: String? = null
        if (!packet.mediaBase64.isNullOrEmpty()) {
            localMediaUri = saveReceivedImage(packet.messageId ?: UUID.randomUUID().toString(), packet.mediaBase64)
        }

        // 4. Insert message
        val msgId = packet.messageId ?: UUID.randomUUID().toString()
        val isPhoto = localMediaUri != null || packet.mediaType == "IMAGE"
        val message = MessageEntity(
            messageId = msgId,
            conversationId = convId,
            senderId = peerId,
            content = packet.content ?: if (isPhoto) "Photo" else "",
            timestamp = packet.timestamp,
            type = if (isPhoto) "IMAGE" else "TEXT",
            status = "DELIVERED",
            isOutgoing = false,
            mediaUri = localMediaUri
        )
        messageDao.insertOrUpdate(message)

        // 5. Update conversation last message & unread count
        conversationDao.insertOrUpdate(
            conv.copy(
                lastMessage = if (isPhoto) "📷 Photo" else packet.content,
                lastMessageTimestamp = packet.timestamp,
                unreadCount = conv.unreadCount + 1
            )
        )

        // 6. Send ACK_DELIVERED back to sender
        val localName = userPreferencesRepository.displayName.first()
        val localUserId = userPreferencesRepository.userId.first()
        val ack = NearbyPacket(
            type = NearbyPacket.PacketType.ACK_DELIVERED,
            senderId = localUserId,
            senderName = localName,
            messageId = msgId
        )
        nearbyManager.sendPacket(endpointId, ack)
    }

    private suspend fun handleAckDelivered(messageId: String?) {
        if (!messageId.isNullOrEmpty()) {
            messageDao.updateStatus(messageId, "DELIVERED")
        }
    }

    private suspend fun handleAckRead(messageId: String?) {
        if (!messageId.isNullOrEmpty()) {
            messageDao.updateStatus(messageId, "READ")
        }
    }

    private suspend fun handleHandshake(endpointId: String, packet: NearbyPacket) {
        val peerId = packet.senderId.ifBlank { endpointId }
        val existing = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(endpointId)
        val peer = (existing ?: PeerEntity(
            peerId = peerId,
            displayName = packet.senderName,
            avatarEmoji = packet.senderEmoji,
            avatarColor = packet.senderColor
        )).copy(
            peerId = peerId,
            displayName = packet.senderName,
            avatarEmoji = packet.senderEmoji,
            avatarColor = packet.senderColor,
            endpointId = endpointId,
            isConnected = true,
            isNearby = true,
            lastSeen = System.currentTimeMillis()
        )
        peerDao.insertOrUpdate(peer)
        getOrCreateConversation(peerId, packet.senderName)
    }

    private suspend fun handlePeerDisconnect(endpointId: String) {
        peerDao.disconnectByEndpoint(endpointId)
    }

    // --- Public API for UI ---

    fun getAllConversations(): Flow<List<Conversation>> {
        return combine(
            conversationDao.getAllConversations(),
            peerDao.getAllPeers()
        ) { conversations, peers ->
            val peerMap = peers.associateBy { it.peerId }
            conversations.map { entity ->
                val peer = entity.peerId?.let { peerMap[it] }
                Conversation(
                    conversationId = entity.conversationId,
                    peerId = entity.peerId,
                    hiveName = entity.hiveName,
                    isHive = entity.isHive,
                    lastMessage = entity.lastMessage,
                    lastMessageTimestamp = entity.lastMessageTimestamp,
                    unreadCount = entity.unreadCount,
                    createdAt = entity.createdAt,
                    isPinned = entity.isPinned,
                    peerName = peer?.displayName,
                    peerEmoji = peer?.avatarEmoji,
                    peerColor = peer?.avatarColor,
                    isOnline = peer?.isConnected ?: false
                )
            }
        }
    }

    fun getMessages(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId).map { list ->
            list.map { entity ->
                Message(
                    messageId = entity.messageId,
                    conversationId = entity.conversationId,
                    senderId = entity.senderId,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    type = MessageType.valueOf(entity.type),
                    status = MessageStatus.valueOf(entity.status),
                    isOutgoing = entity.isOutgoing,
                    replyToId = entity.replyToId,
                    mediaUri = entity.mediaUri,
                    mediaName = entity.mediaName,
                    mediaSizeBytes = entity.mediaSizeBytes
                )
            }
        }
    }

    suspend fun getPeer(peerId: String): Peer? {
        val entity = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(peerId)
        return entity?.let {
            Peer(
                peerId = it.peerId,
                displayName = it.displayName,
                avatarEmoji = it.avatarEmoji,
                avatarColor = it.avatarColor,
                endpointId = it.endpointId,
                isConnected = it.isConnected,
                isNearby = it.isNearby,
                lastSeen = it.lastSeen,
                hopCount = it.hopCount,
                publicKey = it.publicKey
            )
        }
    }

    suspend fun sendMessage(conversationId: String, peerId: String, text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false

        val localUserId = userPreferencesRepository.userId.first()
        val localName = userPreferencesRepository.displayName.first()
        val localEmoji = userPreferencesRepository.avatarEmoji.first()
        val localColor = userPreferencesRepository.avatarColor.first()

        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val peer = peerDao.getPeerById(peerId) ?: peerDao.getPeerByEndpointId(peerId)
        val canonicalPeerId = peer?.peerId ?: peerId
        val endpointId = peer?.endpointId ?: nearbyManager.connectedPeers.value.find {
            it.userId == canonicalPeerId || it.endpointId == peerId || it.userId == peerId
        }?.endpointId

        // 1. Save to Room
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            senderId = localUserId,
            content = trimmed,
            timestamp = now,
            type = "TEXT",
            status = if (endpointId != null) "SENDING" else "FAILED",
            isOutgoing = true
        )
        messageDao.insertOrUpdate(message)

        // 2. Update conversation
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.insertOrUpdate(
                conv.copy(
                    lastMessage = trimmed,
                    lastMessageTimestamp = now
                )
            )
        }

        // 3. Transmit packet over Nearby if connected
        if (endpointId != null) {
            val packet = NearbyPacket(
                type = NearbyPacket.PacketType.MESSAGE,
                senderId = localUserId,
                senderName = localName,
                senderEmoji = localEmoji,
                senderColor = localColor,
                messageId = messageId,
                conversationId = conversationId,
                content = trimmed,
                timestamp = now
            )
            nearbyManager.sendPacket(endpointId, packet)
            messageDao.updateStatus(messageId, "SENT")
            return true
        }

        return false
    }

    suspend fun sendImage(conversationId: String, peerIdArg: String, imageBytes: ByteArray): Boolean {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. Save local copy to cache
        val imagesDir = File(context.cacheDir, "bumblebee_nectar")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val file = File(imagesDir, "img_${messageId}.jpg")
        FileOutputStream(file).use { it.write(imageBytes) }
        val localPath = file.absolutePath

        val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val localUserId = userPreferencesRepository.userId.first()
        val localName = userPreferencesRepository.displayName.first()
        val localEmoji = userPreferencesRepository.avatarEmoji.first()
        val localColor = userPreferencesRepository.avatarColor.first()

        val peer = peerDao.getPeerById(peerIdArg) ?: peerDao.getPeerByEndpointId(peerIdArg)
        val canonicalPeerId = peer?.peerId ?: peerIdArg
        val endpointId = peer?.endpointId ?: nearbyManager.connectedPeers.value.find {
            it.userId == canonicalPeerId || it.endpointId == peerIdArg || it.userId == peerIdArg
        }?.endpointId

        // 2. Save Message in Room
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            senderId = localUserId,
            content = "Photo",
            timestamp = now,
            type = "IMAGE",
            status = if (endpointId != null) "SENDING" else "FAILED",
            isOutgoing = true,
            mediaUri = localPath
        )
        messageDao.insertOrUpdate(message)

        // 3. Update conversation
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.insertOrUpdate(
                conv.copy(
                    lastMessage = "📷 Photo",
                    lastMessageTimestamp = now
                )
            )
        }

        // 4. Send over Nearby
        if (endpointId != null) {
            val packet = NearbyPacket(
                type = NearbyPacket.PacketType.MESSAGE,
                senderId = localUserId,
                senderName = localName,
                senderEmoji = localEmoji,
                senderColor = localColor,
                messageId = messageId,
                conversationId = conversationId,
                content = "Photo",
                timestamp = now,
                mediaType = "IMAGE",
                mediaBase64 = base64
            )
            nearbyManager.sendPacket(endpointId, packet)
            messageDao.updateStatus(messageId, "SENT")
            return true
        }
        return false
    }

    suspend fun resetAllDataAndLogout() {
        nearbyManager.stopAll()
        userPreferencesRepository.resetProfile()
        peerDao.deleteAll()
        conversationDao.deleteAll()
        messageDao.deleteAll()
    }

    suspend fun markConversationAsRead(conversationId: String) {
        conversationDao.clearUnreadCount(conversationId)
        val conv = conversationDao.getConversationById(conversationId)
        val peerId = conv?.peerId ?: return
        val peer = peerDao.getPeerById(peerId)
        val endpointId = peer?.endpointId

        if (endpointId != null) {
            val localUserId = userPreferencesRepository.userId.first()
            val ack = NearbyPacket(
                type = NearbyPacket.PacketType.ACK_READ,
                senderId = localUserId,
                senderName = "",
                conversationId = conversationId
            )
            nearbyManager.sendPacket(endpointId, ack)
        }
    }

    suspend fun startNearbyServices() {
        val name = userPreferencesRepository.displayName.first()
        val emoji = userPreferencesRepository.avatarEmoji.first()
        val color = userPreferencesRepository.avatarColor.first()
        val id = userPreferencesRepository.userId.first()

        nearbyManager.startAdvertising(name, emoji, color, id)
        nearbyManager.startDiscovery()
    }

    fun stopNearbyServices() {
        nearbyManager.stopAll()
    }

    suspend fun connectToPeer(endpointId: String) {
        val name = userPreferencesRepository.displayName.first()
        val emoji = userPreferencesRepository.avatarEmoji.first()
        val color = userPreferencesRepository.avatarColor.first()
        val id = userPreferencesRepository.userId.first()

        nearbyManager.requestConnection(endpointId, name, emoji, color, id)
    }

    fun acceptConnection(endpointId: String) = nearbyManager.acceptConnection(endpointId)

    fun rejectConnection(endpointId: String) = nearbyManager.rejectConnection(endpointId)

    suspend fun createHive(name: String, emoji: String, color: Long): String {
        val hiveId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val conv = ConversationEntity(
            conversationId = hiveId,
            peerId = null,
            hiveName = name,
            isHive = true,
            lastMessage = "Hive created by you 🍯",
            lastMessageTimestamp = now
        )
        conversationDao.insertOrUpdate(conv)

        val localUserId = userPreferencesRepository.userId.first()
        val localName = userPreferencesRepository.displayName.first()

        val packet = NearbyPacket(
            type = NearbyPacket.PacketType.HIVE_MESSAGE,
            senderId = localUserId,
            senderName = localName,
            hiveId = hiveId,
            hiveName = name,
            content = "Welcome to Hive: $name! 🍯",
            timestamp = now
        )
        nearbyManager.broadcastPacket(packet)
        return hiveId
    }

    suspend fun panicWipeData() {
        nearbyManager.stopAll()
        messageDao.deleteAll()
        conversationDao.deleteAll()
        peerDao.deleteAll()
    }
}
