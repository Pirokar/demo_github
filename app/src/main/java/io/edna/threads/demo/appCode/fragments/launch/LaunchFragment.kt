package io.edna.threads.demo.appCode.fragments.launch

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LaunchFragment : BaseAppFragment<FragmentLaunchBinding>() {
    private val viewModel: LaunchViewModel by viewModel()
    private val stringsProvider: StringsProvider by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = inflater.inflateWithBinding(container, R.layout.fragment_launch)
        binding.viewModel = viewModel
        initView()
        initObservers()
        return binding.root
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun initObservers() {
        viewModel.selectServerAction.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewModel.selectUserAction.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewModel.currentUiTheme.observe(viewLifecycleOwner) { setUiThemeDependentViews(it) }
        viewModel.themeSelector.observe(viewLifecycleOwner) { showUiThemesSelector(it) }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun checkLoginEnabled() {
        binding.login.isEnabled = true
    }

    private fun setUiThemeDependentViews(theme: UiTheme) = with(binding) {
        context?.let { context ->
            when (theme) {
                UiTheme.LIGHT -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.black_color))
                    about.setTextColor(ContextCompat.getColor(context, R.color.info_text_color))
                    uiTheme.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.dark_theme))
                }
                UiTheme.DARK -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.white_color_fa))
                    about.setTextColor(ContextCompat.getColor(context, R.color.gray_color_b7))
                    uiTheme.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.light_theme))
                }
            }
        }
    }

    private fun showUiThemesSelector(theme: CurrentUiTheme) {
        context?.let { context ->
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
            alertDialog.setTitle(stringsProvider.selectTheme)
            val items = arrayOf(stringsProvider.defaultTheme, stringsProvider.lightTheme, stringsProvider.darkTheme)
            val checkedItem = theme.value
            alertDialog.setSingleChoiceItems(
                items,
                checkedItem
            ) { _, selectedIndex ->
                viewModel.saveUserSelectedUiTheme(CurrentUiTheme.fromInt(selectedIndex))
            }
            alertDialog.setPositiveButton(stringsProvider.ok) { _, _ -> }
            val dialog = alertDialog.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.logo_edna_color))
            }
            dialog.show()
        }
    }

    private fun generateAboutText(): String {
        return "${getString(R.string.app_name)}  " +
            "v${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.VERSION_CODE})" +
            "/ ChatCenter SDK ${ThreadsLib.getLibVersion()}"
    }
}
