package com.sequenia.threads.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sequenia.threads.R;

/**
 * Created by yuri on 06.06.2016.
 * layout/view_bottom_attachment_sheet.xml
 */
public class BottomSheetView extends LinearLayout {
    private ButtonsListener buttonsListener;
    private TextView hideButton;
    private boolean isSmthSelected;

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
        findViewById(R.id.camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onCameraClick();
            }
        });
        findViewById(R.id.gallery).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onGalleryClick();
            }
        });
        findViewById(R.id.file).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != buttonsListener) buttonsListener.onFilepickerClick();
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

    public void setSelectedState(boolean isSmthSelected) {
        Drawable d;
        if (isSmthSelected) {
             d = getContext().getResources().getDrawable(R.drawable.ic_send_blue_42dp);
            hideButton.setText(getContext().getResources().getString(R.string.send));
        } else {
            d = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_blue_42dp);
            hideButton.setText(getContext().getResources().getString(R.string.hide));
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

        void onFilepickerClick();

        void onHideClick();

        void onSendClick();

    }
}
