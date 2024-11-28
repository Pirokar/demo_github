package edna.chatcenter.demo.appCode.fragments.log

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.logList.LogListAdapter
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.databinding.FragmentLogBinding
import edna.chatcenter.ui.core.logger.ChatLogLevel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LogFragment : BaseAppFragment<FragmentLogBinding>(FragmentLogBinding::inflate) {

    private var adapter: LogListAdapter? = null
    private val viewModel: LogViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        initListeners()
        initNextAndPrevButtons()
        subscribeForData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onLogViewResume()
    }

    private fun initListeners() = getBinding()?.apply {
        logLevelSelector.setOnClickListener { showSelectLogLevelMenu(logLevelSelector) }
        clearLog.setOnClickListener {
            adapter?.clear()
            viewModel.clearLog()
            noLogsTextView.visibility = View.VISIBLE
        }
    }

    private fun initNextAndPrevButtons() = getBinding()?.apply {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        scrollUpButton.visibility = if (layoutManager.findFirstVisibleItemPosition() > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
        scrollDownButton.visibility = if (layoutManager.findLastVisibleItemPosition() < layoutManager.itemCount - 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        scrollDownButton.setOnClickListener {
            val currentPosition = layoutManager.findFirstVisibleItemPosition()
            if (currentPosition < layoutManager.itemCount - 1) {
                smoothScroller.targetPosition = currentPosition + 1
                layoutManager.startSmoothScroll(smoothScroller)
            }
        }

        scrollUpButton.setOnClickListener {
            val currentPosition = layoutManager.findFirstVisibleItemPosition()
            if (currentPosition > 0) {
                smoothScroller.targetPosition = currentPosition - 1
                layoutManager.startSmoothScroll(smoothScroller)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                scrollUpButton.visibility = if (firstVisibleItem > 0) View.VISIBLE else View.GONE
                scrollDownButton.visibility = if (lastVisibleItem < layoutManager.itemCount - 1) View.VISIBLE else View.GONE
            }
        })
    }

    private fun createAdapter() = getBinding()?.apply {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LogListAdapter()
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() = getBinding()?.apply {
        viewModel.logsLiveData.observe(viewLifecycleOwner) {
            if (adapter != null && adapter?.getCount() == 0) {
                adapter?.setItems(it)
            } else {
                adapter?.addItems(it)
            }

            if (adapter != null && adapter?.getCount() != 0) {
                noLogsTextView.visibility = View.GONE
            } else {
                noLogsTextView.visibility = View.VISIBLE
            }
        }

        viewModel.selectedLogLevelLiveData.observe(viewLifecycleOwner) {
            logLevelSelector.text = buildString {
                append(getString(R.string.log_level))
                append(" ")
                append(it.name)
            }
            viewModel.filterAndShow(it)
        }
    }

    private fun showSelectLogLevelMenu(view: View) {
        val menu = PopupMenu(requireActivity(), view)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.VERBOSE.name)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.DEBUG.name)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.INFO.name)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.WARNING.name)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.ERROR.name)
        menu.menu.add(Menu.NONE, 0, 0, ChatLogLevel.FLUSH.name)

        menu.setOnMenuItemClickListener {
            viewModel.setLogLevel(ChatLogLevel.fromLevelName(it.title.toString()))
            true
        }
        menu.show()
    }
}
