package im.threads.business.audio.audioRecorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date

data class AudioRecorderConfig(
    val audioSource: Int? = null,
    val outputFormat: Int? = null,
    val audioEncoder: Int? = null,
    val audioEncodingBitRate: Int? = null,
    val audioSamplingRate: Int? = null,
    val outputFilePath: String? = null,
    val fileNameDateFormat: SimpleDateFormat
) {
    @SuppressLint("InlinedApi")
    fun getDefaultConfig(context: Context): AudioRecorderConfig {
        val isVersionQAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        return AudioRecorderConfig(
            MediaRecorder.AudioSource.MIC,
            if (isVersionQAndUpper) MediaRecorder.OutputFormat.OGG else MediaRecorder.OutputFormat.THREE_GPP,
            if (isVersionQAndUpper) MediaRecorder.AudioEncoder.OPUS else MediaRecorder.AudioEncoder.AMR_WB,
            if (isVersionQAndUpper) null else 128000,
            if (isVersionQAndUpper) null else 44100,
            "${context.filesDir.absolutePath}${String.format(
                if (isVersionQAndUpper) "/voice%s.ogg" else "/voice%s.wav",
                fileNameDateFormat.format(Date())
            )}",
            fileNameDateFormat
        )
    }
}
