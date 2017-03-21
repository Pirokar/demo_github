package com.sequenia.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

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
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato-regular.ttf"));
    }
}
