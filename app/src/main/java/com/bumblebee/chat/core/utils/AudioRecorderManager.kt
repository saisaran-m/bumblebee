package com.bumblebee.chat.core.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0L

    fun startRecording(): File? {
        stopRecording()
        val dir = File(context.cacheDir, "bumblebee_voice_temp")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "record_${System.currentTimeMillis()}.m4a")

        recorder = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(32000)
                setAudioSamplingRate(22050)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            recorder?.release()
            recorder = null
            return null
        }

        currentFile = file
        startTimeMs = System.currentTimeMillis()
        return file
    }

    fun stopRecording(): Pair<File?, Long> {
        val durationMs = if (startTimeMs > 0) System.currentTimeMillis() - startTimeMs else 0L
        startTimeMs = 0L
        val file = currentFile
        currentFile = null

        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioRecorder", "Error stopping recorder", e)
        } finally {
            recorder = null
        }

        return Pair(file, durationMs)
    }

    fun cancelRecording() {
        val (file, _) = stopRecording()
        file?.delete()
    }
}
