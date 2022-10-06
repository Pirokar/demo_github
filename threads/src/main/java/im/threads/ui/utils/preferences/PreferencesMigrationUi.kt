package im.threads.ui.utils.preferences

import android.content.Context
import im.threads.business.utils.preferences.PreferencesMigrationBase

internal class PreferencesMigrationUi(context: Context) : PreferencesMigrationBase(context) {
    override val keys = PrefUtilsKeys()
}
