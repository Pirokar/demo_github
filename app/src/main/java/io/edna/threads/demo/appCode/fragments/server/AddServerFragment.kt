package io.edna.threads.demo.appCode.fragments.server

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
import io.edna.threads.demo.appCode.fragments.server.ServersFragment.Companion.SERVER_CONFIG_KEY
import io.edna.threads.demo.databinding.FragmentAddServerBinding
import io.edna.threads.demo.models.ServerConfig
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class AddServerFragment : Fragment() {

    private lateinit var binding: FragmentAddServerBinding
    private val viewModel: AddServerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_server, container, false)
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
        viewModel.finalServerConfigLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(SERVER_CONFIG_KEY, Parcels.wrap(it))
            setFragmentResult(SERVER_CONFIG_KEY, args)
        }
    }

    private fun initData() {
        arguments?.let {
            if (it.containsKey(SERVER_CONFIG_KEY)) {
                val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(it.getParcelable(SERVER_CONFIG_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(it.getParcelable(SERVER_CONFIG_KEY))
                }
                viewModel.setSrcConfig(config)
            }
        }
    }
}
