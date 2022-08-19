package im.threads.internal.domain.audioConverter.callback

interface ILoadCallback {
    fun onSuccess()
    fun onFailure(error: Exception)
}
