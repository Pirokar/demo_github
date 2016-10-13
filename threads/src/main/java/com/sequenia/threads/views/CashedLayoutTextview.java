package com.sequenia.threads.views;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by yuri on 07.10.2016.
 */

public class CashedLayoutTextView extends TextView {
    private static final String TAG = "CashedLayoutTextView ";
    public Layout lastLayout;

    public CashedLayoutTextView(Context context) {
        super(context);
    }

    public CashedLayoutTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CashedLayoutTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lastLayout = getLayout();
    }
}
