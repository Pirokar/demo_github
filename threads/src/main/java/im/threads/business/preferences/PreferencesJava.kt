package im.threads.business.preferences

import im.threads.business.serviceLocator.core.inject

/**
 * Класс совместимости для Preferences и Java кода уровня Core.
 */
open class PreferencesJava {
    protected val preferences: Preferences by inject()

    /**
     * Возвращает ThreadId в режиме совместимости. Использовать только для вызовов из Java.
     */
    fun getThreadId(): Long? = preferences.get(PreferencesCoreKeys.THREAD_ID)
}
