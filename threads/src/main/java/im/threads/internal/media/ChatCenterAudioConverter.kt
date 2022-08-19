package im.threads.internal.media

import im.threads.internal.Config
import im.threads.internal.domain.audioConverter.AudioConverter
import im.threads.internal.domain.audioConverter.callback.IConvertCallback
import im.threads.internal.domain.audioConverter.model.AudioFormat
import im.threads.internal.domain.logger.LoggerEdna
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
                    LoggerEdna.error("error finishing voice message recording", error)
                }
            })
            .convert()
    }
}

interface ChatCenterAudioConverterCallback {
    fun acceptConvertedFile(convertedFile: File)
}
