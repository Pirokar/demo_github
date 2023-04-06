package io.edna.threads.demo.appCode.fragments.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        return binding.root
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun initObservers() {
        viewModel.selectServerAction.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewModel.selectUserAction.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
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
}
