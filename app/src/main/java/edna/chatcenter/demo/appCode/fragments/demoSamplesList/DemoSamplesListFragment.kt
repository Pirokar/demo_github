package edna.chatcenter.demo.appCode.fragments.demoSamplesList

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.demoSamplesList.DemoSamplesAdapter
import edna.chatcenter.demo.appCode.adapters.demoSamplesList.SampleListItemOnClick
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.appCode.models.DemoSamplesListItem
import edna.chatcenter.demo.databinding.FragmentSamplesListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference

class DemoSamplesListFragment : BaseAppFragment<FragmentSamplesListBinding>(FragmentSamplesListBinding::inflate), SampleListItemOnClick {
    private val viewModel: DemoSamplesListViewModel by viewModel()
    private var adapter: WeakReference<DemoSamplesAdapter>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.providerChatCenterUI(chatCenterUI)
        createAdapter()
        setNavigationIcon()
        subscribeForData()
        subscribeToGlobalBackClick()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onClick(item: DemoSamplesListItem) {
        viewModel.onItemClick(item)
    }

    private fun createAdapter() = getBinding()?.apply {
        val newAdapter = DemoSamplesAdapter(this@DemoSamplesListFragment)
        adapter = WeakReference(newAdapter)
        recyclerView.adapter = newAdapter
    }

    private fun setNavigationIcon() = getBinding()?.apply {
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white_color_ec))
        toolbar.setNavigationOnClickListener {
            chatCenterUI?.logout()
            findNavController().navigateUp()
        }
    }

    private fun subscribeForData() {
        viewModel.demoSamplesLiveData.observe(viewLifecycleOwner) { adapter?.get()?.addItems(it) }
        viewModel.navigationLiveData.observe(viewLifecycleOwner) { findNavController().navigate(it) }
    }
}
