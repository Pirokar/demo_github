package im.threads.internal.fragments;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.utils.ColorsHelper;
import im.threads.internal.views.BottomGallery;
import im.threads.internal.views.BottomSheetView;

public class AttachmentBottomSheetDialogFragment extends BottomSheetDialogFragment implements BottomSheetView.ButtonsListener {

    public static final String TAG = AttachmentBottomSheetDialogFragment.class.getSimpleName();

    @Nullable
    private Callback callback = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof Callback) {
            callback = (Callback) getParentFragment();
        } else if (context instanceof Callback) {
            callback = (Callback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callback != null) {
            callback.onBottomSheetDetached();
            callback = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_attachment, container, false);
        BottomSheetView fileInputSheet = view.findViewById(R.id.file_input_sheet);
        BottomGallery bottomGallery = view.findViewById(R.id.bottom_gallery);
        fileInputSheet.setButtonsTint(Config.instance.getChatStyle().chatBodyIconsTint);
        fileInputSheet.setButtonsListener(this);
        ArrayList<String> allItems = new ArrayList<>();
        Context context = getContext();
        if (context != null) {
            ColorsHelper.setBackgroundColor(context, fileInputSheet, Config.instance.getChatStyle().chatMessageInputColor);
            ColorsHelper.setBackgroundColor(context, bottomGallery, Config.instance.getChatStyle().chatMessageInputColor);
            try (Cursor c = MediaHelper.getAllPhotos(context)) {
                if (c != null) {
                    int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        allItems.add(c.getString(DATA));
                    }
                }
            }
        }
        bottomGallery.setImages(allItems, items -> {
            if (callback != null) {
                callback.onImageSelectionChanged(items);
            }
            if (items.size() > 0) {
                fileInputSheet.showSend();
            } else {
                fileInputSheet.showMainItemList();
            }
        });
        return view;
    }

    @Override
    public void onCameraClick() {
        if (callback != null) {
            callback.onCameraClick();
        }
    }

    @Override
    public void onGalleryClick() {
        if (callback != null) {
            callback.onGalleryClick();
        }
    }

    @Override
    public void onFilePickerClick() {
        if (callback != null) {
            callback.onFilePickerClick();
        }
    }

    @Override
    public void onSelfieClick() {
        if (callback != null) {
            callback.onSelfieClick();
        }
    }

    @Override
    public void onSendClick() {
        if (callback != null) {
            callback.onSendClick();
        }
    }

    public interface Callback extends BottomSheetView.ButtonsListener {
        void onImageSelectionChanged(List<String> imageList);

        void onBottomSheetDetached();
    }
}
