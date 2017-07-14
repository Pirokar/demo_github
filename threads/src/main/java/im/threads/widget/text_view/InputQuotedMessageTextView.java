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

public class InputQuotedMessageTextView extends CustomFontTextView {
    public InputQuotedMessageTextView(Context context) {
        super(context);
    }

    public InputQuotedMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = PrefUtils.getIncomingStyle(context);
        if (style != null) {
            if (!TextUtils.isEmpty(style.inputQuotedMessageFont)) {
                setTypeface(Typeface.createFromAsset(context.getAssets(), style.inputQuotedMessageFont));
            } else {
                super.setTypefaceView(context);
            }
        }
    }
}