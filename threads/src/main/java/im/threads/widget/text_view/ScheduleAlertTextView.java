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

public class ScheduleAlertTextView extends CustomFontTextView {
    public ScheduleAlertTextView(Context context) {
        super(context);
    }

    public ScheduleAlertTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.scheduleAlertFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.scheduleAlertFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}