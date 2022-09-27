package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.ui.widget.CustomFontTextView;
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
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.inputQuotedMessageAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.inputQuotedMessageAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
