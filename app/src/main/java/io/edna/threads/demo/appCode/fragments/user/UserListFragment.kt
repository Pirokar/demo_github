package io.edna.threads.demo.appCode.fragments.user

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import im.threads.ui.utils.ColorsHelper
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.userList.UserListAdapter
import io.edna.threads.demo.appCode.adapters.userList.UserListItemOnClickListener
import io.edna.threads.demo.appCode.business.TouchHelper
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.fragments.launch.LaunchFragment.Companion.SELECTED_USER_KEY
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.databinding.FragmentUserListBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class UserListFragment :
    BaseAppFragment<FragmentUserListBinding>(),
    UserListItemOnClickListener,
    TouchHelper.OnSwipeItemListener {

    private val uiThemeProvider: UiThemeProvider by inject()
    private val viewModel: UserListViewModel by viewModel()
    private var adapter: UserListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflater.inflateWithBinding(container, R.layout.fragment_user_list)
        binding.viewModel = viewModel
        initView()
        setResultListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        createAdapter()
        subscribeForData()
        initAdapter()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.loadUserList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    override fun navigateUp() {
        if (adapter?.isMenuShown() == true) {
            adapter?.closeMenu()
        } else {
            viewModel.backToLaunchScreen(activity)
        }
    }

    override fun onSwiped(position: Int) {
        adapter?.showMenu(position)
    }

    override fun onClick(item: UserInfo) {
        val args = Bundle()
        args.putParcelable(SELECTED_USER_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_USER_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(item: UserInfo) {
        adapter?.closeMenu()
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(USER_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_UserListFragment_to_AddUserFragment, args)
    }

    override fun onRemoveItem(item: UserInfo) {
        adapter?.closeMenu()
        viewModel.removeUser(item)
    }

    private fun initView() {
        binding.addUser.background = null
        binding.addUser.setImageResource(R.drawable.ic_plus)
        if (uiThemeProvider.isDarkThemeOn()) {
            ColorsHelper.setTint(activity, binding.addUser, R.color.black_color)
            binding.addUser.setBackgroundResource(R.drawable.buttons_bg_selector_dark)
        } else {
            ColorsHelper.setTint(activity, binding.addUser, R.color.white_color_fa)
            binding.addUser.setBackgroundResource(R.drawable.buttons_bg_selector)
        }
    }

    private fun initAdapter() {
        val touchHelper = TouchHelper(this)
        ItemTouchHelper(touchHelper.touchHelperCallback).attachToRecyclerView(binding.recyclerView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ -> adapter?.closeMenu() }
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(USER_KEY)
    }

    private fun setResultListeners() {
        setFragmentResultListener(USER_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun createAdapter() = with(binding) {
        adapter = UserListAdapter(this@UserListFragment)
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.userListLiveData.observe(viewLifecycleOwner) {
            adapter?.addItems(it)
            if (adapter?.itemCount == 0) {
                binding.emptyView.isVisible = true
                binding.recyclerView.isVisible = false
            } else {
                binding.emptyView.isVisible = false
                binding.recyclerView.isVisible = true
            }
        }
        viewModel.backButtonClickedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                navigateUp()
            }
        }
    }

    companion object {
        const val USER_KEY = "user_key"
    }
}
