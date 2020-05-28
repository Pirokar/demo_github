package im.threads.internal.widget.text_view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.span.MDImageSpan;

import im.threads.ChatStyle;
import im.threads.internal.Config;
import im.threads.internal.widget.CustomFontTextView;

public final class BubbleMessageTextView extends CustomFontTextView {

    private static final Spanned SPACE = Html.fromHtml("&#160;");

    private boolean mHasImageInText;
    private String lastLinePadding = "";
    private MarkdownProcessor markdownProcessor;

    public BubbleMessageTextView(Context context) {
        super(context);
    }

    public BubbleMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTypefaceView(Context context) {
        ChatStyle style = Config.instance.getChatStyle();
        if (!TextUtils.isEmpty(style.bubbleMessageFont)) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), style.bubbleMessageFont));
        } else {
            super.setTypefaceView(context);
        }
    }

    public void enableMarkdown(MarkdownProcessor processor) {
        markdownProcessor = processor;
    }

    public void bindTimestampView(BubbleTimeTextView timeTextView) {
        timeTextView.measure(0, 0);
        int timeWidth = (int) (timeTextView.getMeasuredWidth() * 1.7);
        StringBuilder paddingBuilder = new StringBuilder(" ");
        Rect bounds = new Rect();
        Paint textPaint = getPaint();
        int width = 0;
        while (width < timeWidth) {
            paddingBuilder.append("_");
            textPaint.getTextBounds(paddingBuilder.toString(), 0, paddingBuilder.toString().length(), bounds);
            width = bounds.width();
        }
        lastLinePadding = paddingBuilder.toString().replace("_", SPACE);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(lastLinePadding)) {
            text = new SpannableStringBuilder(text).append(lastLinePadding);
        }
        if (markdownProcessor != null) {
            if (mHasImageInText) {
                onDetach();
                mHasImageInText = false;
            }
            if (text instanceof Spanned) {
                MDImageSpan[] spans = ((Spanned) text).getSpans(0, text.length(), MDImageSpan.class);
                mHasImageInText = spans.length > 0;
                for (MDImageSpan imageSpans : spans) {
                    imageSpans.onAttach(this);
                }
            }
        }
        super.setText(text, type);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        onDetach();
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (mHasImageInText) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    final void onDetach() {
        MDImageSpan[] images = getImages();
        for (MDImageSpan image : images) {
            Drawable drawable = image.getDrawable();
            if (drawable != null) {
                unscheduleDrawable(drawable);
            }
            image.onDetach();
        }
    }

    private MDImageSpan[] getImages() {
        if (mHasImageInText && length() > 0) {
            return ((Spanned) getText()).getSpans(0, length(), MDImageSpan.class);
        }
        return new MDImageSpan[0];
    }

}
