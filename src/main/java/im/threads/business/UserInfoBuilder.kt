package im.threads.business

class UserInfoBuilder(var clientId: String) {

    init {
        require(clientId.isNotBlank()) { "clientId must not be empty" }
    }

    var authToken: String? = null
        private set
    var authSchema: String? = null
        private set
    var authMethod: AuthMethod = AuthMethod.HEADERS
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
    fun setAuthData(
        authToken: String?,
        authSchema: String?,
        authMethod: AuthMethod = AuthMethod.HEADERS
    ): UserInfoBuilder {
        this.authToken = authToken
        this.authSchema = authSchema
        this.authMethod = authMethod
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfoBuilder

        if (clientId != other.clientId) return false
        if (authToken != other.authToken) return false
        if (authSchema != other.authSchema) return false
        if (clientData != other.clientData) return false
        if (clientIdSignature != other.clientIdSignature) return false
        if (userName != other.userName) return false
        if (appMarker != other.appMarker) return false
        if (clientIdEncrypted != other.clientIdEncrypted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientId.hashCode()
        result = 31 * result + (authToken?.hashCode() ?: 0)
        result = 31 * result + (authSchema?.hashCode() ?: 0)
        result = 31 * result + (clientData?.hashCode() ?: 0)
        result = 31 * result + (clientIdSignature?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (appMarker?.hashCode() ?: 0)
        result = 31 * result + clientIdEncrypted.hashCode()
        return result
    }
}
