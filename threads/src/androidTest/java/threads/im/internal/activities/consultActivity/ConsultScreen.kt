package threads.im.internal.activities.consultActivity

import com.kaspersky.kaspresso.screens.KScreen
import im.threads.R
import im.threads.internal.activities.ConsultActivity
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.toolbar.KToolbar

object ConsultScreen : KScreen<ConsultScreen>() {
    override val layoutId: Int = R.layout.activity_consult_page
    override val viewClass: Class<*> = ConsultActivity::class.java

    val consultImage = KImageView { withId(R.id.consultImage) }
    val toolbar = KToolbar { withId(R.id.toolbar) }
    val consultTitle = KTextView { withId(R.id.consultTitle) }
    val consultStatus = KTextView { withId(R.id.consult_status) }
}
