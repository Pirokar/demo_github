package im.threads.internal.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.widget.BoldCustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class QuoteAuthorTextView extends BoldCustomFontTextView {
    public QuoteAuthorTextView(Context context) {
        super(context);
    }

    public QuoteAuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.quoteAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}