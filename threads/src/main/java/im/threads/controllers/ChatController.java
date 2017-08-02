package im.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.AnalyticsTracker;
import im.threads.BuildConfig;
import im.threads.activities.ChatActivity;
import im.threads.activities.ConsultActivity;
import im.threads.activities.ImagesActivity;
import im.threads.database.DatabaseHolder;
import im.threads.formatters.MessageFormatter;
import im.threads.fragments.ChatFragment;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ChatStyle;
import im.threads.model.CompletionHandler;
import im.threads.model.ConsultChatPhrase;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.EmptyChatItem;
import im.threads.model.FileDescription;
import im.threads.model.HistoryResponseV2;
import im.threads.model.MessageState;
import im.threads.model.MessgeFromHistory;
import im.threads.model.PushMessageCheckResult;
import im.threads.model.ScheduleInfo;
import im.threads.model.Survey;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.retrofit.RetrofitService;
import im.threads.retrofit.ServiceGenerator;
import im.threads.services.DownloadService;
import im.threads.services.NotificationService;
import im.threads.utils.Callback;
import im.threads.utils.CallbackNoError;
import im.threads.utils.ConsultWriter;
import im.threads.utils.DualFilePoster;
import im.threads.utils.FileUtils;
import im.threads.utils.MessageMatcher;
import im.threads.utils.PrefUtils;
import im.threads.utils.Seeker;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Fragment. all bells and whistles in fragment,
 * all work here
 * don't forget to unbindFragment() in ChatFragment onDestroy, to avoid leaks;
 */
public class ChatController {
    // Некоторые операции производятся в отдельном потоке через Executor.
    // Чтобы отправить результат из него в UI Thread используется Handler.
    private static final Handler h = new Handler(Looper.getMainLooper());
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private Executor mMessagesExecutor = Executors.newSingleThreadExecutor();

    // Сообщения для Broadcast Receivers
    public static final String PROGRESS_BROADCAST = "im.threads.controllers.PROGRESS_BROADCAST";
    public static final String DOWNLOADED_SUCCESSFULLY_BROADCAST = "im.threads.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST";
    public static final String DOWNLOAD_ERROR_BROADCAST = "im.threads.controllers.DOWNLOAD_ERROR_BROADCAST";
    public static final String DEVICE_ID_IS_SET_BROADCAST = "im.threads.controllers.DEVICE_ID_IS_SET_BROADCAST";

    // Состояния консультанта
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;

    public static final String TAG = "ChatController ";

    // Ссылка на фрагмент, которым управляет контроллер
    private ChatFragment fragment;

    // Для приема сообщений из сервиса по скачиванию файлов
    private ProgressReceiver mProgressReceiver;
    private boolean isSearchingConsult;
    private DatabaseHolder mDatabaseHolder;
    private Context appContext;
    private static ChatController instance;
    private int currentOffset = 0;
    private int searchOffset = 0;
    private Long lastMessageId;
    private boolean isActive;
    private long lastUserTypingSend = System.currentTimeMillis();
    private ConsultWriter mConsultWriter;
    private AnalyticsTracker mAnalyticsTracker;
    private List<ChatItem> lastItems = new ArrayList<>();
    private Seeker seeker = new Seeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;

    private static Long lastLoadId;

    // Используется для создания PendingIntent при открытии чата из пуш уведомления.
    // По умолчанию открывается ChatActivity.
    private static PendingIntentCreator pendingIntentCreator;
    private static ShortPushListener shortPushListener;
    private static FullPushListener fullPushListener;

    // Для оповещения об изменении количества непрочитанных сообщений
    private static WeakReference<UnreadMessagesCountListener> unreadMessagesCountListener;
    private String firstUnreadMessageId;

    /**
     * Оповещает о приходе короткого Push-уведомления.
     * Не срабатывает при опознанных системных Push-уведомлениях
     */
    public interface ShortPushListener {
        void onNewShortPushNotification(PushBroadcastReceiver pushBroadcastReceiver, Context context, String s, Bundle bundle);
    }

    /**
     * Оповещает о приходе полного Push-уведомления.
     * Не срабатывает, если удалось определить, что это уведомления для библиотеки чата.
     */
    public interface FullPushListener {
        void onNewFullPushNotification(PushServerIntentService pushServerIntentService, PushMessage pushMessage);
    }

    private interface ExceptionListener {
        void onException(Exception e);
    }

    public static ChatController getInstance(final Context ctx, String clientId) {
        if (BuildConfig.DEBUG) Log.i(TAG, "getInstance clientId = " + clientId);
        if (instance == null) {
            instance = new ChatController(ctx);
        }
        if (clientId == null) {
            clientId = PrefUtils.getClientID(ctx);
        }
        if ((TextUtils.isEmpty(PrefUtils.getClientID(ctx)) && !TextUtils.isEmpty(clientId))
                || !clientId.equals(PrefUtils.getClientID(ctx))) {
            Log.i(TAG, "setting new client id");
            Log.i(TAG, "clientId = " + clientId);
            Log.i(TAG, "old client id = " + PrefUtils.getClientID(ctx));

            final String finalClientId = clientId;
            // Начальная инициализация чата.
            // Сдесь происходит первоначальная загрузка истории сообщений,
            // отправка сообщения о клиенте
            // и т.п.
            instance.mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    onClientIdChanged(ctx, finalClientId);
                }
            });
        }
        return instance;
    }

    private static void onClientIdChanged(Context ctx, String finalClientId) {
        PrefUtils.setNewClientId(ctx, finalClientId);
        try {
            getPushControllerInstance(ctx);
        } catch (PushServerErrorException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "device address was not set, returning");
            return;
        }
        try {
            instance.cleanAll();
            if (instance.fragment != null) {
                instance.fragment.removeSearching();
            }
            instance.mConsultWriter.setCurrentConsultLeft();
            String oldClientId = PrefUtils.getClientID(ctx);
            if (!TextUtils.isEmpty(oldClientId)) {
                // send CLIENT_OFFLINE message
                sendMessageMFMSSync(ctx, MessageFormatter.getMessageClientOffline(oldClientId), true);
            }
            PrefUtils.setClientId(ctx, finalClientId);
            String environmentMessage = MessageFormatter.createEnvironmentMessage(PrefUtils.getUserName(ctx), finalClientId, ctx);
            sendMessageMFMSSync(ctx, environmentMessage, true);
            getPushControllerInstance(ctx).resetCounterSync();

            HistoryResponseV2 response = getHistorySync(ctx, null, null);
            List<ChatItem> serverItems = getChatItemFromHistoryResponse(response);
            instance.mDatabaseHolder.putMessagesSync(serverItems);
            ArrayList<ChatItem> phrases = (ArrayList<ChatItem>) instance.setLastAvatars(serverItems);
            if (instance.fragment != null) {
                instance.fragment.addChatItems(phrases);
                ConsultInfo info = response != null ? response.getConsultInfo() : null;
                if (info != null) {
                    instance.fragment.setStateConsultConnected(info.getId(), info.getName(), info.getStatus());
                }
            }
            PrefUtils.setClientIdWasSet(true, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRatingClick(Context context, final Survey survey) {
        ChatItem chatItem = convertRatingItem(survey);
        if (chatItem != null) {
            addMessage(chatItem, appContext);
        }
        String ratingDoneMessage = MessageFormatter.createRatingDoneMessage(
                survey.getSendingId(),
                survey.getQuestions().get(0).getId(),
                survey.getQuestions().get(0).getRate(),
                PrefUtils.getClientID(appContext)
        );

        sendMessageMFMSAsync(context, ratingDoneMessage, false,
                new RequestCallback<String, PushServerErrorException>() {
                    @Override
                    public void onResult(String s) {
                        survey.setMessageId(s);
                        setSurveyState(survey, MessageState.STATE_SENT);
                        if (instance.fragment != null) {
                            instance.fragment.updateUi();
                        }
                    }

                    @Override
                    public void onError(PushServerErrorException e) {

                    }
                }, null);
    }

//    public void onRatingStarsClick(Context context, final Survey survey) {
//        ChatItem chatItem = convertRatingItem(survey);
//        if (chatItem != null) {
//            addMessage(chatItem, appContext);
//        }
//        String ratingThumbsMessage = MessageFormatter.createRatingStarsMessage(ratingStars.getRating(), ratingStars.getMessageId());
//        PushController.getInstance(context).sendMessageAsync(ratingThumbsMessage, false, new RequestCallback<String, PushServerErrorException>() {
//            @Override
//            public void onResult(String s) {
//                ratingStars.setMessageId(s);
//                if (instance.fragment != null) {
//                    instance.fragment.updateUi();
//                }
//            }
//
//            @Override
//            public void onError(PushServerErrorException e) {
//
//            }
//        });
//    }

    public static PendingIntentCreator getPendingIntentCreator() {
        if (pendingIntentCreator == null) {
            pendingIntentCreator = new PendingIntentCreator() {
                @Override
                public PendingIntent createPendingIntent(Context context) {
                    Intent i = new Intent(context, ChatActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    return PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                }
            };
        }
        return pendingIntentCreator;
    }

    /**
     * Оповещает об изменении количества непрочитанных сообщений.
     * Срабатывает при показе пуш уведомления в Статус Баре и
     * при прочтении сообщений.
     * Все места, где срабатывает прочтение сообщений, можно найти по
     * NotificationService.ACTION_ALL_MESSAGES_WERE_READ.
     * Данный тип сообщения отправляется в Сервис пуш уведомлений при прочтении сообщений.
     * <p>
     * Можно было бы поместить оповещение в точку прихода NotificationService.ACTION_ALL_MESSAGES_WERE_READ,
     * но иногда в этот момент в сообщения еще не помечены, как прочитанные.
     */
    public static void notifyUnreadMessagesCountChanged(Context context) {
        UnreadMessagesCountListener unreadMessagesCountListener = getUnreadMessagesCountListener();
        if (unreadMessagesCountListener != null) {
            ChatController controller = getInstance(context, PrefUtils.getClientID(context));
            int unreadCount = controller.getUnreadMessagesCount();
            unreadMessagesCountListener.onUnreadMessagesCountChanged(unreadCount);
        }
    }

    public static int getUnreadMessagesCount(Context context) {
        String clientId = PrefUtils.getClientID(context);
        if (clientId.equals("")) {
            return 0;
        } else {
            return getInstance(context, clientId).getUnreadMessagesCount();
        }
    }

    private int getUnreadMessagesCount() {
        return mDatabaseHolder.getUnreaMessagesId().size();
    }

    public static void resetPendingIntentCreator() {
        ChatController.pendingIntentCreator = null;
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
            sendMessageMFMSAsync(appContext,
                    MessageFormatter.getMessageTyping(PrefUtils.getClientID(appContext)),
                    true, new RequestCallback<String, PushServerErrorException>() {
                        @Override
                        public void onResult(String aVoid) {
                        }

                        @Override
                        public void onError(PushServerErrorException e) {
                        }
                    }, null);
        }
    }

    public boolean isNeedToShowWelcome() {
        return !(mDatabaseHolder.getMessagesCount() > 0);
    }

    public void bindFragment(ChatFragment f) {
        if (BuildConfig.DEBUG) Log.i(TAG, "bindFragment:");
        fragment = f;
        Activity activity = f.getActivity();
        appContext = activity.getApplicationContext();
        currentOffset = 0;
        if (mConsultWriter == null) {
            mConsultWriter = new ConsultWriter(appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        }
        if (mConsultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        }
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(appContext);
        }
        updateChatItemsOnBindAsync();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String environmentMessage = MessageFormatter.createEnvironmentMessage(PrefUtils.getUserName(appContext), PrefUtils.getClientID(appContext), appContext);
                    sendMessageMFMSSync(appContext, environmentMessage, true);
                } catch (PushServerErrorException e) {
                    e.printStackTrace();
                }
            }
        });
        if (mConsultWriter.isConsultConnected()) {
            fragment.setStateConsultConnected(mConsultWriter.getCurrentConsultId(), mConsultWriter.getCurrentConsultName(), mConsultWriter.getCurrentConsultTitle());
        } else if (mConsultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        } else {
            fragment.setTitleStateDefault();
        }
        mProgressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PROGRESS_BROADCAST);
        intentFilter.addAction(DOWNLOADED_SUCCESSFULLY_BROADCAST);
        intentFilter.addAction(DOWNLOAD_ERROR_BROADCAST);
        intentFilter.addAction(DEVICE_ID_IS_SET_BROADCAST);
        activity.registerReceiver(mProgressReceiver, intentFilter);
    }

    private void updateChatItemsOnBindAsync() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                updateChatItemsOnBind();
            }
        });
    }

    private void updateChatItemsOnBind() {
        final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, (int) getHistoryLoadingCount(fragment.getActivity())));
        if (null != fragment) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (fragment != null) {
                        fragment.addChatItems(items);
                    }
                }
            });
        }
        currentOffset = items.size();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                updateChatHistoryOnBind();
            }
        });
    }

//    private void updateChatHistoryOnBind() {
//        if (fragment != null) {
//            try {
//                PushController.getInstance(appContext).resetCounterSync();
//                List<ChatItem> dbItems = mDatabaseHolder.getChatItems(0, 20);
//                List<ChatItem> serverItems = MessageFormatter.format(PushController.getInstance(appContext).getMessageHistory(20));
//                if (dbItems.size() != serverItems.size()
//                        || !dbItems.containsAll(serverItems)) {
//                    Log.i(TAG, "not same!");
//                    mDatabaseHolder.putMessagesSync(serverItems);
//                    h.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, 20));
//                            if (null != fragment) fragment.addChatItems(items);
//                        }
//                    });
//                }
//            } catch (PushServerErrorException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void updateChatHistoryOnBind() {
        if (fragment != null) {
            try {
                HistoryResponseV2 response = getHistorySync(instance.fragment.getActivity(), null, null);
                List<ChatItem> serverItems = getChatItemFromHistoryResponse(response);
                final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                final int count = (int) getHistoryLoadingCount(instance.fragment.getActivity());
                List<ChatItem> dbItems = mDatabaseHolder.getChatItems(0, count);
                if (dbItems.size() != serverItems.size()
                        || !dbItems.containsAll(serverItems)) {
                    Log.i(TAG, "not same!");
                    mDatabaseHolder.putMessagesSync(serverItems);
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, count));
                            if (null != fragment) {
                                fragment.addChatItems(items);
                                if (info != null) {
                                    fragment.setStateConsultConnected(info.getId(), info.getName(), info.getStatus());
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public void unbindFragment() {
        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (activity != null) {
                activity.unregisterReceiver(mProgressReceiver);
            }
        }
        fragment = null;
    }

    public void onUserInput(final UpcomingUserMessage upcomingUserMessage) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onUserInput: " + upcomingUserMessage);
        if (upcomingUserMessage == null) return;
        if (appContext == null && fragment == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "(appContext == null && fragment == null");
            return;
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "upcomingUserMessage = " + upcomingUserMessage);
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um, appContext);
        if (!mConsultWriter.isConsultConnected()) {
            if (fragment != null) {
                fragment.setStateSearchingConsult();
            }
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

        reportAbountSendMessage(userPhrase);

        try {
            if (!userPhrase.hasFile()) {
                sendTextMessage(userPhrase, consultInfo);
            } else {
                sendFileMessage(userPhrase, consultInfo);
            }
        } catch (Exception e) {
            onSentMessageException(userPhrase, e);
        }

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment != null)
                            try {
                                getPushControllerInstance(appContext).notifyMessageUpdateNeeded();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }).start();
            }
        }, 60000);
    }

    private void sendTextMessage(final UserPhrase userPhrase, ConsultInfo consultInfo) {
        String message = MessageFormatter.format(userPhrase, consultInfo, null, null,
                PrefUtils.getClientID(appContext));

        sendMessageMFMSAsync(appContext, message, false, new RequestCallback<String, PushServerErrorException>() {
            @Override
            public void onResult(String string) {
                onMessageSent(userPhrase, string);
            }

            @Override
            public void onError(PushServerErrorException e) {
                onMessageSentError(userPhrase);
            }
        }, new ExceptionListener() {
            @Override
            public void onException(Exception e) {
                onMessageSentError(userPhrase);
            }
        });
    }

    private void sendFileMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        FileDescription fileDescription = userPhrase.getFileDescription();
        FileDescription quoteFileDescription = userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() : null;

        new DualFilePoster(fileDescription, quoteFileDescription, appContext) {
            @Override
            public void onResult(String mfmsFilePath, String mfmsQuoteFilePath) {
                onFileSent(userPhrase, consultInfo, mfmsFilePath, mfmsQuoteFilePath);
            }

            @Override
            public void onError(Throwable e) {
                onFileSentError(userPhrase, e);
            }
        };
    }

    private void onFileSent(final UserPhrase userPhrase, ConsultInfo consultInfo, String mfmsFilePath, String mfmsQuoteFilePath) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
        }

        String message = MessageFormatter.format(userPhrase, consultInfo, mfmsQuoteFilePath, mfmsFilePath,
                PrefUtils.getClientID(appContext));

        sendMessageMFMSAsync(appContext, message, false, new RequestCallback<String, PushServerErrorException>() {
            @Override
            public void onResult(String string) {
                onMessageSent(userPhrase, string);
            }

            @Override
            public void onError(PushServerErrorException e) {
                onFileMessageSentError(userPhrase, e);
            }
        }, null);
    }

    private void onMessageSent(UserPhrase userPhrase, String newId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "server answer on pharse sent with id " + newId);
        }
        setMessageState(userPhrase, MessageState.STATE_SENT);
        mDatabaseHolder.setUserPhraseMessageId(userPhrase.getId(), newId);
        if (fragment != null) {
            fragment.setUserPhraseMessageId(userPhrase.getId(), newId);
        }
    }

    private void onMessageSentError(UserPhrase userPhrase) {
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        if (fragment != null && isActive) {
            fragment.showConnectionError();
        }
        if (appContext != null) {
            Intent i = new Intent(appContext, NotificationService.class);
            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
            if (!isActive) appContext.startService(i);
        }
    }

    private void onFileMessageSentError(UserPhrase userPhrase, PushServerErrorException e) {
        Log.e(TAG, "error while sending message to server");
        e.printStackTrace();
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        if (appContext != null) {
            Intent i = new Intent(appContext, NotificationService.class);
            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
            if (!isActive) appContext.startService(i);
        }
    }

    private void onFileSentError(UserPhrase userPhrase, Throwable e) {
        Log.e(TAG, "error while sending files to server");
        e.printStackTrace();
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        if (appContext != null) {
            Intent i = new Intent(appContext, NotificationService.class);
            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
            if (!isActive) appContext.startService(i);
        }
    }

    private void onSentMessageException(UserPhrase userPhrase, Exception e) {
        e.printStackTrace();
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        if (e instanceof PushServerErrorException) {
            PushController.getInstance(appContext).init();
        } else if (e instanceof IllegalStateException) {
            PushController.getInstance(appContext).init();
        }
    }

    private void reportAbountSendMessage(UserPhrase userPhrase) {
        if (BuildConfig.DEBUG) Log.i(TAG, "sendMessage: " + userPhrase);
        if (BuildConfig.DEBUG) Log.i(TAG, "sendMessage: " + mAnalyticsTracker);
        if (userPhrase.isWithPhrase())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setTextWasSent();
        if (userPhrase.isWithFile())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setFileWasSent();
        if (userPhrase.isWithQuote())
            if (null != mAnalyticsTracker) mAnalyticsTracker.setQuoteWasSent();
        if (userPhrase.isCopy()) if (null != mAnalyticsTracker) mAnalyticsTracker.setCopyWasSent();
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
        Log.e(TAG, "downloadMessagesTillEnd");
        if (isAllMessagesDownloaded) return;
        if (appContext == null) return;
        mMessagesExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    isDownloadingMessages = true;
                    final Long chunk = 100L;
                    Long start = lastMessageId == null ? null : lastMessageId;
                    HistoryResponseV2 response = getHistorySync(instance.fragment.getActivity(), start, chunk);
                    List<MessgeFromHistory> items = response != null ? response.getMessages() : null;
                    if (items == null || items.size() == 0) return;
                    lastMessageId = items.get(items.size() - 1).getId();
                    currentOffset += items.size();
                    isAllMessagesDownloaded = items.size() != chunk;
                    List<ChatItem> chatItems = MessageFormatter.formatNew(items);
                    mDatabaseHolder.putMessagesSync(chatItems);
                    isDownloadingMessages = false;
                    if (!isAllMessagesDownloaded) downloadMessagesTillEnd();
                } catch (Exception e) {
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
                if (null != fragment) {
                    ChatItem ci = setLastAvatars(Arrays.asList(new ChatItem[]{cm})).get(0);
                    if (!(ci instanceof ConsultConnectionMessage)
                            || ((ConsultConnectionMessage) ci).isDisplayMessage())
                        fragment.addChatItem(ci);
                    if (ci instanceof ConsultChatPhrase) {
                        fragment.notifyConsultAvatarChanged(((ConsultChatPhrase) ci).getAvatarPath()
                                , ((ConsultChatPhrase) ci).getConsultId());
                    }
                }
            }
        });
        if (cm instanceof ConsultPhrase
                && ctx != null
                && isActive) {
            String messageId = ((ConsultPhrase) cm).getId();
            setConsultMessageRead(ctx, messageId);
        }
        final Context finalContext = ctx;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (appContext != null && isActive) {
                    appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
                    notifyUnreadMessagesCountChanged(finalContext);
                }
            }
        }, 1500);

        // Если пришло сообщение от оператора, нужно удалить расписание из чата.
        if (cm instanceof ConsultPhrase || cm instanceof ConsultConnectionMessage) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (fragment != null && fragment.isAdded()) {
                        fragment.removeSchedule(false);
                    }
                }
            });
        }
    }

    public String getCurrentConsultName() {
        return mConsultWriter.getCurrentConsultName() + "%%" + mConsultWriter.getCurrentConsultTitle();
    }

    public void onFileClick(final FileDescription fileDescription) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onFileClick " + fileDescription);
        if (fragment != null && fragment.isAdded()) {
            Activity activity = fragment.getActivity();
            if (activity != null) {
                if (fileDescription.getFilePath() == null) {
                    Intent i = new Intent(activity, DownloadService.class);
                    i.setAction(DownloadService.START_DOWNLOAD_FD_TAG);
                    i.putExtra(DownloadService.FD_TAG, fileDescription);
                    activity.startService(i);
                } else if (fileDescription.hasImage() && fileDescription.getFilePath() != null) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
                } else if (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PDF) {
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    File file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
                    target.setDataAndType(FileProvider.getUriForFile(
                            activity, /*BuildConfig.APPLICATION_ID*/ activity.getPackageName() + ".fileprovider", file), "application/pdf"
                    );
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        mAnalyticsTracker.setAttachmentWasOpened();
                        activity.startActivity(target);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            if (fragment != null) {
                fragment.setMessageState(userPhrase.getMessageId(), MessageState.STATE_SENDING);
            }
            sendMessage(userPhrase);
        }
    }

    void cleanAll() throws PushServerErrorException {
        if (BuildConfig.DEBUG) Log.i(TAG, "cleanAll: ");
        mDatabaseHolder.cleanDatabase();
        if (fragment != null) fragment.cleanChat();
        mConsultWriter.setCurrentConsultLeft();
        mConsultWriter.setSearchingConsult(false);
        isSearchingConsult = false;
        searchOffset = 0;
        currentOffset = 0;
        h.removeCallbacksAndMessages(null);
        if (appContext != null) {
            appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
            notifyUnreadMessagesCountChanged(appContext);
        }
    }

    public void setActivityIsForeground(boolean isForeground) {
        this.isActive = isForeground;
        if (isForeground && fragment != null && fragment.isAdded()) {
            Activity activity = fragment.getActivity();
            if (activity != null) {
                ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null
                        && cm.getActiveNetworkInfo() != null
                        && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    List<String> unread = mDatabaseHolder.getUnreaMessagesId();
                    if (unread != null && !unread.isEmpty()) {
                        firstUnreadMessageId = unread.get(0); // для скролла к первому непрочитанному сообщению
                    } else {
                        firstUnreadMessageId = null;
                    }
                    if (unread != null) {
                        for (String id : unread) {
                            setConsultMessageRead(appContext, id);
                        }
                    }
                }
            }
        }
        if (isForeground) h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (appContext != null)
                    appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
                notifyUnreadMessagesCountChanged(appContext);
            }
        }, 1500);
    }

    public String getFirstUnreadMessageId() {
        return firstUnreadMessageId;
    }

    void setMessageState(UserPhrase up, MessageState messageState) {
        up.setSentState(messageState);
        if (fragment != null) {
            fragment.setPhraseSentStatus(up.getId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhrase(up.getId(), up.getSentState());
    }

    void setSurveyState(Survey survey, MessageState messageState) {
        survey.setSentState(messageState);
        if (fragment != null) {
            fragment.setPhraseSentStatus(survey.getMessageId(), survey.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhrase(survey.getMessageId(), survey.getSentState());
    }

    private UserPhrase convert(UpcomingUserMessage message) {
        if (message == null)
            return new UserPhrase("local" + UUID.randomUUID().toString(), "", null, System.currentTimeMillis(), null, null);
        if (message.getFileDescription() != null && !message.getFileDescription().getFilePath().contains("file://")) {
            message.getFileDescription().setFilePath("file://" + message.getFileDescription().getFilePath());
        }
        UserPhrase up = new UserPhrase("local" + UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription(), null);
        up.setCopy(message.isCopyied());
        return up;
    }

    private ChatItem convertRatingItem(ChatItem chatItem) {
        if (chatItem instanceof Survey) {
            Survey survey = (Survey) chatItem;
            survey.setMessageId("local" + UUID.randomUUID().toString());
            return survey;
        }
        return null;
    }

    public void onSystemMessageFromServer(Context ctx, Bundle bundle, String shortMessage) {
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
                    if (fragment != null) {
                        fragment.setPhraseSentStatus(s, MessageState.STATE_WAS_READ);
                    }
                    if (mDatabaseHolder != null)
                        mDatabaseHolder.setStateOfUserPhrase(s, MessageState.STATE_WAS_READ);
                }
                break;
            case MessageMatcher.TYPE_REMOVE_PUSHES:
                Intent intent = new Intent(ctx, NotificationService.class);
                intent.setAction(NotificationService.ACTION_REMOVE_NOTIFICATION);
                ctx.startService(intent);
                break;
            case MessageMatcher.TYPE_UNREAD_MESSAGE_NOTIFICATION:
                Intent intent2 = new Intent(ctx, NotificationService.class);
                intent2.putExtra(NotificationService.ACTION_ADD_UNREAD_MESSAGE_TEXT, shortMessage);
                intent2.setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE_TEXT);
                ctx.startService(intent2);
                break;
        }
    }

//    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
//        if (BuildConfig.DEBUG) Log.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet(appContext));
//        if (!PrefUtils.isClientIdNotEmpty(appContext)) {
//            callback.onSuccess(new ArrayList<ChatItem>());
//            return;
//        }
//        mExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                final int[] currentOffset = {fragment.getCurrentItemsCount()};
//                try {
//                    List<InOutMessage> messages = PushController.getInstance(appContext).getMessageHistory(currentOffset[0] + 20);
//                    mDatabaseHolder.putMessagesSync(MessageFormatter.format(messages));
//                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], 20));
//                    currentOffset[0] += chatItems.size();
//                    h.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.onSuccess(chatItems);
//                        }
//                    });
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], 20));
//                    currentOffset[0] += chatItems.size();
//                    h.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.onSuccess(chatItems);
//                        }
//                    });
//                }
//            }
//        });
//    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        if (BuildConfig.DEBUG) Log.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet(appContext));
        if (!PrefUtils.isClientIdNotEmpty(appContext)) {
            callback.onSuccess(new ArrayList<ChatItem>());
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final int[] currentOffset = {fragment.getCurrentItemsCount()};
                final int count = (int) getHistoryLoadingCount(instance.fragment.getActivity());
                try {
                    HistoryResponseV2 response = getHistorySync(instance.fragment.getActivity(), lastLoadId, null);
                    List<ChatItem> serverItems = getChatItemFromHistoryResponse(response);
                    mDatabaseHolder.putMessagesSync(serverItems);
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], count));
                    currentOffset[0] += chatItems.size();
                    ConsultInfo info = response != null ? response.getConsultInfo() : null;
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(chatItems);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], count));
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
        if (fragment != null && fragment.isAdded()) {
            Activity activity = fragment.getActivity();
            if (activity != null) {
                Intent i = new Intent(activity, DownloadService.class);
                i.setAction(DownloadService.START_DOWNLOAD_WITH_NO_STOP);
                i.putExtra(DownloadService.FD_TAG, fileDescription);
                activity.startService(i);
            }
        }
    }

    /**
     * @return true, если формат сообщения распознан и обработан чатом.
     * false, если push уведомление не относится к чату и никак им не обработано.
     */
    public synchronized PushMessageCheckResult onFullMessage(PushMessage pushMessage, final Context ctx) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onFullMessage: " + pushMessage);
        final ChatItem chatItem = MessageFormatter.format(pushMessage);

        PushMessageCheckResult pushMessageCheckResult = new PushMessageCheckResult();

        if (chatItem != null && (!MessageFormatter.checkId(pushMessage, PrefUtils.getClientID(ctx))
                || chatItem instanceof EmptyChatItem)) {
            pushMessageCheckResult.setDetected(true);
            pushMessageCheckResult.setNeedsShowIsStatusBar(false);
            return pushMessageCheckResult;
        }

        if (chatItem instanceof Survey) {
            final Survey survey = (Survey) chatItem;
            String ratingDoneMessage = MessageFormatter.createRatingRecievedMessage(
                    survey.getSendingId(),
                    PrefUtils.getClientID(appContext)
            );

            sendMessageMFMSAsync(ctx, ratingDoneMessage, false, new RequestCallback<String, PushServerErrorException>() {
                @Override
                public void onResult(String s) {
                    survey.setMessageId(s);
                }

                @Override
                public void onError(PushServerErrorException e) {

                }
            }, null);
        }

        if (chatItem != null) {
            ConsultMessageReaction consultReactor = new ConsultMessageReaction(
                    mConsultWriter,
                    new ConsultMessageReactions() {
                        @Override
                        public void consultConnected(final String id, final String name, final String title) {
                            if (fragment != null)
                                fragment.setStateConsultConnected(id, name, title);
                            // Отправка данных об окружении оператору
                            try {
                                String userName = PrefUtils.getUserName(ctx);
                                String clientId = PrefUtils.getClientID(ctx);
                                String message = MessageFormatter.createEnvironmentMessage(userName, clientId, appContext);
                                sendMessageMFMSSync(appContext, message, true);
                            } catch (PushServerErrorException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConsultLeft() {
                            if (null != fragment) fragment.setTitleStateDefault();
                        }
                    });
            consultReactor.onPushMessage(chatItem);
            addMessage(chatItem, ctx);
            if (chatItem instanceof ConsultPhrase) {
                mAnalyticsTracker.setConsultMessageWasReceived();
            }

            pushMessageCheckResult.setDetected(true);
            if ((chatItem instanceof ScheduleInfo || chatItem instanceof UserPhrase)) {
                // не показывать уведомление для расписания и сообщений пользователя
                pushMessageCheckResult.setNeedsShowIsStatusBar(false);
            } else {
                // не показывать уведомление, если shortMessage пустой
                pushMessageCheckResult.setNeedsShowIsStatusBar(!TextUtils.isEmpty(pushMessage.getShortMessage()));
            }
        } else {
            pushMessageCheckResult.setDetected(false);
            pushMessageCheckResult.setNeedsShowIsStatusBar(false);
        }

        return pushMessageCheckResult;
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

    private void onSettingClientId(final Context ctx) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onSettingClientId:");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (fragment != null) {
                    if (PrefUtils.getNewClientID(ctx) == null) return;
                    try {
                        cleanAll();
//                        getPushControllerInstance(ctx).setClientId(PrefUtils.getNewClientID(ctx));
                        PrefUtils.setClientId(ctx, PrefUtils.getNewClientID(ctx));
                        PrefUtils.setClientIdWasSet(true, ctx);

                        getPushControllerInstance(ctx).resetCounterSync();

                        sendMessageMFMSSync(ctx, MessageFormatter.createEnvironmentMessage(PrefUtils
                                        .getUserName(ctx),
                                PrefUtils.getNewClientID(ctx), ctx), true);

                        HistoryResponseV2 response = getHistorySync(instance.fragment.getActivity(), null, null);
                        List<ChatItem> serverItems = getChatItemFromHistoryResponse(response);
                        mDatabaseHolder.putMessagesSync(serverItems);
                        if (fragment != null) {
                            fragment.addChatItems((List<ChatItem>) setLastAvatars(serverItems));
                            ConsultInfo info = response != null ? response.getConsultInfo() : null;
                            if (info != null) {
                                fragment.setStateConsultConnected(info.getId(), info.getName(), info.getStatus());
                            }
                        }
                        currentOffset = serverItems.size();

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
        if (fragment == null && appContext == null) return;
        Context cxt = null;
        if (fragment != null && fragment.isAdded()) {
            cxt = fragment.getActivity();
        }
        if (cxt == null) cxt = appContext;
        final Context finalContext = cxt;
        cxt.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        DatabaseHolder.getInstance(cxt).setAllMessagesRead(new CompletionHandler<Void>() {
            @Override
            public void onComplete(Void data) {
                notifyUnreadMessagesCountChanged(finalContext);
            }

            @Override
            public void onError(Throwable e, String message, Void data) {

            }
        });
        if (fragment != null) fragment.setAllMessagesWereRead();
    }

    /**
     * В чате есть возможность скачать файл из сообщения.
     * Он скачивается через сервис.
     * Для приема сообщений из сервиса используется данный BroadcastReceiver
     */
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
                if (fragment != null && fileDescription != null)
                    fragment.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOADED_SUCCESSFULLY_BROADCAST)) {
                Log.i(TAG, "onReceive: DOWNLOADED_SUCCESSFULLY_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                fileDescription.setDownloadProgress(100);
                if (fragment != null)
                    fragment.updateProgress(fileDescription);
            } else if (action.equals(DOWNLOAD_ERROR_BROADCAST)) {
                Log.i(TAG, "onReceive: DOWNLOAD_ERROR_BROADCAST ");
                FileDescription fileDescription = intent.getParcelableExtra(DownloadService.FD_TAG);
                if (fragment != null && fileDescription != null) {
                    Throwable t = (Throwable) intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST);
                    fragment.onDownloadError(fileDescription, t);
                }
            } else if (action.equals(DEVICE_ID_IS_SET_BROADCAST)) {
                onSettingClientId(context);
            }
        }
    }

    /**
     * обращение к пуш контроллеру, если нет DeviceAddress,
     * то выкидывает PushServerErrorException
     */
    private static PushController getPushControllerInstance(Context ctx) throws PushServerErrorException {
        PushController controller = PushController.getInstance(ctx);
        String deviceAddress = controller.getDeviceAddress();
        if (deviceAddress != null && !deviceAddress.isEmpty()) {
            return PushController.getInstance(ctx);
        } else {
            throw new PushServerErrorException(PushServerErrorException.DEVICE_ADDRESS_INVALID);
        }
    }

    public static UnreadMessagesCountListener getUnreadMessagesCountListener() {
        return unreadMessagesCountListener == null ? null : unreadMessagesCountListener.get();
    }

    public static void setPendingIntentCreator(PendingIntentCreator pendingIntentCreator) {
        ChatController.pendingIntentCreator = pendingIntentCreator;
    }

    public static void setUnreadMessagesCountListener(UnreadMessagesCountListener unreadMessagesCountListener) {
        ChatController.unreadMessagesCountListener = new WeakReference<>(unreadMessagesCountListener);
    }

    public static void removeUnreadMessagesCountListener() {
        ChatController.unreadMessagesCountListener = null;
    }

    public static void setShortPushListener(ShortPushListener shortPushListener) {
        ChatController.shortPushListener = shortPushListener;
    }

    public static void removeShortPushListener() {
        ChatController.shortPushListener = null;
    }

    public static void setFullPushListener(FullPushListener fullPushListener) {
        ChatController.fullPushListener = fullPushListener;
    }

    public static void removeFullPushListener() {
        ChatController.fullPushListener = null;
    }

    public static ShortPushListener getShortPushListener() {
        return shortPushListener;
    }

    public static FullPushListener getFullPushListener() {
        return fullPushListener;
    }

    public interface PendingIntentCreator {
        PendingIntent createPendingIntent(Context context);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }

    /**
     * @return версию библиотеки
     */
    private static String getLibraryVersion() {
        return BuildConfig.VERSION_NAME;
    }


    /**
     * метод-обертка над методом mfms sendMessage
     */
    private static void sendMessageMFMSSync(Context ctx, String message, boolean isSystem) throws PushServerErrorException {
        getPushControllerInstance(ctx).sendMessage(message, isSystem);
    }

    /**
     * метод обертка над методом mfms sendMessageAsync
     *
     * @param message           сообщение для отправки
     * @param isSystem          системное сообщение
     * @param listener          слушатель успешной/неуспешной отправки
     * @param exceptionListener слушатель ошибки отсутствия DeviceAdress
     */
    private static void sendMessageMFMSAsync(Context ctx,
                                             String message,
                                             boolean isSystem,
                                             final RequestCallback<String, PushServerErrorException> listener,
                                             final ExceptionListener exceptionListener) {
        try {
            getPushControllerInstance(ctx).sendMessageAsync(
                    message, isSystem, new RequestCallback<String, PushServerErrorException>() {
                        @Override
                        public void onResult(String aVoid) {
                            if (listener != null) {
                                listener.onResult(aVoid);
                            }
                        }

                        @Override
                        public void onError(PushServerErrorException e) {
                            if (listener != null) {
                                listener.onError(e);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (exceptionListener != null) {
                exceptionListener.onException(e);
            }
        }
    }

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param start ид сообщения от которого грузить, null если с начала
     * @param count количество сообщений для загрузки
     */
    private static HistoryResponseV2 getHistorySync(Context ctx, Long start, Long count) throws Exception {
        // todo при необходимости можно вынести строку снизу как метод получени токена
        String token = getPushControllerInstance(ctx).getDeviceAddress() + ":" + PrefUtils.getClientID(ctx);
        String userAgent = MessageFormatter.getUserAgent(ctx);
        String url = PrefUtils.getServerUrlMetaInfo(ctx);
        if (count == null) {
            count = getHistoryLoadingCount(ctx);
        }
        if (url != null && !url.isEmpty() && !token.isEmpty()) {
            ServiceGenerator.setUrl(url);
            RetrofitService retrofitService = ServiceGenerator.getRetrofitService();
            Call<HistoryResponseV2> call = retrofitService.historyV2(token, userAgent, start, count, getLibraryVersion());
            Response<HistoryResponseV2> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                Call<List<MessgeFromHistory>> call2 = retrofitService.history(token, userAgent, start, count, getLibraryVersion());
                return new HistoryResponseV2(call2.execute().body());
            }
        } else {
            throw new IOException();
        }
    }

    private static long getHistoryLoadingCount(Context ctx) {
        ChatStyle style = PrefUtils.getIncomingStyle(ctx);
        return style != null ? (long) style.historyLoadingCount : ChatStyle.DEFAULT_HISTORY_LOADING_COUNT;
    }

    private static List<ChatItem> getChatItemFromHistoryResponse(HistoryResponseV2 response) {
        List<ChatItem> list = new ArrayList<>();
        if (response != null) {
            List<MessgeFromHistory> responseList = response.getMessages();
            if (responseList != null) {
                list = MessageFormatter.formatNew(responseList);
                setupLastItemIdFromHistory(responseList);
            }
        }
        return list;
    }

    private static void setupLastItemIdFromHistory(List<MessgeFromHistory> list) {
        if (list != null) {
            for (MessgeFromHistory item : list) {
                if (lastLoadId == null) {
                    lastLoadId = item.getId();
                } else if (lastLoadId > item.getId()) {
                    lastLoadId = item.getId();
                }
            }
        }
    }

    private void setConsultMessageRead(Context ctx, String messageId) {
        try {
            getPushControllerInstance(ctx).notifyMessageRead(messageId);
            mDatabaseHolder.setMessageWereRead(messageId);
        } catch (PushServerErrorException e) {
            e.printStackTrace();
        }
    }
}
