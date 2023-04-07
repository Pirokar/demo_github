package im.threads.ui.preferences

import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.preferences.PreferencesJava

/**
 * Класс совместимости для Preferences и Java кода уровня UI.
 */
class PreferencesJavaUI : PreferencesJava() {
    /**
     * Возвращает ClientNotificationDisplayType в режиме совместимости. Использовать только для вызовов из Java.
     */
    fun getClientNotificationDisplayType(): ClientNotificationDisplayType {
        val pref = preferences.get<String>(PreferencesUiKeys.CLIENT_NOTIFICATION_DISPLAY_TYPE)
        return ClientNotificationDisplayType.fromString(pref)
    }
}
