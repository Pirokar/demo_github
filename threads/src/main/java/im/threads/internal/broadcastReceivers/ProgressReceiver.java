package im.threads.internal.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.workers.FileDownloadWorker;

/**
 * В чате есть возможность скачать файл из сообщения.
 * Он скачивается через сервис.
 * Для приема сообщений из сервиса используется данный BroadcastReceiver
 */
public final class ProgressReceiver extends BroadcastReceiver {
    private static final String TAG = "ProgressReceiver ";
    // Сообщения для Broadcast Receivers
    public static final String PROGRESS_BROADCAST = "im.threads.internal.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "im.threads.internal.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "im.threads.internal.controllers.DOWNLOAD_ERROR_BROADCAST";

    private final WeakReference<Callback> callback;

    public ProgressReceiver(@NonNull Callback callback) {
        this.callback = new WeakReference<>(callback);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ThreadsLogger.i(TAG, "onReceive:");
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case PROGRESS_BROADCAST: {
                ThreadsLogger.i(TAG, "onReceive: PROGRESS_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(FileDownloadWorker.FD_TAG);
                if (callback.get() != null && fileDescription != null) {
                    callback.get().updateProgress(fileDescription);
                }
                break;
            }
            case DOWNLOADED_SUCCESSFULLY_BROADCAST: {
                ThreadsLogger.i(TAG, "onReceive: DOWNLOADED_SUCCESSFULLY_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(FileDownloadWorker.FD_TAG);
                fileDescription.setDownloadProgress(100);
                if (callback.get() != null) {
                    callback.get().updateProgress(fileDescription);
                }
                break;
            }
            case DOWNLOAD_ERROR_BROADCAST: {
                ThreadsLogger.e(TAG, "onReceive: DOWNLOAD_ERROR_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(FileDownloadWorker.FD_TAG);
                if (callback.get() != null && fileDescription != null) {
                    Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                    callback.get().onDownloadError(fileDescription, t);
                }
                break;
            }
        }
    }

    public interface Callback {
        void updateProgress(FileDescription fileDescription);

        void onDownloadError(FileDescription fileDescription, Throwable t);
    }
}
