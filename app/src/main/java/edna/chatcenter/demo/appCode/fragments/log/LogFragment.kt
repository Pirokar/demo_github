package edna.chatcenter.demo.appCode.fragments.log

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var isNewMessageUpdateTimeoutOn = false
    private val handler = Handler(Looper.getMainLooper())
    private val invisibleMessagesCount = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(activity)
        createAdapter()
        initListeners()
        subscribeForData()
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
        scrollDownButton.setOnClickListener {
            scrollDownWithBtnClick()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrollChange()
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun createAdapter() = getBinding()?.apply {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LogListAdapter()
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() = getBinding()?.apply {
        viewModel.logLiveData.observe(viewLifecycleOwner) {
            viewModel.addItems(it)
        }

        viewModel.logListLiveData.observe(viewLifecycleOwner) {
            val lastMessageVisible = isLastMessageVisible()
            if (adapter?.getCount() == 0) {
                adapter?.setItems(it)
            } else {
                adapter?.addItems(it)
            }
            scrollDelayedOnNewMessageReceived(lastMessageVisible)
        }

        viewModel.selectedLogLevelLiveData.observe(viewLifecycleOwner) {
            logLevelSelector.text = buildString {
                append(getString(R.string.log_level))
                append(" ")
                append(getLogLevelString(it))
            }
            viewModel.filter(it)
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

    private fun scrollToPosition(itemPosition: Int) = binding?.get()?.apply {
        if (itemPosition >= 0 && isAdded) {
            recyclerView.scrollToPosition(itemPosition)
        }
    }

    private fun scrollDownWithBtnClick() {
        adapter?.list.let { list ->
            if (list != null) {
                scrollToPosition(list.lastIndex)
//                binding?.get()?.scrollDownButton?.gone()
            }
        }
    }

    private fun scrollDelayedOnNewMessageReceived(isLastMessageVisible: Boolean) {
        if (!isNewMessageUpdateTimeoutOn) {
            isNewMessageUpdateTimeoutOn = true
            handler.postDelayed({
                if (isAdded && adapter != null) {
                    val itemCount = adapter!!.itemCount
                    if (isLastMessageVisible) {
                        scrollToPosition(itemCount - 1)
//                        binding?.get()?.scrollDownButton.invisible()
                    } else {
//                        binding?.get()?.scrollDownButton.visible()
                    }
                }
                isNewMessageUpdateTimeoutOn = false
            }, 100)
        }
    }

    private fun isLastMessageVisible(): Boolean {
        val layoutManager =
            (binding?.get()?.recyclerView?.layoutManager as LinearLayoutManager?) ?: return false
        return try {
            adapter!!.itemCount - 1 - layoutManager.findLastVisibleItemPosition() < invisibleMessagesCount
        } catch (exc: Exception) {
            false
        }
    }

    private fun onScrollChange() {
        if (adapter != null) {
            val itemCount = adapter!!.itemCount
            Log.e(
                "LogFragment",
                "!!!!!!!!!!!!!!!     onDyScrollChange()  -  ${isLastMessageVisible()}"
            )
            if (isLastMessageVisible()) {
                scrollToPosition(itemCount - 1)
                binding?.get()?.scrollDownButton?.visibility = View.INVISIBLE
            } else {
                binding?.get()?.scrollDownButton?.visibility = View.VISIBLE
            }
        }
    }
}
