package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.widget.BoldCustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class PlaceholderTitleTextView extends BoldCustomFontTextView {
    public PlaceholderTitleTextView(Context context) {
        super(context);
    }

    public PlaceholderTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.placeholderTitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.placeholderTitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}