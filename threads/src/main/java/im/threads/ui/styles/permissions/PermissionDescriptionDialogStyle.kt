package im.threads.ui.styles.permissions

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import im.threads.R
import java.io.Serializable

/**
 * Настройки экрана с описанием причины запроса доступа.
 */
class PermissionDescriptionDialogStyle : Serializable {
    val imageStyle = PermissionDescriptionImageStyle()
    val titleStyle = PermissionDescriptionTextStyle()
    val messageStyle = PermissionDescriptionTextStyle()
    val positiveButtonStyle = PermissionDescriptionButtonStyle()
    val negativeButtonStyle = PermissionDescriptionButtonStyle()
    val backgroundStyle = PermissionDescriptionDialogBackgroundStyle()

    companion object {

        @JvmStatic
        fun getDefaultDialogStyle(type: PermissionDescriptionType) =
            when (type) {
                PermissionDescriptionType.STORAGE -> createTypeSpecificDialogStyle(
                    titleResId = R.string.ecc_permission_description_access_to_files_title,
                    messageResId = R.string.ecc_permission_description_access_to_files_message
                )
                PermissionDescriptionType.RECORD_AUDIO -> createTypeSpecificDialogStyle(
                    titleResId = R.string.ecc_permission_description_access_to_audio_recording_title,
                    messageResId = R.string.ecc_permission_description_access_to_audio_recording_message
                )
                PermissionDescriptionType.CAMERA -> createTypeSpecificDialogStyle(
                    titleResId = R.string.ecc_permission_description_access_to_camera_title,
                    messageResId = R.string.ecc_permission_description_access_to_camera_message
                )
            }

        private fun createTypeSpecificDialogStyle(
            @DrawableRes imageResId: Int = R.drawable.ecc_image_placeholder,
            @StringRes titleResId: Int,
            @StringRes messageResId: Int
        ) = getDefaultDialogStyle().apply {
            imageStyle.imageResId = imageResId
            titleStyle.textResId = titleResId
            messageStyle.textResId = messageResId
        }

        private fun getDefaultDialogStyle() =
            PermissionDescriptionDialogStyle().apply {
                messageStyle.textSizeSpResId = R.dimen.ecc_text_regular
                negativeButtonStyle.apply {
                    textResId = R.string.ecc_close
                    marginTopDpResId = R.dimen.ecc_margin_quarter
                    marginBottomDpResId = R.dimen.ecc_margin_material
                    backgroundColorResId = android.R.color.transparent
                }
            }
    }
}

/**
 * Настройка изображения.
 */
class PermissionDescriptionImageStyle : Serializable {
    @DrawableRes
    var imageResId: Int = R.drawable.ecc_image_placeholder

    @DimenRes
    var marginTopDpResId: Int = R.dimen.ecc_margin_material
    var layoutGravity: ContentGravity = ContentGravity.CENTER
}

/**
 * Настройки поля текста заголовка или подзаголовка.
 */
class PermissionDescriptionTextStyle : Serializable {
    @StringRes
    var textResId: Int = 0

    @StyleRes
    var textAppearanceResId: Int = 0
    var fontPath: String = ""

    @DimenRes
    var textSizeSpResId: Int = R.dimen.ecc_text_medium

    @ColorRes
    var textColorResId: Int = R.color.ecc_black

    @DimenRes
    var marginTopDpResId: Int = R.dimen.ecc_margin_three_fourth
    var gravity: ContentGravity = ContentGravity.CENTER
}

/**
 * Настройки кнопки.
 */
class PermissionDescriptionButtonStyle : Serializable {
    @StringRes
    var textResId: Int = R.string.ecc_allow

    @StyleRes
    var textAppearanceResId: Int = 0
    var fontPath: String = ""

    @DimenRes
    var textSizeSpResId: Int = R.dimen.ecc_text_medium

    @ColorRes
    var textColorResId: Int = R.color.ecc_black

    @DimenRes
    var marginTopDpResId: Int = R.dimen.ecc_margin_material

    @DimenRes
    var marginBottomDpResId: Int = R.dimen.ecc_margin_zero

    @DrawableRes
    var backgroundResId: Int = 0

    @DimenRes
    var cornerRadiusDpResId: Int = R.dimen.ecc_threads_radius_big

    @ColorRes
    var backgroundColorResId: Int = R.color.ecc_teal_009688

    @ColorRes
    var strokeColorResId: Int = 0

    @DimenRes
    var strokeWidthDpResId: Int = R.dimen.ecc_stroke_width_small
}

/**
 * Настройки фона и формы диалогового окна и кнопок.
 */
class PermissionDescriptionDialogBackgroundStyle : Serializable {
    @DrawableRes
    var backgroundResId: Int = 0

    @DimenRes
    var cornerRadiusDpResId: Int = R.dimen.ecc_threads_radius_big

    @ColorRes
    var backgroundColorResId: Int = R.color.ecc_cian_b2dfdb

    @ColorRes
    var strokeColorResId: Int = 0

    @DimenRes
    var strokeWidthDpResId: Int = R.dimen.ecc_stroke_width_small
}

enum class ContentGravity {
    LEFT, CENTER, RIGHT
}
