package im.threads.internal.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.widget.BoldCustomFontTextView;

public final class PlaceholderTitleTextView extends BoldCustomFontTextView {

    public PlaceholderTitleTextView(Context context) {
        super(context);
    }

    public PlaceholderTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.placeholderTitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.placeholderTitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
