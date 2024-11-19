package edna.chatcenter.demo.integrationCode.fragments.chatFragment

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.appCode.fragments.StartChatFragment
import edna.chatcenter.demo.appCode.fragments.log.LogFragment
import edna.chatcenter.demo.databinding.FragmentChatBinding
import edna.chatcenter.ui.core.annotation.OpenWay
import edna.chatcenter.ui.visual.fragments.ChatFragment
import org.koin.java.KoinJavaComponent.inject

class ChatAppFragment : BaseAppFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val uiThemeProvider: UiThemeProvider by inject(UiThemeProvider::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        initTabs()
        subscribeToGlobalBackClick()
    }

    private fun initTabs() = getBinding()?.apply {
        viewPager.isUserInputEnabled = false
        viewPager.adapter = TabAdapter(requireActivity(), this@ChatAppFragment)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.view?.setPadding(0, 0, 0, 0)
            tabLayout.getTabAt(i)?.customView = createTabView(i)
        }
        viewPager.currentItem = 0
    }

    private fun createTabView(position: Int): View {
        var normalColor = R.color.light_toolbar
        var selectedColor = R.color.blue_color_dark
        var textColor = ContextCompat.getColor(requireContext(), R.color.alt_threads_chat_context_menu)
        if (uiThemeProvider.isDarkThemeOn()) {
            normalColor = R.color.dark_toolbar
            selectedColor = R.color.blue_color_dark
            textColor = ContextCompat.getColor(requireContext(), R.color.alt_threads_chat_context_menu)
        }
        val view = LayoutInflater.from(context).inflate(R.layout.tab_view, null)
        val tabName: TextView = view.findViewById(R.id.name)
        val bg: FrameLayout = view.findViewById(R.id.rootLayout)
        tabName.setText(getTabNameResId(position))
        tabName.setTextColor(textColor)
        bg.background = getStateListDrawable(normalColor, selectedColor)
        return view
    }

    private fun getTabNameResId(position: Int): Int {
        return when (position) {
            0 -> R.string.ecc_start
            1 -> R.string.ecc_chat
            else -> R.string.ecc_log
        }
    }

    private fun getStateListDrawable(normalColor: Int, selectedColor: Int): StateListDrawable {
        val states = StateListDrawable()
        states.addState(
            IntArray(1) { android.R.attr.state_pressed },
            ColorDrawable(ContextCompat.getColor(requireContext(), selectedColor))
        )
        states.addState(
            IntArray(1) { android.R.attr.state_selected },
            ColorDrawable(ContextCompat.getColor(requireContext(), selectedColor))
        )
        states.addState(
            IntArray(1) { android.R.attr.state_enabled },
            ColorDrawable(ContextCompat.getColor(requireContext(), normalColor))
        )
        return states
    }

    fun changeTab(tabIndex: Int) {
        getBinding()?.viewPager?.currentItem = tabIndex
    }

    internal class TabAdapter(activity: FragmentActivity, val fragment: ChatAppFragment) : FragmentStateAdapter(activity) {
        private var chatFragment: ChatFragment = ChatFragment.newInstance(OpenWay.DEFAULT)
        private var logFragment: LogFragment = LogFragment()

        override fun getItemCount(): Int {
            return TABS_COUNT
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                TAB_INDEX_CHAT -> chatFragment
                TAB_INDEX_LOG -> logFragment
                else -> StartChatFragment { tabIndex ->
                    fragment.changeTab(tabIndex)
                }
            }
        }
    }

    companion object {
        const val TAB_INDEX_CHAT = 1
        const val TAB_INDEX_LOG = 2
        const val TABS_COUNT = 3
    }
}
