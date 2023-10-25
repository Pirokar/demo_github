package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ui.ChatStyle;
import im.threads.ui.widget.BoldCustomFontTextView;
import im.threads.ui.config.Config;

public final class PlaceholderTitleTextView extends BoldCustomFontTextView {

    public PlaceholderTitleTextView(Context context) {
        super(context);
    }

    public PlaceholderTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.placeholderTitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.placeholderTitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
