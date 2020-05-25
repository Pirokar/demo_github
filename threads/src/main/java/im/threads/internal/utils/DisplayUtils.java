package im.threads.internal.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DisplayUtils {

    private DisplayUtils() {
    }

    public static int getDisplayHeight(final @NonNull Context context) {
        final Display display = getDisplay(context);
        if (display != null) {
            final Point size = new Point();
            display.getSize(size);
            return size.y;
        }
        return 0;
    }

    public static int getDisplayWidth(final @NonNull Context context) {
        final Display display = getDisplay(context);
        if (display != null) {
            final Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return 0;
    }

    @Nullable
    private static Display getDisplay(final @NonNull Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm == null ? null : wm.getDefaultDisplay();
    }
}
