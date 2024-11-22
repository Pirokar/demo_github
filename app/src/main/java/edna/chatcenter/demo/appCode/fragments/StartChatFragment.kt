package edna.chatcenter.demo.appCode.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.activity.ModalChatActivity
import edna.chatcenter.demo.appCode.business.PreferencesProvider
import edna.chatcenter.demo.appCode.business.ServersProvider
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.databinding.FragmentStartChatBinding
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent

class StartChatFragment(private val changeTabCallBack: (Int) -> Unit) :
    BaseAppFragment<FragmentStartChatBinding>(FragmentStartChatBinding::inflate) {

    private val uiThemeProvider: UiThemeProvider by KoinJavaComponent.inject(UiThemeProvider::class.java)
    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        initData()
        initView()
    }

    private fun initListeners() = getBinding()?.apply {
        goToChat.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ModalChatActivity::class.java)
                startActivity(intent)
            } ?: Log.e("StartChatFragment", "context is null")
        }
        logout.setOnClickListener {
            chatCenterUI?.logout()
            findNavController().navigateUp()
        }
    }

    private fun initData() = getBinding()?.apply {
        about.text = generateAboutText()
    }

    private fun initView() = getBinding()?.apply {
        if (uiThemeProvider.isDarkThemeOn()) {
            goToChat.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_color))
            goToChat.setBackgroundResource(R.drawable.buttons_bg_selector_dark)
        } else {
            goToChat.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_color_fa))
            goToChat.setBackgroundResource(R.drawable.buttons_bg_selector)
        }
    }

    private fun generateAboutText(): String {
        return "Server: ${serversProvider.getSelectedServer()?.name}\n" +
            "User: ${preferences.getSelectedUser()?.userId}\n" +
            "Api Version: ${preferences.getSelectedApiVersion()}"
    }
}
