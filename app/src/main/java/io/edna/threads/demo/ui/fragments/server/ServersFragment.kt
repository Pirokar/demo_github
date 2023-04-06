package io.edna.threads.demo.ui.fragments.server

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.recyclerview.widget.RecyclerView
import im.threads.business.logger.LoggerEdna
import io.edna.threads.demo.R
import io.edna.threads.demo.adapters.serverList.ServerListAdapter
import io.edna.threads.demo.adapters.serverList.ServerListItemOnClickListener
import io.edna.threads.demo.databinding.FragmentServersBinding
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.ui.fragments.launch.LaunchFragment.Companion.SELECTED_SERVER_CONFIG_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class ServersFragment : Fragment(), ServerListItemOnClickListener {

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
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(binding.recyclerView)
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.copyServersFromFileIfNeed(activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(ServersFragment.SERVER_CONFIG_KEY)
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

    override fun onClick(item: ServerConfig) {
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_SERVER_CONFIG_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(item: ServerConfig) {
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_ServersFragment_to_AddServerFragment, args)
    }

    override fun onRemoveItem(item: ServerConfig) {
        TODO("Not yet implemented")
    }

    var touchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            private val background = ColorDrawable(Color.RED)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                LoggerEdna.error("SWIPE!!!!!!!")
                adapter?.showMenu(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val itemView = viewHolder.itemView
                if (dX > 0) {
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                } else if (dX < 0) {
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
            }
        }

    companion object {
        const val SERVER_CONFIG_KEY = "server_config_key"
    }
}
