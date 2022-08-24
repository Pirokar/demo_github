package im.threads.internal.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.config.BaseConfig;
import im.threads.ui.Config;

public final class CustomFontEditText extends androidx.appcompat.widget.AppCompatEditText {
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

    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.defaultFontRegular)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontRegular));
        }
    }
}
