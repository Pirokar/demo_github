package im.threads.internal.retrofit

import im.threads.internal.Config

class BackendApiGenerator private constructor() : ApiGenerator(Config.instance.serverBaseUrl) {
    override fun createThreadsApi() {
        threadsApi = ThreadsApi(
            apiBuild.create(OldThreadsBackendApi::class.java),
            apiBuild.create(NewThreadsBackendApi::class.java)
        )
    }

    companion object {
        private var apiGenerator: BackendApiGenerator? = null

        @JvmStatic
        fun getApi(): ThreadsApi {
            if (apiGenerator == null) {
                apiGenerator = BackendApiGenerator()
            }
            return apiGenerator!!.threadsApi
        }
    }
}
