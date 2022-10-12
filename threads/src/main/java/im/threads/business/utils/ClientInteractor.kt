package im.threads.business.utils

import im.threads.business.UserInfoBuilder
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys

class ClientInteractor(private val preferences: Preferences) {
    fun initClientId() {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val newClientId = preferences.get<String>(PreferencesCoreKeys.TAG_NEW_CLIENT_ID)
        val oldClientId = userInfo?.clientId
        LoggerEdna.info("getInstance newClientId = $newClientId, oldClientId = $oldClientId")

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
            preferences.save(PreferencesCoreKeys.USER_INFO, nonNullUserInfo)
        }
    }

    fun isClientIdNotEmpty(): Boolean {
        return preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)?.clientId != null
    }
}
