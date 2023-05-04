package im.threads.business.database

import im.threads.business.config.BaseConfig
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DatabaseHolder private constructor() {
    private val mMyOpenHelper: ThreadsDbHelper = ThreadsDbHelper(BaseConfig.instance.context)

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> {
        return mMyOpenHelper.getChatItems(offset, limit)
    }

    val allFileDescriptions: Single<List<FileDescription>>
        get() = Single.fromCallable { mMyOpenHelper.allFileDescriptions }
            .subscribeOn(Schedulers.io())

    companion object {
        var instance: DatabaseHolder? = null
            get() {
                if (field == null) {
                    field = DatabaseHolder()
                }
                return field
            }
            private set
    }
}
