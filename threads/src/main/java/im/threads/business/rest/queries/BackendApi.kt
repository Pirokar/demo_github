package im.threads.business.rest.queries

import im.threads.internal.Config

class BackendApi private constructor(config: Config) : ApiGenerator(config) {
    override fun createThreadsApi() {
        threadsApi = ThreadsApi(
            apiBuild.create(OldThreadsBackendApi::class.java),
            apiBuild.create(NewThreadsBackendApi::class.java)
        )
    }

    companion object {
        private var apiGenerator: BackendApi? = null

        @JvmStatic
        fun init(config: Config) {
            apiGenerator = BackendApi(config)
        }

        @JvmStatic
        fun get(): ThreadsApi {
            if (apiGenerator == null) {
                throw IllegalStateException("You should call \"init()\" for \"${BackendApi::class.java.simpleName}\" first!")
            }
            return apiGenerator!!.threadsApi
        }
    }
}
