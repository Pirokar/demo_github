package edna.chatcenter.demo.integrationCode.fragments.chatFragment

import android.os.Bundle
import android.view.View
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.databinding.FragmentChatBinding
import edna.chatcenter.ui.core.annotation.OpenWay
import edna.chatcenter.ui.visual.fragments.ChatFragment
import java.lang.ref.WeakReference

class ChatAppFragment : BaseAppFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        ChatFragment.newInstance(OpenWay.DEFAULT).let {
            fragment = WeakReference(it)
            childFragmentManager
                .beginTransaction()
                .add(R.id.chatFragmentContainer, it)
                .commit()
        }
    }
}
