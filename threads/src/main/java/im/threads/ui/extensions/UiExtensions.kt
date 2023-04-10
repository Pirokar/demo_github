package im.threads.ui.extensions

import android.content.Context
import android.content.res.Configuration

/**
 * Возвращает положительное значение, если на устройстве включена темная тема
 */
fun Context.isDarkThemeOn(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
