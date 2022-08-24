package im.threads.internal.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.config.BaseConfig;
import im.threads.ui.Config;

public class LightCustomFontTextView extends androidx.appcompat.widget.AppCompatTextView {

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
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.defaultFontLight)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontLight));
        }
    }
}
