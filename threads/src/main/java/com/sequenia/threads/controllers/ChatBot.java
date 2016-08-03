package com.sequenia.threads.controllers;

import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

import com.pushserver.android.PushGcmIntentService;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.utils.ConsultInfo;

import java.util.UUID;

/**
 * Created by yuri on 27.07.2016.
 */
public class ChatBot {
    public boolean isBotActive = false;
    private ChatController controller;

    public ChatBot(ChatController controller) {
        this.controller = controller;
    }

    public ConsultPhrase convert(final UserPhrase up) {
        FileDescription fd = null;
        if (up.getFileDescription() != null) {
            String userFP = up.getFileDescription().getFilePath();
            String filepath = userFP;
            if (!filepath.contains("file://")) {
                filepath = "file://" + filepath;
            }
            fd = new FileDescription(up.getFileDescription().getFileSentTo(), filepath, 100500, System.currentTimeMillis());
        }
        Quote q = null;
        if (null != up.getQuote()) {
            q = new Quote("Я", up.getQuote().getText(), up.getFileDescription(), System.currentTimeMillis());
        }

        return new ConsultPhrase(fd, q, ConsultInfo.getCurrentConsultName(controller.appContext), UUID.randomUUID().toString(), up.getPhrase(), System.currentTimeMillis(), ConsultInfo.getCurrentConsultName(controller.appContext), ConsultInfo.getCurrentConsultPhoto(controller.appContext));
    }

    public void answerToUser(final UserPhrase up, final boolean showIsTyping) {
        if (up.getSentState() == MessageState.STATE_SENT_AND_SERVER_RECEIVED) {
            if (!controller.isSearchingConsult && !ConsultInfo.isConsultConnected(controller.appContext)) {
                controller.isSearchingConsult = true;
                Bundle b = new Bundle();
                b.putString("operatorStatus", "УВЧ СР!");
                b.putString("operatorName", "Чат Бот");
                b.putString("alert", "Оператор ");
                b.putString("operatorPhoto", Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://com.sequenia.appwithchat/drawable/consult_photo").toString());
                ConsultInfo.setCurrentConsultInfo(UUID.randomUUID().toString(), b, controller.appContext);
                controller.activity.setTitleStateSearchingConsult();
                controller.h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (controller.activity != null) {
                            controller.activity.setTitleStateOperatorConnected(
                                    ConsultInfo.getCurrentConsultId(controller.activity)
                                    , ConsultInfo.getCurrentConsultName(controller.activity)
                                    , ConsultInfo.getCurrentConsultTitle(controller.activity));
                        }
                        ConsultConnectionMessage cc = new ConsultConnectionMessage(
                                "Чат Бот"
                                , ConsultConnectionMessage.TYPE_JOINED
                                , ConsultInfo.getCurrentConsultName(controller.activity)
                                , false
                                , System.currentTimeMillis()
                                , ConsultInfo.getCurrentConsultPhoto(controller.activity));
                        controller.addMessage(cc);
                        if (showIsTyping)
                            controller.addMessage(new ConsultTyping("Чат Бот", System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(controller.activity)));
                        postConsultPhrase(up, 2000, null);
                    }
                }, 3500);
            } else {
                if (showIsTyping) {
                    controller.activity.addMessage(new ConsultTyping("Чат Бот", System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(controller.activity)));
                }
                postConsultPhrase(up, 2000, null);
            }
        }
    }

    public boolean processServiceMessage(UpcomingUserMessage upcomingUserMessage) {
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("Thanks a lot")) {
            controller.cleanAll();
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot connect")) {
            Bundle b = new Bundle();
            b.putString(PushGcmIntentService.EXTRA_TYPE, "OPERATOR_JOINED");
            b.putString("operatorPhoto", "http://i.imgur.com/uJ8YJ.jpg");
            b.putString("operatorName", "Чат Бот");
            b.putString("operatorStatus", "УВЧ СР!");
            b.putString("alert", "Оператор Чат Бот присоединился");
            controller.onSystemMessageFromServer(controller.activity, b);
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot disconnect")) {
            Bundle b = new Bundle();
            b.putString(PushGcmIntentService.EXTRA_TYPE, "OPERATOR_LEFT");
            b.putString("operatorName", "Чат Бот");
            b.putString("alert", "Оператор Чат Бот  покинул чятик");
            controller.onSystemMessageFromServer(controller.activity, b);
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot on")) {
            isBotActive = true;
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot off")) {
            isBotActive = false;
            return true;
        }
        return false;
    }

    private void postConsultPhrase(final UserPhrase up, long delay, final CompletionHandler<ConsultPhrase> handler) {
        controller.h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConsultPhrase cp = convert(up);
                controller. addMessage(cp);
                if (handler == null) return;
                handler.setSuccessful(true);
                handler.onComplete(cp);

            }
        }, delay);
    }
    public void postConsultPhrase(final UserPhrase up) {
        controller.h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConsultPhrase cp = convert(up);
                controller. addMessage(cp);
            }
        }, 1000);
    }

    public void processUserPhrase(final UserPhrase up){
        final CompletionHandler<UserPhrase> handler  =new CompletionHandler<UserPhrase>() {
            @Override
            public void onComplete(UserPhrase data) {
                answerToUser(up,true);
            }

            @Override
            public void onError(Throwable e, String message, UserPhrase data) {

            }
        };
        controller.h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (controller.mDatabaseHolder.getMessagesCount() % 2 == 0) {
                    controller.setMessageState(up, MessageState.STATE_NOT_SENT);
                    handler.setSuccessful(false);
                    handler.onError(new NetworkErrorException("no connection"), "check internet connection", up);
                } else {
                    controller.setMessageState(up, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    handler.setSuccessful(true);
                    handler.onComplete(up);
                }
            }
        },1000);
    }
}
