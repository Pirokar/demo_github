package im.threads.business.audio.audioRecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import java.io.IOException
import java.text.SimpleDateFormat

class AudioRecorder(private val context: Context) {
    private var currentConfig: AudioRecorderConfig? = null
    val voiceFilePath: String
        get() {
            return currentConfig?.outputFilePath ?: ""
        }

    private val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }

    fun prepareWithDefaultConfig(fileNameDateFormat: SimpleDateFormat) {
        prepare(AudioRecorderConfig(fileNameDateFormat = fileNameDateFormat).getDefaultConfig(context))
    }

    private fun prepare(config: AudioRecorderConfig) {
        currentConfig = config

        config.audioSource?.let { recorder.setAudioSource(it) }
        config.outputFormat?.let { recorder.setOutputFormat(it) }
        config.audioEncoder?.let { recorder.setAudioEncoder(it) }
        config.audioEncodingBitRate?.let { recorder.setAudioEncodingBitRate(it) }
        config.audioSamplingRate?.let { recorder.setAudioSamplingRate(it) }
        config.outputFilePath?.let { recorder.setOutputFile(it) }

        try {
            recorder.prepare()
        } catch (e: IOException) {
            error("prepare() failed")
        }
    }

    fun start() {
        recorder.start()
    }

    fun stop() {
        try {
            recorder.stop()
            recorder.release()
        } catch (runtimeException: RuntimeException) {
            info("Exception occurred in releaseRecorder but it's fine")
        }
    }
}
