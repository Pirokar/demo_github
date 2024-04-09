package edna.chatcenter.demo.kaspressoSreens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.fragments.demoSamplesList.DemoSamplesListFragment
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object DemoSamplesListView : KScreen<DemoSamplesListView>() {
    override val layoutId: Int = R.layout.fragment_samples_list
    override val viewClass: Class<*> = DemoSamplesListFragment::class.java

    val sampleListRecyclerView = KRecyclerView(
        builder = { withId(R.id.recyclerView) },
        itemTypeBuilder = { itemType(::SampleListItem) }
    )

    class SampleListItem(matcher: Matcher<View>) : KRecyclerItem<SampleListItem>(matcher) {
        val descriptionTextView = KTextView(matcher) { withId(R.id.textTextView) }
    }
}
