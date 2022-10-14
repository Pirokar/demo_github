package im.threads.ui.preferences

import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.preferences.PreferencesJava

class PreferencesJavaUI : PreferencesJava() {
    fun getClientNotificationDisplayType(): ClientNotificationDisplayType {
        val pref = preferences.get<String>(PreferencesUiKeys.CLIENT_NOTIFICATION_DISPLAY_TYPE)
        return ClientNotificationDisplayType.fromString(pref)
    }
}
