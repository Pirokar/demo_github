package im.threads.business.rest.models

import im.threads.business.models.ConsultInfo
import im.threads.business.models.MessageFromHistory
import im.threads.business.transport.models.Operator

/**
 * ответ на запрос истории v2
 * в структуре появилась информация об операторе
 */
class HistoryResponse(val messages: List<MessageFromHistory>) {
    private val agentInfo: AgentInfo? = null

    fun hasThread(): Boolean {
        return agentInfo?.thread != null
    }

    fun getConsultInfo(): ConsultInfo? {
        return agentInfo?.agent?.let { operator ->
            ConsultInfo(
                operator.aliasOrName,
                operator.id.toString(),
                operator.status,
                operator.organizationUnit,
                operator.photoUrl,
                operator.role
            )
        }
    }

    private inner class AgentInfo {
        var agent: Operator? = null
        var thread: Thread? = null
    }

    inner class Thread {
        var id: Integer? = null
        var state: String? = null
    }
}
