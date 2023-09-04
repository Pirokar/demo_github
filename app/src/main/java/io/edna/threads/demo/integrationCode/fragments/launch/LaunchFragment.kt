package io.edna.threads.demo.integrationCode.fragments.launch

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LaunchFragment : BaseAppFragment<FragmentLaunchBinding>(FragmentLaunchBinding::inflate) {
    private val viewModel: LaunchViewModel by viewModel()
    private val stringsProvider: StringsProvider by inject()

    private var receiver: InitUnreadCountReceiver? = null
    private val filter = IntentFilter(APP_UNREAD_COUNT_BROADCAST)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initReceiver()
        initObservers()
        setResultListeners()
        initView()
        setOnClickListeners()
        subscribeForData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
        unregisterReceiver()
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun setOnClickListeners() = with(binding) {
        uiTheme.setOnClickListener { viewModel.click(uiTheme) }
        serverButton.setOnClickListener { viewModel.click(serverButton) }
        userButton.setOnClickListener { viewModel.click(userButton) }
        demonstrations.setOnClickListener { viewModel.click(demonstrations) }
        uiTheme.setOnClickListener { viewModel.click(uiTheme) }
        login.setOnClickListener {
            viewModel.click(login)
            setUnreadCount(0)
        }
        autoLogout.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoLogout(isChecked)
        }
    }

    private fun subscribeForData() = with(binding) {
        viewModel.selectedServerConfigLiveData.observe(viewLifecycleOwner) {
            serverButton.text = it?.name
        }
        viewModel.selectedUserLiveData.observe(viewLifecycleOwner) {
            userButton.text = it?.userId
        }
        viewModel.enabledLoginButtonLiveData.observe(viewLifecycleOwner) {
            login.isEnabled = it == true
        }
    }

    private fun initObservers() {
        viewModel.currentUiThemeLiveData.observe(viewLifecycleOwner) { setUiThemeDependentViews(it) }
        viewModel.themeSelectorLiveData.observe(viewLifecycleOwner) { showUiThemesSelector(it) }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.subscribeForData(viewLifecycleOwner)
    }

    private fun setResultListeners() {
        setFragmentResultListener(SELECTED_USER_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
        setFragmentResultListener(SELECTED_SERVER_CONFIG_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SELECTED_USER_KEY)
        clearFragmentResultListener(SELECTED_SERVER_CONFIG_KEY)
    }

    private fun setUiThemeDependentViews(theme: UiTheme) = with(binding) {
        context?.let { context ->
            when (theme) {
                UiTheme.LIGHT -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.black_color))
                    about.setTextColor(ContextCompat.getColor(context, R.color.info_text_color))
                    uiTheme.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.dark_theme
                        )
                    )
                }
                UiTheme.DARK -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.white_color_fa))
                    about.setTextColor(ContextCompat.getColor(context, R.color.gray_color_b7))
                    uiTheme.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.light_theme
                        )
                    )
                }
            }
        }
    }

    fun setUnreadCount(count: Int) {
        binding.count.isVisible = count > 0
        binding.count.text = count.toString()
    }

    private fun initReceiver() {
        receiver = InitUnreadCountReceiver(this)
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            filter,
            ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS
        )
    }

    class InitUnreadCountReceiver(val fragment: LaunchFragment) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == APP_UNREAD_COUNT_BROADCAST) {
                val count = intent.getIntExtra(UNREAD_COUNT_KEY, 0)
                fragment.setUnreadCount(count)
            }
        }
    }

    private fun unregisterReceiver() {
        receiver?.let {
            requireActivity().unregisterReceiver(it)
            receiver = null
        }
    }

    private fun showUiThemesSelector(theme: CurrentUiTheme) {
        context?.let { context ->
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            var alertDialog: AlertDialog? = null
            alertDialogBuilder.setTitle(stringsProvider.selectTheme)
            val items = arrayOf(
                stringsProvider.defaultTheme,
                stringsProvider.lightTheme,
                stringsProvider.darkTheme
            )
            val checkedItem = theme.value
            alertDialogBuilder.setSingleChoiceItems(
                items,
                checkedItem
            ) { _, selectedIndex ->
                viewModel.saveUserSelectedUiTheme(CurrentUiTheme.fromInt(selectedIndex))
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun generateAboutText(): String {
        return "${getString(R.string.app_name)}  " +
            "v${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.VERSION_CODE})" +
            "/ ChatCenter SDK ${ThreadsLib.getLibVersion()}"
    }

    companion object {
        const val SELECTED_USER_KEY = "selected_user_key"
        const val SELECTED_SERVER_CONFIG_KEY = "selected_server_key"
        const val UNREAD_COUNT_KEY = "unread_cont_key"
        const val APP_UNREAD_COUNT_BROADCAST = "unread_count_broadcast"
    }
}
