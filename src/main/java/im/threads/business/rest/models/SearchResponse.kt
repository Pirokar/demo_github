package im.threads.business.rest.models

import im.threads.business.models.MessageFromHistory

class SearchResponse {
    var total: Int? = null
    var pages: Int? = null
    var content: List<MessageFromHistory>? = null
}
