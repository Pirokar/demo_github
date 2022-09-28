package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ui.ChatStyle;
import im.threads.ui.widget.LightCustomFontTextView;
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
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.quoteMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
