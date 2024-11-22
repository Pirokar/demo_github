package edna.chatcenter.demo.appCode.fragments

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
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.fragments.demoSamplesList.DemoSamplesListFragment
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication
import edna.chatcenter.ui.visual.core.ChatCenterUI
import edna.chatcenter.ui.visual.extensions.isDarkThemeOn
import java.lang.ref.SoftReference

abstract class BaseAppFragment<T : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> T
) : Fragment() {
    protected val chatCenterUI: ChatCenterUI?
        get() {
            return (context?.applicationContext as? EdnaChatCenterApplication)?.chatCenterUI
        }
    internal var binding: SoftReference<T>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SoftReference(bindingInflater.invoke(inflater))
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
        if (isDemoListFragment && isAdded) {
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
