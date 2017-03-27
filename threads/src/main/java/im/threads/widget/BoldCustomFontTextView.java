package im.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Admin on 20.03.2017.
 */

public class BoldCustomFontTextView extends android.support.v7.widget.AppCompatTextView {

    public BoldCustomFontTextView(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public BoldCustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public void setTypefaceView(Context context){
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-bold.ttf"));
    }
}
