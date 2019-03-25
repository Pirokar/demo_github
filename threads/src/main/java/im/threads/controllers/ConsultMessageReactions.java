package im.threads.controllers;

import im.threads.model.ConsultInfo;

/**
 * Created by yuri on 01.09.2016.
 */
public interface ConsultMessageReactions {
    void consultConnected(ConsultInfo consultInfo);
    void onConsultLeft();
}
