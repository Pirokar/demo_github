package io.edna.threads.demo.ui.fragments.launch

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.ui.extenstions.inflateWithBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private val viewModel: LaunchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = inflater.inflateWithBinding(container, R.layout.fragment_launch)
        binding.viewModel = viewModel
        initView()
        initObservers()
        setResultListeners()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun initObservers() {
        viewModel.selectedServerConfigLiveData.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewModel.selectUserAction.observe(viewLifecycleOwner) { checkLoginEnabled() }
    }

    private fun setResultListeners() {
        setFragmentResultListener(SELECTED_SERVER_CONFIG_KEY) { key, bundle ->
            if (key == SELECTED_SERVER_CONFIG_KEY && bundle.containsKey(SELECTED_SERVER_CONFIG_KEY)) {
                val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(bundle.getParcelable(SELECTED_SERVER_CONFIG_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(bundle.getParcelable(SELECTED_SERVER_CONFIG_KEY))
                }
                config?.let {
                    if (it.isAllFieldsFilled()) {
                        viewModel.setupServerConfig(it)
                    }
                }
            }
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SELECTED_SERVER_CONFIG_KEY)
    }

    private fun checkLoginEnabled() {
        binding.login.isEnabled = true
    }

    private fun generateAboutText(): String {
        return "${getString(R.string.app_name)}  " +
            "v${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.VERSION_CODE})" +
            "/ ChatCenter SDK ${ThreadsLib.getLibVersion()}"
    }

    companion object {
        const val SELECTED_SERVER_CONFIG_KEY = "selected_server_config_key"
    }
}
