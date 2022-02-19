package im.threads.internal.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import im.threads.internal.useractivity.LastUserActivityTimeCounter;
import im.threads.internal.useractivity.LastUserActivityTimeCounterSingletonProvider;

/**
 * Родитель для всех Activity библиотеки
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }

    @ColorInt
    protected int getColorInt(@ColorRes int color) {
        return ContextCompat.getColor(this, color);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LastUserActivityTimeCounter timeCounter = LastUserActivityTimeCounterSingletonProvider
                .INSTANCE.getLastUserActivityTimeCounter();
        if (MotionEvent.ACTION_DOWN == ev.getAction()) {
            timeCounter.updateLastUserActivityTime();
        }
        return super.dispatchTouchEvent(ev);
    }
}
