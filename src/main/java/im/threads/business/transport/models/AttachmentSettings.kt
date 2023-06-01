package im.threads.business.transport.models

class AttachmentSettings {
    var clientId: String? = null
    var content: Content? = null

    class Content(maxSize: Int, fileExtensions: Array<String>) {
        var maxSize = maxSize
            private set
        var fileExtensions: Array<String> = fileExtensions
            private set
    }
}
