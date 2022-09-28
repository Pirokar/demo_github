package im.threads.ui.widget.textView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ui.ChatStyle;
import im.threads.ui.widget.CustomFontTextView;
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
