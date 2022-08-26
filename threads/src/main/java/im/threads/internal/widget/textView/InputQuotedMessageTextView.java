package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class InputQuotedMessageTextView extends CustomFontTextView {

    public InputQuotedMessageTextView(Context context) {
        super(context);
    }

    public InputQuotedMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.inputQuotedMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.inputQuotedMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
