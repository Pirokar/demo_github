package io.edna.threads.demo.kaspressoSreens

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.withId
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
    val sendImageBtn = KButton { withId(R.id.send) }
    val errorImage = KImageView { withId(R.id.errorImage) }
    val errorText = KTextView { withId(R.id.errorMessage) }
    val errorRetryBtn = KButton { withId(R.id.retryInitChatBtn) }
    val replyBtn = KImageView { withId(R.id.reply) }
    val copyBtn = KImageView { withId(R.id.content_copy) }
    val quoteText = KTextView { withId(R.id.quote_text) }
    val quoteHeader = KTextView { withId(R.id.quote_header) }
    val quoteImage = KImageView { withId(R.id.quote_image) }
    val quoteClear = KImageView { withId(R.id.quote_clear) }
    val addAttachmentBtn = KImageView { withId(R.id.add_attachment) }

    val chatItemsRecyclerView = KRecyclerView(
        builder = { withId(R.id.chatItemsRecycler) },
        itemTypeBuilder = { itemType(::ChatRecyclerItem) }
    )

    val bottomGalleryRecycler = KRecyclerView(
        builder = { withId(R.id.bottom_gallery_recycler) },
        itemTypeBuilder = { itemType(::BottomGalleryItem) }
    )

    class ChatRecyclerItem(matcher: Matcher<View>) : KRecyclerItem<ChatRecyclerItem>(matcher) {
        val itemText = KTextView(matcher) { withId(R.id.text) }
        val itemTime = KTextView(matcher) { withId(R.id.timeStamp) }
    }

    class BottomGalleryItem(matcher: Matcher<View>) : KRecyclerItem<BottomGalleryItem>(matcher)
}