package io.edna.threads.demo.kaspressoSreens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import im.threads.R
import im.threads.ui.fragments.ChatFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ChatMainScreen : KScreen<ChatMainScreen>() {
    override val layoutId: Int = R.layout.ecc_fragment_chat
    override val viewClass: Class<*> = ChatFragment::class.java

    val progressBar = KImageView { withId(R.id.progress_bar) }
    val progressBarText = KTextView { withId(R.id.tv_empty_state_hint) }
    val emptyStateLayout = KView { withId(R.id.fl_empty) }
    val inputEditView = KEditText { withId(R.id.input_edit_view) }
    val welcomeScreen = KView { withId(R.id.welcome) }
    val sendMessageBtn = KImageView { withId(R.id.send_message) }
    val errorImage = KImageView { withId(R.id.errorImage) }
    val errorText = KTextView { withId(R.id.errorMessage) }
    val errorRetryBtn = KButton { withId(R.id.retryInitChatBtn) }

    val recyclerView = KRecyclerView(
        builder = { withId(R.id.recycler) },
        itemTypeBuilder = { itemType(ChatMainScreen::ChatRecyclerItem) }
    )

    class ChatRecyclerItem(matcher: Matcher<View>) : KRecyclerItem<ChatRecyclerItem>(matcher) {
        val itemText = KTextView(matcher) { withId(R.id.text) }
        val itemTime = KTextView(matcher) { withId(R.id.timeStamp) }
    }
}