package io.edna.threads.demo.kaspressoSreens

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kaspersky.kaspresso.screens.KScreen
import im.threads.R
import im.threads.ui.fragments.ChatFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.pager.KViewPager
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
    val quoteClear = KImageView { withId(R.id.quote_clear) }
    val addAttachmentBtn = KImageView { withId(R.id.add_attachment) }
    val imagePager = KViewPager { withId(R.id.pager) }
    val popupMenuButton = KButton { withId(R.id.popup_menu_button) }
    val searchInput = KEditText { withId(R.id.searchInput) }
    val searchClearButton = KImageView { withId(R.id.searchClearButton) }
    val searchProgressBar = KImageView { withId(R.id.searchProgressBar) }
    val toolbarOperatorName = KTextView { withId(R.id.consult_name) }

    val chatItemsRecyclerView = KRecyclerView(
        builder = { withId(R.id.chatItemsRecycler) },
        itemTypeBuilder = { itemType(::ChatRecyclerItem) }
    )

    val bottomGalleryRecycler = KRecyclerView(
        builder = { withId(R.id.bottom_gallery_recycler) },
        itemTypeBuilder = { itemType(::BottomGalleryItem) }
    )

    val searchRecycler = KRecyclerView(
        builder = { withId(R.id.searchResultsListView) },
        itemTypeBuilder = { itemType(::SearchRecyclerItem) }
    )

    class ChatRecyclerItem(matcher: Matcher<View>) : KRecyclerItem<ChatRecyclerItem>(matcher) {
        val itemText = KTextView(matcher) { withId(R.id.text) }
        val itemTime = KTextView(matcher) { withId(R.id.timeStamp) }
        val image = KImageView(matcher) { withId(R.id.image) }
        val star = KImageView(matcher) { withId(R.id.star) }
        val surveyHeader = KTextView(matcher) { withId(R.id.header) }
        val rateStarsCount = KTextView(matcher) { withId(R.id.rate_stars_count) }
        val totalStarsCount = KTextView(matcher) { withId(R.id.total_stars_count) }
        val fromTextSurvey = KTextView(matcher) { withId(R.id.from) }
        val askForRateText = KTextView(matcher) { withId(R.id.ask_for_rate) }
        val thumbUp = KImageView(matcher) { withId(R.id.thumb_up) }
        val thumbDown = KImageView(matcher) { withId(R.id.thumb_down) }
        val ratingStars = KView(matcher) { withId(R.id.mark) }
    }

    class BottomGalleryItem(matcher: Matcher<View>) : KRecyclerItem<BottomGalleryItem>(matcher)

    class SearchRecyclerItem(matcher: Matcher<View>) : KRecyclerItem<SearchRecyclerItem>(matcher) {
        val avatarImage = KImageView(matcher) { withId(R.id.avatarImage) }
        val nameTextView = KTextView(matcher) { withId(R.id.nameTextView) }
        val dateTextView = KTextView(matcher) { withId(R.id.dateTextView) }
        val rightArrowImageView = KImageView(matcher) { withId(R.id.rightArrowImageView) }
        val messageTextView = KTextView(matcher) { withId(R.id.messageTextView) }
        val dividerView = KView(matcher) { withId(R.id.dividerView) }
        val clickableView = KView(matcher) { withId(R.id.clickableView) }
    }
}
