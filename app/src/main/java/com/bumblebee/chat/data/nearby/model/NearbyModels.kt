package com.bumblebee.chat.data.nearby.model

data class DiscoveredPeer(
    val endpointId: String,
    val displayName: String,
    val avatarEmoji: String = "\uD83D\uDC1D",
    val avatarColor: Long = 0xFF9B7EDE,
    val userId: String = ""
)

data class ConnectedPeer(
    val endpointId: String,
    val displayName: String,
    val avatarEmoji: String = "\uD83D\uDC1D",
    val avatarColor: Long = 0xFF9B7EDE,
    val userId: String = ""
)

data class ConnectionRequest(
    val endpointId: String,
    val endpointName: String,
    val authenticationDigits: String,
    val avatarEmoji: String = "\uD83D\uDC1D",
    val avatarColor: Long = 0xFF9B7EDE,
    val userId: String = ""
)
