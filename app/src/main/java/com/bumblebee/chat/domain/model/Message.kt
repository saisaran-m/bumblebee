package com.bumblebee.chat.domain.model

data class Message(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENDING,
    val isOutgoing: Boolean = true,
    val replyToId: String? = null,
    val mediaUri: String? = null,
    val mediaName: String? = null,
    val mediaSizeBytes: Long = 0
)

enum class MessageType {
    TEXT, IMAGE, FILE, VOICE, SYSTEM
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}
