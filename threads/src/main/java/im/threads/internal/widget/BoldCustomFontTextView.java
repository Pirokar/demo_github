package im.threads.internal.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.business.config.BaseConfig;
import im.threads.ui.config.Config;

public class BoldCustomFontTextView extends androidx.appcompat.widget.AppCompatTextView {

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

    public void setTypefaceView(Context context) {
        ChatStyle style =((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.defaultFontBold)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontBold));
        }
    }
}
