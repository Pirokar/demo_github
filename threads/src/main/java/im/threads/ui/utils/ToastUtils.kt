package im.threads.ui.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import im.threads.R
import im.threads.ui.config.Config.Companion.getInstance

object ToastUtils {

    @JvmStatic
    fun showToast(context: Context, view: View?, message: String) {
        if (getInstance().getChatStyle().isToastStylable && view != null) {
            showSnackbar(context, view, message)
        } else {
            showToast(context, message)
        }
    }

    @JvmStatic
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    fun showSnackbar(context: Context, view: View, message: String) {
        val chatStyle = getInstance().getChatStyle()
        if (chatStyle.isToastStylable) {
            val bar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            val layout = bar.view as Snackbar.SnackbarLayout
            val inflater: LayoutInflater = (context as Activity).layoutInflater
            val snackView: View = inflater.inflate(R.layout.edna_snackbar, null)
            val messageText = snackView.findViewById<View>(R.id.message) as TextView
            val background = snackView.findViewById<View>(R.id.background) as ImageView
            ColorsHelper.setBackgroundColor(context, layout, R.color.threads_transparent)
            ColorsHelper.setTint(context, background, chatStyle.toastBackgroundColor)
            ColorsHelper.setTextColor(context, messageText, chatStyle.toastTextColor)
            val textSize = context.resources.getDimension(chatStyle.toastTextSize)
            messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            messageText.text = message
            layout.removeAllViews()
            layout.addView(snackView)
            bar.show()
        } else {
            showToast(context, message)
        }
    }
}
