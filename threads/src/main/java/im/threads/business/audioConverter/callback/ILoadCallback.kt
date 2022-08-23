package im.threads.business.audioConverter.callback

interface ILoadCallback {
    fun onSuccess()
    fun onFailure(error: Exception)
}
