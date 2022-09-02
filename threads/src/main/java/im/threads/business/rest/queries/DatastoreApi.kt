package im.threads.business.rest.queries

import im.threads.internal.Config

class DatastoreApi private constructor(config: Config) : ApiGenerator(config, true) {
    override fun createThreadsApi() {
        threadsApi = ThreadsApi(
            datastoreApi = apiBuild.create(ThreadsDatastoreApi::class.java)
        )
    }

    companion object {
        private var apiGenerator: DatastoreApi? = null

        @JvmStatic
        fun init(config: Config) {
            apiGenerator = DatastoreApi(config)
        }

        @JvmStatic
        fun get(): ThreadsApi {
            if (apiGenerator == null) {
                throw IllegalStateException("You should call \"init()\" for \"${DatastoreApi::class.java.simpleName}\" first!")
            }
            return apiGenerator!!.threadsApi
        }
    }
}
