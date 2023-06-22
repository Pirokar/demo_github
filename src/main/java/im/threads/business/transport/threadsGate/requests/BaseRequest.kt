package im.threads.business.transport.threadsGate.requests

import im.threads.business.transport.threadsGate.Action

abstract class BaseRequest<Data>(
    private val action: Action,
    private val correlationId: String?,
    private val data: Data
)
