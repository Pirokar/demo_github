package im.threads.business.audio.audioConverter

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import im.threads.business.audio.audioConverter.callback.IConvertCallback
import im.threads.business.audio.audioConverter.callback.ILoadCallback
import im.threads.business.audio.audioConverter.model.AudioFormat
import java.io.File
import java.io.IOException

class AudioConverter private constructor(private val context: Context) {
    private var audioFile: File? = null
    private var format: AudioFormat? = null
    private var callback: IConvertCallback? = null

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
        if (!isLoaded) {
            callback?.onFailure(Exception("FFmpeg not loaded"))
            return
        }
        audioFile?.let { audioFile ->
            if (!audioFile.exists()) {
                callback!!.onFailure(IOException("File not exists"))
                return
            }
            if (!audioFile.canRead()) {
                callback!!.onFailure(IOException("Can't read the file. Missing permission?"))
                return
            }
            val convertedFile = getConvertedFile(audioFile, format)
            val cmd = arrayOf("-y", "-i", audioFile.path, convertedFile.path)
            try {
                FFmpeg.getInstance(context).execute(
                    cmd,
                    object : FFmpegExecuteResponseHandler {
                        override fun onStart() {}
                        override fun onProgress(message: String) {}
                        override fun onSuccess(message: String) {
                            callback?.onSuccess(convertedFile)
                        }

                        override fun onFailure(message: String) {
                            callback?.onFailure(IOException(message))
                        }

                        override fun onFinish() {}
                    }
                )
            } catch (e: Exception) {
                callback?.onFailure(e)
            }
        }
    }

    companion object {
        var isLoaded = false
            private set

        @JvmStatic
        fun load(context: Context?, callback: ILoadCallback) {
            try {
                FFmpeg.getInstance(context).loadBinary(object : FFmpegLoadBinaryResponseHandler {
                    override fun onStart() {}
                    override fun onSuccess() {
                        isLoaded = true
                        callback.onSuccess()
                    }

                    override fun onFailure() {
                        isLoaded = false
                        callback.onFailure(Exception("Failed to loaded FFmpeg lib"))
                    }

                    override fun onFinish() {}
                })
            } catch (e: Exception) {
                isLoaded = false
                callback.onFailure(e)
            }
        }

        @JvmStatic
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
