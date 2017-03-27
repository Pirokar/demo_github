package im.threads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import im.threads.R;

/**
 * Created by yuri on 03.11.2016.
 */

public class MyTextView extends View {
    private TextPaint mTextPaint;
    private String mText;

    public MyTextView(Context context) {
        super(context);
        init(context, null);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attributeSet) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(14f);
        mText = "";
        if (attributeSet != null) {
            TypedArray a = ctx.obtainStyledAttributes(attributeSet, R.styleable.MyTextView);
            int textSize = a.getInt(R.styleable.MyTextView_textSize, 14);
            mTextPaint.setTextSize(textSize);
            int color = a.getColor(R.styleable.MyTextView_textColor, Color.BLACK);
            mTextPaint.setColor(color);
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}

