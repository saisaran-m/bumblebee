package com.bumblebee.chat.domain.model

/**
 * Domain model representing a peer in the Bumblebee network.
 */
data class Peer(
    val peerId: String,
    val displayName: String,
    val avatarEmoji: String = "\uD83D\uDC1D",
    val avatarColor: Long = 0xFF9B7EDE,
    val endpointId: String? = null,
    val isConnected: Boolean = false,
    val isNearby: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val hopCount: Int = 0,
    val publicKey: String? = null
)
