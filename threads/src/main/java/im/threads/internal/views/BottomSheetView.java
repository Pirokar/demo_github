package im.threads.internal.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import im.threads.R;
import im.threads.internal.utils.ViewUtils;

public class BottomSheetView extends LinearLayout {
    private ButtonsListener buttonsListener;
    private TextView hideButton;
    private boolean isSmthSelected;
    private Button camera;
    private Button file;
    private Button gallery;
    private @ColorInt
    int color;

    public BottomSheetView(Context context) {
        super(context);
        init();
    }

    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_bottom_attachment_sheet, this, true);
        camera = findViewById(R.id.camera);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(camera, R.drawable.ic_camera_alt_blue_42dp, ViewUtils.DrawablePosition.TOP);
        camera.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onCameraClick();
        });
        gallery = findViewById(R.id.gallery);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(gallery, R.drawable.ic_insert_photo_blue_42dp, ViewUtils.DrawablePosition.TOP);
        gallery.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onGalleryClick();
        });
        file = findViewById(R.id.file);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(file, R.drawable.ic_insert_file_blue_42dp, ViewUtils.DrawablePosition.TOP);
        file.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onFilePickerClick();
        });
        hideButton = findViewById(R.id.bmsheet_hide);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(hideButton, R.drawable.ic_keyboard_arrow_down_blue_42dp, ViewUtils.DrawablePosition.TOP);
        hideButton.setOnClickListener(v -> {
            if (null != buttonsListener) {
                if (isSmthSelected) {
                    buttonsListener.onSendClick();
                } else {
                    buttonsListener.onHideClick();
                }
            }
        });
        this.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
    }

    public void setButtonsTint(@ColorRes int colorRes) {
        color = ContextCompat.getColor(getContext(), colorRes);
        ArrayList<Drawable> drawables = new ArrayList<>();
        file.setTextColor(color);
        camera.setTextColor(color);
        gallery.setTextColor(color);
        hideButton.setTextColor(color);
        drawables.addAll(Arrays.asList(file.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(camera.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(gallery.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(hideButton.getCompoundDrawables()));
        for (Drawable d : drawables) {
            if (d != null) {
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void setSelectedState(boolean isSmthSelected) {
        Drawable d;
        if (isSmthSelected) {
            d = AppCompatResources.getDrawable(getContext(), R.drawable.ic_send_blue_42dp);
            if (color != 0) {
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            hideButton.setText(getContext().getResources().getString(R.string.threads_send));
        } else {
            d = AppCompatResources.getDrawable(getContext(), R.drawable.ic_keyboard_arrow_down_blue_42dp);
            if (color != 0) {
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            hideButton.setText(getContext().getResources().getString(R.string.threads_hide));
        }
        hideButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, d, null, null);
        this.isSmthSelected = isSmthSelected;
    }

    public void setButtonsListener(ButtonsListener listener) {
        this.buttonsListener = listener;
    }

    public interface ButtonsListener {
        void onCameraClick();

        void onGalleryClick();

        void onFilePickerClick();

        void onHideClick();

        void onSendClick();
    }
}
