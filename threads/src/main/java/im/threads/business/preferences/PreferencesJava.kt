package im.threads.business.preferences

import im.threads.business.UserInfoBuilder
import im.threads.business.models.FileDescription
import im.threads.business.serviceLocator.core.inject

class PreferencesJava {
    private val preferences: Preferences by inject()

    fun getUserInfo(): UserInfoBuilder? = preferences.get(PreferencesCoreKeys.USER_INFO)

    fun getThreadId(): Long? = preferences.get(PreferencesCoreKeys.THREAD_ID)

    fun setThreadId(threadId: Long?) {
        preferences.save(PreferencesCoreKeys.THREAD_ID, threadId)
    }

    fun getFileDescriptionDraft(): FileDescription? {
        return preferences.get(PreferencesCoreKeys.FILE_DESCRIPTION_DRAFT)
    }

    fun setFileDescriptionDraft(fileDescription: FileDescription?) {
        preferences.save(PreferencesCoreKeys.FILE_DESCRIPTION_DRAFT, fileDescription)
    }
}
