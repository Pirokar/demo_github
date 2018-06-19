package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.widget.LightCustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class QuoteMessageTextView extends LightCustomFontTextView {
    public QuoteMessageTextView(Context context) {
        super(context);
    }

    public QuoteMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.quoteMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.quoteMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}