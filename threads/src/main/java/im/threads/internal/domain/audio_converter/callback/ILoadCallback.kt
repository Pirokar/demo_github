package im.threads.internal.domain.audio_converter.callback

import java.lang.Exception

interface ILoadCallback {
    fun onSuccess()
    fun onFailure(error: Exception)
}
