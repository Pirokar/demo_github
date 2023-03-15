package io.edna.threads.demo.appCode.fragments.demoSamplesList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.demoSamplesList.DemoSamplesAdapter
import io.edna.threads.demo.appCode.adapters.demoSamplesList.SampleListItemOnClick
import io.edna.threads.demo.appCode.extenstions.inflateWithBinding
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.databinding.FragmentSamplesListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesListFragment : Fragment(), SampleListItemOnClick {
    private lateinit var binding: FragmentSamplesListBinding
    private val viewModel: DemoSamplesListViewModel by viewModel()
    private var adapter: DemoSamplesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_samples_list)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        subscribeForData()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onClick(item: DemoSamplesListItem) {
        viewModel.onItemClick(item)
    }

    private fun createAdapter() = with(binding) {
        adapter = DemoSamplesAdapter(this@DemoSamplesListFragment)
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.demoSamplesLiveData.observe(viewLifecycleOwner) { adapter?.addItems(it) }
        viewModel.navigationLiveData.observe(viewLifecycleOwner) { findNavController().navigate(it) }
    }
}
