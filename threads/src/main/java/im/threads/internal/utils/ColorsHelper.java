package im.threads.internal.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.BoolRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;

import im.threads.R;

public final class ColorsHelper {

    public static void setStatusBarColor(Activity activity,
                                         @ColorRes int colorResId,
                                         @BoolRes int isLightResId) {
        if (colorResId != 0 && activity != null) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));
            if (activity.getResources().getBoolean(isLightResId)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView()
                            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
    }

    public static void setDrawableColor(@Nullable Context context,
                                        @Nullable Drawable drawable,
                                        @ColorRes int colorResId) {
        if (drawable != null && context != null) {
            if (colorResId == 0) {
                DrawableCompat.clearColorFilter(drawable);
            } else {
                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorResId));
            }
        }
    }

    public static void setBackgroundColor(Context context, View view, @ColorRes int colorResId) {
        if (colorResId != 0 && view != null && context != null) {
            view.setBackgroundColor(ContextCompat.getColor(context, colorResId));
        }
    }

    public static void setTextColor(final @NonNull TextView textView, final @ColorRes int colorResId) {
        setTextColor(textView.getContext(), textView, colorResId);
    }

    public static void setTextColor(Context context, TextView textView, @ColorRes int colorResId) {
        if (colorResId != 0 && textView != null && context != null) {
            textView.setTextColor(ContextCompat.getColor(context, colorResId));
        }
    }

    public static void setHintTextColor(Context context, TextView textView, @ColorRes int colorResId) {
        if (colorResId != 0 && textView != null && context != null) {
            textView.setHintTextColor(ContextCompat.getColor(context, colorResId));
        }
    }

    public static void setTint(@Nullable Context context,
                               @Nullable ImageView view,
                               int colorResId) {
        if (view != null && context != null) {
            if (colorResId == 0) {
                view.clearColorFilter();
            } else {
                view.setColorFilter(ContextCompat.getColor(context, colorResId));
            }
        }
    }

    public static ColorStateList getColorStateList(Context context,
                                                   @ColorRes int disabledColorResId,
                                                   @ColorRes int enabledColorResId,
                                                   @ColorRes int pressedColorResId) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled}
                },
                new int[]{
                        ContextCompat.getColor(context, pressedColorResId),
                        ContextCompat.getColor(context, enabledColorResId),
                        ContextCompat.getColor(context, disabledColorResId)
                }
        );
    }

    public static ColorStateList getSimpleColorStateList(Context context, @ColorRes int colorResId) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled}
                },
                new int[]{
                        ContextCompat.getColor(context, colorResId),
                        ContextCompat.getColor(context, colorResId),
                        ContextCompat.getColor(context, R.color.threads_grey_aaa)
                }
        );
    }

    public static void setTintColorStateList(@Nullable ImageView view, ColorStateList colorStateList) {
        ImageViewCompat.setImageTintList(view, colorStateList);
    }

}