package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.config.BaseConfig;
import im.threads.internal.widget.BoldCustomFontTextView;
import im.threads.ui.Config;

public final class MessageHeaderTextView extends BoldCustomFontTextView {

    public MessageHeaderTextView(Context context) {
        super(context);
    }

    public MessageHeaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (!TextUtils.isEmpty(style.messageHeaderFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.messageHeaderFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
