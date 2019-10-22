package im.threads.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import im.threads.R;
import im.threads.internal.activities.BaseActivity;

/**
 * Вся логика находится во фрагменте. Смотрите {@link ChatFragment}
 */
public final class ChatActivity extends BaseActivity {

    private ChatFragment chatFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_activity);
        chatFragment = ChatFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.chat_frame_layout, chatFragment, "chatFragment")
                .commit();
    }

    @Override
    public void onBackPressed() {
        boolean needsCloseChat = chatFragment.onBackPressed();
        if (needsCloseChat) {
            super.onBackPressed();
        }
    }
}
