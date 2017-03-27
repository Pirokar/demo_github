package im.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Admin on 20.03.2017.
 */

public class LightCustomFontTextView extends android.support.v7.widget.AppCompatTextView {

    public LightCustomFontTextView(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public LightCustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public void setTypefaceView(Context context){
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-light.ttf"));
    }
}
