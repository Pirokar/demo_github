package im.threads.ui.extensions

import android.content.Context
import android.content.res.Configuration
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib

/**
 * Возвращает положительное значение, если на устройстве включена темная тема
 */
fun Context.isDarkThemeOn(): Boolean {
    val isDarkThemeEnabledLocally = ThreadsLib.getInstance().currentUiTheme == CurrentUiTheme.DARK
    return isDarkThemeEnabledLocally ||
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
}
