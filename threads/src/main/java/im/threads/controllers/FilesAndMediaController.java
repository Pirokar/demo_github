package im.threads.controllers;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import im.threads.activities.FilesActivity;
import im.threads.activities.ImagesActivity;
import im.threads.database.DatabaseHolder;
import im.threads.model.CompletionHandler;
import im.threads.model.FileDescription;
import im.threads.utils.FileUtils;

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
                        if (null != activity) {
                            List<FileDescription> list = new ArrayList<>();
                            for (FileDescription fd : data) {
                                if (FileUtils.isImage(fd)) {
                                    list.add(fd);
                                }
                                if (FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.PDF
                                        ||FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.OTHER_DOC_FORMATS) {
                                    list.add(fd);
                                }
                            }
                         //   Collections.reverse(list);
                            activity.onFileReceive(list);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable e, String message, List<FileDescription> data) {

            }
        });
    }

    public void onFileClick(FileDescription fileDescription) {
        if (FileUtils.isImage(fileDescription)) {
            activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
        } else if (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PDF) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            File file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                startActivity(target);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
                Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
