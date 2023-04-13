package io.edna.threads.demo.appCode.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.fragments.user.UserListFragment.Companion.USER_KEY
import io.edna.threads.demo.databinding.FragmentAddUserBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class AddUserFragment : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private val viewModel: AddUserViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_add_user)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        subscribeForData()
        initData()
        return binding.root
    }

    private fun subscribeForData() {
        viewModel.subscribeForData(viewLifecycleOwner)
        viewModel.finalUserLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(USER_KEY, Parcels.wrap(it))
            setFragmentResult(USER_KEY, args)
        }
    }

    private fun initData() {
        viewModel.initData(arguments)
    }
}
