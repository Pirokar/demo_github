package im.threads.ui.activities

import android.os.Bundle
import im.threads.R
import im.threads.ui.fragments.ChatFragment

/**
 * Вся логика находится во фрагменте [ChatFragment]
 */
class ChatActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_activity)
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag("frag_chat") as? ChatFragment)?.let { chatFragment ->
            if (chatFragment.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }
}
