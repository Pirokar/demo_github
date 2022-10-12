package im.threads.business.preferences

object PreferencesCoreKeys {
    private val migrationKeys = PrefKeysForMigration()

    val TAG_CLIENT_ID = "TAG_CLIENT_ID"
    val TAG_NEW_CLIENT_ID = "TAG_NEW_CLIENT_ID"
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
    val USER_INFO = "USER_INFO"
    val STORE_NAME = "im.threads.internal.utils.PrefStore"
    val ENCRYPTED_STORE_NAME = "im.threads.internal.utils.EncryptedPrefStore"
    val IS_DATABASE_PASSWORD_MIGRATED = "IS_DATABASE_PASSWORD_MIGRATED"

    val allPrefKeys = listOf(
        TAG_CLIENT_ID,
        migrationKeys.TAG_CLIENT_ID_ENCRYPTED,
        migrationKeys.CLIENT_ID_SIGNATURE_KEY,
        migrationKeys.TAG_NEW_CLIENT_ID,
        migrationKeys.CLIENT_NAME,
        migrationKeys.EXTRA_DATA,
        LAST_COPY_TEXT,
        migrationKeys.APP_MARKER_KEY,
        DEVICE_ADDRESS,
        FCM_TOKEN,
        HCM_TOKEN,
        CLOUD_MESSAGING_TYPE,
        DEVICE_UID,
        migrationKeys.AUTH_TOKEN,
        migrationKeys.AUTH_SCHEMA,
        THREAD_ID,
        FILE_DESCRIPTION_DRAFT,
        CAMPAIGN_MESSAGE,
        UNREAD_PUSH_COUNT,
        STORE_NAME,
        ENCRYPTED_STORE_NAME,
        IS_DATABASE_PASSWORD_MIGRATED
    )
}
