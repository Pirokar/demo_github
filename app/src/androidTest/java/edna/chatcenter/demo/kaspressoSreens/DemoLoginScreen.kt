package edna.chatcenter.demo.kaspressoSreens

import com.kaspersky.kaspresso.screens.KScreen
import edna.chatcenter.demo.R
import edna.chatcenter.demo.integrationCode.fragments.launch.LaunchFragment
import io.github.kakaocup.kakao.text.KButton

object DemoLoginScreen : KScreen<DemoLoginScreen>() {
    override val layoutId: Int = R.layout.fragment_launch
    override val viewClass: Class<*> = LaunchFragment::class.java

    val loginButton = KButton { withId(R.id.login) }
    val demoButton = KButton { withId(R.id.demonstrations) }
}
