package com.bumblebee.chat.domain.model

data class Conversation(
    val conversationId: String,
    val peerId: String? = null,
    val hiveName: String? = null,
    val isHive: Boolean = false,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val peerName: String? = null,
    val peerEmoji: String? = null,
    val peerColor: Long? = null,
    val isOnline: Boolean = false
)
