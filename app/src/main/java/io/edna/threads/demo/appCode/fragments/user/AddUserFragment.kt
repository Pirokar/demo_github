package io.edna.threads.demo.appCode.fragments.user

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.fragments.user.UserListFragment.Companion.USER_KEY
import io.edna.threads.demo.databinding.FragmentAddUserBinding
import io.edna.threads.demo.models.UserInfo
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_user, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        subscribeForData()
        initData()
        return binding.root
    }

    private fun subscribeForData() {
        viewModel.subscribeForData(viewLifecycleOwner)
        viewModel.enabledSaveButtonLiveData.observe(viewLifecycleOwner) {
            binding.okButton.isEnabled = it
        }
        viewModel.finalUserLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(USER_KEY, Parcels.wrap(it))
            setFragmentResult(USER_KEY, args)
        }
    }

    private fun initData() {
        arguments?.let {
            if (it.containsKey(USER_KEY)) {
                val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(it.getParcelable(USER_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(it.getParcelable(USER_KEY))
                }
                viewModel.setSrcUser(user)
            }
        }
    }
}
