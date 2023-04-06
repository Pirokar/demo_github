package io.edna.threads.demo.ui.fragments.server

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import io.edna.threads.demo.R
import io.edna.threads.demo.adapters.serverList.ServerListAdapter
import io.edna.threads.demo.adapters.serverList.ServerListItemOnClickListener
import io.edna.threads.demo.databinding.FragmentServersBinding
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.ui.fragments.launch.LaunchFragment.Companion.SELECTED_SERVER_CONFIG_KEY
import io.edna.threads.demo.utils.TouchHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class ServersFragment : Fragment(), ServerListItemOnClickListener, TouchHelper.OnSwipeItemListener {

    private lateinit var binding: FragmentServersBinding
    private val viewModel: ServersViewModel by viewModel()
    private var adapter: ServerListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_servers, container, false)
        binding.viewModel = viewModel
        setResultListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        subscribeForData()
        initAdapter()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.copyServersFromFileIfNeed(activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    override fun onSwiped(position: Int) {
        adapter?.showMenu(position)
    }

    override fun onClick(item: ServerConfig) {
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_SERVER_CONFIG_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(item: ServerConfig) {
        adapter?.closeMenu()
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_ServersFragment_to_AddServerFragment, args)
    }

    override fun onRemoveItem(item: ServerConfig) {
        adapter?.closeMenu()
        viewModel.removeConfig(activity, item)
    }

    private fun initAdapter() {
        val touchHelper = TouchHelper(this)
        ItemTouchHelper(touchHelper.touchHelperCallback).attachToRecyclerView(binding.recyclerView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ -> adapter?.closeMenu() }
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SERVER_CONFIG_KEY)
    }

    private fun setResultListeners() {
        setFragmentResultListener(SERVER_CONFIG_KEY) { key, bundle ->
            if (key == SERVER_CONFIG_KEY && bundle.containsKey(SERVER_CONFIG_KEY)) {
                val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(bundle.getParcelable(SERVER_CONFIG_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(bundle.getParcelable(SERVER_CONFIG_KEY))
                }
                config?.let {
                    viewModel.addConfig(activity, it)
                }
            }
        }
    }

    private fun createAdapter() = with(binding) {
        adapter = ServerListAdapter(this@ServersFragment)
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.serverConfigLiveData.observe(viewLifecycleOwner) { adapter?.addItems(it) }
    }

    companion object {
        const val SERVER_CONFIG_KEY = "server_config_key"
    }
}
