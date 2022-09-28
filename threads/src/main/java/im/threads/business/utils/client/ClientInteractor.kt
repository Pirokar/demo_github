package im.threads.business.utils.client

interface ClientInteractor {
    /**
     * Устанавливает clientId, проверяя ожидащий новый id в настройках
     */
    fun initClientId()
}
