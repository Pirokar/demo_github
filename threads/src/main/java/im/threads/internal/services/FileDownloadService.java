package im.threads.internal.services;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import im.threads.internal.Config;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileDownloader;
import im.threads.internal.utils.ThreadsLogger;

public final class FileDownloadService extends ThreadsService {

    public static final String FD_TAG = "im.threads.internal.services.DownloadService.FD_TAG";
    private static final String TAG = "DownloadService ";
    private static final String START_DOWNLOAD_FD_TAG = "im.threads.internal.services.DownloadService.START_DOWNLOAD_FD_TAG";
    private static final String START_DOWNLOAD_WITH_NO_STOP = "im.threads.internal.services.DownloadService.START_DOWNLOAD_WITH_NO_STOP";
    private static final HashMap<FileDescription, FileDownloader> runningDownloads = new HashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(3);

    public FileDownloadService() {
    }

    public static void startDownloadFD(Context context, FileDescription fileDescription) {
        startService(context, new Intent(context, FileDownloadService.class)
                .setAction(FileDownloadService.START_DOWNLOAD_FD_TAG)
                .putExtra(FileDownloadService.FD_TAG, fileDescription));
    }

    public static void startDownloadWithNoStop(Context context, FileDescription fileDescription) {
        startService(context, new Intent(context, FileDownloadService.class)
                .setAction(FileDownloadService.START_DOWNLOAD_WITH_NO_STOP)
                .putExtra(FileDownloadService.FD_TAG, fileDescription));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ThreadsLogger.i(TAG, "onStartCommand");
        if (intent == null) {
            return START_STICKY;
        }
        final FileDescription fileDescription = intent.getParcelableExtra(FD_TAG);
        if (fileDescription == null) {
            return START_STICKY;
        }
        if (fileDescription.getDownloadPath() == null || fileDescription.getFileUri() != null) {
            ThreadsLogger.e(TAG, "cant download with fileDescription = " + fileDescription);
            return START_STICKY;
        }
        final FileDownloader fileDownloader = new FileDownloader(
                fileDescription.getDownloadPath(),
                this,
                new FileDownloader.DownloadLister() {
                    @Override
                    public void onProgress(double progress) {
                        if (progress < 1) progress = 1.0;
                        fileDescription.setDownloadProgress((int) progress);
                        DatabaseHolder.getInstance().updateFileDescription(fileDescription);
                        sendDownloadProgressBroadcast(fileDescription);
                    }

                    @Override
                    public void onComplete(final File file) {
                        fileDescription.setDownloadProgress(100);
                        fileDescription.setFileUri(FileProviderHelper.getUriForFile(Config.instance.context, file));
                        DatabaseHolder.getInstance().updateFileDescription(fileDescription);
                        runningDownloads.remove(fileDescription);
                        sendFinishBroadcast(fileDescription);
                        if (runningDownloads.size() == 0) stopSelf();
                    }

                    @Override
                    public void onFileDownloadError(final Exception e) {
                        ThreadsLogger.e(TAG, "error while downloading file ", e);
                        fileDescription.setDownloadProgress(0);
                        DatabaseHolder.getInstance().updateFileDescription(fileDescription);
                        sendDownloadErrorBroadcast(fileDescription, e);
                    }
                });

        if (START_DOWNLOAD_FD_TAG.equals(intent.getAction())) {
            if (runningDownloads.containsKey(fileDescription)) {
                FileDownloader tfileDownloader = runningDownloads.get(fileDescription);
                runningDownloads.remove(fileDescription);
                if (tfileDownloader != null) {
                    tfileDownloader.stop();
                }
                fileDescription.setDownloadProgress(0);
                sendDownloadProgressBroadcast(fileDescription);
                DatabaseHolder.getInstance().updateFileDescription(fileDescription);
                return START_STICKY;
            } else {
                runningDownloads.put(fileDescription, fileDownloader);
                fileDescription.setDownloadProgress(1);
                sendDownloadProgressBroadcast(fileDescription);
                executor.execute(fileDownloader::download);
            }
        } else if (START_DOWNLOAD_WITH_NO_STOP.equals(intent.getAction())) {
            if (!runningDownloads.containsKey(fileDescription)) {
                runningDownloads.put(fileDescription, fileDownloader);
                fileDescription.setDownloadProgress(1);
                sendDownloadProgressBroadcast(fileDescription);
                executor.execute(fileDownloader::download);
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendDownloadProgressBroadcast(FileDescription fileDescription) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ProgressReceiver.PROGRESS_BROADCAST).putExtra(FD_TAG, fileDescription));
    }

    private void sendFinishBroadcast(FileDescription fileDescription) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST).putExtra(FD_TAG, fileDescription));
    }

    private void sendDownloadErrorBroadcast(FileDescription fileDescription, Throwable throwable) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST)
                .putExtra(FD_TAG, fileDescription)
                .putExtra(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST, throwable)
        );
    }
}
