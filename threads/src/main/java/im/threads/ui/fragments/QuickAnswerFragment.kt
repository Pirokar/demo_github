package im.threads.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import im.threads.R
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.serviceLocator.core.inject
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.ui.ChatStyle
import im.threads.ui.activities.QuickAnswerActivity
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper

class QuickAnswerFragment : BaseDialogFragment() {
    private var editText: EditText? = null
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val style = getInstance().chatStyle
        val view = inflater.inflate(R.layout.ecc_dialog_fast_answer, container, false)
        val consultNameTextView = view.findViewById<TextView>(R.id.consult_name)
        val textView = view.findViewById<TextView>(R.id.question)
        editText = view.findViewById(R.id.answer)
        val imageView = view.findViewById<ImageView>(R.id.consult_image)
        initSendButton(style, view)
        view.findViewById<View>(R.id.close_button).setOnClickListener {
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(QuickAnswerActivity.ACTION_CANCEL))
            dismiss()
        }
        val arguments = arguments
        if (null != arguments) {
            var avatarPath = arguments.getString("avatarPath")
            val consultName = arguments.getString("consultName")
            val consultPhrase = arguments.getString("consultPhrase")
            if (null != avatarPath && avatarPath != "null") {
                avatarPath = convertRelativeUrlToAbsolute(avatarPath)
                get()
                    .load(avatarPath)
                    .scales(ImageView.ScaleType.FIT_XY)
                    .modifications(ImageModifications.CircleCropModification)
                    .into(imageView)
            }
            if (null != consultName && consultName != "null") consultNameTextView.text = consultName
            if (null != consultPhrase && consultPhrase != "null") textView.text = consultPhrase
        }
        view.findViewById<View>(R.id.layout_root).setBackgroundColor(getColorInt(style.chatBackgroundColor))
        view.findViewById<View>(R.id.header).setBackgroundColor(getColorInt(style.chatToolbarColorResId))
        consultNameTextView.setTextColor(getColorInt(style.chatToolbarTextColorResId))
        textView.setTextColor(getColorInt(style.notificationQuickReplyMessageTextColor))
        textView.setBackgroundColor(getColorInt(style.notificationQuickReplyMessageBackgroundColor))
        editText?.setTextColor(getColorInt(style.incomingMessageTextColor))
        view.findViewById<View>(R.id.answer_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor))
        editText?.setHintTextColor(getColorInt(style.chatMessageInputHintTextColor))
        editText?.layoutParams?.height = requireContext().resources.getDimension(style.inputHeight).toInt()
        editText?.background = AppCompatResources.getDrawable(requireContext(), style.inputBackground)
        return view
    }

    private fun initSendButton(style: ChatStyle, view: View) {
        val sendButton = view.findViewById<ImageButton>(R.id.send)
        sendButton.setImageResource(style.sendMessageIconResId)
        val iconTint = if (style.chatBodyIconsTint == 0) style.inputIconTintResId else style.chatBodyIconsTint
        ColorsHelper.setTint(requireContext(), sendButton, iconTint)
        sendButton.setOnClickListener {
            if (editText?.text.toString().trim { it <= ' ' }.isEmpty()) {
                return@setOnClickListener
            }
            val intent = Intent(QuickAnswerActivity.ACTION_ANSWER)
                .putExtra(QuickAnswerActivity.ACTION_ANSWER, editText?.text.toString())
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            editText?.setText("")
            dismiss()
        }
    }

    @ColorInt
    private fun getColorInt(@ColorRes colorResId: Int): Int {
        return ContextCompat.getColor(requireContext(), colorResId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = object : Dialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                val timeCounter = getLastUserActivityTimeCounter()
                if (MotionEvent.ACTION_DOWN == ev.action) {
                    timeCounter.updateLastUserActivityTime()
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        val width = (resources.displayMetrics.widthPixels * 0.9f).toInt()
        dialog.window?.setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        BaseConfig.instance.transport.setLifecycle(lifecycle)
        dialog?.let {
            val width = resources.displayMetrics.widthPixels
            it.window?.setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onResume() {
        super.onResume()
        if (null != editText) {
            editText?.requestFocus()
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity: Activity? = activity
        activity?.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseConfig.instance.transport.setLifecycle(null)
    }

    companion object {
        @JvmField
        var TAG: String = QuickAnswerFragment::class.java.simpleName

        @JvmStatic
        fun getInstance(
            avatarPath: String?,
            consultName: String?,
            consultPhrase: String?
        ): QuickAnswerFragment {
            val frag = QuickAnswerFragment()
            val bundle = Bundle().apply {
                putString("avatarPath", avatarPath)
                putString("consultName", consultName)
                putString("consultPhrase", consultPhrase)
            }

            frag.arguments = bundle
            return frag
        }
    }
}
