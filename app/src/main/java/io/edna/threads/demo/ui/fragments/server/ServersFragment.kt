package io.edna.threads.demo.ui.fragments.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.FragmentServersBinding
import io.edna.threads.demo.ui.extenstions.inflateWithBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServersFragment : Fragment() {

    private lateinit var binding: FragmentServersBinding
    private val viewModel: ServerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflateWithBinding(container, R.layout.fragment_servers)
        return binding.root
    }
}
