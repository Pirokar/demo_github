package im.threads.internal.retrofit

import im.threads.internal.Config

class DatastoreApiGenerator private constructor() : ApiGenerator(Config.instance.datastoreUrl) {
    override fun createThreadsApi() {
        threadsApi = ThreadsApi(
            datastoreApi = apiBuild.create(ThreadsDatastoreApi::class.java)
        )
    }

    companion object {
        private var apiGenerator: DatastoreApiGenerator? = null

        @JvmStatic
        fun getApi(): ThreadsApi {
            if (apiGenerator == null) {
                apiGenerator = DatastoreApiGenerator()
            }
            return apiGenerator!!.threadsApi
        }
    }
}
