package io.edna.threads.demo.appCode.fragments.demoSamplesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.databinding.FragmentSamplesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesFragment : Fragment() {
    private lateinit var binding: FragmentSamplesBinding
    private val viewModel: DemoSamplesViewModel by viewModel()
    private var fragment: ChatFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_samples)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToData()
        subscribeToBackClick()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun subscribeToData() {
        viewModel.chatFragmentLiveData.observe(viewLifecycleOwner) {
            fragment = it
            childFragmentManager
                .beginTransaction()
                .add(R.id.chatFragmentContainer, it)
                .commit()
        }
    }

    private fun subscribeToBackClick() {
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (fragment?.onBackPressed() != false) {
                    findNavController().navigateUp()
                }
            }
        })
    }
}
