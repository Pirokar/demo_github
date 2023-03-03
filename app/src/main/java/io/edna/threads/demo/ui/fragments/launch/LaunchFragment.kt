package io.edna.threads.demo.ui.fragments.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import im.threads.business.utils.Balloon
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
        binding = FragmentLaunchBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[LaunchViewModel::class.java]
        initView()
        initObservers()
        return binding.root
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        login.setOnClickListener {}
        serverButton.setOnClickListener {
            findNavController().navigate(R.id.action_LaunchFragment_to_ServersFragment)
        }
        userButton.setOnClickListener {
            findNavController().navigate(R.id.action_LaunchFragment_to_ServersFragment)
        }
        demonstrations.setOnClickListener {
            Balloon.show(requireContext(), getString(R.string.functional_not_support))
        }
        settings.setOnClickListener {
            Balloon.show(requireContext(), getString(R.string.functional_not_support))
        }
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
