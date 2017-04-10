package im.threads.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import im.threads.R;
import im.threads.controllers.ChatController;
import im.threads.fragments.ChatFragment;
import im.threads.model.ChatStyle;


/**
 *
 */
public class ChatActivity extends BaseActivity {

    private ChatFragment chatFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_activity);
        chatFragment = ChatFragment.newInstance(getIntent().getBundleExtra(ChatStyle.CHAT_FRAGMENT_BUNDLE));
        FragmentManager fragmentManager = getFragmentManager();
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
