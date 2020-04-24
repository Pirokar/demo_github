package im.threads.internal.widget.text_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.span.MDImageSpan;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.utils.MarkdownParser;
import im.threads.internal.widget.CustomFontTextView;

public final class BubbleMessageTextView extends CustomFontTextView {

    private static final Spanned SPACE = Html.fromHtml("&#160;");

    private boolean mHasImageInText;
    private String lastLinePadding = "";
    private MarkdownProcessor markdownProcessor;

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

    @Override
    public void setText(CharSequence text, BufferType type) {
        String originalString = text.toString();
        if (!TextUtils.isEmpty(originalString) && !TextUtils.isEmpty(lastLinePadding) && !originalString.endsWith(lastLinePadding)) {
            text = new StringBuilder(originalString).append(lastLinePadding);
        }
        if (markdownProcessor != null) {
            text = MarkdownParser.INSTANCE.parse(text.toString(), markdownProcessor);
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
