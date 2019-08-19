package im.threads.internal.controllers;

import im.threads.internal.model.ConsultInfo;

public interface ConsultMessageReactions {
    void consultConnected(ConsultInfo consultInfo);
    void onConsultLeft();
}
