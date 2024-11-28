package edna.chatcenter.demo.kaspressoSreens

import com.kaspersky.kaspresso.screens.KScreen
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.fragments.StartChatFragment
import io.github.kakaocup.kakao.text.KButton

object StartChatScreen : KScreen<StartChatScreen>() {
    override val layoutId: Int = R.layout.fragment_start_chat
    override val viewClass: Class<*> = StartChatFragment::class.java

    val openChatButton = KButton { withId(R.id.goToChat) }
}
