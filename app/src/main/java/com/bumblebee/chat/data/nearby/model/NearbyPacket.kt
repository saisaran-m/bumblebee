package com.bumblebee.chat.data.nearby.model

import org.json.JSONObject

/**
 * Universal mesh envelope for all Bumblebee peer-to-peer transmissions:
 * 1-to-1 chat, Hives (groups), Pollen store-and-forward mesh hops,
 * Hum (typing indicators), and Emergency Buzz (SOS).
 */
data class NearbyPacket(
    val type: PacketType,
    val senderId: String,
    val senderName: String,
    val senderEmoji: String = "\uD83D\uDC1D",
    val senderColor: Long = 0xFF9B7EDE,
    val recipientId: String? = null,
    val messageId: String? = null,
    val conversationId: String? = null,
    val hiveId: String? = null,
    val hiveName: String? = null,
    val content: String? = null,
    val replyToId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String? = null,
    val hopCount: Int = 0,
    val maxHops: Int = 7,
    val isEmergencySos: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val mediaType: String? = null,
    val mediaBase64: String? = null,
    val chunkIndex: Int = 0,
    val totalChunks: Int = 1,
    val mediaDurationMs: Long = 0
) {
    enum class PacketType {
        HANDSHAKE,
        MESSAGE,
        ACK_DELIVERED,
        ACK_READ,
        HUM,
        SOS_BUZZ,
        HIVE_MESSAGE,
        DISCONNECT
    }

    fun toByteArray(): ByteArray {
        val json = JSONObject().apply {
            put("type", type.name)
            put("senderId", senderId)
            put("senderName", senderName)
            put("senderEmoji", senderEmoji)
            put("senderColor", senderColor)
            put("recipientId", recipientId ?: "")
            put("messageId", messageId ?: "")
            put("conversationId", conversationId ?: "")
            put("hiveId", hiveId ?: "")
            put("hiveName", hiveName ?: "")
            put("content", content ?: "")
            put("replyToId", replyToId ?: "")
            put("timestamp", timestamp)
            put("status", status ?: "")
            put("hopCount", hopCount)
            put("maxHops", maxHops)
            put("isEmergencySos", isEmergencySos)
            if (latitude != null) put("latitude", latitude)
            if (longitude != null) put("longitude", longitude)
            if (mediaType != null) put("mediaType", mediaType)
            if (mediaBase64 != null) put("mediaBase64", mediaBase64)
            put("chunkIndex", chunkIndex)
            put("totalChunks", totalChunks)
            put("mediaDurationMs", mediaDurationMs)
        }
        return json.toString().toByteArray(Charsets.UTF_8)
    }

    companion object {
        fun fromByteArray(bytes: ByteArray): NearbyPacket? {
            return try {
                val str = String(bytes, Charsets.UTF_8)
                val json = JSONObject(str)
                NearbyPacket(
                    type = PacketType.valueOf(json.optString("type", PacketType.MESSAGE.name)),
                    senderId = json.optString("senderId"),
                    senderName = json.optString("senderName"),
                    senderEmoji = json.optString("senderEmoji", "\uD83D\uDC1D"),
                    senderColor = json.optLong("senderColor", 0xFF9B7EDE),
                    recipientId = json.optString("recipientId").takeIf { it.isNotEmpty() },
                    messageId = json.optString("messageId").takeIf { it.isNotEmpty() },
                    conversationId = json.optString("conversationId").takeIf { it.isNotEmpty() },
                    hiveId = json.optString("hiveId").takeIf { it.isNotEmpty() },
                    hiveName = json.optString("hiveName").takeIf { it.isNotEmpty() },
                    content = json.optString("content").takeIf { it.isNotEmpty() },
                    replyToId = json.optString("replyToId").takeIf { it.isNotEmpty() },
                    timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                    status = json.optString("status").takeIf { it.isNotEmpty() },
                    hopCount = json.optInt("hopCount", 0),
                    maxHops = json.optInt("maxHops", 7),
                    isEmergencySos = json.optBoolean("isEmergencySos", false),
                    latitude = if (json.has("latitude")) json.optDouble("latitude") else null,
                    longitude = if (json.has("longitude")) json.optDouble("longitude") else null,
                    mediaType = json.optString("mediaType").takeIf { it.isNotEmpty() },
                    mediaBase64 = json.optString("mediaBase64").takeIf { it.isNotEmpty() },
                    chunkIndex = json.optInt("chunkIndex", 0),
                    totalChunks = json.optInt("totalChunks", 1),
                    mediaDurationMs = json.optLong("mediaDurationMs", 0L)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
