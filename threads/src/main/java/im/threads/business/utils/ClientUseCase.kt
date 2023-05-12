package im.threads.business.utils

import im.threads.business.UserInfoBuilder
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys

/**
 * Вспомогательный класс для работы с клиентом
 */
class ClientUseCase(private val preferences: Preferences) {
    /**
     * Инициирует clientId, проверяя наличие нового значение в настройках
     */
    fun initClientId() {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val newClientId = preferences.get<String>(PreferencesCoreKeys.TAG_NEW_CLIENT_ID)
        val oldClientId = userInfo?.clientId

        val isClientHasNotChanged = newClientId == oldClientId
        if (isClientHasNotChanged) {
            preferences.save(PreferencesCoreKeys.TAG_NEW_CLIENT_ID, "")
        } else if (!newClientId.isNullOrEmpty()) {
            val nonNullUserInfo = if (userInfo != null) {
                userInfo.clientId = newClientId
                userInfo
            } else {
                UserInfoBuilder(newClientId)
            }
            LoggerEdna.info("Saving newClientId = $newClientId, oldClientId = $oldClientId")
            preferences.save(PreferencesCoreKeys.USER_INFO, nonNullUserInfo)
        }
    }

    /**
     * Проверяет значение clientId на пустоту (null)
     */
    fun isClientIdNotEmpty(): Boolean {
        return preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)?.clientId != null
    }
}
