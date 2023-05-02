package im.threads.business.audio.audioConverter.callback

import java.io.File

interface IConvertCallback {
    fun onSuccess(convertedFile: File)
    fun onFailure(error: Exception)
}
