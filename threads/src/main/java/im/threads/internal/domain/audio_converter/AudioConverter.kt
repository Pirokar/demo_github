package im.threads.internal.domain.audio_converter

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import im.threads.internal.domain.audio_converter.callback.IConvertCallback
import im.threads.internal.domain.audio_converter.model.AudioFormat
import java.io.File
import java.io.IOException

class AudioConverter private constructor(private val context: Context) {
    private var audioFile: File? = null
    private var format: AudioFormat? = null
    private var callback: IConvertCallback? = null
    private val tag = "AudioConverter"

    fun setFile(originalFile: File?): AudioConverter {
        audioFile = originalFile
        return this
    }

    fun setFormat(format: AudioFormat?): AudioConverter {
        this.format = format
        return this
    }

    fun setCallback(callback: IConvertCallback?): AudioConverter {
        this.callback = callback
        return this
    }

    fun convert() {
        audioFile?.let { audioFile ->
            if (!audioFile.exists()) {
                callback?.onFailure(IOException("File not exists"))
                return
            }
            if (!audioFile.canRead()) {
                callback?.onFailure(IOException("Can't read the file. Missing permission?"))
                return
            }
            if (audioFile.path.endsWith(".wav")) {
                callback?.onSuccess(audioFile)
                return
            }
            val convertedFile = getConvertedFile(
                audioFile, format
            )
            val cmd = "-i -y ${audioFile.absolutePath} ${convertedFile.absolutePath}"
            try {
                Log.i(tag, cmd)
                val session = FFmpegKit.execute(cmd)
                if (ReturnCode.isSuccess(session.returnCode)) {
                    callback?.onSuccess(convertedFile)
                } else {
                    callback?.onFailure(IOException("Cannot convert file to wav with AudioConverter"))
                }
            } catch (e: Exception) {
                callback?.onFailure(e)
            }
        }
    }

    companion object {
        fun with(context: Context): AudioConverter {
            return AudioConverter(context)
        }

        private fun getConvertedFile(originalFile: File, format: AudioFormat?): File {
            val f = originalFile.path.split(".").toTypedArray()
            val filePath = originalFile.path.replace(f[f.size - 1], format!!.format)
            return File(filePath)
        }
    }
}
