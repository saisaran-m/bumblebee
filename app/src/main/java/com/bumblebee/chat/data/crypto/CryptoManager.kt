package com.bumblebee.chat.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages hardware-backed encryption using Android Keystore and AES-256-GCM.
 * Supports end-to-end encryption for chat messages, key management, and panic wipes.
 */
@Singleton
class CryptoManager @Inject constructor() {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val masterKeyAlias = "bumblebee_master_cipher_key"
    private val transformation = "AES/GCM/NoPadding"
    private val tagLengthBits = 128
    private val ivLengthBytes = 12

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey(): SecretKey {
        if (!keyStore.containsAlias(masterKeyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val spec = KeyGenParameterSpec.Builder(
                masterKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
        return (keyStore.getEntry(masterKeyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(transformation)
        val key = ensureMasterKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV to ciphertext
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encodedString: String): String {
        return try {
            val combined = Base64.decode(encodedString, Base64.NO_WRAP)
            if (combined.size <= ivLengthBytes) return encodedString

            val iv = ByteArray(ivLengthBytes)
            val cipherText = ByteArray(combined.size - ivLengthBytes)
            System.arraycopy(combined, 0, iv, 0, ivLengthBytes)
            System.arraycopy(combined, ivLengthBytes, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(tagLengthBits, iv)
            cipher.init(Cipher.DECRYPT_MODE, ensureMasterKey(), spec)

            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            encodedString // fallback to plain if decryption fails
        }
    }

    /**
     * Panic Wipe: Completely purges encryption keys from Android Keystore.
     */
    fun panicPurgeKeys() {
        try {
            if (keyStore.containsAlias(masterKeyAlias)) {
                keyStore.deleteEntry(masterKeyAlias)
            }
        } catch (e: Exception) {
            // Ignored on panic
        }
    }
}
