package im.threads.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuri on 25.08.2016.
 */
public class ViewUtils {
    private static final String TAG = "ViewUtils ";
    private static int charWidth = -1;

    private ViewUtils() {
    }


    public static void setClickListener(ViewGroup viewGroup, View.OnClickListener listener) {
        viewGroup.setOnClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) instanceof View) {
                viewGroup.getChildAt(i).setOnClickListener(listener);
            }
        }
    }

    public static void setClickListener(ViewGroup viewGroup, View.OnLongClickListener listener) {
        viewGroup.setOnLongClickListener(listener);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setClickListener((ViewGroup) viewGroup.getChildAt(i), listener);
            } else if (viewGroup.getChildAt(i) instanceof View) {
                viewGroup.getChildAt(i).setOnLongClickListener(listener);
            }
        }
    }

    public static boolean containsRule(int[] rules, int rule) {
        for (int i : rules) {
            if (i == rule) return true;
        }
        return false;
    }


    public static void setCompoundDrawablesWithIntrinsicBoundsCompat(TextView tv, @DrawableRes int drawableId, @DrawablePosition int drawablePosition) {

        Context context = tv.getContext();
        Drawable drawable;

        drawable = AppCompatResources.getDrawable(context, drawableId);

//        drawable = getVectorDrawableCompat(drawableId, context);

        tv.setCompoundDrawablesWithIntrinsicBounds(
                drawablePosition == DrawablePosition.LEFT ? drawable : null,
                drawablePosition == DrawablePosition.TOP ? drawable : null,
                drawablePosition == DrawablePosition.RIGHT ? drawable : null,
                drawablePosition == DrawablePosition.BOTTOM ? drawable : null);

    }

    public static Drawable getVectorDrawableCompat(@DrawableRes int drawableId, Context context) {

        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = VectorDrawableCompat.create(context.getResources(), drawableId, context.getTheme());
        } else {
            drawable = context.getResources().getDrawable(drawableId, context.getTheme());
        }

        return drawable;
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
