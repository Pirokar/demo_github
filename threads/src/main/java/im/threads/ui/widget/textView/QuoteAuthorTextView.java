package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ui.ChatStyle;
import im.threads.ui.widget.BoldCustomFontTextView;
import im.threads.ui.config.Config;

public final class QuoteAuthorTextView extends BoldCustomFontTextView {

    public QuoteAuthorTextView(Context context) {
        super(context);
    }

    public QuoteAuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.quoteAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
