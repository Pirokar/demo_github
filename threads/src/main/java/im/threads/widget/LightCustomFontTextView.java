package im.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

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

    public void setTypefaceView(Context context) {
        ChatStyle style = PrefUtils.getIncomingStyle(context);
        if (style != null && !TextUtils.isEmpty(style.defaultFontLight)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontLight));
            //setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-light.ttf"));
        }

    }
}
