package im.threads.business.audio.audioConverter.callback

interface ILoadCallback {
    fun onSuccess()
    fun onFailure(error: Exception)
}
