package im.threads.internal.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;

public final class QuoteTimeTextView extends CustomFontTextView {

    public QuoteTimeTextView(Context context) {
        super(context);
    }

    public QuoteTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.quoteTimeFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteTimeFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
