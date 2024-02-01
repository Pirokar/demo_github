package im.threads.ui.views.recordview

/**
 * Created by Devlomi on 24/08/2017.
 */
internal interface VoiceOnRecordListener {
    fun onStart()
    fun onCancel()
    fun onFinish(recordTime: Long, limitReached: Boolean)
    fun onLessThanSecond()
    fun onLock()
}
