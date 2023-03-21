package im.threads.business.preferences

/**
 * Ключи, подлежашие миграции на Preferences
 */
internal class PrefKeysForMigration {
    val APP_MARKER = "APP_MARKER"
    val TAG_CLIENT_ID = "TAG_CLIENT_ID"
    val AUTH_TOKEN = "AUTH_TOKEN"
    val AUTH_SCHEMA = "AUTH_SCHEMA"
    val CLIENT_ID_SIGNATURE = "CLIENT_ID_SIGNATURE"
    val DEFAULT_CLIENT_NAMETITLE_TAG = "DEFAULT_CLIENT_NAMETITLE_TAG"
    val EXTRA_DATE = "EXTRA_DATE"
    val TAG_CLIENT_ID_ENCRYPTED = "TAG_CLIENT_ID_ENCRYPTED"

    val list = arrayListOf(
        APP_MARKER,
        TAG_CLIENT_ID,
        AUTH_TOKEN,
        AUTH_SCHEMA,
        CLIENT_ID_SIGNATURE,
        DEFAULT_CLIENT_NAMETITLE_TAG,
        EXTRA_DATE,
        TAG_CLIENT_ID_ENCRYPTED
    )
}
