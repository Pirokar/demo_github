package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.business.config.BaseConfig;
import im.threads.internal.widget.LightCustomFontTextView;
import im.threads.ui.config.Config;

public final class QuoteMessageTextView extends LightCustomFontTextView {

    public QuoteMessageTextView(Context context) {
        super(context);
    }

    public QuoteMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.quoteMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
