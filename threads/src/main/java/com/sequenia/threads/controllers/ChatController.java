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
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.services.DownloadService;
import com.sequenia.threads.services.NotificationService;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.ConsultWriter;
import com.sequenia.threads.utils.DualFilePoster;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.utils.PrefUtils;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

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
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;
    public static final String TAG = "ChatController ";
    private ProgressReceiver mProgressReceiver;
    ChatActivity activity;
    boolean isSearchingConsult;
    DatabaseHolder mDatabaseHolder;
    Context appContext;
    private static ChatController instance;
    int currentOffset = 0;
    private int searchOffset = 0;
    private boolean isActive;
    private long lastUserTypingSend = System.currentTimeMillis();
    public static Picasso p;
    private boolean isInitError;
    private ConsultWriter mConsultWriter;


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
                Log.i(TAG, "setting client id async");
                PushController.getInstance(ctx).setClientIdAsync(clientId, new RequestCallback<Void, PushServerErrorException>() {
                    @Override
                    public void onResult(Void aVoid) {
                        PushController.getInstance(ctx).sendMessageAsync(MessageFormatter.getStartMessage(PrefUtils.getClientName(ctx), PrefUtils.getClientID(ctx), ""), true, new RequestCallback<String, PushServerErrorException>() {
                            @Override
                            public void onResult(String string) {
                                Log.e(TAG, "client id was set string =" + string);
                                PrefUtils.setClientId(ctx, clientId);
                                PrefUtils.setClientIdWasSet(true, ctx);
                                instance.cleanAll();
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
                                                                instance.activity.addChatItems(data);
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
            if (e instanceof IllegalStateException) {
                PushController.getInstance(ctx).init();
            }
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

    public int getStateOfConsult() {
        if (mConsultWriter.istSearchingConsult()) return CONSULT_STATE_SEARCHING;
        else if (mConsultWriter.isConsultConnected()) return CONSULT_STATE_FOUND;
        else return CONSULT_STATE_DEFAULT;
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
        if (mConsultWriter == null)
            mConsultWriter = new ConsultWriter(ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE));
    }

    public void onUserTyping() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUserTypingSend) >= 3000) {
            lastUserTypingSend = currentTime;
            try {
                PushController.getInstance(activity).sendMessageAsync(MessageFormatter.getMessageTyping(), true, new RequestCallback<String, PushServerErrorException>() {
                    @Override
                    public void onResult(String aVoid) {

                    }

                    @Override
                    public void onError(PushServerErrorException e) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isNeedToShowWelcome() {
        return !(mDatabaseHolder.getMessagesCount() > 0);
    }

    public void bindActivity(ChatActivity ca) {
        activity = ca;
        appContext = activity.getApplicationContext();
        currentOffset = 0;
        if (mConsultWriter == null) {
            mConsultWriter = new ConsultWriter(ca.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        }
        if (mConsultWriter.istSearchingConsult()) {
            activity.setStateSearchingConsult();
        }
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(activity);
        }
        if (mDatabaseHolder.getMessagesCount() > 0) {
            mDatabaseHolder.getChatItemsAsync(0, 20, new CompletionHandler<List<ChatItem>>() {
                @Override
                public void onComplete(List<ChatItem> data) {
                    if (null != activity) activity.addChatItems(data);
                    currentOffset = data.size();
                }

                @Override
                public void onError(Throwable e, String message, List<ChatItem> data) {
                }
            });
        }
        if (mConsultWriter.isConsultConnected()) {
            activity.setStateConsultConnected(mConsultWriter.getCurrentConsultId(), mConsultWriter.getCurrentConsultName(), mConsultWriter.getCurrentConsultTitle());
        } else if (mConsultWriter.istSearchingConsult()) {
            activity.setStateSearchingConsult();
        } else {
            activity.setTitleStateDefault();
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
        return mConsultWriter.isConsultConnected();
    }

    public void unbindActivity() {
        if (activity != null) activity.unregisterReceiver(mProgressReceiver);
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
        Log.i(TAG, "upcomingUserMessage = " + upcomingUserMessage);
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um, activity);
        if (!mConsultWriter.isConsultConnected()) {
            activity.setStateSearchingConsult();
            mConsultWriter.setSearchingConsult(true);
        }
        sendMessage(um);
    }

    private void sendMessage(final UserPhrase userPhrase) {
        try {
            if (!MessageFormatter.hasFile(userPhrase)) {
                PushController
                        .getInstance(appContext)
                        .sendMessageAsync(MessageFormatter.format(userPhrase, null, null), false, new RequestCallback<String, PushServerErrorException>() {
                            @Override
                            public void onResult(String string) {
                                setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                                mDatabaseHolder.setUserPhraseMessageId(userPhrase.getId(), string);
                                if (activity != null)
                                    activity.setUserPhraseMessageId(userPhrase.getId(), string);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                                if (activity != null && isActive) activity.showConnectionError();
                                if (appContext != null) {
                                    Intent i = new Intent(appContext, NotificationService.class);
                                    i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
                                    if (!isActive) appContext.startService(i);
                                }
                            }
                        });
            } else {
                new DualFilePoster(
                        userPhrase.getFileDescription() != null ? userPhrase.getFileDescription() : null
                        , userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() != null ? userPhrase.getQuote().getFileDescription() : null : null
                        , activity) {
                    @Override
                    public void onResult(String mfmsFilePath, String mfmsQuoteFilePath) {
                        Log.e(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
                        PushController.getInstance(activity).sendMessageAsync(MessageFormatter.format(userPhrase, mfmsQuoteFilePath, mfmsFilePath), false, new RequestCallback<String, PushServerErrorException>() {
                            @Override
                            public void onResult(String string) {
                                Log.e(TAG, "sending with files string = " + string);
                                setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                Log.e(TAG, "error while sending message to server");
                                setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                                if (appContext != null) {
                                    Intent i = new Intent(appContext, NotificationService.class);
                                    i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
                                    if (!isActive) appContext.startService(i);
                                }
                                if (activity != null && isActive) activity.showConnectionError();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "error while sending files to server");
                        e.printStackTrace();
                        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                        if (appContext != null) {
                            Intent i = new Intent(appContext, NotificationService.class);
                            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
                            if (!isActive) appContext.startService(i);
                        }
                        if (activity != null && isActive) activity.showConnectionError();
                    }
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
            if (e instanceof PushServerErrorException) {
                PushController.getInstance(activity).init();
            } else if (e instanceof IllegalStateException) {
                PushController.getInstance(activity).init();
            }
            if (activity != null && isActive) activity.showConnectionError();
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
    }

    void addMessage(final ChatItem cm, Context ctx) {
        mDatabaseHolder.putChatItem(cm);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (null != activity) activity.addChatItem(cm);
            }
        });
        if (cm instanceof ConsultPhrase && ctx != null) {
            PushController.getInstance(ctx).notifyMessageRead(((ConsultPhrase) cm).getId());
        }
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null && isActive) {
                    activity.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
                }
            }
        }, 1500);
    }


    public String getCurrentConsultName() {
        return mConsultWriter.getCurrentConsultName() + "%%" + mConsultWriter.getCurrentConsultTitle();
    }

    public void onFileClick(final FileDescription fileDescription) {
        if (activity != null) {
            if (fileDescription.getFilePath() == null) {
                Intent i = new Intent(activity, DownloadService.class);
                i.setAction(DownloadService.START_DOWNLOAD_FD_TAG);
                i.putExtra(DownloadService.FD_TAG, fileDescription);
                activity.startService(i);
            } else if (fileDescription.hasImage() && fileDescription.getFilePath() != null) {
                if (activity != null) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
                }
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
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            sendMessage(userPhrase);
        }
    }

    void cleanAll() {
        mDatabaseHolder.cleanDatabase();
        if (activity != null) activity.cleanChat();
        mConsultWriter.setCurrentConsultLeft();
        mConsultWriter.setSearchingConsult(false);
        isSearchingConsult = false;
        h.removeCallbacksAndMessages(null);
        if (activity != null) {
            activity.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (activity != null) {
                        try {
                            PushController.getInstance(activity).resetCounterSync();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public void setActivityIsForeground(boolean isForeground) {
        this.isActive = isForeground;
        mDatabaseHolder.setAllMessagesRead(new CompletionHandler<Void>() {
            @Override
            public void onComplete(Void data) {

            }

            @Override
            public void onError(Throwable e, String message, Void data) {

            }
        });
        if (isForeground) h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null)
                    activity.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
            }
        }, 1500);
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
        Log.d(TAG, "onSystemMessageFromServer:");
        switch (MessageMatcher.getType(bundle)) {
            case MessageMatcher.TYPE_OPERATOR_TYPING:
                addMessage(new ConsultTyping(mConsultWriter.getCurrentConsultId(), System.currentTimeMillis(), mConsultWriter.getCurrentAvatarPath()), ctx);
                break;
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

    public void onImageDownloadRequest(FileDescription fileDescription) {
        Intent i = new Intent(activity, DownloadService.class);
        i.setAction(DownloadService.START_DOWNLOAD_WITH_NO_STOP);
        i.putExtra(DownloadService.FD_TAG, fileDescription);
        activity.startService(i);
    }

    public synchronized void onConsultMessage(PushMessage pushMessage, Context ctx) throws JSONException {
        Log.i(TAG, "onConsultMessage: " + pushMessage);
        final ChatItem chatItem = MessageFormatter.format(pushMessage);
        ConsultMessageReactor consultReactor = new ConsultMessageReactor(mConsultWriter, new ConsultMessageReactions() {
            @Override
            public void consultConnected(final String id, final String name, final String title) {
                if (activity != null) activity.setStateConsultConnected(id, name, title);
            }

            @Override
            public void onConsultLeft() {
                if (null != activity) activity.setTitleStateDefault();
            }
        });
        consultReactor.onPushMessage(chatItem);
        addMessage(chatItem, ctx);
    }

    public void onConsultChoose(Activity activity, String consultId) {
        Intent i = ConsultActivity.getStartIntent(activity, mConsultWriter.getConsultAvatarPath(consultId), mConsultWriter.getConsultName(consultId), mConsultWriter.getConsultStatus(consultId));
        activity.startActivity(i);
    }

    public void getLastUnreadConsultPhrase(CompletionHandler<ConsultPhrase> handler) {
        mDatabaseHolder.getLastUnreadPhrase(handler);
    }

    public void requestFilteredPhrases(boolean searchInServerHistory
            , final String query
            , final Callback<Pair<Boolean, List<ChatPhrase>>, Exception> callback) {

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
                                    callback.onFail((Exception) e);
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e, String message, Void data) {
                            callback.onFail((Exception) e);
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
                            callback.onFail((Exception) e);
                        }
                    });
                }
            });
        }
    }

    public void requestFilteredFiles(boolean searchInServerHistory
            , final String query
            , final Callback<Pair<Boolean, List<ChatPhrase>>, Exception> callback) {

        if (!searchInServerHistory) {
            mDatabaseHolder.queryFilesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
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
                    callback.onFail((Exception) e);
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
                            mDatabaseHolder.queryFilesAsync(query, new CompletionHandler<List<ChatPhrase>>() {
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
                                    callback.onFail((Exception) e);
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e, String message, Void data) {
                            callback.onFail((Exception) e);
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
                            callback.onFail((Exception) e);
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
                                        activity.addChatItems(data);
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
        return mConsultWriter.getConsultName(id);
    }

    private class ProgressReceiver extends BroadcastReceiver {
        private static final String TAG = "ProgressReceiver ";

        public ProgressReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:");
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(PROGRESS_BROADCAST)) {
                Log.d(TAG, "onReceive: PROGRESS_BROADCAST " + intent.getParcelableExtra(DownloadService.FD_TAG));
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOADED_SUCCESSFULLY_BROADCAST)) {
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOAD_ERROR_BROADCAST)) {
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null) {
                    Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                    activity.onDownloadError(fileDescription, t);
                }
            } else if (action.equals(CLIENT_ID_IS_SET_BROADCAST)) {
                onSettingClientId(context);
            }
        }
    }
}
