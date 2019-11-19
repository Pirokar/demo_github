package im.threads.internal.transport.threads_gate.requests;

import im.threads.internal.transport.threads_gate.Action;

abstract class BaseRequest<Data> {

    private final Action action;

    private final String correlationId;

    private final Data data;

    BaseRequest(Action action, String correlationId, Data data) {
        this.action = action;
        this.correlationId = correlationId;
        this.data = data;
    }
}
