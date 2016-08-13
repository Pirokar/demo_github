package com.sequenia.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.advisa.client.api.InOutMessage;
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
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.utils.ThreadsInitializer;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
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
    public static final String PROGRESS_BROADCAST = "com.sequenia.threads.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "com.sequenia.threads.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "com.sequenia.threads.controllers.DOWNLOAD_ERROR_BROADCAST";
    public static final String CLIENT_ID_IS_SET_BROADCAST = "com.sequenia.threads.controllers.CLIENT_ID_IS_SET_BROADCAST";
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    public static final String TAG = "ChatController ";
    private ProgressReceiver mProgressReceiver;
    ChatActivity activity;
    boolean isSearchingConsult;
    DatabaseHolder mDatabaseHolder;
    Context appContext;
    private static ChatController instance;
    int currentOffset = 0;
    private int searchOffset = 0;
    List<Pair<UpcomingUserMessage, UserPhrase>> pendingMessages = new ArrayList<>();

    public static ChatController getInstance(final Context ctx, final String clientId) {
        if (instance == null) {
            instance = new ChatController(ctx);
        }
        if (PrefUtils.getClientID(ctx) == null) {
            PrefUtils.setClientId(ctx, clientId);
        }
        try {
            if (!PrefUtils.isClientIdSet(ctx)
                    || !PrefUtils.getClientID(ctx).equals(clientId)) {
                Log.e(TAG, "setting client id async");// TODO: 13.08.2016  
                PushController.getInstance(ctx).setClientIdAsync(clientId, new RequestCallback<Void, PushServerErrorException>() {
                    @Override
                    public void onResult(Void aVoid) {
                        PushController.getInstance(ctx).sendMessageAsync(MessageFormatter.getStartMessage("Пупкин Василий Петрович", PrefUtils.getClientID(ctx), ""), true, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                Log.e(TAG, "client id was set");// TODO: 09.08.2016
                                PrefUtils.setClientId(ctx, clientId);
                                PrefUtils.setClientIdWasSet(true, ctx);
                                if (instance.mDatabaseHolder.getMessagesCount() == 0 && instance.activity != null) {
                                    instance.activity.showDownloading();
                                    PushController.getInstance(instance.activity).getMessageHistoryAsync(20, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
                                        @Override
                                        public void onResult(List<InOutMessage> inOutMessages) {
                                            ArrayList<ChatItem> phrases = MessageFormatter.format(inOutMessages);
                                            if (null != instance.activity) {
                                                instance.activity.removeDownloading();
                                            }
                                            instance.mDatabaseHolder.putMessagesAsync(phrases, new CompletionHandler<Void>() {
                                                @Override
                                                public void onComplete(Void data) {
                                                    instance.mDatabaseHolder.getChatItemsAsync(0, 20, new CompletionHandler<List<ChatItem>>() {
                                                        @Override
                                                        public void onComplete(List<ChatItem> data) {
                                                            if (null != instance.activity)
                                                                instance.activity.addMessages(data);
                                                            instance.currentOffset = data.size();
                                                        }

                                                        @Override
                                                        public void onError(Throwable e, String message, List<ChatItem> data) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onError(Throwable e, String message, Void data) {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(PushServerErrorException e) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(PushServerErrorException e) {

                            }
                        });
                    }

                    @Override
                    public void onError(PushServerErrorException e) {
                        Log.e(TAG, "error while setting client id" + e);
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public ChatController(final Context ctx) {
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
        currentOffset = 0;
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(activity);
        }
        if (mDatabaseHolder.getMessagesCount() > 0) {
            mDatabaseHolder.getChatItemsAsync(0, 20, new CompletionHandler<List<ChatItem>>() {
                @Override
                public void onComplete(List<ChatItem> data) {
                    if (null != activity) activity.addMessages(data);
                    currentOffset = data.size();
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
        intentFilter.addAction(CLIENT_ID_IS_SET_BROADCAST);
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
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um, activity);
        sendMessage(um);
    }

    private void sendMessage(final UserPhrase userPhrase) {
        try {
            if (!MessageFormatter.hasFile(userPhrase)) {
                PushController
                        .getInstance(appContext)
                        .sendMessageAsync(MessageFormatter.format(userPhrase, null, null), false, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                Log.e(TAG, "onResult sending without files");
                                setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                                activity.showConnectionError();
                            }
                        });
            } else {
                new DualFilePoster(
                        userPhrase.getFileDescription() != null ? userPhrase.getFileDescription() : null
                        , userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() != null ? userPhrase.getQuote().getFileDescription() : null : null//// TODO: 02.08.2016  fix bug with crash of sending quote with downloaded image
                        , activity) {
                    @Override
                    public void onResult(String mfmsFilePath, String mfmsQuoteFilePath) {
                        Log.e(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
                        PushController.getInstance(activity).sendMessageAsync(MessageFormatter.format(userPhrase, mfmsQuoteFilePath, mfmsFilePath), false, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                Log.e(TAG, "error while sending message to server");
                                setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                                activity.showConnectionError();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "error while sending files to server");
                        e.printStackTrace();
                        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                        activity.showConnectionError();
                    }
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
            if (!ThreadsInitializer.getInstance(activity).isInited())
                ThreadsInitializer.getInstance(activity).init();
            activity.showConnectionError();
        }
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (activity != null)
                            try {
                                PushController.getInstance(activity).notifyMessageUpdateNeeded();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }).start();
            }
        }, 60000);
        PushController.getInstance(activity).getMessageHistoryAsync(10, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
            @Override
            public void onResult(List<InOutMessage> inOutMessages) {
                Log.e(TAG, "" + MessageFormatter.format(inOutMessages));// TODO: 13.08.2016
            }

            @Override
            public void onError(PushServerErrorException e) {

            }
        });
    }

    void addMessage(final ChatItem cm, Context ctx) {
        mDatabaseHolder.putChatItem(cm);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (null != activity) activity.addMessage(cm);
            }
        });
        if (cm instanceof ConsultPhrase) {
            PushController.getInstance(ctx).notifyMessageRead(((ConsultPhrase) cm).getId());
        }
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

    public void onFileClick(final FileDescription fileDescription) {
        if (activity != null) {// TODO: 10.08.2016 implement opening of incoming images on click
            if (fileDescription.getFilePath() == null) {
                Intent i = new Intent(activity, DownloadService.class);
                i.putExtra(DownloadService.FD_TAG, fileDescription);
                activity.startService(i);
            } else if (fileDescription.hasImage()) {
                activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
            } else if (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PDF) {
                Intent target = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
                target.setDataAndType(Uri.fromFile(file), "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    startActivity(target);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_SENT || userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            sendMessage(userPhrase);
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
            return new UserPhrase("local" + UUID.randomUUID().toString(), "", null, System.currentTimeMillis(), null);
        if (message.getFileDescription() != null && !message.getFileDescription().getFilePath().contains("file://")) {
            message.getFileDescription().setFilePath("file://" + message.getFileDescription().getFilePath());
        }
        return new UserPhrase("local" + UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription());
    }

    public void onSystemMessageFromServer(Context ctx, Bundle bundle) {
        switch (MessageMatcher.getType(bundle)) {
            case MessageMatcher.TYPE_OPERATOR_JOINED:
                Context notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultConnectionMessage(bundle.getString("operatorName"), ConsultConnectionMessage.TYPE_JOINED, bundle.getString("operatorName"), true, System.currentTimeMillis(), bundle.getString("operatorPhoto")), ctx);
                if (null != notNullContext) {
                    ConsultInfo.setCurrentConsultInfo(bundle.getString("operatorName"), bundle, notNullContext);
                }
                if (activity != null) {
                    activity.setTitleStateOperatorConnected(bundle.getString("operatorName"), bundle.getString("operatorName"), ConsultInfo.getCurrentConsultTitle(activity));
                }
                break;
            case MessageMatcher.TYPE_OPERATOR_LEFT:
                notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultConnectionMessage(bundle.getString("operatorName"), ConsultConnectionMessage.TYPE_LEFT, bundle.getString("operatorName"), true, System.currentTimeMillis(), ConsultInfo.getConsultPhoto(notNullContext, bundle.getString("operatorName"))), ctx);
                if (null != notNullContext) {
                    ConsultInfo.setCurrentConsultLeft(notNullContext);
                }
                if (activity != null) {
                    activity.setTitleStateDefault();
                }
                break;
            case MessageMatcher.TYPE_OPERATOR_TYPING:
                notNullContext = appContext == null ? ctx : appContext;
                addMessage(new ConsultTyping(ConsultInfo.getCurrentConsultId(notNullContext), System.currentTimeMillis(), ConsultInfo.getCurrentConsultPhoto(appContext)), ctx);
                break;
            default:
                Log.e(TAG, "unknown message type " + bundle);
        }
    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        if (!PrefUtils.isClientIdSet(activity)) {
            callback.onSuccess(new ArrayList<ChatItem>());
            return;
        }
        final int[] currentOffset = {activity.getCurrentItemsCount()};
        mDatabaseHolder.getChatItemsAsync(currentOffset[0], 20, new CompletionHandler<List<ChatItem>>() {
            @Override
            public void onComplete(final List<ChatItem> data) {
                if (data.size() == 20) {
                    callback.onSuccess(data);
                } else {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            PushController.getInstance(activity).getMessageHistoryAsync(currentOffset[0] + 20, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
                                @Override
                                public void onResult(List<InOutMessage> inOutMessages) {
                                    mDatabaseHolder.putMessagesAsync(MessageFormatter.format(inOutMessages), new CompletionHandler<Void>() {
                                        @Override
                                        public void onComplete(Void data) {
                                            mDatabaseHolder.getChatItemsAsync(currentOffset[0], 20, new CompletionHandler<List<ChatItem>>() {
                                                @Override
                                                public void onComplete(List<ChatItem> data) {
                                                    callback.onSuccess(data);
                                                    currentOffset[0] += data.size();
                                                }

                                                @Override
                                                public void onError(Throwable e, String message, List<ChatItem> data) {
                                                    callback.onSuccess(data);
                                                    currentOffset[0] += data.size();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(Throwable e, String message, Void data) {
                                            callback.onSuccess(new ArrayList<ChatItem>());
                                            currentOffset[0] += 0;
                                        }
                                    });
                                }

                                @Override
                                public void onError(PushServerErrorException e) {
                                    callback.onSuccess(data);
                                    currentOffset[0] += data.size();
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onError(Throwable e, String message, List<ChatItem> data) {
                callback.onFail(e);
            }
        });
    }

    public synchronized void onConsultMessage(PushMessage pushMessage, Context ctx) throws JSONException {
        ConsultInfo.setCurrentConsultInfo(pushMessage, activity);
        ConsultPhrase consultPhrase = MessageFormatter.format(pushMessage);
        addMessage(consultPhrase, ctx);
        Log.e(TAG, "" + ConsultInfo.getCurrentConsultName(activity));
        Log.e(TAG, "" + ConsultInfo.getCurrentConsultTitle(activity));
        h.postDelayed(new Runnable() {
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
        }, 500);
    }

    public void onConsultChoose(Activity activity, String consultId) {
        Intent i = ConsultActivity.getStartIntent(activity, ConsultInfo.getConsultPhoto(activity, consultId), ConsultInfo.getConsultName(activity, consultId), ConsultInfo.getConsultStatus(activity, consultId));
        activity.startActivity(i);
    }

    public void requestFilteredPhrases(boolean searchInServerHistory
            , final String query
            , final Callback<Pair<Boolean
            , List<ChatPhrase>>, Exception> callback) {

        if (!searchInServerHistory) {
            mDatabaseHolder.queryChatPhrasesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
                @Override
                public void onComplete(final List<ChatPhrase> data) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(new Pair<>(true, data));
                        }
                    });
                }

                @Override
                public void onError(Throwable e, String message, List<ChatPhrase> data) {

                }
            });
        } else {
            final int querySize = mDatabaseHolder.getMessagesCount() + searchOffset;
            PushController.getInstance(activity).getMessageHistoryAsync(querySize, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
                @Override
                public void onResult(final List<InOutMessage> inOutMessages) {
                    mDatabaseHolder.putMessagesAsync(MessageFormatter.format(inOutMessages), new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(final Void avoid) {
                            mDatabaseHolder.queryChatPhrasesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
                                @Override
                                public void onComplete(final List<ChatPhrase> data) {
                                    h.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onSuccess(new Pair<>(inOutMessages.size() >= querySize, data));
                                            searchOffset += 20;
                                        }
                                    });
                                }

                                @Override
                                public void onError(Throwable e, String message, List<ChatPhrase> data) {

                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e, String message, Void data) {

                        }
                    });

                }

                @Override
                public void onError(PushServerErrorException e) {
                    mDatabaseHolder.queryChatPhrasesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
                        @Override
                        public void onComplete(final List<ChatPhrase> data) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(new Pair<>(true, data));
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e, String message, List<ChatPhrase> data) {

                        }
                    });
                }
            });
        }

    }

    private void onSettingClientId(Context ctx) {
        if (mDatabaseHolder.getMessagesCount() == 0 && activity != null) {
            activity.showDownloading();
            PushController.getInstance(instance.activity).getMessageHistoryAsync(20, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
                @Override
                public void onResult(List<InOutMessage> inOutMessages) {
                    activity.removeDownloading();
                    ArrayList<ChatItem> phrases = MessageFormatter.format(inOutMessages);
                    mDatabaseHolder.putMessagesAsync(phrases, new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(Void data) {
                            mDatabaseHolder.getChatItemsAsync(0, 20, new CompletionHandler<List<ChatItem>>() {
                                @Override
                                public void onComplete(List<ChatItem> data) {
                                    if (null != activity) {
                                        activity.addMessages(data);
                                        currentOffset = data.size();
                                    }
                                }

                                @Override
                                public void onError(Throwable e, String message, List<ChatItem> data) {
                                    activity.removeDownloading();
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e, String message, Void data) {
                            activity.removeDownloading();
                        }
                    });
                }

                @Override
                public void onError(PushServerErrorException e) {
                    activity.removeDownloading();
                }
            });
        }
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
                    if (t instanceof FileNotFoundException) {
                        Toast.makeText(activity, activity.getString(R.string.error_no_file), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (action.equals(CLIENT_ID_IS_SET_BROADCAST)) {
                onSettingClientId(context);
            }
        }
    }
}
