package edna.chatcenter.demo.appCode.fragments.server

import android.os.Bundle
import android.view.View
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.EccTouchHelperCallBack
import edna.chatcenter.demo.appCode.adapters.ListItemClickListener
import edna.chatcenter.demo.appCode.adapters.serverList.ServerListAdapter
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.databinding.FragmentServerListBinding
import edna.chatcenter.demo.integrationCode.fragments.launch.LaunchFragment.Companion.SELECTED_SERVER_CONFIG_KEY
import edna.chatcenter.ui.visual.utils.EdnaColors
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels
import java.lang.ref.WeakReference

class ServerListFragment :
    BaseAppFragment<FragmentServerListBinding>(FragmentServerListBinding::inflate),
    ListItemClickListener {

    private val uiThemeProvider: UiThemeProvider by inject()
    private val viewModel: ServerListViewModel by viewModel()
    private var adapter: WeakReference<ServerListAdapter>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setResultListeners()
        createAdapter()
        subscribeForData()
        initAdapter()
        setOnClickListeners()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.copyServersFromFileIfNeed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    override fun navigateUp() {
        viewModel.backToLaunchScreen(activity)
    }

    override fun onClick(position: Int) {
        val item = adapter?.get()?.getItem(position)
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_SERVER_CONFIG_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(position: Int) {
        val item = adapter?.get()?.getItem(position)
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(SERVER_CONFIG_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_ServerListFragment_to_AddServerFragment, args)
    }

    override fun onRemoveItem(position: Int) {
        adapter?.get()?.getItem(position)?.let {
            viewModel.removeConfig(it)
        }
    }

    private fun initAdapter() {
        val simpleCallback = EccTouchHelperCallBack(
            requireContext(),
            this,
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(getBinding()?.recyclerView)
    }

    private fun setOnClickListeners() = getBinding()?.apply {
        backButton.setOnClickListener { viewModel.click(backButton) }
        addServer.setOnClickListener { viewModel.click(addServer) }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SERVER_CONFIG_KEY)
    }

    private fun setResultListeners() {
        setFragmentResultListener(SERVER_CONFIG_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun initView() = getBinding()?.apply {
        addServer.background = null
        addServer.setImageResource(R.drawable.ic_plus)
        if (uiThemeProvider.isDarkThemeOn()) {
            EdnaColors.setTint(activity, addServer, R.color.black_color)
            addServer.setBackgroundResource(R.drawable.buttons_bg_selector_dark)
        } else {
            EdnaColors.setTint(activity, addServer, R.color.white_color_fa)
            addServer.setBackgroundResource(R.drawable.buttons_bg_selector)
        }
    }

    private fun createAdapter() = getBinding()?.apply {
        val newAdapter = ServerListAdapter(this@ServerListFragment)
        adapter = WeakReference(newAdapter)
        recyclerView.adapter = newAdapter
    }

    private fun subscribeForData() {
        viewModel.serverConfigLiveData.observe(viewLifecycleOwner) { adapter?.get()?.addItems(it) }
    }

    companion object {
        const val SERVER_CONFIG_KEY = "server_config_key"
        const val SRC_SERVER_NAME_KEY = "src_server_name_key"
    }
}
