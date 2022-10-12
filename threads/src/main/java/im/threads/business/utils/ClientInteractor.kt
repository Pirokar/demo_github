package im.threads.business.utils

import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys

class ClientInteractor(private val preferences: Preferences) {
    fun initClientId() {
        val newClientId = preferences.get<String>(PreferencesCoreKeys.TAG_NEW_CLIENT_ID)
        val oldClientId = preferences.get<String>(PreferencesCoreKeys.TAG_CLIENT_ID)
        LoggerEdna.info("getInstance newClientId = $newClientId, oldClientId = $oldClientId")

        val isClientHasNotChanged = newClientId == oldClientId
        if (isClientHasNotChanged) {
            preferences.save(PreferencesCoreKeys.TAG_NEW_CLIENT_ID, "")
        } else if (!newClientId.isNullOrEmpty()) {
            preferences.save(PreferencesCoreKeys.TAG_CLIENT_ID, newClientId)
        }
    }
}
