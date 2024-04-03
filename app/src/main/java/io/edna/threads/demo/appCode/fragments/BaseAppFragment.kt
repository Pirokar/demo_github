package io.edna.threads.demo.appCode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import edna.chatcenter.ui.visual.core.ChatCenterUI
import edna.chatcenter.ui.visual.extensions.isDarkThemeOn
import edna.chatcenter.ui.visual.fragments.ChatFragment
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.EdnaThreadsApplication
import io.edna.threads.demo.appCode.fragments.demoSamplesList.DemoSamplesListFragment
import io.edna.threads.demo.integrationCode.fragments.chatFragment.ChatAppFragment
import java.lang.ref.WeakReference

abstract class BaseAppFragment<T : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> T
) : Fragment() {
    protected var fragment: WeakReference<ChatFragment>? = null
    protected val chatCenterUI: ChatCenterUI?
        get() {
            return (context?.applicationContext as? EdnaThreadsApplication)?.chatCenterUI
        }
    private var binding: WeakReference<T>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WeakReference(bindingInflater.invoke(inflater))
        return getBinding()?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarColor()
    }

    protected fun subscribeToGlobalBackClick() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp()
                }
            }
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected open fun navigateUp() {
        val isDemoListFragment = this is DemoSamplesListFragment
        val chatBackPressed = fragment?.get()?.onBackPressed() == true
        if ((chatBackPressed || isDemoListFragment) && isAdded) {
            if (this@BaseAppFragment is ChatAppFragment || this@BaseAppFragment is DemoSamplesListFragment) {
                chatCenterUI?.logout()
            }
            findNavController().navigateUp()
        }
    }

    protected fun setToolbarColor() = getBinding()?.apply {
        if (chatCenterUI != null) {
            context?.let { context ->
                val toolbar = try {
                    root.findViewById<Toolbar>(R.id.toolbar)
                } catch (ignored: Exception) {
                    null
                }

                if (context.isDarkThemeOn()) {
                    toolbar?.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_main))
                } else {
                    toolbar?.setBackgroundColor(ContextCompat.getColor(context, R.color.light_main))
                }
            }
        }
    }

    protected fun getBinding() = binding?.get()
}
