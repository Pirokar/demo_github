package im.threads.business.utils

import im.threads.business.UserInfoBuilder

/**
 * Вспомогательный класс для работы с клиентом
 */
class ClientUseCase {
    /**
     * Инициирует clientId, проверяя наличие нового значение в настройках
     */
    fun initClientId() {
        val userInfo = getUserInfo()
        val newClientId = getTagNewClientId()
        val oldClientId = userInfo?.clientId

        val isClientHasNotChanged = newClientId == oldClientId
        if (isClientHasNotChanged) {
            tagNewClientId = ""
        }
    }

    /**
     * Проверяет значение clientId на пустоту (null)
     */
    fun isClientIdNotEmpty(): Boolean {
        return ramUserInfo?.clientId != null
    }

    /**
     * Сохраняет данные о клиенте
     * @param userInfo данные о клиенте
     */
    fun saveUserInfo(userInfo: UserInfoBuilder?) {
        ramUserInfo = userInfo
        tagNewClientId = userInfo?.clientId ?: ""
    }

    /**
     * Очищает сведения о пользователе в памяти
     */
    fun cleanUserInfoFromRam() {
        ramUserInfo = null
        tagNewClientId = null
    }

    /**
     * Возвращает данные о клиенте
     */
    fun getUserInfo() = ramUserInfo

    /**
     * Возвращает тэг нового clientId
     */
    fun getTagNewClientId() = tagNewClientId

    companion object {
        private var ramUserInfo: UserInfoBuilder? = null
        private var tagNewClientId: String? = null
    }
}
