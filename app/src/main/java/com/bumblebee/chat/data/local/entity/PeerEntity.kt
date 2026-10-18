package com.bumblebee.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a discovered or connected peer device in the Bumblebee network.
 */
@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey
    val peerId: String,           // Unique peer identifier
    val displayName: String,      // Peer's chosen display name
    val avatarEmoji: String = "\uD83D\uDC1D", // Default bee emoji
    val avatarColor: Long = 0xFF9B7EDE, // Default lavender
    val endpointId: String? = null, // Nearby Connections endpoint ID (null if not currently connected)
    val isConnected: Boolean = false,
    val isNearby: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val hopCount: Int = 0,        // 0 = directly connected
    val publicKey: String? = null  // Base64 encoded public key for E2E encryption
)
