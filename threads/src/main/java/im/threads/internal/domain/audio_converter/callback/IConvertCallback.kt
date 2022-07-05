package im.threads.internal.domain.audio_converter.callback

import java.io.File
import java.lang.Exception

interface IConvertCallback {
    fun onSuccess(convertedFile: File)
    fun onFailure(error: Exception)
}
