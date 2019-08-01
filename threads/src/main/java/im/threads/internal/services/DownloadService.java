package im.threads.internal.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileDownloader;

/**
 * Created by yuri on 01.08.2016.
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService ";
    public static final String START_DOWNLOAD_FD_TAG = "com.sequenia.threads.services.START_DOWNLOAD_FD_TAG";
    public static final String FD_TAG = "com.sequenia.threads.services.FD_TAG";
    public static final String START_DOWNLOAD_WITH_NO_STOP = "com.sequenia.threads.services.START_DOWNLOAD_WITH_NO_STOP";
    Executor executor = Executors.newFixedThreadPool(3);
    private static HashMap<FileDescription, FileDownloader> runningDownloads = new HashMap<>();

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ThreadsLogger.i(TAG, "onStartCommand");
        if (intent == null) return START_STICKY;
        final FileDescription fileDescription = intent.getParcelableExtra(FD_TAG);
        if (fileDescription == null) return START_STICKY;
        if (fileDescription.getDownloadPath() == null || fileDescription.getFilePath() != null) {
            ThreadsLogger.e(TAG, "cant download with fileDescription = " + fileDescription);
            return START_STICKY;
        }
        final Context context = this;

        final FileDownloader fileDownloader = new FileDownloader(fileDescription.getDownloadPath(), fileDescription.getIncomingName(), context) {

            @Override
            public void onProgress(double progress) {
                if (progress < 1) progress = 1.0;
                fileDescription.setDownloadProgress((int) progress);
                DatabaseHolder.getInstance(context).updateFileDescription(fileDescription);
                sendDownloadProgressBroadcast(fileDescription);
            }

            @Override
            public void onComplete(final File file) {
                fileDescription.setDownloadProgress(100);
                fileDescription.setFilePath(file.getAbsolutePath());
                DatabaseHolder.getInstance(context).updateFileDescription(fileDescription);
                runningDownloads.remove(fileDescription);
                sendFinishBroadcast(fileDescription);
                if (runningDownloads.size() == 0) stopSelf();
            }

            @Override
            public void onFileDonwloaderError(final Exception e) {
                ThreadsLogger.e(TAG, "error while downloading file ", e);
                fileDescription.setDownloadProgress(0);
                DatabaseHolder.getInstance(context).updateFileDescription(fileDescription);
                sendDownloadErrorBroadcast(fileDescription, e);
            }
        };

        if (intent.getAction().equals(START_DOWNLOAD_FD_TAG)) {
            if (runningDownloads.containsKey(fileDescription)) {
                FileDownloader tfileDownloader = runningDownloads.get(fileDescription);
                runningDownloads.remove(fileDescription);
                tfileDownloader.stop();
                fileDescription.setDownloadProgress(0);
                sendDownloadProgressBroadcast(fileDescription);
                DatabaseHolder.getInstance(this).updateFileDescription(fileDescription);
                return START_STICKY;
            } else {
                runningDownloads.put(fileDescription, fileDownloader);
                fileDescription.setDownloadProgress(1);
                sendDownloadProgressBroadcast(fileDescription);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        fileDownloader.download();
                    }
                });
            }
        } else if (intent.getAction().equals(START_DOWNLOAD_WITH_NO_STOP)) {
            if (!runningDownloads.containsKey(fileDescription)) {
                runningDownloads.put(fileDescription, fileDownloader);
                fileDescription.setDownloadProgress(1);
                sendDownloadProgressBroadcast(fileDescription);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        fileDownloader.download();
                    }
                });
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendDownloadProgressBroadcast(FileDescription filedescription) {
        Intent i = new Intent();
        i.setAction(ProgressReceiver.PROGRESS_BROADCAST);
        i.putExtra(FD_TAG, filedescription);
        sendBroadcast(i);
    }

    private void sendFinishBroadcast(FileDescription filedescription) {
        Intent i = new Intent();
        i.setAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST);
        i.putExtra(FD_TAG, filedescription);
        sendBroadcast(i);
    }

    private void sendDownloadErrorBroadcast(FileDescription fileDescription, Throwable throwable) {
        Intent i = new Intent();
        i.setAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST);
        i.putExtra(FD_TAG, fileDescription);
        i.putExtra(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST, throwable);
        sendBroadcast(i);
    }
}
