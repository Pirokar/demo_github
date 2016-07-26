package com.sequenia.threads.controllers;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pushserver.android.PushController;
import com.pushserver.android.PushGcmIntentService;
import com.pushserver.android.PushMessage;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.RequestProgressCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.activities.ConsultActivity;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.ChatItem;
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
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Activity. all bells and whistles in activity,
 * all work here, mvc, right?
 * don't forget to unbindActivity() in ChatActivity onDestroy, to avoid leaks;
 */
public class ChatController extends Fragment {
    private static final Handler h = new Handler(Looper.getMainLooper());
    public static final String TAG = "ChatController ";
    private ChatActivity activity;
    private boolean isSearchingConsult;
    private HashMap<String, ArrayList<ProgressRunnable>> runableMap = new HashMap<>();
    private ArrayList<String> completedDownloads = new ArrayList<>();
    private DatabaseHolder mDatabaseHolder;
    private Context appContext;
    private boolean isBotActive = false;
    private static ChatController instance;
    private List<UpcomingUserMessage> pendingMessages = new ArrayList<>();

    public static ChatController getInstance(Context ctx) {
        if (instance == null) {
            instance = new ChatController(ctx);
        }
        return instance;
    }

    public ChatController() {
    }

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("all")
    public ChatController(Context ctx) {
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(ctx);
        }
    }

    public boolean isNeedToShowWelcome() {
        return !(mDatabaseHolder.getMessagesCount() > 0);
    }

    public void bindActivity(ChatActivity ca) {
        activity = ca;
        activity.connectedConsultId = ConsultInfo.getCurrentConsultId(ca);
        appContext = activity.getApplicationContext();
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(activity);
        }
        if (mDatabaseHolder.getMessagesCount() > 0) {
            mDatabaseHolder.getChatItemsAsync(0, 1000, new CompletionHandler<List<ChatItem>>() {
                @Override
                public void onComplete(List<ChatItem> data) {
                    if (null != activity) activity.addMessages(data);
                }

                @Override
                public void onError(Throwable e, String message, List<ChatItem> data) {
                }
            });
        }

        if (ConsultInfo.isConsultConnected(appContext)) {
            activity.setTitleStateOperatorConnected(ConsultInfo.getCurrentConsultName(appContext), ConsultInfo.getCurrentConsultName(appContext), ConsultInfo.getCurrentConsultTitle(appContext));
        }
    }

    public boolean isConsultFound() {
        return ConsultInfo.isConsultConnected(appContext);
    }

    public void unbindActivity() {
        activity = null;
        appContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onUserInput(final UpcomingUserMessage upcomingUserMessage) {
        if (upcomingUserMessage == null) return;
        if (processServiceMessage(upcomingUserMessage))
            return;//if message contains some service commands we just return
        if (appContext != null && upcomingUserMessage.getText() != null) {
            Log.e(TAG, "" + upcomingUserMessage.getFileDescription());// TODO: 25.07.2016
            String filePath = null;
            if (upcomingUserMessage.getFileDescription() != null) {
                filePath = upcomingUserMessage.getFileDescription().getPath();
                if (upcomingUserMessage.getFileDescription().getPath().contains("file://")) {
                    filePath = filePath.replace("file://", "");
                }
            }
            if (filePath == null) {//if it message with only text
                try {
                    PushController
                            .getInstance(appContext)
                            .sendMessageAsync(MessageFormatter.formatMessage(
                                    upcomingUserMessage.getText()
                                    , upcomingUserMessage.getQuote() == null ? null : upcomingUserMessage.getQuote().getText()
                                    , null
                                    , null).toString()
                                    , false
                                    , new RequestCallback<Void
                                            , PushServerErrorException>() {
                                        @Override
                                        public void onResult(Void aVoid) {
                                            Log.e(TAG, "onResult " + aVoid);
                                        }

                                        @Override
                                        public void onError(PushServerErrorException e) {
                                            Log.e(TAG, "onError " + e);
                                        }
                                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    pendingMessages.add(upcomingUserMessage);
                }
            } else {
                final String finalFilePath = filePath;
                PushController.getInstance(activity).sendFileAsync(new File(filePath), "", 0, new RequestProgressCallback() {
                    @Override
                    public void onProgress(double v) {
                        Log.e(TAG, "progress = " + v);
                    }

                    @Override
                    public void onResult(String s) {
                        Log.e(TAG, "onresult " + s);
                        try {
                            PushController
                                    .getInstance(appContext)
                                    .sendMessageAsync(
                                            (MessageFormatter
                                                    .formatMessage(
                                                            upcomingUserMessage.getText()
                                                            , upcomingUserMessage.getQuote().getText() == null ? null : upcomingUserMessage.getQuote().getText()
                                                            , finalFilePath
                                                            , s)).toString().replaceAll("\\\\","")

                                            , false
                                            , new RequestCallback<Void
                                                    , PushServerErrorException>() {
                                                @Override
                                                public void onResult(Void aVoid) {
                                                    Log.e(TAG, "onResult " + aVoid);
                                                }

                                                @Override
                                                public void onError(PushServerErrorException e) {
                                                    Log.e(TAG, "onError " + e);
                                                }
                                            });// TODO: 13.07.2016
                        } catch (Exception e) {
                            pendingMessages.add(upcomingUserMessage);
                        }
                    }

                    @Override
                    public void onError(PushServerErrorException e) {
                        Log.e(TAG, "on error " + e);
                    }
                });
            }
        }
       /* PushController.getInstance(ctx).getMessageHistoryAsync(1000, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
            @Override
            public void onResult(List<InOutMessage> inOutMessages) {
                Log.e(TAG, "getMessageHistoryAsync onResult" + inOutMessages);
            }

            @Override
            public void onError(PushServerErrorException e) {
                Log.e(TAG, "" + e);
            }
        });// TODO: 13.07.2016
        PushController.getInstance(ctx).getNextMessageHistoryAsync(1000, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
            @Override
            public void onResult(List<InOutMessage> inOutMessages) {
                Log.e(TAG, "getNextMessageHistoryAsync onResult" + inOutMessages);
            }

            @Override
            public void onError(PushServerErrorException e) {
                Log.e(TAG, "" + e);
            }
        });// TODO: 13.07.2016
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("Thanks a lot")) {
            cleanAll();
            return;
        }*/
        postUserPhrase(convert(upcomingUserMessage), 1000, new CompletionHandler<UserPhrase>() {
            @Override
            public void onComplete(UserPhrase data) {
                answerToUser(data, true);
            }

            @Override
            public void onError(Throwable e, String message, UserPhrase data) {
                data.setSentState(MessageState.STATE_NOT_SENT);
            }
        });
    }

    private ConsultPhrase convert(final UserPhrase up) {
        FileDescription fd = null;
        if (up.getFileDescription() != null) {
            String userFP = up.getFileDescription().getPath();
            String filepath = userFP;
            if (!filepath.contains("file://")) {
                filepath = "file://" + filepath;
            }
            fd = new FileDescription(up.getFileDescription().getHeader(), filepath, System.currentTimeMillis());
        }
        Quote q = null;
        if (null != up.getQuote()) {
            q = new Quote("Я", up.getQuote().getText(), System.currentTimeMillis());
        }

        return new ConsultPhrase(fd, q, ConsultInfo.getCurrentConsultName(appContext), UUID.randomUUID().toString(), up.getPhrase(), System.currentTimeMillis(), ConsultInfo.getCurrentConsultName(appContext), ConsultInfo.getCurrentConsultPhoto(appContext));
    }

    private void addMessage(final ChatItem cm) {
        mDatabaseHolder.putChatItem(cm);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (null != activity) activity.addMessage(cm);
            }
        });

    }

    public String getCurrentConsultName() {
        Context ctx = null;
        if (activity != null) {
            ctx = activity;
        }
        if (appContext != null) {
            ctx = activity;
        }
        if (ctx != null) {
            return ConsultInfo.getCurrentConsultName(ctx) + "%%" + ConsultInfo.getCurrentConsultTitle(ctx);
        }
        return "";
    }

    public void onDownloadRequest(final String path) {
        if (completedDownloads.contains(path)) return;
        if (runableMap.containsKey(path)) {
            ArrayList<ProgressRunnable> runnables = runableMap.remove(path);
            for (ProgressRunnable pr : runnables) {
                h.removeCallbacks(pr);
            }
            activity.updateProgress(path, 0);
            return;
        }
        ArrayList<ProgressRunnable> runnables = new ArrayList<>();
        runnables.add(new ProgressRunnable(path, 1));
        runnables.add(new ProgressRunnable(path, 15));
        runnables.add(new ProgressRunnable(path, 40));
        runnables.add(new ProgressRunnable(path, 75));
        runnables.add(new ProgressRunnable(path, 100) {
            @Override
            public void run() {
                super.run();
                completedDownloads.add(getPath());
            }
        });
        runableMap.put(path, runnables);
        h.postDelayed(runnables.get(0), 50);
        h.postDelayed(runnables.get(1), 1000);
        h.postDelayed(runnables.get(2), 2000);
        h.postDelayed(runnables.get(3), 3500);
        h.postDelayed(runnables.get(4), 4000);
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    if (isBotActive) answerToUser(userPhrase, true);
                }
            }, 2000);
        }
    }

    private class ProgressRunnable implements Runnable {
        private String path;
        private int progress;

        public ProgressRunnable(String path, int progress) {
            this.path = path;
            this.progress = progress;
        }

        public String getPath() {
            return path;
        }

        @Override
        public void run() {
            if (null != activity)
                activity.updateProgress(path, progress);
        }
    }

    private void cleanAll() {
        mDatabaseHolder.cleanDatabase();
        activity.cleanChat();
        ConsultInfo.setCurrentConsultLeft(appContext);
        isSearchingConsult = false;
        h.removeCallbacksAndMessages(null);
        runableMap = new HashMap<>();
        completedDownloads = new ArrayList<>();
    }

    private void answerToUser(final UserPhrase up, final boolean showIsTyping) {
        if (!isBotActive) return;
        if (up.getSentState() == MessageState.STATE_SENT_AND_SERVER_RECEIVED) {
            if (!isSearchingConsult && !ConsultInfo.isConsultConnected(appContext)) {
                isSearchingConsult = true;
                Bundle b = new Bundle();
                b.putString("operatorStatus", "УВЧ СР!");
                b.putString("operatorName", "Чат Бот");
                b.putString("alert", "Оператор ");
                b.putString("operatorPhoto", Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://com.sequenia.appwithchat/drawable/consult_photo").toString());
                ConsultInfo.setCurrentConsultInfo(UUID.randomUUID().toString(), b, appContext);
                activity.setTitleStateSearchingConsult();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (activity != null) {
                            activity.setTitleStateOperatorConnected(
                                    ConsultInfo.getCurrentConsultId(activity)
                                    , ConsultInfo.getCurrentConsultName(activity)
                                    , ConsultInfo.getCurrentConsultTitle(activity));
                        }
                        ConsultConnectionMessage cc = new ConsultConnectionMessage(
                                "Чат Бот"
                                , ConsultConnectionMessage.TYPE_JOINED
                                , ConsultInfo.getCurrentConsultName(activity)
                                , false
                                , System.currentTimeMillis()
                                , ConsultInfo.getCurrentConsultPhoto(activity));
                        addMessage(cc);
                        if (showIsTyping)
                            addMessage(new ConsultTyping("Чат Бот", System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(activity)));
                        postConsultPhrase(up, 2000, null);
                    }
                }, 3500);
            } else {
                if (showIsTyping) {
                    activity.addMessage(new ConsultTyping("Чат Бот", System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(activity)));
                }
                postConsultPhrase(up, 2000, null);
            }
        }
    }

    private void postConsultPhrase(final UserPhrase up, long delay, final CompletionHandler<ConsultPhrase> handler) {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConsultPhrase cp = convert(up);
                addMessage(cp);
                if (handler == null) return;
                handler.setSuccessful(true);
                handler.onComplete(cp);

            }
        }, delay);
    }

    private String postUserPhrase(final UserPhrase up, long sendDelay, final CompletionHandler<UserPhrase> handler) {
        addMessage(up);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBotActive) {
                    if (mDatabaseHolder.getMessagesCount() % 2 == 0) {
                        setMessageState(up, MessageState.STATE_NOT_SENT);
                        if (handler == null) return;
                        handler.setSuccessful(false);
                        handler.onError(new NetworkErrorException("no connection"), "check internet connection", up);
                    } else {
                        setMessageState(up, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                        if (handler == null) return;
                        handler.setSuccessful(true);
                        handler.onComplete(up);
                    }
                } else {
                    setMessageState(up, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    if (handler == null) return;
                    handler.setSuccessful(true);
                    handler.onComplete(up);
                }
            }
        }, sendDelay);
        return up.getId();
    }

    private void setMessageState(UserPhrase up, MessageState messageState) {
        up.setSentState(messageState);
        if (activity != null) {
            activity.setPhraseSentStatus(up.getId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhrase(up.getId(), up.getSentState());
    }

    private UserPhrase convert(UpcomingUserMessage message) {
        if (message == null)
            return new UserPhrase(UUID.randomUUID().toString(), "", null, System.currentTimeMillis(), null);
        if (message.getFileDescription() != null && !message.getFileDescription().getPath().contains("file://")) {
            message.getFileDescription().setPath("file://" + message.getFileDescription().getPath());
        }
        return new UserPhrase(UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription());
    }

    public void onSystemMessageFromServer(Context ctx, Bundle bundle) {
        Log.e(TAG, "current consult id is " + ConsultInfo.getCurrentConsultId(ctx));
     /*   Log.e(TAG, "alert = " + bundle.getString(PushGcmIntentService.EXTRA_ALERT));
        Log.e(TAG, "operatorStatus " + bundle.getString("operatorStatus"));
        Log.e(TAG, "messageId " + bundle.getString("messageId"));
        Log.e(TAG, "operatorName " + bundle.getString("operatorName"));
        Log.e(TAG, "type " + bundle.getString(PushGcmIntentService.EXTRA_TYPE));
        Log.e(TAG, "operatorPhoto " + bundle.getString("operatorPhoto"));*/
        switch (MessageMatcher.getType(bundle)) {
            case MessageMatcher.TYPE_MESSAGE:
                if (ConsultInfo.getCurrentConsultId(activity) == null) {
                    ConsultInfo.setCurrentConsultId(UUID.randomUUID().toString(), activity);
                }
                addMessage(
                        new ConsultPhrase(null
                                , null
                                , ConsultInfo.getCurrentConsultName(activity)
                                , bundle.getString("messageId")
                                , bundle.getString(PushGcmIntentService.EXTRA_ALERT)
                                , System.currentTimeMillis()
                                , ConsultInfo.getCurrentConsultId(activity) //before we have tru consult id. i just use it's name
                                , ConsultInfo.getCurrentConsultPhoto(activity)));

                break;
            case MessageMatcher.TYPE_OPERATOR_JOINED:
                Context notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultConnectionMessage(bundle.getString("operatorName"), ConsultConnectionMessage.TYPE_JOINED, bundle.getString("operatorName"), true, System.currentTimeMillis(), bundle.getString("operatorPhoto")));
                if (null != notNullContext) {
                    ConsultInfo.setCurrentConsultInfo(bundle.getString("operatorName"), bundle, notNullContext);
                }
                if (activity != null) {
                    activity.setTitleStateOperatorConnected(bundle.getString("operatorName"), bundle.getString("operatorName"), ConsultInfo.getCurrentConsultTitle(activity));
                }
                break;
            case MessageMatcher.TYPE_OPERATOR_LEFT:
                notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultConnectionMessage(bundle.getString("operatorName"), ConsultConnectionMessage.TYPE_LEFT, bundle.getString("operatorName"), true, System.currentTimeMillis(), ConsultInfo.getConsultPhoto(notNullContext, bundle.getString("operatorName"))));
                if (null != notNullContext) {
                    ConsultInfo.setCurrentConsultLeft(notNullContext);
                }
                if (activity != null) {
                    activity.setTitleStateDefault();
                }
                break;
            case MessageMatcher.TYPE_OPERATOR_TYPING:
                notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultTyping(ConsultInfo.getCurrentConsultId(notNullContext), System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(appContext)));
                break;
            default:
                Log.e(TAG, "unknown message type " + bundle);
        }
        //  addMessage(new ConsultPhrase(getSmallConsultAvatarPath(), System.currentTimeMillis(), chatPhrase, UUID.randomUUID().toString(), "", null, null));
    }

    public synchronized void onConsultMessage(PushMessage pushMessage) throws JSONException {
        JSONObject fullMessage = new JSONObject(pushMessage.getFullMessage());
        Log.e(TAG, "" + pushMessage);
        String messageId = pushMessage.getMessageId();
        long timeStamp = pushMessage.getSentAt();
        String message = fullMessage.getString("text") == null ? pushMessage.getShortMessage() : fullMessage.getString("text");
        JSONObject operatorInfo = fullMessage.getJSONObject("operator");
        final String name = operatorInfo.getString("name");
        String status = operatorInfo.getString("status");
        String photoUrl = operatorInfo.getString("photoUrl");
        JSONArray attachemnts = fullMessage.getJSONArray("attachments");
        JSONArray quotes = fullMessage.getJSONArray("quotes");
        final String title = ConsultInfo.getCurrentConsultTitle(getActivity());
        ConsultInfo.setCurrentConsultInfo(
                name
                , status
                , name
                , title == null ? "Оператор" : title
                , photoUrl
                , activity);
        addMessage(
                new ConsultPhrase(null
                        , null
                        , name
                        , messageId
                        , message
                        , timeStamp
                        , name //before we have tru consult id. i just use it's name
                        , photoUrl));
        h.post(new Runnable() {
            @Override
            public void run() {
                if (activity != null && name != null && title != null) {
                    activity.setTitleStateOperatorConnected(name, name, title);
                }
            }
        });

    }

    public void onPushInit(Context ctx) {
        for (UpcomingUserMessage upm : pendingMessages) {
            if (ctx != null && upm.getText() != null) {
                PushController.getInstance(ctx).sendMessageAsync(upm.getText(), false, new RequestCallback<Void, PushServerErrorException>() {
                    @Override
                    public void onResult(Void aVoid) {
                        Log.e(TAG, "onResult " + aVoid);
                    }

                    @Override
                    public void onError(PushServerErrorException e) {
                        Log.e(TAG, "onError " + e);
                    }
                });
            }
        }
        pendingMessages.clear();
    }

    public void onConsultChoose(Activity activity, String consultId) {
        Intent i = ConsultActivity.getStartIntent(activity, ConsultInfo.getConsultPhoto(activity, consultId), ConsultInfo.getConsultName(activity, consultId), ConsultInfo.getConsultStatus(activity, consultId));
        activity.startActivity(i);
    }

    private boolean processServiceMessage(UpcomingUserMessage upcomingUserMessage) {
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("Thanks a lot")) {
            cleanAll();
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot connect")) {
            Bundle b = new Bundle();
            b.putString(PushGcmIntentService.EXTRA_TYPE, "OPERATOR_JOINED");
            b.putString("operatorPhoto", "http://i.imgur.com/uJ8YJ.jpg");
            b.putString("operatorName", "Тестовый оператор №100500");
            b.putString("operatorStatus", "Тестовый статус тестового оператора");
            b.putString("alert", "Оператор Тестовый оператор №100500 присоединился");
            onSystemMessageFromServer(activity, b);
            return true;
        }
        if (upcomingUserMessage.getText() != null && upcomingUserMessage.getText().trim().equalsIgnoreCase("bot disconnect")) {
            Bundle b = new Bundle();
            b.putString(PushGcmIntentService.EXTRA_TYPE, "OPERATOR_LEFT");
            b.putString("operatorName", "Тестовый оператор №100500");
            b.putString("alert", "Оператор Тестовый оператор №100500 покинул чятик");
            onSystemMessageFromServer(activity, b);
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

    public String getConsultNameById(String id) {
        return ConsultInfo.getConsultName(activity, id);
    }
}
