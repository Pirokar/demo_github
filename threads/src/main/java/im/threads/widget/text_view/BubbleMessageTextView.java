package im.threads.widget.text_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.widget.CustomFontTextView;

/**
 * Created by Vit on 13.07.2017.
 */

public class BubbleMessageTextView extends CustomFontTextView {

    private String lastLinePadding = "";

    public BubbleMessageTextView(Context context) {
        super(context);
        init(null);
    }

    public BubbleMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            final TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                    attributeSet,
                    R.styleable.BubbleMessageTextView,
                    0, 0);
            try {
                int lastLinePaddingSymbols = ta.getInt(R.styleable.BubbleMessageTextView_last_line_padding_symbols, 0);
                StringBuilder paddingBuilder = new StringBuilder();
                for (int i = 0; i < lastLinePaddingSymbols; ++i) {
                    paddingBuilder.append("_");
                }
                lastLinePadding = paddingBuilder.toString();
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = ChatStyle.getInstance();
        if (!TextUtils.isEmpty(style.bubbleMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.bubbleMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }

    @Override
    public boolean onPreDraw() {
        String originalString = getText().toString();
        if (!TextUtils.isEmpty(originalString) && !originalString.endsWith(lastLinePadding)) {
            Spannable lastLineSpan = new SpannableString(lastLinePadding);
            lastLineSpan.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), 0, lastLineSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            append(lastLineSpan);
            return false;
        }
        return true;
    }

}