package im.threads.business.transport

import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys

class ApplicationConfig(
    private val threadsGateProviderUid: String,
    private val threadsGateHuaweiProviderUid: String?,
    private val preferences: Preferences
) {

    fun getCloudPair(): String? {
        val fcmToken = preferences.get<String>(PreferencesCoreKeys.FCM_TOKEN)
        val hcmToken = preferences.get<String>(PreferencesCoreKeys.HCM_TOKEN)

        return fcmToken ?: hcmToken
    }
}

data class CloudPair(val providerUid: String, val token: String?)
