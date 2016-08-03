package com.sequenia.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
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
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.utils.ConsultInfo;
import com.sequenia.threads.utils.DualFilePoster;
import com.sequenia.threads.utils.FileDownloader;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Activity. all bells and whistles in activity,
 * all work here, mvc, right?
 * don't forget to unbindActivity() in ChatActivity onDestroy, to avoid leaks;
 */
public class ChatController extends Fragment {
    static final Handler h = new Handler(Looper.getMainLooper());
    public static final String TAG = "ChatController ";
    ChatActivity activity;
    boolean isSearchingConsult;
    Executor executor = Executors.newFixedThreadPool(3);
    private static HashMap<FileDescription, FileDownloader> runningDownloads = new HashMap<>();
    DatabaseHolder mDatabaseHolder;
    Context appContext;
    private ChatBot mChatBot;
    private static ChatController instance;
    List<UpcomingUserMessage> pendingMessages = new ArrayList<>();

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
        mChatBot = new ChatBot(this);
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
            if (!MessageFormatter.hasFile(upcomingUserMessage)) {
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
            pendingMessages.add(upcomingUserMessage);
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
        if (fileDescription == null || fileDescription.getDownloadPath() == null || fileDescription.getFilePath()!=null) {
            Log.e(TAG, "cant download with fileDescription = " + fileDescription);
            return;
        }
        if (!TextUtils.isEmpty(fileDescription.getFilePath())) return;
        if (runningDownloads.containsKey(fileDescription)) {
            FileDownloader fileDownloader = runningDownloads.get(fileDescription);
            fileDownloader.stop();
            fileDescription.setDownloadProgress(0);
            if (activity != null) activity.updateProgress(fileDescription);
            mDatabaseHolder.updateFileDescription(fileDescription);
            runningDownloads.remove(fileDescription);
            return;
        }
        Toast.makeText(activity, R.string.added_to_download_quee, Toast.LENGTH_SHORT).show();// TODO: 02.08.2016 refactor to service
        final Context context = getActivity();
        final FileDownloader fileDownloader = new FileDownloader(fileDescription.getDownloadPath(), fileDescription.getIncomingName(), activity) {
            @Override
            public void onProgress(double progress) {
                if (progress < 1) progress = 1.0;
                Log.e(TAG, "onprogress = " + progress);// TODO: 01.08.2016
                fileDescription.setDownloadProgress((int) progress);
                ChatController.getInstance(context).mDatabaseHolder.updateFileDescription(fileDescription);
                final double finalProgress = progress;
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalProgress < 100.0) {
                            if (ChatController.getInstance(context).activity != null) {
                                ChatController.getInstance(context).activity.updateProgress(fileDescription);
                            }
                        }
                    }
                });
            }

            @Override
            public void onComplete(final File file) {
                Log.e(TAG, "oncomplete");// TODO: 01.08.2016
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDescription.setDownloadProgress(100);// TODO: 02.08.2016
                        if (ChatController.getInstance(context).activity != null) {
                            ChatController.getInstance(context).activity.updateProgress(fileDescription);
                        }
                        fileDescription.setFilePath("file://" + file.getAbsolutePath());
                        ChatController.getInstance(context).mDatabaseHolder.updateFileDescription(fileDescription);
                        ChatController.getInstance(context).runningDownloads.remove(fileDescription);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                fileDescription.setDownloadProgress(0);
                Log.e(TAG, "error while downloading file " + e);
                e.printStackTrace();
                fileDescription.setDownloadProgress(0);
                ChatController.getInstance(context).mDatabaseHolder.updateFileDescription(fileDescription);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (activity != null) {
                            Toast.makeText(activity, R.string.error_no_file, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        };
        runningDownloads.put(fileDescription, fileDownloader);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                fileDownloader.download();
            }
        });
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

    public synchronized void onConsultMessage(PushMessage pushMessage) throws JSONException {
        ConsultInfo.setCurrentConsultInfo(pushMessage, activity);
        addMessage(MessageFormatter.format(pushMessage));
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


    public String getConsultNameById(String id) {
        return ConsultInfo.getConsultName(activity, id);
    }
}
