package im.threads.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import im.threads.model.ChatStyle;

/**
 * Родитель для всех Activity библиотеки
 * Created by yuri on 27.09.2016.
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

    protected abstract void setActivityStyle(ChatStyle style);

    protected
    @ColorInt
    int getColorInt(@ColorRes int color) {
        return ContextCompat.getColor(this, color);
    }
}
