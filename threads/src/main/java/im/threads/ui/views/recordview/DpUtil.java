package im.threads.ui.views.recordview;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

//convert from/to DP
// TODO: replace it with current system
public class DpUtil {
    public static float toPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float toDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
