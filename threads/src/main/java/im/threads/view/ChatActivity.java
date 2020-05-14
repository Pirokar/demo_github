package im.threads.view;

import android.os.Bundle;

import androidx.annotation.Nullable;

import im.threads.R;
import im.threads.internal.activities.BaseActivity;

/**
 * Вся логика находится во фрагменте. Смотрите {@link ChatFragment}
 */
public final class ChatActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
    }

    @Override
    public void onBackPressed() {
        final ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag("frag_chat");
        if (chatFragment != null && chatFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
