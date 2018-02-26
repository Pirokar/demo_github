package im.threads.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;

import im.threads.fragments.ChatFragment;
import im.threads.model.FileDescription;
import im.threads.services.DownloadService;

/**
 * В чате есть возможность скачать файл из сообщения.
 * Он скачивается через сервис.
 * Для приема сообщений из сервиса используется данный BroadcastReceiver
 */
public class ProgressReceiver extends BroadcastReceiver {
    private static final String TAG = "ProgressReceiver ";
    // Сообщения для Broadcast Receivers
    public static final String PROGRESS_BROADCAST = "im.threads.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "im.threads.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "im.threads.controllers.DOWNLOAD_ERROR_BROADCAST";
    public static final String DEVICE_ID_IS_SET_BROADCAST = "im.threads.controllers.DEVICE_ID_IS_SET_BROADCAST";

    private WeakReference<ChatFragment> fragment;
    private WeakReference<DeviceIdChangedListener> deviceIdChangedListener;

    public ProgressReceiver(ChatFragment fragment, DeviceIdChangedListener deviceIdChangedListener) {
        this.fragment = new WeakReference<>(fragment);
        this.deviceIdChangedListener = new WeakReference<>(deviceIdChangedListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive:");
        String action = intent.getAction();
        if (action == null) return;
        if (action.equals(PROGRESS_BROADCAST)) {
            Log.i(TAG, "onReceive: PROGRESS_BROADCAST ");
            FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
            if (fragment != null && fileDescription != null)
                fragment.get().updateProgress(fileDescription);
        } else if (action.equals(DOWNLOADED_SUCCESSFULLY_BROADCAST)) {
            Log.i(TAG, "onReceive: DOWNLOADED_SUCCESSFULLY_BROADCAST ");
            FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
            fileDescription.setDownloadProgress(100);
            if (fragment != null)
                fragment.get().updateProgress(fileDescription);
        } else if (action.equals(DOWNLOAD_ERROR_BROADCAST)) {
            Log.i(TAG, "onReceive: DOWNLOAD_ERROR_BROADCAST ");
            FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
            if (fragment != null && fileDescription != null) {
                Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                fragment.get().onDownloadError(fileDescription, t);
            }
        } else if (action.equals(DEVICE_ID_IS_SET_BROADCAST)) {
            deviceIdChangedListener.get().onSettingClientId(context);
        }
    }

    public interface DeviceIdChangedListener {
        void onSettingClientId(final Context ctx);
    }
}