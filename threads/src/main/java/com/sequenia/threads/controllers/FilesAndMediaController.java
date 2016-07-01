package com.sequenia.threads.controllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.activities.FilesActivity;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.FileDescription;

import java.util.List;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesAndMediaController extends Fragment {
    private FilesActivity activity;
    private Context appContext;

    public static FilesAndMediaController getInstance() {
        return new FilesAndMediaController();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void bindActivity(FilesActivity activity) {
        this.activity = activity;
        appContext = activity.getApplicationContext();
    }

    public void unbindActivty() {
        activity = null;
        appContext = null;
    }

    public void getFilesAcync() {
        DatabaseHolder.getInstance(activity).getFilesAsync(new CompletionHandler<List<FileDescription>>() {
            @Override
            public void onComplete(final List<FileDescription> data) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != activity)
                            activity.onFileReceive(data);
                    }
                });
            }
            @Override
            public void onError(Throwable e, String message, List<FileDescription> data) {

            }
        });
    }
}
