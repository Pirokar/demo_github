package edna.chatcenter.demo.appCode.fragments.log

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.logList.LogListAdapter
import edna.chatcenter.demo.appCode.fragments.BaseAppFragment
import edna.chatcenter.demo.databinding.FragmentLogBinding
import edna.chatcenter.ui.core.logger.ChatLogLevel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LogFragment : BaseAppFragment<FragmentLogBinding>(FragmentLogBinding::inflate) {

    private var adapter: LogListAdapter? = null
    private val viewModel: LogViewModel by viewModel()
    private var layoutManager: LinearLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(activity)
        createAdapter()
        initListeners()
        subscribeForData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onLogViewResume()
    }

    private fun initListeners() = getBinding()?.apply {
        subscribeToGlobalBackClick()
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        logLevelSelector.setOnClickListener { showSelectLogLevelMenu(logLevelSelector) }
        clearLog.setOnClickListener {
            adapter?.clear()
            viewModel.clearLog()
        }
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
                append(getLogLevelString(it))
            }
            viewModel.filterAndShow(it)
        }
    }

    private fun showSelectLogLevelMenu(view: View) {
        val menu = PopupMenu(requireActivity(), view)
        menu.menu.add(Menu.NONE, 0, 0, getLogLevelString(ChatLogLevel.DEBUG))
        menu.menu.add(Menu.NONE, 0, 0, getLogLevelString(ChatLogLevel.INFO))
        menu.menu.add(Menu.NONE, 0, 0, getLogLevelString(ChatLogLevel.WARNING))
        menu.menu.add(Menu.NONE, 0, 0, getLogLevelString(ChatLogLevel.ERROR))
        menu.menu.add(Menu.NONE, 0, 0, getLogLevelString(ChatLogLevel.FLUSH))
        menu.setOnMenuItemClickListener {
            viewModel.setLogLevel(getLogLevelFromString(it.title.toString()))
            true
        }
        menu.show()
    }

    private fun getLogLevelString(level: ChatLogLevel): String {
        return when (level) {
            ChatLogLevel.FLUSH -> "FLUSH"
            ChatLogLevel.ERROR -> "ERROR"
            ChatLogLevel.WARNING -> "WARN"
            ChatLogLevel.INFO -> "INFO"
            else -> "DEBUG"
        }
    }

    private fun getLogLevelFromString(level: String): ChatLogLevel {
        return when (level) {
            "FLUSH" -> ChatLogLevel.FLUSH
            "ERROR" -> ChatLogLevel.ERROR
            "WARN" -> ChatLogLevel.WARNING
            "INFO" -> ChatLogLevel.INFO
            else -> ChatLogLevel.DEBUG
        }
    }
}
