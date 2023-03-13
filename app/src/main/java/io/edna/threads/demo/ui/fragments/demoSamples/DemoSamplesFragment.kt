package io.edna.threads.demo.ui.fragments.demoSamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.FragmentSamplesBinding
import io.edna.threads.demo.ui.adapters.DemoSamplesAdapter
import io.edna.threads.demo.ui.extenstions.inflateWithBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesFragment : Fragment() {
    private lateinit var binding: FragmentSamplesBinding
    private val viewModel: DemoSamplesViewModel by viewModel()
    private var adapter: DemoSamplesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_samples)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        subscribeForData()
        viewModel.start()
    }

    private fun createAdapter() = with(binding) {
        adapter = DemoSamplesAdapter()
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.demoSamplesData.observe(viewLifecycleOwner) {
            adapter?.addItems(it)
        }
    }
}
