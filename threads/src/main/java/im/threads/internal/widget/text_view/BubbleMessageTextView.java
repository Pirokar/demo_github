package im.threads.internal.widget.text_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.widget.CustomFontTextView;

public final class BubbleMessageTextView extends CustomFontTextView {

    private static final Spanned SPACE = Html.fromHtml("&#160;");

    private boolean mHasImageInText;
    private String lastLinePadding = "";
    private float lastLineExtraPaddingSymbolsCount = 0;

    public BubbleMessageTextView(Context context) {
        super(context);
    }

    public BubbleMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        fetchLastLinePadding(context, attrs);
    }

    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.bubbleMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.bubbleMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }

    public void bindTimestampView(BubbleTimeTextView timeTextView) {
        timeTextView.measure(0, 0);
        int timeWidth = timeTextView.getMeasuredWidth() * 2;

        StringBuilder paddingBuilder = new StringBuilder(" ");

        Rect bounds = new Rect();
        Paint textPaint = getPaint();
        int width = 0;
        while (width < timeWidth) {
            paddingBuilder.append("_");
            textPaint.getTextBounds(paddingBuilder.toString(), 0, paddingBuilder.toString().length(), bounds);
            width = bounds.width();
        }

        for (int i = 0; i < lastLineExtraPaddingSymbolsCount; i++) {
            paddingBuilder.append("_");
        }

        lastLinePadding = paddingBuilder.toString().replace("_", SPACE);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(lastLinePadding)) {
            text = text.toString().replaceAll(SPACE.toString(), " ");
            text = text.toString().trim();
            text = new SpannableStringBuilder(text).append(lastLinePadding);
        }
        if (mHasImageInText) {
            mHasImageInText = false;
        }
        super.setText(text, type);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (mHasImageInText) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    private void fetchLastLinePadding(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleMessageTextView);
        try {
            lastLineExtraPaddingSymbolsCount = typedArray.getFloat(
                    R.styleable.BubbleMessageTextView_last_line_extra_padding_symbols,
                    0
            );
        } finally {
            typedArray.recycle();
        }
    }

}
