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

public class CustomFontButton extends android.support.v7.widget.AppCompatButton {
    public CustomFontButton(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public void setTypefaceView(Context context){
        ChatStyle style = PrefUtils.getIncomingStyle(context);
        if (style != null && !TextUtils.isEmpty(style.defaultFontRegular)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontRegular));
            //setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-regular.ttf"));
        }

    }
}
