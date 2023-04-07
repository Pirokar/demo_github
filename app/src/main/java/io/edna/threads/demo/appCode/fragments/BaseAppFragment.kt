package io.edna.threads.demo.appCode.fragments

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import im.threads.ui.fragments.ChatFragment

open class BaseAppFragment : Fragment() {
    protected var fragment: ChatFragment? = null

    protected fun subscribeToGlobalBackClick() {
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateUp()
            }
        })
    }

    protected fun navigateUp() {
        if (fragment?.onBackPressed() != false && isAdded) {
            findNavController().navigateUp()
        }
    }
}
