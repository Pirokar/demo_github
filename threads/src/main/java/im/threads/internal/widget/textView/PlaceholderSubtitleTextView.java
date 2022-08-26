package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class PlaceholderSubtitleTextView extends CustomFontTextView {

    public PlaceholderSubtitleTextView(Context context) {
        super(context);
    }

    public PlaceholderSubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.placeholderSubtitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.placeholderSubtitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
