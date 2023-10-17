package im.threads.business.utils

import im.threads.business.UserInfoBuilder
import im.threads.business.preferences.Preferences

/**
 * Вспомогательный класс для работы с клиентом
 */
class ClientUseCase(private val preferences: Preferences) {
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
            preferences.save(TAG_NEW_CLIENT_ID_PREFS_KEY, tagNewClientId)
        } else if (!newClientId.isNullOrEmpty()) {
            val nonNullUserInfo = if (userInfo != null) {
                userInfo.clientId = newClientId
                userInfo
            } else {
                UserInfoBuilder(newClientId)
            }
            preferences.save(USER_INFO_PREFS_KEY, nonNullUserInfo)
        }
    }

    /**
     * Проверяет значение clientId на пустоту (null)
     */
    fun isClientIdNotEmpty(): Boolean {
        val userInfo = ramUserInfo ?: try {
            preferences.get(USER_INFO_PREFS_KEY)
        } catch (e: Exception) {
            null
        }
        return userInfo?.clientId != null
    }

    /**
     * Сохраняет данные о клиенте
     * @param userInfo данные о клиенте
     */
    fun saveUserInfo(userInfo: UserInfoBuilder?) {
        ramUserInfo = userInfo
        tagNewClientId = userInfo?.clientId ?: ""
        preferences.save(USER_INFO_PREFS_KEY, userInfo)
        preferences.save(TAG_NEW_CLIENT_ID_PREFS_KEY, tagNewClientId)
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
    fun getUserInfo() = ramUserInfo ?: try {
        preferences.get(USER_INFO_PREFS_KEY)
    } catch (e: Exception) {
        null
    }

    /**
     * Возвращает данные о новом clientId
     */
    fun getTagNewClientId() = tagNewClientId ?: try {
        preferences.get(TAG_NEW_CLIENT_ID_PREFS_KEY)
    } catch (e: Exception) {
        null
    }

    /**
     * Сохраняет данные о новом clientId
     * @param tag новый clientId
     */
    fun saveTagNewClientId(tag: String?) {
        tagNewClientId = tag
        preferences.save(tag ?: "", TAG_NEW_CLIENT_ID_PREFS_KEY)
    }

    companion object {
        const val USER_INFO_PREFS_KEY = "USER_INFO"
        const val TAG_NEW_CLIENT_ID_PREFS_KEY = "TAG_NEW_CLIENT_ID"
        private var ramUserInfo: UserInfoBuilder? = null
        private var tagNewClientId: String? = null
    }
}
