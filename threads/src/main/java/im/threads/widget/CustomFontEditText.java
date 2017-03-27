package im.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Admin on 20.03.2017.
 */

public class CustomFontEditText extends android.support.v7.widget.AppCompatEditText {
    public CustomFontEditText(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public void setTypefaceView(Context context){
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-regular.ttf"));
    }
}
