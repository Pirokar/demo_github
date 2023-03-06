package io.edna.threads.demo.ui.fragments.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.FragmentLaunchBinding

class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private lateinit var viewModel: LaunchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_launch, container, false)
        viewModel = ViewModelProvider(this)[LaunchViewModel::class.java]
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
