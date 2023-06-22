package im.threads.business.models

class Client {
    var id: Long? = null
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var externalClientId: String? = null
    var closedThreads: String? = null
    var lastThreadTime: String? = null
    var online: String? = null
    var blocked: Boolean? = null
    var blockRequested: Boolean? = null
    var data: String? = null
}
