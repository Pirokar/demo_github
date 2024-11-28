package edna.chatcenter.demo.appCode.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edna.chatcenter.demo.R
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication
import edna.chatcenter.ui.core.annotation.OpenWay
import edna.chatcenter.ui.visual.core.ChatCenterUI
import edna.chatcenter.ui.visual.fragments.ChatFragment

class ModalChatActivity : AppCompatActivity() {
    private val chatCenterUI: ChatCenterUI?
        get() {
            return (applicationContext as? EdnaChatCenterApplication)?.chatCenterUI
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modal_chat)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.chatFragmentContainer, ChatFragment.newInstance(OpenWay.DEFAULT))
            .commitNow()
    }
}
