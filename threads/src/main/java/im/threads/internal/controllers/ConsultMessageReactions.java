package im.threads.internal.controllers;

import im.threads.internal.model.ConsultInfo;

/**
 * Created by yuri on 01.09.2016.
 */
public interface ConsultMessageReactions {
    void consultConnected(ConsultInfo consultInfo);
    void onConsultLeft();
}
