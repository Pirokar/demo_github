package edna.chatcenter.demo.appCode.fragments.demoSamplesFragment

import android.os.Bundle
import android.view.View
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.databinding.FragmentChatBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesFragment : BaseAppFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel: DemoSamplesViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToData()
        subscribeToGlobalBackClick()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatCenterUI?.logout()
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
