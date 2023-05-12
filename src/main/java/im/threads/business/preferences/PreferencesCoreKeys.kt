package im.threads.business.preferences

import im.threads.business.utils.ClientUseCase

/**
 * Ключи для Preferences уровня Core
 */
object PreferencesCoreKeys {
    private val migrationKeys = PrefKeysForMigration()

    val LAST_COPY_TEXT = "LAST_COPY_TEXT"
    val DEVICE_ADDRESS = "DEVICE_ADDRESS"
    val FCM_TOKEN = "FCM_TOKEN"
    val HCM_TOKEN = "HCM_TOKEN"
    val CLOUD_MESSAGING_TYPE = "CLOUD_MESSAGING_TYPE"
    val DEVICE_UID = "DEVICE_UID"
    val THREAD_ID = "THREAD_ID"
    val FILE_DESCRIPTION_DRAFT = "FILE_DESCRIPTION_DRAFT"
    val CAMPAIGN_MESSAGE = "CAMPAIGN_MESSAGE"
    val UNREAD_PUSH_COUNT = "UNREAD_PUSH_COUNT"
    val STORE_NAME = "im.threads.internal.utils.PrefStore"
    val ENCRYPTED_STORE_NAME = "im.threads.internal.utils.EncryptedPrefStore"
    val DATABASE_PASSWORD = "DATABASE_PASSWORD"
    val USER_SELECTED_UI_THEME_KEY = "USER_SELECTED_UI_THEME_KEY"
    val INIT_SENT_LAST_USER_ID = "INIT_SENT_LAST_USER_ID"
    val CHAT_STATE = "CHAT_STATE"

    val allPrefKeys = mutableListOf(
        LAST_COPY_TEXT,
        DEVICE_ADDRESS,
        FCM_TOKEN,
        HCM_TOKEN,
        CLOUD_MESSAGING_TYPE,
        DEVICE_UID,
        THREAD_ID,
        FILE_DESCRIPTION_DRAFT,
        CAMPAIGN_MESSAGE,
        ClientUseCase.USER_INFO_PREFS_KEY,
        UNREAD_PUSH_COUNT,
        STORE_NAME,
        ENCRYPTED_STORE_NAME,
        DATABASE_PASSWORD,
        USER_SELECTED_UI_THEME_KEY,
        INIT_SENT_LAST_USER_ID,
        CHAT_STATE
    ).apply { addAll(migrationKeys.list) }
}
