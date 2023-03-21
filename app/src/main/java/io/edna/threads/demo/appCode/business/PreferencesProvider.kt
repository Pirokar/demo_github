package io.edna.threads.demo.appCode.business

import android.content.Context

class PreferencesProvider(private val context: Context) {
    fun putJsonToPreferences(json: String) {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, json)
            .commit()
    }

    fun cleanJsonOnPreferences() {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, "")
            .commit()
    }

    companion object {
        private const val preferenceName = "ecc_demo_json_preference"
        private const val jsonPreferenceKey = "ecc_demo_json_preference_key"
    }
}
