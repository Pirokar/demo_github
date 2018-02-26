package im.threads.controllers;

import im.threads.formatters.PushMessageTypes;
import im.threads.model.ChatItem;
import im.threads.model.ConsultConnectionMessage;
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
                if(null != consultWriter) consultWriter.setSearchingConsult(false);
                if (null != consultWriter) consultWriter.setCurrentConsultInfo(ccm);
                if (null != reactions)
                    reactions.consultConnected(ccm.getConsultId(), ccm.getName(), ccm.getTitle());
            } else {
                  if(null != consultWriter)  consultWriter.setCurrentConsultLeft();
                   if(null != reactions) reactions.onConsultLeft();
            }
        } else if (chatItem instanceof ConsultPhrase) {
            ConsultPhrase cp = (ConsultPhrase) chatItem;
            if(null != consultWriter) consultWriter.setSearchingConsult(false);
            if(null != consultWriter)consultWriter.setCurrentConsultInfo(cp);
            if(null != reactions)reactions.consultConnected(cp.getConsultId(), cp.getConsultName(), null);
        }

    }
}
