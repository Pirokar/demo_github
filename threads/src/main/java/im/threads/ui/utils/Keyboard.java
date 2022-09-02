package im.threads.ui.utils;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

//TODO: replace with extensions
public final class Keyboard {

    private Keyboard() {
    }

    public static void show(final @Nullable Context context, final @Nullable View view, final long delayMills) {
        if (context != null && view != null) {
            new Handler().postDelayed(() -> show(context, view), delayMills);
        }
    }

    public static void hide(final @Nullable Context context, final @Nullable View view, final long delayMills) {
        if (context != null && view != null) {
            new Handler().postDelayed(() -> hide(context, view), delayMills);
        }
    }

    private static void show(final @Nullable Context context, final @Nullable View view) {
        if (context != null && view != null) {
            view.requestFocus();
            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, 0);
            }
        }
    }

    private static void hide(final @Nullable Context context, final @Nullable View view) {
        if (context != null && view != null) {
            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
