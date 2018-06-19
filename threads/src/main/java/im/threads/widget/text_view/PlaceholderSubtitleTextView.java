package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.widget.CustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class PlaceholderSubtitleTextView extends CustomFontTextView {
    public PlaceholderSubtitleTextView(Context context) {
        super(context);
    }

    public PlaceholderSubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.placeholderSubtitleFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.placeholderSubtitleFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}