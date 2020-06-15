package im.threads.internal.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class ColorsHelper {

    public static void setStatusBarColor(Activity activity, @ColorRes int colorResId) {
        if (colorResId != 0 && Build.VERSION.SDK_INT > 20) {
            if (activity != null) {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));
            }
        }
    }

    public static void setDrawableColor(Context context, Drawable drawable, @ColorRes int colorResId) {
        if (colorResId != 0 && drawable != null && context != null) {
            drawable.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_ATOP);
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

    public static void setTint(Context context, ImageView view, int colorResId) {
        if (colorResId != 0 && view != null && context != null) {
            view.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_ATOP);
        }
    }
}
