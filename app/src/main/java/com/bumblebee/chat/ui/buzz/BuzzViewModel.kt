package com.bumblebee.chat.ui.buzz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumblebee.chat.data.nearby.NearbyConnectionManager
import com.bumblebee.chat.data.nearby.model.NearbyPacket
import com.bumblebee.chat.data.repository.UserPreferencesRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BuzzSosState(
    val isBroadcasting: Boolean = false,
    val lastBroadcastTimestamp: Long? = null,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val isSafe: Boolean = true
)

@HiltViewModel
class BuzzViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nearbyManager: NearbyConnectionManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _sosState = MutableStateFlow(BuzzSosState())
    val sosState: StateFlow<BuzzSosState> = _sosState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun sendEmergencyBuzz() {
        triggerHapticAlert()
        _sosState.value = _sosState.value.copy(isBroadcasting = true, isSafe = false)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val lat = location?.latitude
                val lon = location?.longitude
                broadcastSosPacket(lat, lon, "EMERGENCY SOS: I need help! My phone is broadcasting this distress signal offline.")
            }.addOnFailureListener {
                broadcastSosPacket(null, null, "EMERGENCY SOS: I need help! (GPS coordinates unavailable)")
            }
        } catch (e: Exception) {
            broadcastSosPacket(null, null, "EMERGENCY SOS: I need help!")
        }
    }

    fun sendImSafe() {
        viewModelScope.launch {
            val name = userPreferencesRepository.displayName.first()
            val userId = userPreferencesRepository.userId.first()
            val emoji = userPreferencesRepository.avatarEmoji.first()
            val color = userPreferencesRepository.avatarColor.first()

            val packet = NearbyPacket(
                type = NearbyPacket.PacketType.MESSAGE,
                senderId = userId,
                senderName = name,
                senderEmoji = emoji,
                senderColor = color,
                messageId = UUID.randomUUID().toString(),
                content = "✅ I am safe now. Thank you, neighbors! 🐝",
                timestamp = System.currentTimeMillis()
            )

            nearbyManager.broadcastPacket(packet)
            _sosState.value = _sosState.value.copy(isBroadcasting = false, isSafe = true)
        }
    }

    private fun broadcastSosPacket(lat: Double?, lon: Double?, text: String) {
        viewModelScope.launch {
            val name = userPreferencesRepository.displayName.first()
            val userId = userPreferencesRepository.userId.first()
            val emoji = userPreferencesRepository.avatarEmoji.first()
            val color = userPreferencesRepository.avatarColor.first()
            val now = System.currentTimeMillis()

            val packet = NearbyPacket(
                type = NearbyPacket.PacketType.SOS_BUZZ,
                senderId = userId,
                senderName = name,
                senderEmoji = emoji,
                senderColor = color,
                messageId = UUID.randomUUID().toString(),
                content = text,
                timestamp = now,
                isEmergencySos = true,
                latitude = lat,
                longitude = lon,
                maxHops = 7
            )

            nearbyManager.broadcastPacket(packet)
            _sosState.value = _sosState.value.copy(
                isBroadcasting = true,
                lastBroadcastTimestamp = now,
                lastLatitude = lat,
                lastLongitude = lon
            )
        }
    }

    private fun triggerHapticAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(longArrayOf(0, 300, 150, 300, 150, 500), -1)
            }
        } catch (e: Exception) {
            // Ignore vibration errors on devices without vibrator
        }
    }
}
