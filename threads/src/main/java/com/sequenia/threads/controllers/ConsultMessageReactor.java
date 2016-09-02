package com.sequenia.threads.controllers;

import android.util.Log;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.ConsultWriter;

/**
 * Created by yuri on 01.09.2016.
 */
public class ConsultMessageReactor {
    private static final String TAG = "ConsultMessageReactor ";
    ConsultWriter consultWriter;
    ConsultMessageReactions reactions;

    public ConsultMessageReactor(ConsultWriter consultWriter, ConsultMessageReactions reactions) {
        this.consultWriter = consultWriter;
        this.reactions = reactions;
    }

    public synchronized void onPushMessage(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnectionMessage) {
            ConsultConnectionMessage ccm = (ConsultConnectionMessage) chatItem;
            if (ccm.getType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
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
