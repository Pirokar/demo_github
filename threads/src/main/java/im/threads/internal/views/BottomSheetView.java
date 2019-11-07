package im.threads.internal.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import java.util.ArrayList;
import java.util.Arrays;

import im.threads.R;
import im.threads.internal.utils.ViewUtils;

public final class BottomSheetView extends LinearLayout {
    private ButtonsListener buttonsListener;
    private Button camera;
    private Button file;
    private Button gallery;
    private Button selfie;
    private Button send;
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
        selfie = findViewById(R.id.selfie);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(selfie, R.drawable.ic_camera_front_42dp, ViewUtils.DrawablePosition.TOP);
        selfie.setOnClickListener(v -> {
            if (buttonsListener != null) {
                buttonsListener.onSelfieClick();
            }
        });
        send = findViewById(R.id.send);
        ViewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(send, R.drawable.ic_send_blue_42dp, ViewUtils.DrawablePosition.TOP);
        send.setOnClickListener(v -> {
            if (buttonsListener != null) {
                buttonsListener.onSendClick();
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
        selfie.setTextColor(color);
        send.setTextColor(color);
        drawables.addAll(Arrays.asList(file.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(camera.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(gallery.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(selfie.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(send.getCompoundDrawables()));
        for (Drawable d : drawables) {
            if (d != null) {
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void setButtonsListener(ButtonsListener listener) {
        this.buttonsListener = listener;
    }

    public void showMainItemList() {
        if (send.getVisibility() == VISIBLE) {
            send.animate()
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            send.setVisibility(GONE);
                            animateShow(file);
                            animateShow(camera);
                            animateShow(gallery);
                            animateShow(selfie);
                        }
                    });
        }
    }

    public void showSend() {
        if (send.getVisibility() == GONE) {
            animateHide(camera);
            animateHide(gallery);
            animateHide(selfie);
            file.animate()
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            file.setVisibility(GONE);
                            animateShow(send);
                        }
                    });
        }
    }

    private static void animateShow(View view) {
        view.setVisibility(VISIBLE);
        view.setAlpha(0.0f);
        view.animate()
                .alpha(1.0f)
                .setListener(null);
    }

    private static void animateHide(View view) {
        view.animate()
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public interface ButtonsListener {
        void onCameraClick();

        void onGalleryClick();

        void onFilePickerClick();

        void onSelfieClick();

        void onSendClick();
    }
}
