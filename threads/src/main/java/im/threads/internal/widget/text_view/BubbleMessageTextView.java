package im.threads.internal.widget.text_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.widget.CustomFontTextView;

public final class BubbleMessageTextView extends CustomFontTextView {

    private static final Spanned SPACE = Html.fromHtml("&#160;");

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
                if (lastLinePaddingSymbols > 0) {
                    StringBuilder paddingBuilder = new StringBuilder(" ");
                    for (int i = 0; i < lastLinePaddingSymbols; ++i) {
                        paddingBuilder.append(SPACE);
                    }
                    lastLinePadding = paddingBuilder.toString();
                }
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.bubbleMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.bubbleMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String originalString = text.toString();
        if (!TextUtils.isEmpty(originalString) && !TextUtils.isEmpty(lastLinePadding) && !originalString.endsWith(lastLinePadding)) {
            super.setText(new StringBuilder(originalString).append(lastLinePadding), type);
        } else {
            super.setText(text, type);
        }
    }
}
