package im.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushController;
import com.pushserver.android.PushMessage;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import im.threads.AnalyticsTracker;
import im.threads.BuildConfig;
import im.threads.activities.ChatActivity;
import im.threads.activities.ConsultActivity;
import im.threads.activities.ImagesActivity;
import im.threads.database.DatabaseHolder;
import im.threads.formatters.MessageFormatter;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.CompletionHandler;
import im.threads.model.ConsultChatPhrase;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.services.DownloadService;
import im.threads.services.NotificationService;
import im.threads.utils.Callback;
import im.threads.utils.CallbackNoError;
import im.threads.utils.ConsultWriter;
import im.threads.utils.DualFilePoster;
import im.threads.utils.FilePoster;
import im.threads.utils.FileUtils;
import im.threads.utils.MessageMatcher;
import im.threads.utils.PrefUtils;
import im.threads.utils.Seeker;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ChatController {
    private static final Handler h = new Handler(Looper.getMainLooper());
    public static final String PROGRESS_BROADCAST = "im.threads.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "im.threads.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "im.threads.controllers.DOWNLOAD_ERROR_BROADCAST";
    public static final String DEVICE_ID_IS_SET_BROADCAST = "im.threads.controllers.DEVICE_ID_IS_SET_BROADCAST";
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;
    public static final String TAG = "ChatController ";
    private ProgressReceiver mProgressReceiver;
    private ChatActivity activity;
    boolean isSearchingConsult;
    private DatabaseHolder mDatabaseHolder;
    private Context appContext;
    private static ChatController instance;
    int currentOffset = 0;
    private int searchOffset = 0;
    private boolean isActive;
    private long lastUserTypingSend = System.currentTimeMillis();
    private ConsultWriter mConsultWriter;
    private AnalyticsTracker mAnalyticsTracker;
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private List<ChatItem> lastItems = new ArrayList<>();
    private Seeker seeker = new Seeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;
    private Executor mMessagesExecutor = Executors.newSingleThreadExecutor();

    public static ChatController getInstance(final Context ctx,
                                             String clientId) {
        if (BuildConfig.DEBUG) Log.i(TAG, "getInstance clientId = " + clientId);
        if (instance == null) {
            instance = new ChatController(ctx);
        }
        if (clientId == null) clientId = PrefUtils.getClientID(ctx);
        if (TextUtils.isEmpty(PrefUtils.getClientID(ctx))
                || !clientId.equals(PrefUtils.getClientID(ctx))) {
            Log.i(TAG, "setting new client id");
            Log.i(TAG, "clientId = " + clientId);
            Log.i(TAG, "old client id = " + PrefUtils.getClientID(ctx));
            final String finalClientId = clientId;
            instance.mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    PrefUtils.setNewClientId(ctx, finalClientId);
                    if (PrefUtils.getDeviceAddress(ctx) == null) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "device address was not set, returning");
                        return;
                    }
                    try {
                        instance.cleanAll();
                        instance.activity.removeSearching();
                        instance.mConsultWriter.setCurrentConsultLeft();
                      /*  PushController.getInstance(ctx).setDeviceUid(UUID.randomUUID().toString());*/
                        PushController.getInstance(ctx).setClientId(finalClientId);
                        PrefUtils.setClientId(ctx, finalClientId);
                        PushController.getInstance(ctx)
                                .sendMessage(MessageFormatter.createClientAboutMessage(PrefUtils.getUserName(ctx), finalClientId, ""), true);
                        PushController.getInstance(ctx).resetCounterSync();
                        List<InOutMessage> messages = PushController.getInstance(instance.activity).getMessageHistory(20);
                        instance.mDatabaseHolder.putMessagesSync(MessageFormatter.format(messages));
                        ArrayList<ChatItem> phrases = (ArrayList<ChatItem>) instance.setLastAvatars(MessageFormatter.format(messages));
                        instance.activity.addChatItems(phrases);
                        PrefUtils.setClientIdWasSet(true, ctx);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            });
        }
        return instance;
    }

    public int getStateOfConsult() {
        if (mConsultWriter.istSearchingConsult()) return CONSULT_STATE_SEARCHING;
        else if (mConsultWriter.isConsultConnected()) return CONSULT_STATE_FOUND;
        else return CONSULT_STATE_DEFAULT;
    }

    @SuppressLint("all")
    public ChatController(final Context ctx) {
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(ctx);
        }
        if (mConsultWriter == null)
            mConsultWriter = new ConsultWriter(ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        appContext = ctx;
        mAnalyticsTracker = AnalyticsTracker.getInstance(ctx, PrefUtils.getGaTrackerId(ctx));
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
        if (BuildConfig.DEBUG) Log.i(TAG, "bindActivity:");
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
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, 20));
                    if (null != activity) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                activity.addChatItems(items);
                            }
                        });
                    }
                    currentOffset = items.size();
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (activity != null) {
                                try {
                                    PushController.getInstance(activity).resetCounterSync();
                                    List<ChatItem> dbItems = mDatabaseHolder.getChatItems(0, 20);
                                    List<ChatItem> serverItems = MessageFormatter.format(PushController.getInstance(appContext).getMessageHistory(20));
                                    if (dbItems.size() != serverItems.size()
                                            || !dbItems.containsAll(serverItems)) {
                                        Log.i(TAG, "not same!");
                                        mDatabaseHolder.putMessagesSync(serverItems);
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, 20));
                                                if (null != activity) activity.addChatItems(items);
                                            }
                                        });
                                    }
                                } catch (PushServerErrorException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
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
        intentFilter.addAction(DEVICE_ID_IS_SET_BROADCAST);
        activity.registerReceiver(mProgressReceiver, intentFilter);
    }


    public boolean isConsultFound() {
        return mConsultWriter.isConsultConnected();
    }

    private List<? extends ChatItem> setLastAvatars(List<? extends ChatItem> list) {
        for (ChatItem ci : list) {
            if (ci instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                ccm.setAvatarPath(mDatabaseHolder.getLastConsultAvatarPathSync(ccm.getConsultId()));
            }
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) ci;
                cp.setAvatarPath(mDatabaseHolder.getLastConsultAvatarPathSync(cp.getConsultId()));
            }
        }
        return list;
    }

    public void unbindActivity() {
        if (activity != null) activity.unregisterReceiver(mProgressReceiver);
        activity = null;
    }

    public void onUserInput(final UpcomingUserMessage upcomingUserMessage) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onUserInput: " + upcomingUserMessage);
        if (upcomingUserMessage == null) return;
        if (appContext == null && activity == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "(appContext == null && activity == null");
            return;
        }
        Context ctx = activity;
        if (ctx == null) ctx = appContext;
        if (BuildConfig.DEBUG) Log.i(TAG, "upcomingUserMessage = " + upcomingUserMessage);
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um, ctx);
        if (!mConsultWriter.isConsultConnected()) {
            if (activity != null) activity.setStateSearchingConsult();
            mConsultWriter.setSearchingConsult(true);
        }
        sendMessage(um);
    }

    private void sendMessage(final UserPhrase userPhrase) {
        ConsultInfo consultInfo = null;
        if (null != userPhrase.getQuote() && userPhrase.getQuote().isFromConsult()) {
            String id = userPhrase.getQuote().getQuotedPhraseId();
            consultInfo = new ConsultInfo(mConsultWriter.getName(id), id, mConsultWriter.getStatus(id), mConsultWriter.getPhotoUrl(id));
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "sendMessage: " + userPhrase);
        if (BuildConfig.DEBUG) Log.i(TAG, "sendMessage: " + mAnalyticsTracker);
        if (userPhrase.isWithPhrase())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setTextWasSent();
        if (userPhrase.isWithFile())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setFileWasSent();
        if (userPhrase.isWithQuote())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setQuoteWasSent();
        if (userPhrase.isCopy()) if (null != mAnalyticsTracker) mAnalyticsTracker.setCopyWasSent();
        try {
            if (!userPhrase.hasFile()) {
                PushController
                        .getInstance(appContext)
                        .sendMessageAsync(MessageFormatter.format(
                                userPhrase
                                , consultInfo
                                , null
                                , null), false, new RequestCallback<String, PushServerErrorException>() {
                            @Override
                            public void onResult(String string) {
                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "server answer on pharse sent with id " + string);
                                setMessageState(userPhrase, MessageState.STATE_SENT);
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
                final ConsultInfo finalConsultInfo = consultInfo;
                new DualFilePoster(
                        userPhrase.getFileDescription() != null ? userPhrase.getFileDescription() : null
                        , userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() != null ? userPhrase.getQuote().getFileDescription() : null : null
                        , activity) {
                    @Override
                    public void onResult(String mfmsFilePath, String mfmsQuoteFilePath) {
                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
                        PushController.getInstance(activity).sendMessageAsync(MessageFormatter.format(
                                userPhrase
                                , finalConsultInfo
                                , mfmsQuoteFilePath, mfmsFilePath)
                                , false
                                , new RequestCallback<String, PushServerErrorException>() {
                                    @Override
                                    public void onResult(String string) {
                                        if (BuildConfig.DEBUG)
                                            Log.i(TAG, "sending with files string = " + string);
                                        setMessageState(userPhrase, MessageState.STATE_SENT);
                                        if (activity != null)
                                            activity.setUserPhraseMessageId(userPhrase.getId(), string);
                                        mDatabaseHolder.setUserPhraseMessageId(userPhrase.getId(), string);
                                    }

                                    @Override
                                    public void onError(PushServerErrorException e) {
                                        Log.e(TAG, "error while sending message to server");
                                        e.printStackTrace();
                                        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                                        if (appContext != null) {
                                            Intent i = new Intent(appContext, NotificationService.class);
                                            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
                                            if (!isActive) appContext.startService(i);
                                        }
                                        if (activity != null && isActive) {//// TODO: 15.12.2016  for test
                                            String error =
                                                    "error 382  sending message to server" +
                                                            "\ncode = " + e.getErrorCode()
                                                            + "\n" + e.getMessage()
                                                            + "\n" + e.toString()
                                                            + "\nfilepath = " + userPhrase.getFileDescription().getFilePath()
                                                            + "\nisExist = " + new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).exists();
                                            if (e.getCause() != null)
                                                error += "\ncause = " + e.getCause().toString();
                                            if (new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).exists()) {
                                                error += "\nsize = " + new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).length();
                                            }
//                                            activity.showFullError(error); // TODO: 26.01.2017 возможно, придется убрать комментарий
                                            // activity.showConnectionError();
                                        }

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error while sending files to server");
                        e.printStackTrace();
                        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
                        if (appContext != null) {
                            Intent i = new Intent(appContext, NotificationService.class);
                            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
                            if (!isActive) appContext.startService(i);
                        }
                        if (activity != null && isActive) {
                            String error = " error 405  sending files to server"
                                    + "\n" + e.getMessage()
                                    + "\n" + e.toString()
                                    + "\nfilepath = " + userPhrase.getFileDescription().getFilePath()
                                    + "\nisExist = " + new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).exists();
                            if (new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).exists()) {
                                error += "\nsize = " + new File(userPhrase.getFileDescription().getFilePath().replace("file://", "")).length();
                            }
                            if (e.getCause() != null)
                                error += "\ncause = " + e.getCause().toString();
//                            activity.showFullError(error); // TODO: 26.01.2017 возможно, придется убрать комментарий
                        }
                        // activity.showConnectionError();
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
            if (activity != null && isActive) {
                String error = "Generic error 423"
                        + "\n" + e.getMessage()
                        + "\n" + e.toString();
                if (e.getCause() != null) {
                    error += "\ncause = " + e.getCause().toString();
                }
//                activity.showFullError(error); // TODO: 26.01.2017 возможно, придется убрать комментарий

            } //activity.showConnectionError();
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

    public void fancySearch(final String query,
                            final boolean forward,
                            final CallbackNoError<List<ChatItem>> callback) {
        if (!isAllMessagesDownloaded) downloadMessagesTillEnd();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (System.currentTimeMillis() > (lastFancySearchDate + 3000)) {
                        List<ChatItem> fromDb = mDatabaseHolder.getChatItems(0, -1);
                        if (lastItems == null || lastItems.size() == 0) lastItems = fromDb;
                        else {
                            if (lastSearchQuery.equalsIgnoreCase(query)) {
                                for (ChatItem ci : lastItems) {
                                    if (ci instanceof ChatPhrase) {
                                        if (((ChatPhrase) ci).isHighlight()) {
                                            ChatPhrase cp = (ChatPhrase) ci;
                                            if (fromDb.contains(cp)) {
                                                ((ChatPhrase) fromDb.get(fromDb.lastIndexOf(cp))).setHighLighted(true);
                                            }
                                        }
                                    }
                                }
                            }
                            lastItems = fromDb;
                        }
                        lastFancySearchDate = System.currentTimeMillis();
                    }
                    if (query.isEmpty() || !query.equals(lastSearchQuery)) seeker = new Seeker();
                    lastSearchQuery = query;
                    List<ChatItem> list = seeker.seek(lastItems, !forward, query);
                    callback.onCall(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void downloadMessagesTillEnd() {
        if (isDownloadingMessages) return;
        Log.e(TAG, "downloadMessagesTillEnd"); // TODO: 18.12.2016
        if (isAllMessagesDownloaded) return;
        if (appContext == null) return;
        mMessagesExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final int chunk = 100;
                    isDownloadingMessages = true;
                    List<InOutMessage> items = PushController.getInstance(appContext).getNextMessageHistory(chunk);
                    if (items == null || items.size() == 0) return;
                    currentOffset += items.size();
                    isAllMessagesDownloaded = items.size() != chunk;
                    List<ChatItem> chatItems = MessageFormatter.format(items);
                    mDatabaseHolder.putMessagesSync(chatItems);
                    isDownloadingMessages = false;
                    if (!isAllMessagesDownloaded) downloadMessagesTillEnd();
                } catch (PushServerErrorException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    void addMessage(final ChatItem cm, Context ctx) {
        mDatabaseHolder.putChatItem(cm);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (null != activity) {
                    ChatItem ci = setLastAvatars(Arrays.asList(new ChatItem[]{cm})).get(0);
                    activity.addChatItem(ci);
                    if (ci instanceof ConsultChatPhrase) {
                        activity.notifyConsultAvatarChanged(((ConsultChatPhrase) ci).getAvatarPath()
                                , ((ConsultChatPhrase) ci).getConsultId());
                    }
                }
            }
        });
        if (cm instanceof ConsultPhrase
                && ctx != null
                && isActive) {
            PushController.getInstance(ctx).notifyMessageRead(((ConsultPhrase) cm).getId());
            mDatabaseHolder.setMessageWereRead(((ConsultPhrase) cm).getMessageId());
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
        if (BuildConfig.DEBUG) Log.i(TAG, "onFileClick " + fileDescription);
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
//                target.setDataAndType(Uri.fromFile(file), "application/pdf");
                target.setDataAndType(FileProvider.getUriForFile(
                        activity, BuildConfig.APPLICATION_ID + ".fileprovider", file), "application/pdf"
                );
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    mAnalyticsTracker.setAttachmentWasOpened();
                    if (activity != null) activity.startActivity(target);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            if (activity != null) {
                activity.setMessageState(userPhrase.getMessageId(), MessageState.STATE_SENDING);
            }
            sendMessage(userPhrase);
        }
    }

    void cleanAll() throws PushServerErrorException {
        if (BuildConfig.DEBUG) Log.i(TAG, "cleanAll: ");
        mDatabaseHolder.cleanDatabase();
        if (activity != null) activity.cleanChat();
        mConsultWriter.setCurrentConsultLeft();
        mConsultWriter.setSearchingConsult(false);
        isSearchingConsult = false;
        searchOffset = 0;
        currentOffset = 0;
        h.removeCallbacksAndMessages(null);
        if (activity != null) {
            activity.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        }
    }

    public void setActivityIsForeground(boolean isForeground) {
        this.isActive = isForeground;
        if (isForeground && activity != null) {
            ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null
                    && cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                List<String> unread = mDatabaseHolder.getUnreaMessagesId();
                for (String id : unread) {
                    PushController.getInstance(activity).notifyMessageRead(id);
                    mDatabaseHolder.setMessageWereRead(id);
                }
            }
        }
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
        UserPhrase up = new UserPhrase("local" + UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription());
        up.setCopy(message.isCopyied());
        return up;
    }

    public void onSystemMessageFromServer(Context ctx, Bundle bundle) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onSystemMessageFromServer:");
        switch (MessageMatcher.getType(bundle)) {
            case MessageMatcher.TYPE_OPERATOR_TYPING:
                addMessage(new ConsultTyping(mConsultWriter.getCurrentConsultId(), System.currentTimeMillis(), mConsultWriter.getCurrentAvatarPath()), ctx);
                break;
            case MessageMatcher.TYPE_MESSAGES_READ:
                List<String> list = MessageFormatter.getReadIds(bundle);
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "onSystemMessageFromServer: read messages " + list);
                for (String s : list) {
                    if (activity != null) {
                        activity.setPhraseSentStatus(s, MessageState.STATE_WAS_READ);
                    }
                    if (mDatabaseHolder != null)
                        mDatabaseHolder.setStateOfUserPhrase(s, MessageState.STATE_WAS_READ);
                }
                break;
        }
    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        if (BuildConfig.DEBUG) Log.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet(activity));
        if (!PrefUtils.isClientIdNotEmpty(activity)) {
            callback.onSuccess(new ArrayList<ChatItem>());
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final int[] currentOffset = {activity.getCurrentItemsCount()};
                try {
                    List<InOutMessage> messages = PushController.getInstance(activity).getMessageHistory(currentOffset[0] + 20);
                    mDatabaseHolder.putMessagesSync(MessageFormatter.format(messages));
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], 20));
                    currentOffset[0] += chatItems.size();
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(chatItems);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], 20));
                    currentOffset[0] += chatItems.size();
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(chatItems);
                        }
                    });
                }
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
        if (BuildConfig.DEBUG) Log.i(TAG, "onConsultMessage: " + pushMessage);
        final ChatItem chatItem = MessageFormatter.format(pushMessage);
        ConsultMessageReaction consultReactor = new ConsultMessageReaction(
                mConsultWriter,
                new ConsultMessageReactions() {
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
        if (chatItem instanceof ConsultPhrase) {
            mAnalyticsTracker.setConsultMessageWasReceived();
        }
    }

    public void onConsultChoose(Activity activity, String consultId) {
        ConsultInfo info = mDatabaseHolder.getConsultInfoSync(consultId);
        if (info == null) info = new ConsultInfo("", consultId, "", "");
        Intent i = ConsultActivity.getStartIntent(activity, info.getPhotoUrl(), info.getName(), info.getStatus());
        activity.startActivity(i);
    }

    public void getLastUnreadConsultPhrase(CompletionHandler<ConsultPhrase> handler) {
        mDatabaseHolder.getLastUnreadPhrase(handler);
    }

   /* public void requestFilteredPhrases(boolean searchInServerHistory
            , final String query
            , final Callback<Pair<Boolean, List<ChatPhrase>>, Exception> callback) {

        if (!searchInServerHistory) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final List<ChatPhrase> list = (List<ChatPhrase>) setLastAvatars(mDatabaseHolder.queryChatPhrasesSync(query));
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(new Pair<>(true, list));
                        }
                    });
                }
            });
        } else {
            final int querySize = mDatabaseHolder.getMessagesCount() + searchOffset;
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final List<InOutMessage> inOutMessages = PushController.getInstance(activity).getMessageHistory(querySize);
                        mDatabaseHolder.putMessagesSync(MessageFormatter.format(inOutMessages));
                        final List<ChatPhrase> phrases = (List<ChatPhrase>) setLastAvatars(mDatabaseHolder.queryChatPhrasesSync(query));
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(new Pair<>(inOutMessages.size() >= querySize, phrases));
                            }
                        });
                        searchOffset += 20;
                    } catch (Exception e) {
                        e.printStackTrace();
                        final List<ChatPhrase> phrases = (List<ChatPhrase>) setLastAvatars(mDatabaseHolder.queryChatPhrasesSync(query));
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(new Pair<>(false, phrases));
                            }
                        });
                    }
                }
            });
        }
    }*/
/*
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

    }*/

    private void onSettingClientId(final Context ctx) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onSettingClientId:");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    if (PrefUtils.getNewClientID(ctx) == null) return;
                    try {
                        cleanAll();
                        PushController
                                .getInstance(ctx)
                                .setClientId(PrefUtils.getNewClientID(ctx));
                        PrefUtils.setClientId(ctx, PrefUtils.getNewClientID(ctx));
                        PrefUtils.setClientIdWasSet(true, ctx);

                        PushController.getInstance(ctx).resetCounterSync();

                        PushController
                                .getInstance(ctx)
                                .sendMessage(MessageFormatter.createClientAboutMessage(PrefUtils
                                                .getUserName(ctx),
                                        PrefUtils.getNewClientID(ctx), ""), true);

                        List<InOutMessage> messages = PushController.getInstance(activity).getMessageHistory(20);
                        mDatabaseHolder.putMessagesSync(MessageFormatter.format(messages));
                        if (activity != null)
                            activity.addChatItems((List<ChatItem>) setLastAvatars(MessageFormatter.format(messages)));
                        currentOffset = messages.size();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String getConsultNameById(String id) {
        return mConsultWriter.getName(id);
    }

    void setAllMessagesWereRead() {
        if (activity == null && appContext == null) return;
        Context cxt = activity;
        if (cxt == null) cxt = appContext;
        cxt.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        DatabaseHolder.getInstance(cxt).setAllMessagesRead(new CompletionHandler<Void>() {
            @Override
            public void onComplete(Void data) {

            }

            @Override
            public void onError(Throwable e, String message, Void data) {

            }
        });
        if (activity != null) activity.setAllMessagesWereRead();
    }

    private class ProgressReceiver extends BroadcastReceiver {
        private static final String TAG = "ProgressReceiver ";

        public ProgressReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive:");
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(PROGRESS_BROADCAST)) {
                Log.i(TAG, "onReceive: PROGRESS_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOADED_SUCCESSFULLY_BROADCAST)) {
                Log.i(TAG, "onReceive: DOWNLOADED_SUCCESSFULLY_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                fileDescription.setDownloadProgress(100);
                if (activity != null)
                    activity.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOAD_ERROR_BROADCAST)) {
                Log.i(TAG, "onReceive: DOWNLOAD_ERROR_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (activity != null && fileDescription != null) {
                    Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                    activity.onDownloadError(fileDescription, t);
                }
            } else if (action.equals(DEVICE_ID_IS_SET_BROADCAST)) {
                onSettingClientId(context);
            }
        }
    }

    public DatabaseHolder getmDatabaseHolder() {
        return mDatabaseHolder;
    }

    public Executor getmExecutor() {
        return mExecutor;
    }
}