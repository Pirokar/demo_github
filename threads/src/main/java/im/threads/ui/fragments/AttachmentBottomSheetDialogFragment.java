package im.threads.ui.fragments;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.business.utils.MediaHelper;
import im.threads.business.useractivity.UserActivityTime;
import im.threads.business.useractivity.UserActivityTimeProvider;
import im.threads.ui.utils.ColorsHelper;
import im.threads.ui.views.BottomGallery;
import im.threads.ui.views.BottomSheetView;
import im.threads.ui.config.Config;

public class AttachmentBottomSheetDialogFragment extends BottomSheetDialogFragment implements BottomSheetView.ButtonsListener {

    public static final String TAG = AttachmentBottomSheetDialogFragment.class.getSimpleName();

    @Nullable
    private Callback callback = null;

    @Override
    public void onAttach(@NonNull Context context) {
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
        Dialog dialog = new BottomSheetDialog(requireContext(), getTheme()) {
            @Override
            public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
                UserActivityTime timeCounter =
                        UserActivityTimeProvider.INSTANCE.getLastUserActivityTimeCounter();
                if (MotionEvent.ACTION_DOWN == ev.getAction()) {
                    timeCounter.updateLastUserActivityTime();
                }
                return super.dispatchTouchEvent(ev);
            }
        };
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ChatStyle chatStyle = Config.getInstance().getChatStyle();
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_attachment, container, false);
        BottomSheetView fileInputSheet = view.findViewById(R.id.file_input_sheet);
        BottomGallery bottomGallery = view.findViewById(R.id.bottom_gallery);
        int attachmentBottomSheetButtonTintResId = chatStyle.chatBodyIconsTint == 0
                ? chatStyle.attachmentBottomSheetButtonTintResId : chatStyle.chatBodyIconsTint;
        fileInputSheet.setButtonsTint(attachmentBottomSheetButtonTintResId);
        fileInputSheet.setButtonsListener(this);
        ArrayList<Uri> allItems = new ArrayList<>();
        Context context = getContext();
        if (context != null) {
            ColorsHelper.setBackgroundColor(context, fileInputSheet,
                    chatStyle.chatMessageInputColor);
            ColorsHelper.setBackgroundColor(context, bottomGallery,
                    chatStyle.chatMessageInputColor);
            try (Cursor c = MediaHelper.getAllPhotos(context)) {
                if (c != null) {
                    int _ID = c.getColumnIndex(MediaStore.Images.Media._ID);
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        allItems.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(_ID)));
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
    public void onSendClick() {
        if (callback != null) {
            callback.onSendClick();
        }
    }

    public interface Callback extends BottomSheetView.ButtonsListener {
        void onImageSelectionChanged(List<Uri> imageList);

        void onBottomSheetDetached();
    }
}
