package im.threads.internal.domain.audioConverter.callback

import java.io.File

interface IConvertCallback {
    fun onSuccess(convertedFile: File)
    fun onFailure(error: Exception)
}
