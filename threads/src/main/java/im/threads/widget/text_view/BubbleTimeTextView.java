package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;
import im.threads.widget.CustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 *
 */

public class BubbleTimeTextView extends CustomFontTextView {
    public BubbleTimeTextView(Context context) {
        super(context);
    }

    public BubbleTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = PrefUtils.getIncomingStyle(context);
        if (style != null) {
            if (!TextUtils.isEmpty(style.bubbleTimeFont)) {
                setTypeface(Typeface.createFromAsset(context.getAssets(), style.bubbleTimeFont));
            } else {
                super.setTypefaceView(context);
            }
        }
    }
}