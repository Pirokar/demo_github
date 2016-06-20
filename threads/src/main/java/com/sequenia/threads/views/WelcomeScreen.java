package com.sequenia.threads.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.sequenia.threads.R;

/**
 * Created by yuri on 08.06.2016.
 */
public class WelcomeScreen extends FrameLayout {
    public WelcomeScreen(Context context) {
        super(context);
        init();
    }

    public WelcomeScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WelcomeScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_welcome, this, true);
    }

    public void removeViewWithAnimation(int time, @Nullable final Runnable onComplete) {
        final View v = this;
        this.animate().alpha(0.0f).setInterpolator(new AccelerateInterpolator()) .setDuration(time).withEndAction(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) getParent()).removeView(v);
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
