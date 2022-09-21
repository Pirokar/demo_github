package im.threads.ui.holders

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnDetach
import im.threads.R
import im.threads.business.logger.LoggerEdna
import im.threads.business.media.FileDescriptionMediaPlayer
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import im.threads.business.models.FileDescriptionUri
import im.threads.ui.config.Config
import im.threads.ui.utils.visible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class VoiceMessageBaseHolder internal constructor(
    itemView: View,
    highlightStream: PublishSubject<ChatItem>?,
    private val fdMediaPlayer: FileDescriptionMediaPlayer,
    private val isIncomingMessage: Boolean
) : BaseHolder(
    itemView,
    highlightStream
) {
    abstract var fileDescription: FileDescription?
    abstract fun init(maxValue: Int, progress: Int, isPlaying: Boolean)
    abstract fun updateProgress(progress: Int)
    abstract fun updateIsPlaying(isPlaying: Boolean)
    abstract fun resetProgress()

    protected abstract val voiceMessage: ViewGroup
    protected abstract val buttonPlayPause: ImageView

    private val disposables = CompositeDisposable()

    fun startLoader() {
        voiceMessage.visible()
        val color = if (isIncomingMessage) {
            Config.getInstance().getChatStyle().incomingMessageLoaderColor
        } else {
            R.color.threads_white
        }
        initAnimation(buttonPlayPause, color)
    }

    fun stopLoader() {
        buttonPlayPause.setImageResource(R.drawable.threads_voice_message_play)
    }

    fun subscribeForVoiceMessageDownloaded() {
        itemView.doOnDetach {
            disposables.clear()
        }

        fileDescription?.run {
            disposables.add(
                this.onCompleteSubject
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { fileDescriptionUri: FileDescriptionUri ->
                            val ourDownloadPath = fileDescription?.downloadPath
                            val isCurrentPath = ourDownloadPath == fileDescriptionUri.downloadPath
                            val isClickedPath = ourDownloadPath == fdMediaPlayer.clickedDownloadPath

                            if (isCurrentPath && isClickedPath) {
                                stopLoader()
                                fileDescription?.fileUri = fileDescriptionUri.fileUri
                                val mediaPlayer = fdMediaPlayer.restartMediaPlayer(fileDescription!!)
                                if (mediaPlayer != null) {
                                    val duration: Int = fdMediaPlayer.duration
                                    var currentPosition = mediaPlayer.currentPosition
                                    if (currentPosition < 0) currentPosition = 0

                                    init(duration, currentPosition, mediaPlayer.isPlaying)
                                    fdMediaPlayer.processPlayPause(fileDescription!!)
                                }
                            }
                        },
                        LoggerEdna::error
                    )
            )
        }
    }
}
