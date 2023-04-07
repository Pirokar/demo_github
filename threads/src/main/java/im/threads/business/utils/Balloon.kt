package im.threads.business.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import im.threads.R
import im.threads.databinding.EccSnackbarBinding
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper

object Balloon {

    @JvmStatic
    fun show(context: Context, message: String) {
        if (getInstance().getChatStyle().isToastStylable && context is Activity) {
            showSnackbar(context, message)
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    private fun showSnackbar(context: Activity, messageString: String) {
        val chatStyle = getInstance().getChatStyle()
        val inflater: LayoutInflater = context.layoutInflater
        val bar = Snackbar.make(context.window.decorView, messageString, Snackbar.LENGTH_LONG)
        val layout = bar.view as Snackbar.SnackbarLayout
        EccSnackbarBinding.inflate(inflater).apply {
            ColorsHelper.setBackgroundColor(context, layout, R.color.ecc_transparent)
            ColorsHelper.setTint(context, background, chatStyle.toastBackgroundColor)
            ColorsHelper.setTextColor(context, message, chatStyle.toastTextColor)
            val textSize = context.resources.getDimension(chatStyle.toastTextSize)
            message.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            message.text = messageString
            layout.removeAllViews()
            layout.addView(root.rootView)
        }
        bar.show()
    }
}
