package im.threads.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import im.threads.R;

/**
 * Created by yuri on 07.06.2016.
 * layout/view_typing.xml
 */
public class ViewTypingInProgress extends LinearLayout {
    private static final String TAG = "ViewTypingInProgress ";
    Handler h = new Handler(Looper.getMainLooper());
    View v1, v2, v3;

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
        v1 = findViewById(R.id.first);
        v2 = findViewById(R.id.second);
        v3 = findViewById(R.id.third);
    }
    public void setColor(@ColorRes int color){
        v1.getBackground().setColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_ATOP);
        v2.getBackground().setColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_ATOP);
        v3.getBackground().setColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_ATOP);
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
                                if (getVisibility() == GONE) removeAnimation();
                                animateViews(v1, v2, v3);
                            }
                        });
                    }
                });
            }
        });
    }

    public void removeAnimation() {
        v1.clearAnimation();
        v1.animate().scaleX(1f).scaleY(1f).cancel();
        v2.clearAnimation();
        v2.animate().scaleX(1f).scaleY(1f).cancel();
        v3.clearAnimation();
        v3.animate().scaleX(1f).scaleY(1f).cancel();
    }

    public void animateViews() {
        v1.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
        v2.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
        v3.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
        h.post(new Runnable() {
            @Override
            public void run() {
                animateViews(v1, v2, v3);
            }
        });
    }
}