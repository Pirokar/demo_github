package im.threads.internal.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.DialogFragment
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.utils.ColorsHelper
import im.threads.styles.permissions.ContentGravity
import im.threads.styles.permissions.PermissionDescriptionButtonStyle
import im.threads.styles.permissions.PermissionDescriptionDialogBackgroundStyle
import im.threads.styles.permissions.PermissionDescriptionImageStyle
import im.threads.styles.permissions.PermissionDescriptionTextStyle
import im.threads.styles.permissions.PermissionDescriptionType


/**
 * Диалоговое окно с описанием запроса разрешения.
 */
class PermissionDescriptionAlertDialogFragment : DialogFragment() {

    private var onAllowPermissionClickListener: OnAllowPermissionClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fragment = parentFragment
        if (fragment is OnAllowPermissionClickListener) {
            onAllowPermissionClickListener = fragment
        } else if (context is OnAllowPermissionClickListener) {
            onAllowPermissionClickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        val listener = onAllowPermissionClickListener
        if (listener != null) {
            listener.onDialogDetached()
            onAllowPermissionClickListener = null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        Dialog(requireContext(), theme).apply {
            setCanceledOnTouchOutside(false)
        }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.fragment_dialog_permission_description, container)

        val type = requireArguments().getSerializable(KEY_PERMISSION_DESCRIPTION_TYPE)
                as PermissionDescriptionType
        val requestCode = requireArguments().getInt(KEY_REQUEST_CODE)

        val dialogStyle = getPermissionDescriptionDialogStyle(type)
        setIllustration(dialogView, dialogStyle.imageStyle)
        setTextStyle(dialogView.findViewById(R.id.title), dialogStyle.titleStyle)
        setTextStyle(dialogView.findViewById(R.id.message), dialogStyle.messageStyle)
        setButtonStyle(
            dialogView.findViewById(R.id.positive_button),
            dialogStyle.positiveButtonStyle
        ) {
            onAllowPermissionClickListener?.onClick(type, requestCode)
            dismiss()
        }
        setButtonStyle(
            dialogView.findViewById(R.id.negative_button),
            dialogStyle.negativeButtonStyle
        ) { dismiss() }
        setDialogBackground(
            dialogView.findViewById(R.id.dialog_layout),
            dialogStyle.backgroundStyle
        )
        return dialogView
    }

    private fun getPermissionDescriptionDialogStyle(type: PermissionDescriptionType) =
        when (type) {
            PermissionDescriptionType.STORAGE ->
                Config.instance.storagePermissionDescriptionDialogStyle
            PermissionDescriptionType.RECORD_AUDIO ->
                Config.instance.recordAudioPermissionDescriptionDialogStyle
            PermissionDescriptionType.CAMERA ->
                Config.instance.cameraPermissionDescriptionDialogStyle
        }

    private fun setIllustration(dialogView: View, imageStyle: PermissionDescriptionImageStyle) {
        val imageView: ImageView = dialogView.findViewById(R.id.image)
        if (imageStyle.imageResId == 0) {
            imageView.visibility = View.GONE
        } else {
            imageView.setImageResource(imageStyle.imageResId)
            setImageViewLayoutParams(imageStyle, imageView)
        }
    }

    private fun setImageViewLayoutParams(
        imageStyle: PermissionDescriptionImageStyle,
        imageView: ImageView
    ) {
        val params = imageView.layoutParams as LinearLayout.LayoutParams
        if (imageStyle.marginTopDpResId != 0) {
            params.setMargins(
                0,
                resources.getDimensionPixelSize(imageStyle.marginTopDpResId),
                0,
                0
            )
        }
        params.gravity = getGravity(imageStyle.layoutGravity)
        imageView.layoutParams = params
    }

    private fun setTextStyle(textView: TextView, textStyle: PermissionDescriptionTextStyle) {
        if (textStyle.textResId == 0) {
            textView.visibility = View.GONE
        } else {
            textView.text = textView.context.getString(textStyle.textResId)
            setTextAppearance(textStyle.textAppearanceResId, textView)
            if (textStyle.textAppearanceResId == 0) {
                setFont(textStyle.fontPath, textView)
                setTextSize(textStyle.textSizeSpResId, textView)
                ColorsHelper.setTextColor(textView, textStyle.textColorResId)
            }
            setViewMargins(marginTopDpResId = textStyle.marginTopDpResId, view = textView)
            textView.gravity = getGravity(textStyle.gravity)
        }
    }

    private fun setTextAppearance(@StyleRes textAppearanceResId: Int, textView: TextView) {
        if (textAppearanceResId != 0) {
            TextViewCompat.setTextAppearance(textView, textAppearanceResId)
        }
    }

    private fun setFont(fontPath: String, textView: TextView) {
        if (fontPath.isNotEmpty()) {
            textView.typeface = Typeface.createFromAsset(textView.context.assets, fontPath)
        }
    }

    private fun setTextSize(@DimenRes textSizeSpResId: Int, textView: TextView) {
        if (textSizeSpResId != 0) {
            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(textSizeSpResId)
            )
        }
    }

    private fun setViewMargins(
        @DimenRes marginTopDpResId: Int,
        @DimenRes marginBottomDpResId: Int = 0,
        view: View
    ) {
        val params = view.layoutParams as LinearLayout.LayoutParams
        var marginTopPx = 0
        if (marginTopDpResId != 0) {
            marginTopPx = resources.getDimensionPixelSize(marginTopDpResId)
        }
        var marginBottomPx = 0
        if (marginBottomDpResId != 0) {
            marginBottomPx = resources.getDimensionPixelSize(marginBottomDpResId)
        }
        params.setMargins(
            0,
            marginTopPx,
            0,
            marginBottomPx
        )
        view.layoutParams = params
    }

    private fun setButtonStyle(
        button: Button,
        buttonStyle: PermissionDescriptionButtonStyle,
        onClickListener: View.OnClickListener
    ) {
        button.setOnClickListener(onClickListener)
        setText(buttonStyle.textResId, button)
        setTextAppearance(buttonStyle.textAppearanceResId, button)
        if (buttonStyle.textAppearanceResId == 0) {
            setFont(buttonStyle.fontPath, button)
            setTextSize(buttonStyle.textSizeSpResId, button)
            ColorsHelper.setTextColor(button, buttonStyle.textColorResId)
        }
        setViewMargins(
            marginTopDpResId = buttonStyle.marginTopDpResId,
            marginBottomDpResId = buttonStyle.marginBottomDpResId,
            view = button
        )
        setBackground(buttonStyle.backgroundResId, button)
        if (buttonStyle.backgroundResId == 0) {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.RECTANGLE
            setCornerRadius(buttonStyle.cornerRadiusDpResId, gradientDrawable)
            gradientDrawable.setColor(
                ContextCompat.getColor(button.context, buttonStyle.backgroundColorResId)
            )
            setStroke(
                buttonStyle.strokeColorResId,
                buttonStyle.strokeWidthDpResId,
                gradientDrawable,
                button
            )
            button.background = gradientDrawable
        }
    }

    private fun setText(@StringRes textResId: Int, button: Button) {
        if (textResId != 0) {
            button.text = button.context.getString(textResId)
        }
    }

    private fun setBackground(@DrawableRes backgroundResId: Int, view: View) {
        if (backgroundResId != 0) {
            view.background = ContextCompat.getDrawable(view.context, backgroundResId)
        }
    }

    private fun setCornerRadius(
        @DimenRes cornerRadiusDpResId: Int,
        gradientDrawable: GradientDrawable
    ) {
        if (cornerRadiusDpResId != 0) {
            gradientDrawable.cornerRadius = resources.getDimension(cornerRadiusDpResId)
        }
    }

    private fun setStroke(
        @ColorRes strokeColorResId: Int,
        @DimenRes strokeWidthDpResId: Int,
        gradientDrawable: GradientDrawable,
        view: View
    ) {
        if (strokeColorResId != 0 && strokeWidthDpResId != 0) {
            gradientDrawable.setStroke(
                view.context.resources.getDimensionPixelSize(strokeWidthDpResId),
                ContextCompat.getColor(view.context, strokeColorResId)
            )
        }
    }

    private fun setDialogBackground(
        view: View,
        backgroundStyle: PermissionDescriptionDialogBackgroundStyle
    ) {
        setBackground(backgroundStyle.backgroundResId, view)
        if (backgroundStyle.backgroundResId == 0) {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.RECTANGLE
            setCornerRadius(backgroundStyle.cornerRadiusDpResId, gradientDrawable)
            gradientDrawable.setColor(
                ContextCompat.getColor(view.context, backgroundStyle.backgroundColorResId)
            )
            setStroke(
                backgroundStyle.strokeColorResId,
                backgroundStyle.strokeWidthDpResId,
                gradientDrawable,
                view
            )
            view.background = gradientDrawable
        }
    }

    interface OnAllowPermissionClickListener {
        fun onClick(type: PermissionDescriptionType, requestCode: Int)
        fun onDialogDetached()
    }

    companion object {
        const val TAG = "PermissionDescriptionAlertDialogFragment"
        private const val KEY_PERMISSION_DESCRIPTION_TYPE = "KEY_PERMISSION_DESCRIPTION_TYPE"
        private const val KEY_REQUEST_CODE = "KEY_REQUEST_CODE"

        @JvmStatic
        fun newInstance(
            type: PermissionDescriptionType,
            requestCode: Int
        ): PermissionDescriptionAlertDialogFragment {
            val args = Bundle().apply {
                putSerializable(KEY_PERMISSION_DESCRIPTION_TYPE, type)
                putInt(KEY_REQUEST_CODE, requestCode)
            }
            return PermissionDescriptionAlertDialogFragment().apply {
                arguments = args
            }
        }

        private fun getGravity(contentGravity: ContentGravity) =
            when (contentGravity) {
                ContentGravity.LEFT -> Gravity.START
                ContentGravity.CENTER -> Gravity.CENTER_HORIZONTAL
                ContentGravity.RIGHT -> Gravity.END
            }
    }
}