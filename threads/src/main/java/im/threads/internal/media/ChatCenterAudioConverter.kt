package im.threads.internal.media

import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import im.threads.internal.Config
import im.threads.internal.utils.ThreadsLogger
import java.io.File
import java.lang.ref.WeakReference

class ChatCenterAudioConverter {

    fun convertToWav(file: File, callback: ChatCenterAudioConverterCallback) {
        val callbackReference = WeakReference(callback)
        AndroidAudioConverter.with(Config.instance.context) // Your current audio file
            .setFile(file) // Your desired audio format
            .setFormat(AudioFormat.WAV) // An callback to know when conversion is finished
            .setCallback(object : IConvertCallback {
                override fun onSuccess(convertedFile: File) {
                    callbackReference.get()?.acceptConvertedFile(convertedFile)
                }

                override fun onFailure(error: Exception) {
                    ThreadsLogger.e(TAG, "error finishing voice message recording", error)
                }
            }) // Start conversion
            .convert()
    }

    companion object {
        const val TAG = "ChatCenterAudioConverter"
    }
}

interface ChatCenterAudioConverterCallback {
    fun acceptConvertedFile(convertedFile: File)
}
