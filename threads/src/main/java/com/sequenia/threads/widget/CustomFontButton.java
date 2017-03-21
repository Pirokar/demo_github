package com.sequenia.threads.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Admin on 20.03.2017.
 */

public class CustomFontButton extends android.support.v7.widget.AppCompatButton {
    public CustomFontButton(Context context) {
        super(context);

        if (isInEditMode()) {
            return;
        }

        setTypefaceView(context);
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
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
