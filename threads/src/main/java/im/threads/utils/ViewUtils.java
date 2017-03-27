package im.threads.utils;

import android.view.View;
import android.view.ViewGroup;

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
}
