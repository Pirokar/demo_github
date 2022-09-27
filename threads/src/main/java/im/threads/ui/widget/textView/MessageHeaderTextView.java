package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.ui.widget.BoldCustomFontTextView;
import im.threads.ui.config.Config;

public final class MessageHeaderTextView extends BoldCustomFontTextView {

    public MessageHeaderTextView(Context context) {
        super(context);
    }

    public MessageHeaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.messageHeaderFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.messageHeaderFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
