package im.threads.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import im.threads.R;
import im.threads.model.ChatStyle;

/**
 * Created by yuri on 06.06.2016.
 * layout/view_bottom_attachment_sheet.xml
 */
public class BottomSheetView extends LinearLayout {
    private ButtonsListener buttonsListener;
    private TextView hideButton;
    private boolean isSmthSelected;
    private Button camera;
    private Button file;
    private Button gallery;
    private  @ColorInt int color;

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
        camera = (Button) findViewById(R.id.camera);
        camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onCameraClick();
            }
        });
        gallery = (Button) findViewById(R.id.gallery);
        gallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onGalleryClick();
            }
        });
        file = (Button) findViewById(R.id.file);
        file.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onFilePickerClick();
            }
        });
        hideButton = (TextView) findViewById(R.id.bmsheet_hide);
        hideButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) {
                    if (isSmthSelected) {
                        buttonsListener.onSendClick();
                    } else {
                        buttonsListener.onHideClick();
                    }
                }
            }
        });
        this.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
    }

    public void setButtonsTint(@ColorRes int colorRes) {
        if(colorRes != ChatStyle.INVALID) {
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
    }

    public void setSelectedState(boolean isSmthSelected) {
        Drawable d;
        if (isSmthSelected) {
            d = getContext().getResources().getDrawable(R.drawable.ic_send_blue_42dp);
            if (color!=0){
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            hideButton.setText(getContext().getResources().getString(R.string.threads_send));
        } else {
            d = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_blue_42dp);
            if (color!=0){
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
