package com.bumblebee.chat.data.nearby

import android.util.Log
import com.bumblebee.chat.core.constants.AppConstants
import com.bumblebee.chat.data.nearby.model.NearbyPacket
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class SwarmPeer(
    val peerId: String,
    val displayName: String,
    val avatarEmoji: String,
    val avatarColor: Long,
    val hopCount: Int,
    val lastSeen: Long = System.currentTimeMillis()
)

/**
 * Manages mesh multi-hop routing, duplicate detection, Pollen store-and-forward,
 * and Swarm topology tracking.
 */
@Singleton
class MeshRelayManager @Inject constructor() {
    private val tag = "MeshRelayManager"

    // Seen-ID cache with timestamp for loop prevention (expires after 2 hours)
    private val seenMessageIds = ConcurrentHashMap<String, Long>()
    private val cacheExpiryMs = 2 * 60 * 60 * 1000L

    // Pollen Store-and-Forward: encrypted or undelivered messages held for offline targets
    private val pollenStore = ConcurrentHashMap<String, MutableList<NearbyPacket>>()
    private val maxPollenPerTarget = 50

    // Swarm topology: discovered peers and their hop counts
    private val swarmGraph = ConcurrentHashMap<String, SwarmPeer>()

    fun hasSeenMessage(messageId: String): Boolean {
        cleanExpiredIds()
        return seenMessageIds.containsKey(messageId)
    }

    fun markMessageSeen(messageId: String) {
        cleanExpiredIds()
        seenMessageIds[messageId] = System.currentTimeMillis()
    }

    /**
     * Determines whether this packet should be relayed through mesh.
     * Returns the packet with incremented hopCount if eligible, or null if dropped.
     */
    fun processForRelay(packet: NearbyPacket, myUserId: String): NearbyPacket? {
        val msgId = packet.messageId ?: return null

        // Drop if already seen
        if (hasSeenMessage(msgId)) {
            return null
        }
        markMessageSeen(msgId)

        // Drop if exceeded max hops
        if (packet.hopCount >= packet.maxHops || packet.hopCount >= AppConstants.MAX_HOP_COUNT) {
            Log.d(tag, "Packet $msgId dropped: max hops reached (${packet.hopCount})")
            return null
        }

        // Update swarm topology
        updateSwarmNode(packet)

        // If target is me, don't relay further
        if (packet.recipientId == myUserId) {
            return null
        }

        // Return copy with incremented hop count
        return packet.copy(hopCount = packet.hopCount + 1)
    }

    /**
     * Stores a message in Pollen storage when target is currently out of range.
     */
    fun storePollen(packet: NearbyPacket) {
        val targetId = packet.recipientId ?: return
        val list = pollenStore.computeIfAbsent(targetId) { mutableListOf() }
        synchronized(list) {
            if (list.size < maxPollenPerTarget && list.none { it.messageId == packet.messageId }) {
                list.add(packet)
                Log.d(tag, "Stored pollen for $targetId (total: ${list.size})")
            }
        }
    }

    /**
     * Retrieves and removes stored Pollen when target peer comes into range.
     */
    fun drainPollenForTarget(targetId: String): List<NearbyPacket> {
        val list = pollenStore.remove(targetId) ?: return emptyList()
        val now = System.currentTimeMillis()
        val maxAgeMs = AppConstants.MESSAGE_EXPIRY_HOURS * 60 * 60 * 1000L
        return synchronized(list) {
            list.filter { (now - it.timestamp) < maxAgeMs }
        }
    }

    fun updateSwarmNode(packet: NearbyPacket) {
        val peer = SwarmPeer(
            peerId = packet.senderId,
            displayName = packet.senderName,
            avatarEmoji = packet.senderEmoji,
            avatarColor = packet.senderColor,
            hopCount = packet.hopCount,
            lastSeen = System.currentTimeMillis()
        )
        val existing = swarmGraph[packet.senderId]
        if (existing == null || packet.hopCount <= existing.hopCount) {
            swarmGraph[packet.senderId] = peer
        }
    }

    fun getSwarmNodes(): List<SwarmPeer> {
        val cutoff = System.currentTimeMillis() - (15 * 60 * 1000L) // active within last 15 min
        return swarmGraph.values.filter { it.lastSeen > cutoff }
    }

    private fun cleanExpiredIds() {
        val now = System.currentTimeMillis()
        seenMessageIds.entries.removeIf { (now - it.value) > cacheExpiryMs }
    }
}
