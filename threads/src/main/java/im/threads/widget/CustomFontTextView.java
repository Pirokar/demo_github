package im.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.model.ChatStyle;

/**
 * Created by Admin on 20.03.2017.
 */

public class CustomFontTextView extends android.support.v7.widget.AppCompatTextView {

    public CustomFontTextView(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public void setTypefaceView(Context context){
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.defaultFontRegular)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.defaultFontRegular));
        }
    }
}
