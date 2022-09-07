package im.threads.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.Arrays;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.ui.config.Config;
import im.threads.ui.utils.FileHelper;

/**
 * Dialog fragment for picking folder
 * implement SelectedListener interface to get chosen dir
 * set setFileFilter(FileFilter filefilter) if your need to display not only directories
 */
public final class FilePickerFragment extends DialogFragment
        implements FileFilter, View.OnClickListener, ListView.OnItemClickListener, AlertDialog.OnClickListener {
    private static final String STARTING_FOLDER_TAG = "start";
    private static final String PREVIOUS_FOLDER_DOTS = "...";
    private File currentAbsoluteDir;
    private ListView mListView;
    private ArrayAdapter<String> mAnimatedArrayAdapter;
    private SelectedListener mSelectedListener;
    private FileFilter mFileFilter;
    private boolean isFilterEnabled;
    private ChatStyle chatStyle = Config.getInstance().getChatStyle();

    public static FilePickerFragment newInstance() {
        FilePickerFragment fragment = new FilePickerFragment();
        Bundle b = new Bundle();
        b.putSerializable(STARTING_FOLDER_TAG, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        currentAbsoluteDir = (File) getArguments().getSerializable(STARTING_FOLDER_TAG);
        if (currentAbsoluteDir == null || currentAbsoluteDir.isFile()) {
            currentAbsoluteDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v;
        AlertDialog dialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), chatStyle.fileBrowserDialogStyleResId);
        builder.setTitle(getString(R.string.threads_choose_file));
        builder.setNeutralButton(getString(R.string.threads_folder_up), this);
        builder.setNegativeButton(getString(R.string.threads_cancel), this);
        dialog = builder.create();
        v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_directory_picker, null);
        dialog.setView(v);
        mListView = v.findViewById(R.id.folder_list);
        mAnimatedArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_filepicker, R.id.text);
        mListView.setAdapter(travelToFolder(currentAbsoluteDir, mAnimatedArrayAdapter));
        mListView.setOnItemClickListener(this);
        return dialog;
    }

    public void setFileFilter(FileFilter filefilter) {
        mFileFilter = filefilter;
        isFilterEnabled = true;
    }

    @Override
    public boolean accept(File pathname) {
        if (isFilterEnabled) {
            return (pathname.isDirectory() || mFileFilter.accept(pathname)) && pathname.canRead();
        }
        return pathname.isDirectory() && pathname.canRead();

    }

    // get all dirs in current path
    private File[] getFolderFiles(File pathname) {
        File[] files = pathname.listFiles(this);
        Collator collator = Collator.getInstance();
        if (files != null) {
            Arrays.sort(files, (o1, o2) -> {
                int i = collator.compare(o1.getName(), o2.getName());
                if (o1.isDirectory()) {
                    if (o2.isDirectory()) {
                        return i;
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.isDirectory()) {
                        return 1;
                    } else {
                        return i;
                    }
                }
            });
        }
        return files;
    }

    //convert dirs into conventional array of String[] for ArrayAdapter
    private String[] getFolderLastPathNames(File[] folders) {
        String[] folderNames = new String[folders.length + 1];
        folderNames[0] = PREVIOUS_FOLDER_DOTS;

        if (folders.length == 0) {//if there is no readable folders in catalog
            return folderNames;
        }
        for (int i = 0; i < folders.length; i++) {
            String absPath = folders[i].getAbsolutePath();
            int slashPos = absPath.lastIndexOf("/");
            if (folders[i].isDirectory()) folderNames[i + 1] = absPath.substring(slashPos);
            else folderNames[i + 1] = absPath.substring(slashPos + 1);
        }
        return folderNames;
    }

    //swap data in adapter & (optional) update data in label
    private ArrayAdapter<String> travelToFolder(File absolutePathDir, ArrayAdapter<String> adapter) {
        File[] fullNamedFolders = getFolderFiles(absolutePathDir);
        if (fullNamedFolders == null) return adapter;
        String[] folders = getFolderLastPathNames(getFolderFiles(absolutePathDir));
        adapter.clear();
        adapter.addAll(Arrays.asList(folders));
        if (getDialog() != null) {
            getDialog().setTitle(getString(R.string.threads_now_you_are_in_directory) + "\r\n" + absolutePathDir.getAbsolutePath());
        }
        adapter.notifyDataSetChanged();
        return adapter;
    }

    //helper hook method to get up on directory tree
    private ArrayAdapter<String> travelToParentDir(File dir, ArrayAdapter<String> adapter) {
        String filePath = dir.getAbsolutePath();
        int lastOccurrence = filePath.lastIndexOf(File.separator);
        String parentDirName;
        if (lastOccurrence > 0) {
            parentDirName = filePath.substring(0, lastOccurrence);
        } else {
            parentDirName = File.separator;
        }
        currentAbsoluteDir = new File(parentDirName);
        return travelToFolder(currentAbsoluteDir, adapter);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String textViewText = ((TextView) view).getText().toString();
        if (textViewText.equals(PREVIOUS_FOLDER_DOTS)) {
            travelToParentDir(currentAbsoluteDir, mAnimatedArrayAdapter);
            return;
        }
        File pointedFileToTravel = new File(currentAbsoluteDir.toString() + "/" + textViewText);
        currentAbsoluteDir = pointedFileToTravel;
        if (pointedFileToTravel.exists() && mSelectedListener != null) {
            if (mFileFilter != null && mFileFilter.accept(pointedFileToTravel.getAbsoluteFile())) {
                String path = pointedFileToTravel.getPath();
                if (path.contains(".") && FileHelper.INSTANCE.isAllowedFileExtension(path.substring(path.lastIndexOf(".") + 1))) {
                    if (FileHelper.INSTANCE.isAllowedFileSize(pointedFileToTravel.length())) {
                        mSelectedListener.onFileSelected(pointedFileToTravel);
                        dismiss();
                    } else {
                        // Недопустимый размер файла
                        Toast.makeText(getContext(), getString(R.string.threads_not_allowed_file_size, FileHelper.INSTANCE.getMaxAllowedFileSize()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Недопустимое расширение файла
                    Toast.makeText(getContext(), R.string.threads_not_allowed_file_extension, Toast.LENGTH_SHORT).show();
                }
            }
        }
        travelToFolder(currentAbsoluteDir, mAnimatedArrayAdapter);
    }

    public void setOnDirSelectedListener(SelectedListener getDirectory) {
        if (getDirectory != null) {
            this.mSelectedListener = getDirectory;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_NEGATIVE:
                dismiss();
                break;
            case AlertDialog.BUTTON_POSITIVE:
                if (mSelectedListener != null) {
                    mSelectedListener.onFileSelected(currentAbsoluteDir);
                }
                dismiss();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //make dialog don't close after pushing neutral button.
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> travelToParentDir(currentAbsoluteDir, mAnimatedArrayAdapter));
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), chatStyle.chatToolbarColorResId));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getActivity(), chatStyle.chatToolbarColorResId));
    }

    public interface SelectedListener {
        void onFileSelected(File file);
    }
}
