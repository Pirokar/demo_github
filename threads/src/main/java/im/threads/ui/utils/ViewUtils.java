package im.threads.ui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.appcompat.content.res.AppCompatResources;

public final class ViewUtils {

    public void setClickListener(ViewGroup viewGroup, View.OnClickListener listener) {
        viewGroup.setOnClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) != null) {
                viewGroup.getChildAt(i).setOnClickListener(listener);
            }
        }
    }

    public void setClickListener(ViewGroup viewGroup, View.OnLongClickListener listener) {
        viewGroup.setOnLongClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) != null) {
                viewGroup.getChildAt(i).setOnLongClickListener(listener);
            }
        }
    }

    public void removeClickListener(ViewGroup viewGroup) {
        viewGroup.setOnLongClickListener(null);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                removeClickListener((ViewGroup) viewGroup.getChildAt(i));
            } else if (viewGroup.getChildAt(i) != null) {
                viewGroup.getChildAt(i).setOnLongClickListener(null);
            }
        }
    }

    public void setCompoundDrawablesWithIntrinsicBoundsCompat(TextView tv, @DrawableRes int drawableId, @DrawablePosition int drawablePosition) {
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
