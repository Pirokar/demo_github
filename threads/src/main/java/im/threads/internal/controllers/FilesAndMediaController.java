package im.threads.internal.controllers;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import im.threads.internal.activities.FilesActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileUtils;

public final class FilesAndMediaController extends Fragment {
    private FilesActivity activity;

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
    }

    public void unbindActivty() {
        activity = null;
    }

    public void getFilesAsync() {
        DatabaseHolder.getInstance().getAllFileDescriptions(new CompletionHandler<List<FileDescription>>() {
            @Override
            public void onComplete(final List<FileDescription> data) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (null != activity) {
                        List<FileDescription> list = new ArrayList<>();
                        for (FileDescription fd : data) {
                            if (FileUtils.isSupportedFile(fd)) {
                                list.add(fd);
                            }
                        }
                        activity.onFileReceive(list);
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
        } else if (FileUtils.isDoc(fileDescription)) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            File file = new File(fileDescription.getFilePath());
            target.setDataAndType(FileProviderHelper.getUriForFile(activity, file), FileUtils.getMimeType(file));
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                activity.startActivity(target);
            } catch (final ActivityNotFoundException e) {
                Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
