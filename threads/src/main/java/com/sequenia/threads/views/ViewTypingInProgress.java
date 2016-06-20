package com.sequenia.threads.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sequenia.threads.R;

/**
 * Created by yuri on 07.06.2016.
 * layout/view_typing.xml
 */
public class ViewTypingInProgress extends LinearLayout {
    public ViewTypingInProgress(Context context) {
        super(context);
        init();
    }

    public ViewTypingInProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewTypingInProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_typing, this, true);
        final ImageView iv1 = (ImageView) findViewById(R.id.first);
        final ImageView iv2 = (ImageView) findViewById(R.id.second);
        final ImageView iv3 = (ImageView) findViewById(R.id.third);
        animateViews(iv1, iv2, iv3);
    }

    private void animateViews(final View v1, final View v2, final View v3) {
        final int duration = 500;
        v1.animate().scaleX(1.5f).scaleY(1.5f).setDuration(duration).withEndAction(new Runnable() {
            @Override
            public void run() {
                v1.animate().scaleX((1f / 1.5f)).scaleY((1f / 1.5f)).setDuration(duration);
                v2.animate().scaleX(1.5f).scaleY(1.5f).setDuration(duration).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v2.animate().scaleX((1f / 1.5f)).scaleY((1f / 1.5f)).setDuration(duration);
                        v3.animate().scaleX(1.5f).scaleY(1.5f).setDuration(duration).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v3.animate().scaleX((1f / 1.5f)).scaleY((1f / 1.5f)).setDuration(duration);
                                animateViews(v1, v2, v3);
                            }
                        });
                    }
                });
            }
        });
    }
}