package im.threads.internal.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.ui.config.Config;

public class CustomFontTextView extends androidx.appcompat.widget.AppCompatTextView {

    public CustomFontTextView(Context context) {
        super(context);
        init(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        setTypefaceView(context);
    }

    public void setTypefaceView(Context context) {
        ChatStyle style = getChatStyle();
        if (!TextUtils.isEmpty(style.defaultFontRegular)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontRegular));
        }
    }

    private ChatStyle getChatStyle() {
        try {
            return ((Config)BaseConfig.instance).getChatStyle();
        } catch (NullPointerException exc) {
            return new ChatStyle();
        }
    }
}
