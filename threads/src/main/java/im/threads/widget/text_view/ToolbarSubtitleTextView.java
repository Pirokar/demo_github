package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.widget.CustomFontTextView;

/**
 * Created by Vit on 10.07.2017.
 */

public class ToolbarSubtitleTextView extends CustomFontTextView {
    public ToolbarSubtitleTextView(Context context) {
        super(context);
    }

    public ToolbarSubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.toolbarSubtitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.toolbarSubtitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
