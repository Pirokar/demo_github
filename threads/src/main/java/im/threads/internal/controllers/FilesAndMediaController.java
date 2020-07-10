package im.threads.internal.controllers;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import im.threads.internal.activities.FilesActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class FilesAndMediaController extends Fragment {

    private static final String TAG = FilesAndMediaController.class.getCanonicalName();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    public void bindActivity(FilesActivity activity) {
        this.activity = activity;
    }

    public void getFilesAsync() {
        compositeDisposable.add(DatabaseHolder.getInstance().getAllFileDescriptions()
                .map(data -> {
                    List<FileDescription> list = new ArrayList<>();
                    for (FileDescription fd : data) {
                        if (FileUtils.isSupportedFile(fd)) {
                            list.add(fd);
                        }
                    }
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list -> {
                            if (null != activity) {
                                activity.onFileReceive(list);
                            }
                        },
                        e -> ThreadsLogger.e(TAG, "getAllFileDescriptions error: " + e.getMessage()))
                );
    }

    public void onFileClick(FileDescription fileDescription) {
        if (FileUtils.isImage(fileDescription)) {
            activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
        } else if (FileUtils.isDoc(fileDescription)) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(fileDescription.getFileUri(), FileUtils.getMimeType(fileDescription));
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
