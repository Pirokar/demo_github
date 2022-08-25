package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.business.config.BaseConfig;
import im.threads.internal.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class InputQuotedMessageAuthorTextView extends CustomFontTextView {

    public InputQuotedMessageAuthorTextView(Context context) {
        super(context);
    }

    public InputQuotedMessageAuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();;
        if (!TextUtils.isEmpty(style.inputQuotedMessageAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.inputQuotedMessageAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
