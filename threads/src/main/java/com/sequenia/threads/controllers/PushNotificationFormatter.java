package com.sequenia.threads.controllers;

import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 01.09.2016.
 */
public class PushNotificationFormatter {
    private String connectionMessageFemale, connectionMessageMale, leaveMessageFemale, leaveMessageMale, onlyFileMessage, withFileEnding, youHaveUnreadMessages, withFilesEnding;

    public PushNotificationFormatter(String connectionMessageFemale, String connectionMessageMale, String leaveMessageFemale, String leaveMessageMale, String onlyFileMessage, String withFileEnding, String youHaveUnreadMessages, String withFilesEnding) {
        this.connectionMessageFemale = connectionMessageFemale;
        this.connectionMessageMale = connectionMessageMale;
        this.leaveMessageFemale = leaveMessageFemale;
        this.leaveMessageMale = leaveMessageMale;
        this.onlyFileMessage = onlyFileMessage;
        this.withFileEnding = withFileEnding;
        this.youHaveUnreadMessages = youHaveUnreadMessages;
        this.withFilesEnding = withFilesEnding;
    }

    public List<String> format(
            List<ChatItem> unreadMessages
            , List<ChatItem> incomingPushes) {
        List<String> outPushes = new ArrayList<>();
        for (int i = 0; i < incomingPushes.size(); i++) {
            if (incomingPushes.get(i) instanceof ConsultPhrase || incomingPushes.get(i) instanceof ConsultConnectionMessage)
                unreadMessages.add(incomingPushes.get(i));
        }
        if (unreadMessages.size() == 1) {
            String phrase = null;
            String notif = "";
            if (unreadMessages.get(0) instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) unreadMessages.get(0);
                phrase = cp.getPhrase();
                if (phrase == null || phrase.equals("null")) phrase = "";
                boolean hasFile = cp.hasFile();
                if (phrase.length() != 0) {
                    notif = phrase;
                    if (hasFile) {
                        notif = notif + " " + withFileEnding;
                    }
                } else {
                    notif = onlyFileMessage;
                }
            } else if (unreadMessages.get(0) instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) unreadMessages.get(0);
                if (!ccm.getSex()
                        && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                    notif = ccm.getName() + " " + connectionMessageFemale;
                } else if (!ccm.getSex()
                        && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                    notif = ccm.getName() + " " + leaveMessageFemale;
                } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                    notif = ccm.getName() + " " + connectionMessageMale;
                } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                    notif = ccm.getName() + " " + leaveMessageMale;
                }
            }
            outPushes.add(notif);
        } else if (unreadMessages.size() <= 5 && unreadMessages.size() != 0) {
            String notif = "";
            for (ChatItem ci : unreadMessages) {
                notif = "";
                if (ci instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) ci;
                    String phrase = cp.getPhrase();
                    boolean hasFile = cp.hasFile();
                    if (phrase == null || phrase.equals("null")) phrase = "";
                    if (phrase.length() != 0) {
                        if (phrase.length() > 40) {
                            notif += phrase.substring(0, 40).concat("...");
                        } else {
                            notif += phrase;
                        }
                        if (hasFile) {
                            notif = notif + " " + withFileEnding;
                        }
                    } else {
                        notif = onlyFileMessage;
                    }
                } else if (ci instanceof ConsultConnectionMessage) {
                    ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                    if (!ccm.getSex()
                            && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                        notif = ccm.getName() + " " + connectionMessageFemale;
                    } else if (!ccm.getSex()
                            && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                        notif = ccm.getName() + " " + leaveMessageFemale;
                    } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                        notif = ccm.getName() + " " + connectionMessageMale;
                    } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                        notif = ccm.getName() + " " + leaveMessageMale;
                    }
                }
                outPushes.add(notif);
            }
        } else if (unreadMessages.size() > 5) {
            int filesnum = 0;
            for (ChatItem cp : unreadMessages) {
                if (cp instanceof ConsultPhrase && ((ConsultPhrase) cp).hasFile()) filesnum++;
            }
            outPushes.add(filesnum == 0 ? youHaveUnreadMessages : youHaveUnreadMessages + " " + withFilesEnding);
        }
        return outPushes;
    }
}
