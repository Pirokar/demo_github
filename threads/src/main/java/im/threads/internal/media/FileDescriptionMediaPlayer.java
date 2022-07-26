package im.threads.internal.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

import java.util.concurrent.TimeUnit;

import im.threads.internal.Config;
import im.threads.internal.domain.logger.LoggerEdna;
import im.threads.internal.model.FileDescription;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class FileDescriptionMediaPlayer {
    private static final long UPDATE_PERIOD = 200L;

    private final FlowableProcessor<Boolean> mediaPlayerUpdateProcessor = PublishProcessor.create();
    @NonNull
    private final Disposable disposable;
    @NonNull
    private final AudioManager audioManager;
    @Nullable
    private MediaPlayer mediaPlayer;
    final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
                break;
        }

    };
    @Nullable
    private FileDescription fileDescription;

    public FileDescriptionMediaPlayer(@NonNull AudioManager audioManager) {
        this.audioManager = audioManager;
        disposable = Flowable.interval(UPDATE_PERIOD, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> mediaPlayerUpdateProcessor.onNext(true),
                        error -> LoggerEdna.e("FileDescriptionMediaPlayer ", error)
                );
    }

    public void processPlayPause(@NonNull FileDescription fileDescription) {
        boolean isUriNonNull = fileDescription.getFileUri() != null;
        boolean isDownloadPathNull = fileDescription.getDownloadPath() == null;
        Uri fileUri = isUriNonNull ? fileDescription.getFileUri() :
                isDownloadPathNull ? null : Uri.parse(fileDescription.getDownloadPath());
        fileDescription.setFileUri(fileUri);

        if (fileDescription.getFileUri() == null) {
            LoggerEdna.i("file uri is null");
            return;
        }
        if (ObjectsCompat.equals(this.fileDescription, fileDescription) && mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                abandonAudioFocus();
            } else {
                mediaPlayer.start();
                requestAudioFocus();
            }
            mediaPlayerUpdateProcessor.onNext(true);
        } else {
            releaseMediaPlayer();
            startMediaPlayer(fileDescription);
        }
    }

    public FlowableProcessor<Boolean> getUpdateProcessor() {
        return mediaPlayerUpdateProcessor;
    }

    public void reset() {
        if (mediaPlayer != null) {
            releaseMediaPlayer();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            releaseMediaPlayer();
        }
        disposable.dispose();
    }

    @Nullable
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void requestAudioFocus() {
        audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
        );
    }

    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    private void startMediaPlayer(@NonNull FileDescription fileDescription) {
        this.fileDescription = fileDescription;
        mediaPlayer = MediaPlayer.create(Config.instance.context, fileDescription.getFileUri());
        mediaPlayer.setOnCompletionListener(mp -> {
            abandonAudioFocus();
            releaseMediaPlayer();
        });
        mediaPlayer.start();
        requestAudioFocus();
        mediaPlayerUpdateProcessor.onNext(true);
    }

    private void releaseMediaPlayer() {
        this.fileDescription = null;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayerUpdateProcessor.onNext(true);
    }

    @Nullable
    public FileDescription getFileDescription() {
        return fileDescription;
    }
}
