package com.sequenia.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pushserver.android.PushController;
import com.pushserver.android.PushMessage;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.R;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.activities.ConsultActivity;
import com.sequenia.threads.activities.ImagesActivity;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.ConsultInfo;
import com.sequenia.threads.utils.DownloadService;
import com.sequenia.threads.utils.DualFilePoster;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.utils.PrefUtils;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Activity. all bells and whistles in activity,
 * all work here, mvc, right?
 * don't forget to unbindActivity() in ChatActivity onDestroy, to avoid leaks;
 */
public class ChatController extends Fragment {
    static final Handler h = new Handler(Looper.getMainLooper());
    public static final String PROGRESS_BROADCAST = "com.sequenia.threads.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "com.sequenia.threads.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "com.sequenia.threads.controllers.DOWNLOAD_ERROR_BROADCAST";
    public static final String CLIENT_ID_IS_SET_BROADCAST = "com.sequenia.threads.controllers.CLIENT_ID_IS_SET_BROADCAST";

    public static final String TAG = "ChatController ";
    private ProgressReceiver mProgressReceiver;
    ChatActivity activity;
    boolean isSearchingConsult;
    DatabaseHolder mDatabaseHolder;
    Context appContext;
    private ChatBot mChatBot;
    private static ChatController instance;
    int currentOffset = 0;
    List<Pair<UpcomingUserMessage, UserPhrase>> pendingMessages = new ArrayList<>();

    public static ChatController getInstance(Context ctx, String clientId) {
        if (instance == null) {
            instance = new ChatController(ctx, clientId);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("all")
    public ChatController(final Context ctx, final String clientId) {
        if (clientId == null) throw new IllegalArgumentException("client id is null");
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(ctx);
        }
        mChatBot = new ChatBot(this);
        if (PrefUtils.isClientIdSet(ctx) && !PrefUtils.getClientID(ctx).equals(clientId)) {
            PushController.getInstance(ctx).setClientIdAsync(clientId, new RequestCallback<Void, PushServerErrorException>() {
                @Override
                public void onResult(Void aVoid) {
                    PrefUtils.setClientId(ctx, clientId);
                }

                @Override
                public void onError(PushServerErrorException e) {
                    Log.e(TAG, "error while setting client id" + e);
                    e.printStackTrace();
                }
            });
        } else if (!PrefUtils.isClientIdSet(ctx)) PrefUtils.setClientId(ctx, clientId);
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
            mDatabaseHolder.getChatItemsAsync(0, 20, new CompletionHandler<List<ChatItem>>() {
                @Override
                public void onComplete(List<ChatItem> data) {
                    if (null != activity) activity.addMessages(data);
                    currentOffset = 20;
                }

                @Override
                public void onError(Throwable e, String message, List<ChatItem> data) {
                }
            });
        }

        if (ConsultInfo.isConsultConnected(appContext)) {
            activity.setTitleStateOperatorConnected(ConsultInfo.getCurrentConsultName(appContext), ConsultInfo.getCurrentConsultName(appContext), ConsultInfo.getCurrentConsultTitle(appContext));
        }
        mProgressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PROGRESS_BROADCAST);
        intentFilter.addAction(DOWNLOADED_SUCCESSFULLY_BROADCAST);
        intentFilter.addAction(DOWNLOAD_ERROR_BROADCAST);
        activity.registerReceiver(mProgressReceiver, intentFilter);
    }

    public boolean isConsultFound() {
        return ConsultInfo.isConsultConnected(appContext);
    }

    public void unbindActivity() {
        activity.unregisterReceiver(mProgressReceiver);
        activity = null;
        appContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onUserInput(final UpcomingUserMessage upcomingUserMessage) {//// TODO: 03.08.2016 implemet resend of pending messages
        if (upcomingUserMessage == null) return;
        Log.e(TAG, "upcomingUserMessage = " + upcomingUserMessage);// TODO: 29.07.2016
        if (mChatBot.processServiceMessage(upcomingUserMessage))
            return;//if message contains some service commands we just return
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um);
        if (mChatBot.isBotActive) {
            mChatBot.processUserPhrase(um);
        }
        try {
            if (!MessageFormatter.hasFile(upcomingUserMessage)) {// TODO: 04.08.2016 implement resned on usnet messages
                PushController
                        .getInstance(appContext)
                        .sendMessageAsync(MessageFormatter.format(upcomingUserMessage, null, null), false, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                Log.e(TAG, "onResult sending without files");
                                setMessageState(um, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                setMessageState(um, MessageState.STATE_NOT_SENT);
                            }
                        });
            } else {
                new DualFilePoster(
                        upcomingUserMessage.getFileDescription() != null ? upcomingUserMessage.getFileDescription() : null
                        , upcomingUserMessage.getQuote() != null ? upcomingUserMessage.getQuote().getFileDescription() != null ? upcomingUserMessage.getQuote().getFileDescription() : null : null//// TODO: 02.08.2016  fix bug with crash of sending quote with downloaded image
                        , activity) {
                    @Override
                    public void onResult(String mfmsFilePath, String mfmsQuoteFilePath) {
                        Log.e(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
                        PushController.getInstance(activity).sendMessageAsync(MessageFormatter.format(upcomingUserMessage, mfmsQuoteFilePath, mfmsFilePath), false, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                setMessageState(um, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                Log.e(TAG, "error while sending message to server");
                                setMessageState(um, MessageState.STATE_NOT_SENT);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "error while sending files to server");
                        e.printStackTrace();
                    }
                };
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            pendingMessages.add(new Pair<>(upcomingUserMessage, um));
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
    }

    void addMessage(final ChatItem cm) {
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

    public void onDownloadRequest(final FileDescription fileDescription) {
        Log.e(TAG, "onDownloadRequest with fd = " + fileDescription);// TODO: 01.08.2016
        if (activity != null) {
            if (fileDescription.getFilePath() == null) {
                Intent i = new Intent(activity, DownloadService.class);
                i.putExtra(DownloadService.FD_TAG, fileDescription);
                activity.startService(i);
            } else if (fileDescription.hasImage()) {
                activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
            }
        }
    }


    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    if (mChatBot.isBotActive) mChatBot.answerToUser(userPhrase, true);
                }
            }, 2000);
        }
    }

    void cleanAll() {
        mDatabaseHolder.cleanDatabase();
        activity.cleanChat();
        ConsultInfo.setCurrentConsultLeft(appContext);
        isSearchingConsult = false;
        h.removeCallbacksAndMessages(null);
    }


    void setMessageState(UserPhrase up, MessageState messageState) {
        up.setSentState(messageState);
        if (activity != null) {
            activity.setPhraseSentStatus(up.getId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhrase(up.getId(), up.getSentState());
    }

    private UserPhrase convert(UpcomingUserMessage message) {
        if (message == null)
            return new UserPhrase(UUID.randomUUID().toString(), "", null, System.currentTimeMillis(), null);
        if (message.getFileDescription() != null && !message.getFileDescription().getFilePath().contains("file://")) {
            message.getFileDescription().setFilePath("file://" + message.getFileDescription().getFilePath());
        }
        return new UserPhrase(UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription());
    }

    public void onSystemMessageFromServer(Context ctx, Bundle bundle) {
        switch (MessageMatcher.getType(bundle)) {
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
    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        Log.e(TAG, "requestItems currentOffset = " + currentOffset);// TODO: 09.08.2016  
        mDatabaseHolder.getChatItemsAsync(currentOffset, 20, new CompletionHandler<List<ChatItem>>() {
            @Override
            public void onComplete(List<ChatItem> data) {
                callback.onSuccess(data);
                currentOffset += data.size();
            }

            @Override
            public void onError(Throwable e, String message, List<ChatItem> data) {
                callback.onFail(e);
            }
        });
    }

    public synchronized void onConsultMessage(PushMessage pushMessage) throws JSONException {
        ConsultInfo.setCurrentConsultInfo(pushMessage, activity);
        ConsultPhrase consultPhrase = MessageFormatter.format(pushMessage);
        addMessage(consultPhrase);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (activity != null && ConsultInfo.getCurrentConsultName(activity) != null && ConsultInfo.getCurrentConsultTitle(activity) != null) {
                    activity
                            .setTitleStateOperatorConnected(
                                    ConsultInfo.getCurrentConsultName(activity)
                                    , ConsultInfo.getCurrentConsultName(activity)
                                    , ConsultInfo.getCurrentConsultTitle(activity));
                }
            }
        });
    }

    public void onPushInit(Context ctx) {
       /* for (UpcomingUserMessage upm : pendingMessages) {
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
        pendingMessages.clear();*/
    }

    public void onConsultChoose(Activity activity, String consultId) {
        Intent i = ConsultActivity.getStartIntent(activity, ConsultInfo.getConsultPhoto(activity, consultId), ConsultInfo.getConsultName(activity, consultId), ConsultInfo.getConsultStatus(activity, consultId));
        activity.startActivity(i);
    }

    public void requestFilterefPhrases(String query, final Callback<List<ChatPhrase>,Exception> callback){
        mDatabaseHolder.queryChatPhrasesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
            @Override
            public void onComplete(final List<ChatPhrase> data) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(data);
                    }
                });
            }
            @Override
            public void onError(Throwable e, String message, List<ChatPhrase> data) {
            }
        });
    }

    public String getConsultNameById(String id) {
        return ConsultInfo.getConsultName(activity, id);
    }

    private class ProgressReceiver extends BroadcastReceiver {
        private static final String TAG = "ProgressReceiver ";

        public ProgressReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(PROGRESS_BROADCAST)) {
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOADED_SUCCESSFULLY_BROADCAST)) {
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOAD_ERROR_BROADCAST)) {
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
                Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                if (activity != null) {
                    if (t instanceof FileNotFoundException){
                        Toast.makeText(activity, activity.getString(R.string.error_no_file), Toast.LENGTH_SHORT).show();
                    }

                }
            } else if (action.equals(CLIENT_ID_IS_SET_BROADCAST) && pendingMessages.size() != 0) {


            }
        }
    }
}
