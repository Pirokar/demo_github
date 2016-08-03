package com.sequenia.threads.utils;

import android.content.Context;

import com.pushserver.android.PushController;
import com.pushserver.android.RequestProgressCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.model.FileDescription;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuri on 03.08.2016.
 */
public class FilePoster {
    private FileDescription fileDescription;
    private Context context;

    public FilePoster(FileDescription fileDescription, Context context) {
        this.fileDescription = fileDescription;
        this.context = context;
    }

    public void post(final Callback<String, Exception> callback) {
        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
            PushController
                    .getInstance(context)
                    .sendFileAsync(new File(fileDescription.getFilePath().replaceAll("file://", ""))
                            , ""
                            , TimeUnit.DAYS.toMillis(7)
                            , new RequestProgressCallback() {
                                @Override
                                public void onProgress(double v) {

                                }

                                @Override
                                public void onResult(String s) {
                                    callback.onSuccess(s);
                                }

                                @Override
                                public void onError(PushServerErrorException e) {
                                    callback.onFail(e);
                                }
                            });
        } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
            if (fileDescription.getDownloadPath() != null) {
                callback.onSuccess(fileDescription.getDownloadPath());
            } else {
                callback.onFail(new FileNotFoundException());
            }
        }else if (fileDescription.getFilePath() ==null && fileDescription.getDownloadPath() !=null){
            callback.onSuccess(fileDescription.getDownloadPath());
        }
    }
}
