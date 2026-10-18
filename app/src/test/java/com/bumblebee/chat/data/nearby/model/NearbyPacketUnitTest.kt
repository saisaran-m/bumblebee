package com.bumblebee.chat.data.nearby.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class NearbyPacketUnitTest {

    @Test
    fun testTextMessageSerializationAndDeserialization() {
        val original = NearbyPacket(
            type = NearbyPacket.PacketType.MESSAGE,
            senderId = "user_alpha_123",
            senderName = "Alice",
            senderEmoji = "\uD83D\uDC1D",
            senderColor = 0xFF9B7EDE,
            messageId = UUID.randomUUID().toString(),
            conversationId = "conv_123",
            content = "Hello offline world!",
            timestamp = 1700000000000L
        )

        val bytes = original.toByteArray()
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())

        val restored = NearbyPacket.fromByteArray(bytes)
        assertNotNull(restored)
        assertEquals(original.type, restored?.type)
        assertEquals(original.senderId, restored?.senderId)
        assertEquals(original.senderName, restored?.senderName)
        assertEquals(original.content, restored?.content)
        assertEquals(original.messageId, restored?.messageId)
        assertEquals(original.conversationId, restored?.conversationId)
        assertEquals(original.timestamp, restored?.timestamp)
    }

    @Test
    fun testMultiChunkMediaMetadata() {
        val original = NearbyPacket(
            type = NearbyPacket.PacketType.MESSAGE,
            senderId = "user_beta_456",
            senderName = "Bob",
            messageId = "msg_chunk_999",
            mediaType = "IMAGE",
            mediaBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
            chunkIndex = 2,
            totalChunks = 5,
            mediaDurationMs = 0L
        )

        val bytes = original.toByteArray()
        val restored = NearbyPacket.fromByteArray(bytes)

        assertNotNull(restored)
        assertEquals(2, restored?.chunkIndex)
        assertEquals(5, restored?.totalChunks)
        assertEquals("IMAGE", restored?.mediaType)
        assertEquals(original.mediaBase64, restored?.mediaBase64)
    }

    @Test
    fun testVoiceNoteMetadata() {
        val original = NearbyPacket(
            type = NearbyPacket.PacketType.MESSAGE,
            senderId = "user_voice_789",
            senderName = "Charlie",
            messageId = "msg_voice_101",
            mediaType = "VOICE",
            mediaBase64 = "AAAAHGZ0eXBNNEEgAAACAGlzb21pc28y",
            chunkIndex = 0,
            totalChunks = 1,
            mediaDurationMs = 4500L
        )

        val bytes = original.toByteArray()
        val restored = NearbyPacket.fromByteArray(bytes)

        assertNotNull(restored)
        assertEquals("VOICE", restored?.mediaType)
        assertEquals(4500L, restored?.mediaDurationMs)
        assertEquals(0, restored?.chunkIndex)
        assertEquals(1, restored?.totalChunks)
    }

    @Test
    fun testEmergencySosTelemetryPacket() {
        val original = NearbyPacket(
            type = NearbyPacket.PacketType.SOS_BUZZ,
            senderId = "distress_user_001",
            senderName = "Explorer",
            messageId = "sos_alert_888",
            content = "EMERGENCY SOS: Help needed! [Battery: 42%]",
            isEmergencySos = true,
            latitude = 37.7749,
            longitude = -122.4194,
            maxHops = 7
        )

        val bytes = original.toByteArray()
        val restored = NearbyPacket.fromByteArray(bytes)

        assertNotNull(restored)
        assertEquals(NearbyPacket.PacketType.SOS_BUZZ, restored?.type)
        assertTrue(restored?.isEmergencySos == true)
        assertEquals(37.7749, restored?.latitude ?: 0.0, 0.0001)
        assertEquals(-122.4194, restored?.longitude ?: 0.0, 0.0001)
        assertEquals(7, restored?.maxHops)
    }

    @Test
    fun testDeliveryAndReadAcknowledgements() {
        val ackDelivered = NearbyPacket(
            type = NearbyPacket.PacketType.ACK_DELIVERED,
            senderId = "peer_b",
            senderName = "Bob",
            messageId = "msg_original_001"
        )
        val delivBytes = ackDelivered.toByteArray()
        val delivRestored = NearbyPacket.fromByteArray(delivBytes)
        assertEquals(NearbyPacket.PacketType.ACK_DELIVERED, delivRestored?.type)
        assertEquals("msg_original_001", delivRestored?.messageId)

        val ackRead = NearbyPacket(
            type = NearbyPacket.PacketType.ACK_READ,
            senderId = "peer_b",
            senderName = "Bob",
            conversationId = "conv_123"
        )
        val readBytes = ackRead.toByteArray()
        val readRestored = NearbyPacket.fromByteArray(readBytes)
        assertEquals(NearbyPacket.PacketType.ACK_READ, readRestored?.type)
        assertEquals("conv_123", readRestored?.conversationId)
    }

    @Test
    fun testCorruptedByteArrayHandlingDoesNotCrash() {
        // Completely invalid garbage bytes
        val garbageBytes = byteArrayOf(0x00, 0x1F, 0x7E, 0x43, 0x12)
        val result = NearbyPacket.fromByteArray(garbageBytes)
        assertNull("Malformed byte array should return null instead of throwing an unhandled exception", result)

        // Truncated invalid JSON
        val malformedJson = "{ \"type\": \"MESSA".toByteArray(Charsets.UTF_8)
        val malformedResult = NearbyPacket.fromByteArray(malformedJson)
        assertNull("Truncated JSON should return null safely", malformedResult)
    }
}
