package im.threads.business.utils

import android.content.Context

class DemoModeProvider(private val context: Context) {
    fun getHistoryMock(): String {
        val preferences = context.getSharedPreferences("ecc_demo_json_preference", Context.MODE_PRIVATE)
        return preferences.getString("ecc_demo_json_preference_key", "") ?: ""
    }

    fun isDemoModeEnabled() = try {
        context.applicationInfo.packageName == "io.edna.threads.demo" &&
            context.getSharedPreferences("ecc_demo_json_preference", Context.MODE_PRIVATE)
                .getBoolean("ecc_is_demo_mode_enabled_key", false)
    } catch (exc: Exception) {
        false
    }
}
