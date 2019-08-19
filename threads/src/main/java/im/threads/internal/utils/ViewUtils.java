package im.threads.internal.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ViewUtils {

    private ViewUtils() {
    }

    public static void setClickListener(ViewGroup viewGroup, View.OnClickListener listener) {
        viewGroup.setOnClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) != null) {
                viewGroup.getChildAt(i).setOnClickListener(listener);
            }
        }
    }

    public static void setClickListener(ViewGroup viewGroup, View.OnLongClickListener listener) {
        viewGroup.setOnLongClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) != null) {
                viewGroup.getChildAt(i).setOnLongClickListener(listener);
            }
        }
    }

    public static void setCompoundDrawablesWithIntrinsicBoundsCompat(TextView tv, @DrawableRes int drawableId, @DrawablePosition int drawablePosition) {
        Context context = tv.getContext();
        Drawable drawable;
        drawable = AppCompatResources.getDrawable(context, drawableId);
        tv.setCompoundDrawablesWithIntrinsicBounds(
                drawablePosition == DrawablePosition.LEFT ? drawable : null,
                drawablePosition == DrawablePosition.TOP ? drawable : null,
                drawablePosition == DrawablePosition.RIGHT ? drawable : null,
                drawablePosition == DrawablePosition.BOTTOM ? drawable : null);

    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DrawablePosition.LEFT, DrawablePosition.TOP, DrawablePosition.RIGHT, DrawablePosition.BOTTOM})
    public @interface DrawablePosition {
        int LEFT = 0;
        int TOP = 1;
        int RIGHT = 2;
        int BOTTOM = 3;
    }
}
