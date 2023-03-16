package im.threads.business.models

import java.util.UUID

class QuestionDTO {
    var correlationId: String = ""
    var id: Long = 0
    var sendingId: Long = 0
    var text: String? = null
    var scale = 0
    var simple = false
    var rate: Int? = null
    var phraseTimeStamp: Long = 0

    fun hasRate(): Boolean {
        return rate != null
    }

    fun generateCorrelationId() {
        correlationId = UUID.randomUUID().toString()
    }
}
