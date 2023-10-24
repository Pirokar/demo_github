package im.threads.business.media

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.util.ObjectsCompat
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.FileDescription
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit

class FileDescriptionMediaPlayer(private val audioManager: AudioManager) {
    var clickedDownloadPath: String? = null
    private var restartCount = 0
    val updateProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()
    private val disposable: Disposable = Flowable.interval(UPDATE_PERIOD, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .timeInterval()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { updateProcessor.onNext(true) }
        ) { error: Throwable? -> error("FileDescriptionMediaPlayer ", error) }
    var mediaPlayer: MediaPlayer? = null
        private set
    private val onAudioFocusChangeListener = OnAudioFocusChangeListener { focusChange: Int ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.start()
            }

            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.pause()
            }
        }
    }
    private var audioFocusRequest: AudioFocusRequest? = null

    var fileDescription: FileDescription? = null
        private set

    fun processPlayPause(fileDescription: FileDescription) {
        fileUri
        if (ObjectsCompat.equals(this.fileDescription, fileDescription)) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    abandonAudioFocus()
                } else {
                    player.start()
                    requestAudioFocus()
                }
                updateProcessor.onNext(true)
            }
        } else {
            releaseMediaPlayer()
            startMediaPlayer(fileDescription)
        }
    }

    fun reset() {
        if (mediaPlayer != null) {
            releaseMediaPlayer()
        }
    }

    fun release() {
        if (mediaPlayer != null) {
            releaseMediaPlayer()
        }
        disposable.dispose()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(getAudioFocusRequest())
        } else {
            audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(getAudioFocusRequest())
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    fun clearClickedDownloadPath() {
        clickedDownloadPath = null
    }

    fun restartMediaPlayer(fileDescription: FileDescription): MediaPlayer? {
        releaseMediaPlayer()
        createMediaPlayer(fileDescription)
        return mediaPlayer
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAudioFocusRequest(): AudioFocusRequest {
        return audioFocusRequest ?: let {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.STREAM_MUSIC)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build()
            audioFocusRequest!!
        }
    }

    private fun startMediaPlayer(fileDescription: FileDescription) {
        createMediaPlayer(fileDescription)
        mediaPlayer?.let { player ->
            player.start()
            requestAudioFocus()
            updateProcessor.onNext(true)
        }
    }

    private fun createMediaPlayer(fileDescription: FileDescription) {
        this.fileDescription = fileDescription
        try {
            mediaPlayer = MediaPlayer.create(BaseConfig.getInstance().context, fileUri)
            restartCount = 0
        } catch (exception: Exception) {
            if (restartCount++ < 3) {
                restartMediaPlayer(fileDescription)
            } else {
                releaseMediaPlayer()
            }
        }
        mediaPlayer?.setOnCompletionListener {
            abandonAudioFocus()
            releaseMediaPlayer()
        }
    }

    private fun releaseMediaPlayer() {
        fileDescription = null
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        updateProcessor.onNext(true)
    }

    private val fileUri: Uri?
        get() = if (fileDescription != null) {
            val isUriNonNull = fileDescription?.fileUri != null
            val isDownloadPathNull = fileDescription?.downloadPath == null
            val fileUri =
                if (isUriNonNull) {
                    fileDescription?.fileUri
                } else if (isDownloadPathNull) {
                    null
                } else {
                    Uri.parse(
                        fileDescription?.downloadPath
                    )
                }
            fileDescription?.fileUri = fileUri
            if (fileDescription?.fileUri == null) {
                info("file uri is null")
                null
            } else {
                fileUri
            }
        } else {
            info("file uri is null")
            null
        }
    val duration: Int
        get() {
            val defaultDuration = 1
            mediaPlayer?.let { player ->
                val duration = player.duration
                if (duration >= 0) {
                    return duration
                }
            }
            return defaultDuration
        }

    companion object {
        private const val UPDATE_PERIOD = 200L
    }
}
