package com.bumblebee.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a chat conversation (1-to-1 or Hive group chat).
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val conversationId: String,    // UUID
    val peerId: String? = null,    // Null for group chats (Hives)
    val hiveName: String? = null,  // Non-null for Hives
    val isHive: Boolean = false,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
