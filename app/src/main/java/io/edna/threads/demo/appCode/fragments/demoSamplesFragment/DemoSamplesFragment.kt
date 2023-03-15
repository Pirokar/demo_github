package io.edna.threads.demo.appCode.fragments.demoSamplesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extenstions.inflateWithBinding
import io.edna.threads.demo.databinding.FragmentSamplesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesFragment : Fragment() {
    private lateinit var binding: FragmentSamplesBinding
    private val viewModel: DemoSamplesViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_samples)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToData()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun subscribeToData() {
        viewModel.chatFragmentLiveData.observe(viewLifecycleOwner) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.chatFragmentContainer, it)
                .commit()
        }
    }
}
