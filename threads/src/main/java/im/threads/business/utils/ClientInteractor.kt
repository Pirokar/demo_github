package im.threads.business.utils

import im.threads.business.logger.core.LoggerEdna
import im.threads.business.utils.preferences.PrefUtilsBase

class ClientInteractor {
    fun initClientId() {
        val newClientId = PrefUtilsBase.newClientID
        val oldClientId = PrefUtilsBase.clientID
        LoggerEdna.info("getInstance newClientId = $newClientId, oldClientId = $oldClientId")

        val isClientHasNotChanged = newClientId == oldClientId
        if (isClientHasNotChanged) {
            PrefUtilsBase.setNewClientId("")
        } else if (!newClientId.isNullOrEmpty()) {
            PrefUtilsBase.setClientId(newClientId)
        }
    }
}
