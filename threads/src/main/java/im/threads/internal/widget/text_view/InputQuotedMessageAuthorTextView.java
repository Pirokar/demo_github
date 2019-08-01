package im.threads.internal.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class InputQuotedMessageAuthorTextView extends CustomFontTextView {
    public InputQuotedMessageAuthorTextView(Context context) {
        super(context);
    }

    public InputQuotedMessageAuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.inputQuotedMessageAuthorFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.inputQuotedMessageAuthorFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}