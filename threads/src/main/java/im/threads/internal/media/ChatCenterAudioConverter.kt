package im.threads.internal.media

import im.threads.internal.Config
import im.threads.internal.domain.audio_converter.AudioConverter
import im.threads.internal.domain.audio_converter.callback.IConvertCallback
import im.threads.internal.domain.audio_converter.model.AudioFormat
import im.threads.internal.utils.ThreadsLogger
import java.io.File
import java.lang.ref.WeakReference

class ChatCenterAudioConverter {

    fun convertToWav(file: File, callback: ChatCenterAudioConverterCallback) {
        val callbackReference = WeakReference(callback)
        AudioConverter.with(Config.instance.context)
            .setFile(file)
            .setFormat(AudioFormat.WAV)
            .setCallback(object : IConvertCallback {
                override fun onSuccess(convertedFile: File) {
                    callbackReference.get()?.acceptConvertedFile(convertedFile)
                }

                override fun onFailure(error: Exception) {
                    ThreadsLogger.e(TAG, "error finishing voice message recording", error)
                }
            })
            .convert()
    }

    companion object {
        const val TAG = "ChatCenterAudioConverter"
    }
}

interface ChatCenterAudioConverterCallback {
    fun acceptConvertedFile(convertedFile: File)
}
