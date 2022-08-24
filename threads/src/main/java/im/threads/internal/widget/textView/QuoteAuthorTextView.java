package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.config.BaseConfig;
import im.threads.internal.widget.BoldCustomFontTextView;
import im.threads.ui.Config;

public final class QuoteAuthorTextView extends BoldCustomFontTextView {

    public QuoteAuthorTextView(Context context) {
        super(context);
    }

    public QuoteAuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.quoteAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
