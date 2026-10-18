package com.bumblebee.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single chat message.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,         // UUID
    val conversationId: String,    // FK to conversations
    val senderId: String,          // Peer ID of sender
    val content: String,           // Message text content
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "TEXT",     // TEXT, IMAGE, FILE, VOICE, SYSTEM
    val status: String = "SENDING", // SENDING, SENT, DELIVERED, READ, FAILED
    val isOutgoing: Boolean = true,
    val replyToId: String? = null, // ID of message being replied to
    val mediaUri: String? = null,  // Local URI for media messages
    val mediaName: String? = null, // Original filename for media
    val mediaSizeBytes: Long = 0,
    val isDeleted: Boolean = false,
    val selfDestructAt: Long? = null // Timestamp when message should self-destruct
)
