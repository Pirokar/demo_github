package im.threads.internal.widget.textView;

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
import android.widget.TextView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.markdown.MarkdownProcessor;
import im.threads.internal.markdown.MarkwonMarkdownProcessor;
import im.threads.internal.widget.CustomFontTextView;

public final class BubbleMessageTextView extends CustomFontTextView {

    private static final Spanned SPACE = Html.fromHtml("&#160;");

    private boolean mHasImageInText;
    private String lastLinePadding = "";
    private float lastLineExtraPaddingSymbolsCount = 0;

    private MarkdownProcessor markdownProcessor = new MarkwonMarkdownProcessor();

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
        text = addPadding(text);
        if (mHasImageInText) {
            mHasImageInText = false;
        }
        super.setText(text, type);
    }

    public void setFormattedText(CharSequence text, Boolean isOperatorMessage) {
        text = addPadding(text);
        Spanned spannedText = getSpanned(text, isOperatorMessage);
        if (mHasImageInText) {
            mHasImageInText = false;
        }
        super.setText(spannedText, TextView.BufferType.NORMAL);
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

    private CharSequence addPadding(CharSequence text) {
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(lastLinePadding)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            builder = trimEndSpannable(builder);
            text = builder.append(lastLinePadding);
        }
        return text;
    }

    private Spanned getSpanned(CharSequence text, Boolean isOperatorMessage) {
        Spanned spannedText;
        if (isOperatorMessage) {
            spannedText = markdownProcessor.parseOperatorMessage(text.toString());
        } else {
            spannedText = markdownProcessor.parseClientMessage(text.toString());
        }
        return spannedText;
    }

    private SpannableStringBuilder trimEndSpannable(SpannableStringBuilder spannable) {
        if (spannable == null) {
            return null;
        }
        boolean shouldTrim = false;

        int trimStart = spannable.length() - 1;
        int trimEnd = spannable.length() - 1;

        while (spannable.charAt(trimStart) == ' ') {
            shouldTrim = true;
            trimStart--;
        }

        SpannableStringBuilder result;
        if (shouldTrim) {
            result = spannable.delete(trimStart, trimEnd);
        } else {
            result = spannable;
        }
        return result;
    }
}
