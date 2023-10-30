package im.threads.ui.fragments

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import im.threads.R
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.views.BottomSheetView
import im.threads.ui.views.BottomSheetView.ButtonsListener

class AttachmentBottomSheetDialogFragment : BottomSheetDialogFragment(), ButtonsListener {
    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (parentFragment is Callback) {
            callback = parentFragment as Callback?
        } else if (context is Callback) {
            callback = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback?.onBottomSheetDetached()
        callback = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = object : BottomSheetDialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                val timeCounter = getLastUserActivityTimeCounter()
                if (MotionEvent.ACTION_DOWN == ev.action) {
                    timeCounter.updateLastUserActivityTime()
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        val window = dialog.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val chatStyle = getInstance().chatStyle
        val view = inflater.inflate(R.layout.ecc_bottom_sheet_dialog_attachment, container, false)
        val fileInputSheet = view.findViewById<BottomSheetView>(R.id.file_input_sheet)
        val attachmentBottomSheetButtonTintResId = if (chatStyle.chatBodyIconsTint == 0) {
            chatStyle.attachmentBottomSheetButtonTintResId
        } else {
            chatStyle.chatBodyIconsTint
        }
        fileInputSheet.setButtonsTint(attachmentBottomSheetButtonTintResId)
        fileInputSheet.setButtonsListener(this)
        val allItems = ArrayList<Uri>()
        val context = context
        if (context != null) {
            ColorsHelper.setBackgroundColor(
                context,
                fileInputSheet,
                chatStyle.chatMessageInputColor
            )
        }

        return view
    }

    override fun onCameraClick() {
        callback?.onCameraClick()
    }

    override fun onGalleryClick() {
        callback?.onGalleryClick()
    }

    override fun onFilePickerClick() {
        callback?.onFilePickerClick()
    }

    override fun onSendClick() {
        callback?.onSendClick()
    }

    interface Callback : ButtonsListener {
        fun onImageSelectionChanged(imageList: List<Uri>?)
        fun onBottomSheetDetached()
    }

    companion object {
        val TAG: String = AttachmentBottomSheetDialogFragment::class.java.simpleName
    }
}
