package im.threads.ui.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import im.threads.R
import im.threads.databinding.EdnaSnackbarBinding
import im.threads.ui.config.Config.Companion.getInstance

object Balloon {

    @JvmStatic
    fun show(context: Context, message: String) {
        if (getInstance().getChatStyle().isToastStylable) {
            showSnackbar(context, message)
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun showSnackbar(context: Context, messageString: String) {
        val chatStyle = getInstance().getChatStyle()
        if (chatStyle.isToastStylable && context is Activity) {
            val inflater: LayoutInflater = context.layoutInflater
            val bar = Snackbar.make(context.window.decorView, messageString, Snackbar.LENGTH_LONG)
            val layout = bar.view as Snackbar.SnackbarLayout
            EdnaSnackbarBinding.inflate(inflater).apply {
                ColorsHelper.setBackgroundColor(context, layout, R.color.threads_transparent)
                ColorsHelper.setTint(context, background, chatStyle.toastBackgroundColor)
                ColorsHelper.setTextColor(context, message, chatStyle.toastTextColor)
                val textSize = context.resources.getDimension(chatStyle.toastTextSize)
                message.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                message.text = messageString
                layout.removeAllViews()
                layout.addView(root.rootView)
            }
            bar.show()
        } else {
            Toast.makeText(context, messageString, Toast.LENGTH_LONG).show()
        }
    }
}
