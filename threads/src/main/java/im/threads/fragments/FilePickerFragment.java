package im.threads.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

import static im.threads.model.ChatStyle.INVALID;


/**
 * Dialog fragment for picking folder
 * implement SelectedListener interface to get chosen dir
 * set setFileFilter(FileFilter filefilter) if your need to display not only directories
 */
public class FilePickerFragment extends DialogFragment
        implements FileFilter, View.OnClickListener, ListView.OnItemClickListener, AlertDialog.OnClickListener {
    public static final String TAG = "DirChooserDialogFrag";
    private File currentAbsoluteDir;
    private static final String STARTING_FOLDER_TAG = "start";
    private static final String PREVIOUS_FOLDER_DOTS = "...";
    private ListView mListView;
    private Button mOkButton, mCancelButton, mDirectoryUpButton;
    private  ArrayAdapter<String> mAnimatedArrayAdapter;
    private SelectedListener mSelectedListener;
    private FileFilter mFileFilter;
    private boolean isFilterEnabled;


    public static FilePickerFragment newInstance(@Nullable File startingFolder) {
        FilePickerFragment fragment = new FilePickerFragment();
        Bundle b = new Bundle();
        if (startingFolder == null) {
            startingFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }
        b.putSerializable(STARTING_FOLDER_TAG, startingFolder);
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


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v;
        AlertDialog dialog;
        ChatStyle style = PrefUtils.getIncomingStyle(this.getActivity());
        AlertDialog.Builder builder;
        if (style != null && style.fileBrowserDialogStyleResId != INVALID) {
            builder = new AlertDialog.Builder(getActivity(), style.fileBrowserDialogStyleResId);
        } else {
            builder = new AlertDialog.Builder(getActivity(), R.style.FileDialogStyleTransparent);
        }
        builder.setTitle(getString(R.string.lib_choose_file));
        builder.setNeutralButton(getString(R.string.lib_folder_up), this);
        builder.setNegativeButton(getString(R.string.lib_cancel), this);
        dialog = builder.create();
        v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_directory_picker, null);

        dialog.setView(v);
        mListView = (ListView) v.findViewById(R.id.folder_list);
        mAnimatedArrayAdapter = new  ArrayAdapter<String>(getActivity(), R.layout.item_filepicker, R.id.text);
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
        return pathname.listFiles(this);
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
        File[] fullNamedfolders = getFolderFiles(absolutePathDir);
        if (fullNamedfolders == null) return adapter;
        String[] folders = getFolderLastPathNames(getFolderFiles(absolutePathDir));
        adapter.clear();
        adapter.addAll(Arrays.asList(folders));
        if (absolutePathDir != null && getDialog() != null) {
            getDialog().setTitle(getString(R.string.lib_now_you_are_in_directory) + "\r\n" + absolutePathDir.getAbsolutePath());
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
                mSelectedListener.onFileSelected(pointedFileToTravel);
                dismiss();
            }
        }
        travelToFolder(currentAbsoluteDir, mAnimatedArrayAdapter);
    }

    public interface SelectedListener {
        void onFileSelected(File directory);
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
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelToParentDir(currentAbsoluteDir, mAnimatedArrayAdapter);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ChatStyle style = PrefUtils.getIncomingStyle(getActivity());
        if (style != null) {
            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), style.chatToolbarColorResId));
                ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getActivity(), style.chatToolbarColorResId));
            }
        }
    }
}
