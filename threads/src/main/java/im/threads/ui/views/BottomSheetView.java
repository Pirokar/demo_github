package im.threads.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.ui.config.Config;
import im.threads.ui.utils.ColorsHelper;
import im.threads.ui.utils.ViewUtils;

public final class BottomSheetView extends LinearLayout {
    private ButtonsListener buttonsListener;
    private Button camera;
    private Button file;
    private Button gallery;
    private Button send;
    private ChatStyle chatStyle = Config.getInstance().getChatStyle();
    private ViewUtils viewUtils = new ViewUtils();

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

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ecc_view_bottom_attachment_sheet, this, true);
        camera = findViewById(R.id.camera);
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(camera,
                chatStyle.attachmentCameraIconResId, ViewUtils.DrawablePosition.TOP);
        camera.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onCameraClick();
        });
        gallery = findViewById(R.id.gallery);
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(gallery,
                chatStyle.attachmentGalleryIconResId, ViewUtils.DrawablePosition.TOP);
        gallery.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onGalleryClick();
        });
        file = findViewById(R.id.file);
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(file,
                chatStyle.attachmentFileIconResId, ViewUtils.DrawablePosition.TOP);
        file.setOnClickListener(v -> {
            if (null != buttonsListener) buttonsListener.onFilePickerClick();
        });
        send = findViewById(R.id.send);
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(send,
                chatStyle.attachmentSendIconResId, ViewUtils.DrawablePosition.TOP);
        send.setOnClickListener(v -> {
            if (buttonsListener != null) {
                buttonsListener.onSendClick();
            }
        });
        this.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
    }

    public void setButtonsTint(@ColorRes int colorRes) {
        int textColor;
        if (colorRes == 0) {
            textColor = chatStyle.inputTextColor;
        } else {
            textColor = colorRes;
        }
        int color = ContextCompat.getColor(getContext(), textColor);
        file.setTextColor(color);
        camera.setTextColor(color);
        gallery.setTextColor(color);
        send.setTextColor(color);
        ArrayList<Drawable> drawables = new ArrayList<>();
        drawables.addAll(Arrays.asList(file.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(camera.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(gallery.getCompoundDrawables()));
        drawables.addAll(Arrays.asList(send.getCompoundDrawables()));
        for (Drawable drawable : drawables) {
            ColorsHelper.setDrawableColor(file.getContext(), drawable, colorRes);
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
                        }
                    });
        }
    }

    public void showSend() {
        if (send.getVisibility() == GONE) {
            animateHide(camera);
            animateHide(gallery);
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

    public interface ButtonsListener {
        void onCameraClick();

        void onGalleryClick();

        void onFilePickerClick();

        void onSendClick();
    }
}
