package im.threads.business.models

interface SystemMessage {
    fun getText(): String
    fun getType(): String
}
