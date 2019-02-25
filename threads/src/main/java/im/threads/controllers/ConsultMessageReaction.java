package im.threads.controllers;

import im.threads.formatters.PushMessageTypes;
import im.threads.model.ChatItem;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.utils.ConsultWriter;

/**
 * Created by yuri on 01.09.2016.
 */
public class ConsultMessageReaction {
    private static final String TAG = "ConsultMessageReaction ";
    ConsultWriter consultWriter;
    ConsultMessageReactions reactions;

    public ConsultMessageReaction(ConsultWriter consultWriter, ConsultMessageReactions reactions) {
        this.consultWriter = consultWriter;
        this.reactions = reactions;
    }

    public synchronized void onPushMessage(ChatItem chatItem) {

        if (chatItem instanceof ConsultConnectionMessage) {

            ConsultConnectionMessage ccm = (ConsultConnectionMessage) chatItem;

            if (ccm.getType().equalsIgnoreCase(PushMessageTypes.OPERATOR_JOINED.name())) {

                if (consultWriter != null) {
                    consultWriter.setSearchingConsult(false);
                    consultWriter.setCurrentConsultInfo(ccm);
                }

                if (reactions != null) {
                    reactions.consultConnected(new ConsultInfo(ccm.getName(), ccm.getConsultId(),
                            ccm.getStatus(), ccm.getOrgUnit(), ccm.getAvatarPath()));
                }

            } else {
                if (consultWriter != null) consultWriter.setCurrentConsultLeft();
                if (reactions != null) reactions.onConsultLeft();
            }

        } else if (chatItem instanceof ConsultPhrase) {

            ConsultPhrase cp = (ConsultPhrase) chatItem;

            if (consultWriter != null) {
                consultWriter.setSearchingConsult(false);
                consultWriter.setCurrentConsultInfo(cp);
            }

            if (reactions != null) {
                //TODO #THREADS-4426 What is this for? It overrides OPERATOR_JOINED info
                reactions.consultConnected(new ConsultInfo(cp.getConsultName(), cp.getConsultId(),
                        "", "", ""));
            }
        }

    }
}
