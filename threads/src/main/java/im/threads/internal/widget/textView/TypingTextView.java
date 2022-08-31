package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class TypingTextView extends CustomFontTextView {
    public TypingTextView(Context context) {
        super(context);
    }

    public TypingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.typingFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.typingFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}