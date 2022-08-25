package im.threads.internal.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;
import im.threads.ChatStyle;
import im.threads.business.config.BaseConfig;
import im.threads.ui.config.Config;

public final class CustomFontButton extends AppCompatButton {
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

    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.defaultFontRegular)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontRegular));
        }
    }
}
