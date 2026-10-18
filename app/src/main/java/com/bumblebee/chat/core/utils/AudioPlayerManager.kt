package com.bumblebee.chat.core.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingUri: String? = null
        private set

    fun play(filePath: String, onCompletion: () -> Unit) {
        stop()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    currentlyPlayingUri = null
                    onCompletion()
                }
                start()
            }
            currentlyPlayingUri = filePath
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file", e)
            currentlyPlayingUri = null
            onCompletion()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Error releasing player", e)
        } finally {
            mediaPlayer = null
            currentlyPlayingUri = null
        }
    }

    fun isPlaying(filePath: String): Boolean {
        return currentlyPlayingUri == filePath && mediaPlayer?.isPlaying == true
    }
}
