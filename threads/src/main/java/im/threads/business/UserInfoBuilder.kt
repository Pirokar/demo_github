package im.threads.business

class UserInfoBuilder(var clientId: String) {
    var authToken: String? = null
        private set
    var authSchema: String? = null
        private set
    var clientData: String? = null
        private set
    var clientIdSignature: String? = null
        private set
    var userName: String? = null
        private set
    var appMarker: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                null
            } else {
                field
            }
        }
        private set

    /**
     * true if client id is encrypted
     */
    var clientIdEncrypted = false
    fun setAuthData(authToken: String?, authSchema: String?): UserInfoBuilder {
        this.authToken = authToken
        this.authSchema = authSchema
        return this
    }

    fun setClientIdSignature(clientIdSignature: String?): UserInfoBuilder {
        this.clientIdSignature = clientIdSignature
        return this
    }

    fun setUserName(userName: String?): UserInfoBuilder {
        this.userName = userName
        return this
    }

    /**
     * Any additional information can be provided in data string, i.e. "{balance:"1000.00", fio:"Vasya Pupkin"}"
     */
    @Deprecated("use {@link #setClientData(String)} instead")
    fun setData(clientData: String?): UserInfoBuilder {
        this.clientData = clientData
        return this
    }

    /**
     * Any additional information can be provided in data string, i.e. "{balance:"1000.00", fio:"Vasya Pupkin"}"
     */
    fun setClientData(clientData: String?): UserInfoBuilder {
        this.clientData = clientData
        return this
    }

    fun setAppMarker(appMarker: String?): UserInfoBuilder {
        this.appMarker = appMarker
        return this
    }

    fun setClientIdEncrypted(clientIdEncrypted: Boolean): UserInfoBuilder {
        this.clientIdEncrypted = clientIdEncrypted
        return this
    }

    init {
        require(clientId.isNotBlank()) { "clientId must not be empty" }
    }
}
