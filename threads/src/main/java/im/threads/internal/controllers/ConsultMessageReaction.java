package im.threads.internal.controllers;

import im.threads.internal.formatters.PushMessageType;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.utils.ConsultWriter;

final class ConsultMessageReaction {
    private ConsultWriter consultWriter;
    private ConsultMessageReactions reactions;

    ConsultMessageReaction(ConsultWriter consultWriter, ConsultMessageReactions reactions) {
        this.consultWriter = consultWriter;
        this.reactions = reactions;
    }

    void onPushMessage(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnectionMessage) {
            ConsultConnectionMessage ccm = (ConsultConnectionMessage) chatItem;
            if (ccm.getType().equalsIgnoreCase(PushMessageType.OPERATOR_JOINED.name())) {
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
        }
    }
}
