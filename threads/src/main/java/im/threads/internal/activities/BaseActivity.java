package im.threads.internal.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

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
}
