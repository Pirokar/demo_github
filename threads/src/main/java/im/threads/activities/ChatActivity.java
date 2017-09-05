package im.threads.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import im.threads.R;
import im.threads.fragments.ChatFragment;
import im.threads.model.ChatStyle;


/**
 * Вся логика находится во фрагменте. Смотрите ChatFragment
 */
public class ChatActivity extends BaseActivity {

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
    protected void setActivityStyle(ChatStyle style) {

    }

    @Override
    public void onBackPressed() {
        boolean needsCloseChat = chatFragment.onBackPressed();
        if(needsCloseChat) {
            super.onBackPressed();
        }
    }

}
