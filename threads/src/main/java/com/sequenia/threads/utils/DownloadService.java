package com.sequenia.threads.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.utils.FileDownloader;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by yuri on 01.08.2016.
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService ";
    public static final String FD_TAG = "FD_TAG";
    Executor executor = Executors.newFixedThreadPool(3);
    private static HashMap<FileDescription, FileDownloader> runningDownloads = new HashMap<>();

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final FileDescription fileDescription = intent.getParcelableExtra(FD_TAG);
        if (fileDescription == null) return START_STICKY;
        if (fileDescription.getDownloadPath() == null || fileDescription.getFilePath() != null) {
            Log.e(TAG, "cant download with fileDescription = " + fileDescription);
            return START_STICKY;
        }
        if (runningDownloads.containsKey(fileDescription)) {
            FileDownloader fileDownloader = runningDownloads.get(fileDescription);
            runningDownloads.remove(fileDescription);
            fileDownloader.stop();
            fileDescription.setDownloadProgress(0);
            sendDownloadProgressBroadcast(fileDescription);
            DatabaseHolder.getInstance(this).updateFileDescription(fileDescription);
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
                fileDescription.setFilePath("file://" + file.getAbsolutePath());
                DatabaseHolder.getInstance(context).updateFileDescription(fileDescription);
                runningDownloads.remove(fileDescription);
                sendFinishBroadcast(fileDescription);
                if (runningDownloads.size()==0)stopSelf();
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "error while downloading file " + e);
                e.printStackTrace();
                fileDescription.setDownloadProgress(0);
                DatabaseHolder.getInstance(context).updateFileDescription(fileDescription);
                sendDownloadErrorBroadcast(fileDescription, e);
            }
        };
        runningDownloads.put(fileDescription, fileDownloader);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                fileDownloader.download();
            }
        });
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendDownloadProgressBroadcast(FileDescription filedescription) {
        Intent i = new Intent();
        i.setAction(ChatController.PROGRESS_BROADCAST);
        i.putExtra(FD_TAG, filedescription);
        sendBroadcast(i);
    }

    private void sendFinishBroadcast(FileDescription filedescription) {
        Intent i = new Intent();
        i.setAction(ChatController.DOWNLOADED_SUCCESSFULLY_BROADCAST);
        i.putExtra(FD_TAG, filedescription);
        sendBroadcast(i);
    }

    private void sendDownloadErrorBroadcast(FileDescription fileDescription, Throwable throwable) {
        Intent i = new Intent();
        i.setAction(ChatController.DOWNLOAD_ERROR_BROADCAST);
        i.putExtra(FD_TAG, fileDescription);
        i.putExtra(ChatController.DOWNLOAD_ERROR_BROADCAST, throwable);
        sendBroadcast(i);
    }
}
