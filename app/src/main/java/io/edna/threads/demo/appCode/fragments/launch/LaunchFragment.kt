package io.edna.threads.demo.appCode.fragments.launch

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
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.databinding.FragmentLaunchBinding
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
        initObservers()
        setResultListeners()
        binding = inflater.inflateWithBinding(container, R.layout.fragment_launch)
        binding.viewModel = viewModel
        initView()
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
        viewModel.selectedUserLiveData.observe(viewLifecycleOwner) { checkLoginEnabled() }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun setResultListeners() {
        setFragmentResultListener(SELECTED_USER_KEY) { key, bundle ->
            if (key == SELECTED_USER_KEY && bundle.containsKey(SELECTED_USER_KEY)) {
                val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(bundle.getParcelable(SELECTED_USER_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(bundle.getParcelable(SELECTED_USER_KEY))
                }
                user?.let {
                    if (it.isAllFieldsFilled()) {
                        viewModel.setupUser(it)
                    }
                }
            }
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SELECTED_USER_KEY)
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
        const val SELECTED_USER_KEY = "selected_user_key"
    }
}
