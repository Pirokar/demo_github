package im.threads.internal.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;
import im.threads.ui.config.Config;

public final class ScheduleAlertTextView extends CustomFontTextView {

    public ScheduleAlertTextView(Context context) {
        super(context);
    }

    public ScheduleAlertTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.getInstance().getChatStyle();
        if (!TextUtils.isEmpty(style.scheduleAlertFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.scheduleAlertFont));
        } else {
            super.setTypefaceView(context);
        }
    }
}
