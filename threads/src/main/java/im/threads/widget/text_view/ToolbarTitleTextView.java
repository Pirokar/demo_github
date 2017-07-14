package im.threads.widget.text_view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;
import im.threads.widget.CustomFontTextView;

/**
 * Created by Vit on 10.07.2017.
 *
 */

public class ToolbarTitleTextView extends CustomFontTextView {
    public ToolbarTitleTextView(Context context) {
        super(context);
    }

    public ToolbarTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = PrefUtils.getIncomingStyle(context);
        if (style != null) {
            if (!TextUtils.isEmpty(style.toolbarTitleFont)) {
                setTypeface(Typeface.createFromAsset(context.getAssets(), style.toolbarTitleFont));
            } else {
                super.setTypefaceView(context);
            }
        }
    }
}
