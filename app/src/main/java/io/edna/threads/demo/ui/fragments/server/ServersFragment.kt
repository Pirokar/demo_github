package io.edna.threads.demo.ui.fragments.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.edna.threads.demo.databinding.FragmentServersBinding

class ServersFragment : Fragment() {

    private lateinit var binding: FragmentServersBinding
    private lateinit var viewModel: ServerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServersBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ServerViewModel::class.java]

        return binding.root
    }
}
