package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ui.ChatStyle;
import im.threads.ui.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class ToolbarSubtitleTextView extends CustomFontTextView {

    public ToolbarSubtitleTextView(Context context) {
        super(context);
    }

    public ToolbarSubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.toolbarSubtitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.toolbarSubtitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}