package im.threads.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mfms.android.push_lite.PushBroadcastReceiver;
import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.RequestCallback;
import com.mfms.android.push_lite.exception.PushServerErrorException;
import com.mfms.android.push_lite.repo.push.remote.api.InMessageSend;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.R;
import im.threads.activities.ChatActivity;
import im.threads.activities.ConsultActivity;
import im.threads.activities.ImagesActivity;
import im.threads.broadcastReceivers.ProgressReceiver;
import im.threads.database.DatabaseHolder;
import im.threads.formatters.IncomingMessageParser;
import im.threads.formatters.OutgoingMessageCreator;
import im.threads.formatters.PushMessageAttributes;
import im.threads.formatters.PushMessageTypes;
import im.threads.fragments.ChatFragment;
import im.threads.helpers.FileProviderHelper;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ChatStyle;
import im.threads.model.ClearThreadIdChatItem;
import im.threads.model.CompletionHandler;
import im.threads.model.ConsultChatPhrase;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.EmptyChatItem;
import im.threads.model.FileDescription;
import im.threads.model.HistoryResponse;
import im.threads.model.MessageFromHistory;
import im.threads.model.MessageState;
import im.threads.model.PushMessageCheckResult;
import im.threads.model.RequestResolveThread;
import im.threads.model.SaveThreadIdChatItem;
import im.threads.model.ScheduleInfo;
import im.threads.model.Survey;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.opengraph.OGData;
import im.threads.opengraph.OGDataProvider;
import im.threads.retrofit.ServiceGenerator;
import im.threads.services.DownloadService;
import im.threads.services.NotificationService;
import im.threads.utils.Callback;
import im.threads.utils.CallbackNoError;
import im.threads.utils.ConsultWriter;
import im.threads.utils.DeviceInfoHelper;
import im.threads.utils.DualFilePoster;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;
import im.threads.utils.Seeker;
import im.threads.utils.Transport;
import im.threads.utils.UrlUtils;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Fragment. all bells and whistles in fragment,
 * all work here
 * don't forget to unbindFragment() in ChatFragment onDestroy, to avoid leaks;
 */
public class ChatController implements ProgressReceiver.DeviceIdChangedListener {
    // Некоторые операции производятся в отдельном потоке через Executor.
    // Чтобы отправить результат из него в UI Thread используется Handler.
    private static final Handler h = new Handler(Looper.getMainLooper());
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private Executor mMessagesExecutor = Executors.newSingleThreadExecutor();


    // Состояния консультанта
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;

    public static final int SURVEY_CHANGE_STATE_TIMEOUT = 2;

    public static final String TAG = "ChatController ";
    private static final int RESEND_MSG = 123;

    // Ссылка на фрагмент, которым управляет контроллер
    private ChatFragment fragment;

    // Для приема сообщений из сервиса по скачиванию файлов
    private ProgressReceiver mProgressReceiver;
    // Было получено сообщение, что чат не работает
    private boolean isScheduleInfoReceived;
    // this flag is keeping the visibility state of the request to resolve thread
    private boolean isResolveRequestVisible;

    // keep an active and visible for user survey id
    private long activeSurveySendingId;

    private DatabaseHolder mDatabaseHolder;
    private Context appContext;
    private static ChatController instance;
    private int currentOffset = 0;
    private Long lastMessageTimestamp;
    private boolean isActive;
    private long lastUserTypingSend = System.currentTimeMillis();
    private ConsultWriter mConsultWriter;
    private List<ChatItem> lastItems = new ArrayList<>();
    private Seeker seeker = new Seeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;
    private List<UserPhrase> unsendMessages = new ArrayList<>();
    private int resendTimeInterval;
    private List<UserPhrase> sendQueue = new ArrayList<>();

    private Handler mUnsendMessageHandler;
    // Используется для создания PendingIntent при открытии чата из пуш уведомления.
    // По умолчанию открывается ChatActivity.
    private static PendingIntentCreator pendingIntentCreator;
    private static ShortPushListener shortPushListener;
    private static FullPushListener fullPushListener;

    // Для оповещения об изменении количества непрочитанных сообщений
    private static WeakReference<UnreadMessagesCountListener> unreadMessagesCountListener;
    private String firstUnreadProviderId;

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

    public static ChatController getInstance(final Context ctx) {

        ChatStyle.updateContext(ctx);

        if (instance == null) {
            instance = new ChatController(ctx);
        }

        String newClientId = PrefUtils.getNewClientID(ctx);
        String oldClientId = PrefUtils.getClientID(ctx);

        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "getInstance newClientId = " + newClientId
                    + ", oldClientClientId = " + oldClientId);
        }

        if (TextUtils.isEmpty(newClientId) || newClientId.equals(oldClientId)) {
            // clientId has not changed
            PrefUtils.setNewClientId(ctx, "");

        } else {
            final String clientId = newClientId;
            PrefUtils.setClientId(ctx, clientId);

            instance.mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    onClientIdChanged(ctx, clientId);
                }
            });
        }
        return instance;
    }

    @SuppressLint("all")
    private ChatController(final Context ctx) {
        appContext = ctx;
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance(ctx);
        }
        if (mConsultWriter == null) {
            mConsultWriter = new ConsultWriter(ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        }

        ServiceGenerator.setUserAgent(OutgoingMessageCreator.getUserAgent(ctx));

        resendTimeInterval = ctx.getResources().getInteger(R.integer.check_internet_interval_ms);

        mUnsendMessageHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                if (msg.what == RESEND_MSG) {
                    if (!unsendMessages.isEmpty()) {
                        if (DeviceInfoHelper.hasNoInternet(ctx)) {
                            scheduleResend();
                        } else {
                            // try to send all unsent messages
                            mUnsendMessageHandler.removeMessages(RESEND_MSG);
                            final ListIterator<UserPhrase> iterator = unsendMessages.listIterator();
                            while (iterator.hasNext()) {
                                final UserPhrase phrase = iterator.next();
                                checkAndResendPhrase(phrase);
                                iterator.remove();
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    private static void onClientIdChanged(final Context ctx, final String finalClientId) {
        try {
            Transport.getPushControllerInstance(ctx);
        } catch (final PushServerErrorException e) {
            Log.e(TAG, "device address was not set, returning");
            return;
        }
        try {
            instance.cleanAll();
            if (instance.fragment != null) {
                instance.fragment.removeSearching();
            }
            instance.mConsultWriter.setCurrentConsultLeft();
            Transport.sendMessageMFMSSync(ctx, OutgoingMessageCreator.createInitChatMessage(finalClientId, ctx), true);
            final String environmentMessage = OutgoingMessageCreator.createEnvironmentMessage(PrefUtils.getUserName(ctx),
                    finalClientId,
                    PrefUtils.getClientIDEncrypted(ctx),
                    PrefUtils.getData(ctx),
                    ctx);
            Transport.sendMessageMFMSSync(ctx, environmentMessage, true);
            Transport.getPushControllerInstance(ctx).resetCounterSync();

            final HistoryResponse response = Transport.getHistorySync(ctx, null, true);
            final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
            instance.mDatabaseHolder.putMessagesSync(serverItems);
            final ArrayList<ChatItem> phrases = (ArrayList<ChatItem>) instance.setLastAvatars(serverItems);
            if (instance.fragment != null) {
                instance.fragment.addChatItems(phrases);
                final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                if (info != null) {
                    instance.fragment.setStateConsultConnected(info);
                }
            }

            instance.checkAndLoadOgData(phrases);
            PrefUtils.setClientIdWasSet(true, ctx);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    // send CLIENT_OFFLINE message
    public static void logoutClient(final Context ctx, final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            Transport.sendMessageMFMSAsync(ctx, OutgoingMessageCreator.createMessageClientOffline(clientId, ctx), true, null, null);
        }
    }

    public void onRatingClick(final Context context, final Survey survey) {
//        final ChatItem chatItem = convertRatingItem(survey); //TODO THREADS-3395 Figure out what is this for
        final ChatItem chatItem = survey;
        if (chatItem != null) {
            addMessage(chatItem, appContext);
        }
        String ratingDoneMessage = OutgoingMessageCreator.createRatingDoneMessage(survey,
                PrefUtils.getClientID(appContext),
                PrefUtils.getAppMarker(appContext));

        Transport.sendMessageMFMSAsync(context, ratingDoneMessage, false,
                new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
                    @Override
                    public void onResult(InMessageSend.Response response) {
                        // Change survey view after 2 seconds
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setSurveyState(survey, MessageState.STATE_SENT);
                                resetActiveSurvey();
                                updateUi();
                            }
                        }, SURVEY_CHANGE_STATE_TIMEOUT * 1000);
                    }

                    @Override
                    public void onError(final PushServerErrorException e) {

                    }
                }, null);
    }

    public void onResolveThreadClick(final Context context, final boolean approveResolve) {
        // if user approve to resolve the thread - send CLOSE_THREAD push
        // else if user doesn't approve to resolve the thread - send REOPEN_THREAD push
        // and then delete the request from the chat history
        final String clientID = PrefUtils.getClientID(appContext);
        final String resolveThreadMessage = approveResolve ?
                OutgoingMessageCreator.createResolveThreadMessage(clientID, appContext) :
                OutgoingMessageCreator.createReopenThreadMessage(clientID, appContext);

        Transport.sendMessageMFMSAsync(context, resolveThreadMessage, true,
                new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
                    @Override
                    public void onResult(InMessageSend.Response response) {
                        removeResolveRequest();
                    }

                    @Override
                    public void onError(final PushServerErrorException e) {

                    }
                }, null);
    }

    private void removeResolveRequest() {
        if (fragment != null) {
            final boolean removed = fragment.removeResolveRequest();
            if (removed) {
                updateUi();
            }
            isResolveRequestVisible = false;
        }
    }

    private void removeActiveSurvey() {
        if (activeSurveySendingId == -1) {
            return;
        }

        if (fragment != null) {
            final boolean removed = fragment.removeSurvey(activeSurveySendingId);
            if (removed) {
                updateUi();
            }
            resetActiveSurvey();
        }
    }

    private void resetActiveSurvey() {
        activeSurveySendingId = -1;
    }

    private void updateUi() {
        if (fragment != null) {
            fragment.updateUi();
        }
    }

    public static PendingIntentCreator getPendingIntentCreator() {
        if (pendingIntentCreator == null) {
            pendingIntentCreator = new PendingIntentCreator() {
                @Override
                public PendingIntent createPendingIntent(final Context context, String appMarker) {
                    final Intent i = new Intent(context, ChatActivity.class);
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
    public static void notifyUnreadMessagesCountChanged(final Context context) {
        final UnreadMessagesCountListener unreadMessagesCountListener = getUnreadMessagesCountListener();
        if (unreadMessagesCountListener != null) {
            final ChatController controller = getInstance(context);
            controller.getUnreadMessagesCount(false, unreadMessagesCountListener);
        }
    }

    public static void getUnreadMessagesCount(final Context context, final ChatController.UnreadMessagesCountListener unreadMessagesCountListener) {
        if (TextUtils.isEmpty(PrefUtils.getClientID(context))) {
            if (unreadMessagesCountListener != null) {
                unreadMessagesCountListener.onUnreadMessagesCountChanged(0);
            }
        } else {
            getInstance(context).getUnreadMessagesCount(true, unreadMessagesCountListener);
        }
    }

    private void getUnreadMessagesCount(boolean immediate, final ChatController.UnreadMessagesCountListener unreadMessagesCountListener) {
        mDatabaseHolder.getUnreadMessagesCount(immediate, unreadMessagesCountListener);
    }

    public static void resetPendingIntentCreator() {
        pendingIntentCreator = null;
    }

    public int getStateOfConsult() {
        if (mConsultWriter.istSearchingConsult()) return CONSULT_STATE_SEARCHING;
        else if (mConsultWriter.isConsultConnected()) return CONSULT_STATE_FOUND;
        else return CONSULT_STATE_DEFAULT;
    }

    public void onUserTyping(String input) {
        Transport.sendMessageMFMSAsync(appContext,
                OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(appContext), input, appContext),
                true, null, null);
    }

    public boolean isNeedToShowWelcome() {
        return !(mDatabaseHolder.getMessagesCount() > 0);
    }

    public void bindFragment(final ChatFragment f) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "bindFragment:");
        }
        fragment = f;
        final Activity activity = f.getActivity();
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
        Transport.sendMessageMFMSAsync(appContext, OutgoingMessageCreator.createInitChatMessage(
                PrefUtils.getClientID(appContext), appContext), true, null, null);

        final String environmentMessage = OutgoingMessageCreator.createEnvironmentMessage(PrefUtils.getUserName(appContext),
                PrefUtils.getClientID(appContext),
                PrefUtils.getClientIDEncrypted(appContext),
                PrefUtils.getData(appContext),
                appContext);
        Transport.sendMessageMFMSAsync(appContext, environmentMessage, true, null, null);

        if (mConsultWriter.isConsultConnected()) {
            fragment.setStateConsultConnected(mConsultWriter.getCurrentConsultInfo());
        } else if (mConsultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        } else {
            fragment.setTitleStateDefault();
        }
        mProgressReceiver = new ProgressReceiver(fragment, this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DEVICE_ID_IS_SET_BROADCAST);
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
        if (null != fragment) {
            final int historyLoadingCount = (int) Transport.getHistoryLoadingCount(fragment.getActivity());
            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, historyLoadingCount));
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (fragment != null) {
                        fragment.addChatItems(items);
                    }
                }
            });
            checkAndLoadOgData(items);
            currentOffset = items.size();

            final List<UserPhrase> unsendUserPhrase = mDatabaseHolder.getUnsendUserPhrase(historyLoadingCount);
            if (!unsendUserPhrase.isEmpty()) {
                unsendMessages.clear();
                unsendMessages.addAll(unsendUserPhrase);
                scheduleResend();
            }
        }

        loadHistory();
    }

    private void loadHistory() {

        if (fragment != null && !isDownloadingMessages) {
            isDownloadingMessages = true;
            mExecutor.execute(() -> {
                try {
                    final HistoryResponse response = Transport.getHistorySync(instance.fragment.getActivity(), null, true);
                    final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
                    final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                    final int count = (int) Transport.getHistoryLoadingCount(instance.fragment.getActivity());
                    final List<ChatItem> dbItems = mDatabaseHolder.getChatItems(0, count);

                    if (dbItems.size() != serverItems.size()
                            || !dbItems.containsAll(serverItems)) {

                        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                            Log.d(TAG, "Local and downloaded history are not same");
                        }

                        mDatabaseHolder.putMessagesSync(serverItems);
                        final int serverCount = serverItems.size();

                        h.post(() -> {
                            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, serverCount));
                            if (null != fragment) {
                                fragment.addChatItems(items);
                                checkAndLoadOgData(items);
                                if (info != null) {
                                    fragment.setStateConsultConnected(info);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    isDownloadingMessages = false;
                }
            });
        }
    }

    public static void reloadHistory(Context context) {
        getInstance(context).loadHistory();
    }

    public boolean isConsultFound() {
        return !isScheduleInfoReceived && mConsultWriter.isConsultConnected();
    }

    private List<? extends ChatItem> setLastAvatars(final List<? extends ChatItem> list) {
        for (final ChatItem ci : list) {
            if (ci instanceof ConsultConnectionMessage) {
                final ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                ccm.setAvatarPath(mDatabaseHolder.getLastConsultAvatarPathSync(ccm.getConsultId()));
            }
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                cp.setAvatarPath(mDatabaseHolder.getLastConsultAvatarPathSync(cp.getConsultId()));
            }
        }
        return list;
    }

    public void unbindFragment() {
        if (fragment != null) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                activity.unregisterReceiver(mProgressReceiver);
            }
        }
        fragment = null;
    }

    public void onUserInput(final UpcomingUserMessage upcomingUserMessage) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) Log.i(TAG, "onUserInput: " + upcomingUserMessage);
        if (upcomingUserMessage == null) return;
        if (appContext == null && fragment == null) {
            if (ChatStyle.getInstance().isDebugLoggingEnabled) Log.e(TAG, "(appContext == null && fragment == null");
            return;
        }
        if (ChatStyle.getInstance().isDebugLoggingEnabled) Log.i(TAG, "upcomingUserMessage = " + upcomingUserMessage);

        // If user has written a message while the request to resolve the thread is visible
        // we should make invisible the resolve request
        if (isResolveRequestVisible) {
            removeResolveRequest();
        }

        // If user has written a message while the active survey is visible
        // we should make invisible the survey
        removeActiveSurvey();

        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um, appContext);
        if (!isScheduleInfoReceived && !mConsultWriter.isConsultConnected()) {
            if (fragment != null) {
                fragment.setStateSearchingConsult();
            }
            mConsultWriter.setSearchingConsult(true);
        }

        queueMessageSending(um);
        checkAndLoadOgData(um);
    }

    private void sendMessage(final UserPhrase userPhrase) {
        ConsultInfo consultInfo = null;

        if (null != userPhrase.getQuote() && userPhrase.getQuote().isFromConsult()) {
            final String id = userPhrase.getQuote().getQuotedPhraseId();
            consultInfo = mConsultWriter.getConsultInfo(id);
        }

        try {
            if (!userPhrase.hasFile()) {
                sendTextMessage(userPhrase, consultInfo);
            } else {
                sendFileMessage(userPhrase, consultInfo);
            }
        } catch (final Exception e) {
            onSentMessageException(userPhrase, e);
            proceedSendingQueue();
        }

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment != null)
                            try {
                                Transport.getPushControllerInstance(appContext).notifyMessageUpdateNeeded();
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                    }
                }).start();
            }
        }, 60000);
    }

    private void sendTextMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        final String message = OutgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, null, null,
                PrefUtils.getClientID(appContext),
                PrefUtils.getThreadID(appContext),
                appContext);

        Transport.sendMessageMFMSAsync(appContext, message, false, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
            @Override
            public void onResult(InMessageSend.Response response) {
                long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
                onMessageSent(userPhrase, response.getMessageId(), sentAt);
            }

            @Override
            public void onError(final PushServerErrorException e) {
                onMessageSentError(userPhrase);
            }
        }, new Transport.ExceptionListener() {
            @Override
            public void onException(final Exception e) {
                onMessageSentError(userPhrase);
            }
        });
    }

    private void sendFileMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        final FileDescription fileDescription = userPhrase.getFileDescription();
        final FileDescription quoteFileDescription = userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() : null;

        new DualFilePoster(fileDescription, quoteFileDescription, appContext) {
            @Override
            public void onResult(final String mfmsFilePath, final String mfmsQuoteFilePath) {
                onFileSent(userPhrase, consultInfo, mfmsFilePath, mfmsQuoteFilePath);
            }

            @Override
            public void onError(final Throwable e) {
                onMessageSentError(userPhrase);
                if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                    Log.w(TAG, "File send failed", e);
                }
            }
        };
    }

    private void onFileSent(final UserPhrase userPhrase, final ConsultInfo consultInfo, final String mfmsFilePath, final String mfmsQuoteFilePath) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
        }

        final String message = OutgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, mfmsQuoteFilePath, mfmsFilePath,
                PrefUtils.getClientID(appContext),
                PrefUtils.getThreadID(appContext),
                appContext);

        Transport.sendMessageMFMSAsync(appContext, message, false, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
            @Override
            public void onResult(InMessageSend.Response response) {
                long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
                onMessageSent(userPhrase, response.getMessageId(), sentAt);
            }

            @Override
            public void onError(final PushServerErrorException e) {
                onMessageSentError(userPhrase);
            }
        }, null);
    }

    private void onMessageSent(final UserPhrase userPhrase, String providerId, long sentAtTimestamp) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.d(TAG, "server answer on pharse sent with id " + providerId);
        }
        userPhrase.setProviderId(providerId);
        if (sentAtTimestamp > 0) {
            userPhrase.setTimeStamp(sentAtTimestamp);
        }

        mDatabaseHolder.putChatItem(userPhrase);

        if (fragment != null) {
            fragment.updateChatItem(userPhrase, true);
        }
        setMessageState(userPhrase, MessageState.STATE_SENT);
        proceedSendingQueue();
    }

    private void onMessageSentError(final UserPhrase userPhrase) {
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        addMsgToResendQueue(userPhrase);

        if (fragment != null && isActive) {
            fragment.showConnectionError();
        }

        if (appContext != null) {
            final Intent i = new Intent(appContext, NotificationService.class);
            i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
            i.putExtra(NotificationService.EXTRA_APP_MARKER, PrefUtils.getAppMarker(appContext));
            if (!isActive) appContext.startService(i);
        }
        proceedSendingQueue();
    }

    private void addMsgToResendQueue(final UserPhrase userPhrase) {
        if (unsendMessages.indexOf(userPhrase) == -1) {
            unsendMessages.add(userPhrase);
            scheduleResend();
        }
    }

    private void scheduleResend() {
        if (!mUnsendMessageHandler.hasMessages(RESEND_MSG)) {
            mUnsendMessageHandler.sendEmptyMessageDelayed(RESEND_MSG, resendTimeInterval);
        }
    }

    private void onSentMessageException(final UserPhrase userPhrase, final Exception e) {
        e.printStackTrace();
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
        if (e instanceof PushServerErrorException) {
            PushController.getInstance(appContext).init();
        } else if (e instanceof IllegalStateException) {
            PushController.getInstance(appContext).init();
        }
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
                        final List<ChatItem> fromDb = mDatabaseHolder.getChatItems(0, -1);
                        if (lastItems == null || lastItems.size() == 0) lastItems = fromDb;
                        else {
                            if (lastSearchQuery.equalsIgnoreCase(query)) {
                                for (final ChatItem ci : lastItems) {
                                    if (ci instanceof ChatPhrase) {
                                        if (((ChatPhrase) ci).isHighlight()) {
                                            final ChatPhrase cp = (ChatPhrase) ci;
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
                    final List<ChatItem> list = seeker.seek(lastItems, !forward, query);
                    callback.onCall(list);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void downloadMessagesTillEnd() {

        if (!isDownloadingMessages && !isAllMessagesDownloaded && appContext != null) {

            isDownloadingMessages = true;

            if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                Log.d(TAG, "downloadMessagesTillEnd");
            }

            mMessagesExecutor.execute(() -> {
                try {
                    final Long chunk = 100L;
                    final HistoryResponse response = Transport.getHistorySync(instance.fragment.getActivity(), lastMessageTimestamp, chunk);
                    final List<MessageFromHistory> items = response != null ? response.getMessages() : null;

                    if (items == null || items.isEmpty()) {
                        isDownloadingMessages = false;
                        isAllMessagesDownloaded = true;
                    } else {
                        lastMessageTimestamp = items.get(0).getTimeStamp();
                        currentOffset += items.size();
                        isAllMessagesDownloaded = items.size() < chunk; // Backend can give us more than chunk anytime, it will give less only on history end
                        final List<ChatItem> chatItems = IncomingMessageParser.formatNew(items);
                        mDatabaseHolder.putMessagesSync(chatItems);
                        isDownloadingMessages = false;
                        if (!isAllMessagesDownloaded) {
                            downloadMessagesTillEnd();
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    void addMessage(final ChatItem cm, final Context ctx) {
        mDatabaseHolder.putChatItem(cm);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (null != fragment) {
                    final ChatItem ci = setLastAvatars(Arrays.asList(new ChatItem[]{cm})).get(0);
                    if (!(ci instanceof ConsultConnectionMessage)
                            || ((ConsultConnectionMessage) ci).isDisplayMessage()) {
                        fragment.addChatItem(ci);
                        checkAndLoadOgData(ci);
                    }

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
            final String providerId = ((ConsultPhrase) cm).getProviderId();
            setConsultMessageRead(ctx, providerId);
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

        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (cm instanceof ConsultPhrase || cm instanceof ConsultConnectionMessage
                || (cm instanceof ScheduleInfo && ((ScheduleInfo) cm).isChatWorking())) {
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

    public ConsultInfo getCurrentConsultInfo() {
        return mConsultWriter.getCurrentConsultInfo();
    }

    public void onFileClick(final FileDescription fileDescription) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onFileClick " + fileDescription);
        }
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {

                if (fileDescription.getFilePath() == null) {
                    final Intent i = new Intent(activity, DownloadService.class);
                    i.setAction(DownloadService.START_DOWNLOAD_FD_TAG);
                    i.putExtra(DownloadService.FD_TAG, fileDescription);
                    activity.startService(i);

                } else if (FileUtils.isImage(fileDescription) && fileDescription.getFilePath() != null) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));

                } else if (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PDF) {
                    final Intent target = new Intent(Intent.ACTION_VIEW);

                    final File file = new File(fileDescription.getFilePath());

                    target.setDataAndType(FileProviderHelper.getUriForFile(activity, file),
                            "application/pdf"
                    );
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivity(target);
                    } catch (final ActivityNotFoundException e) {
                        Toast.makeText(activity, "No application support this type of file", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            if (fragment != null) {
                fragment.setMessageState(userPhrase.getProviderId(), MessageState.STATE_SENDING);
            }
            queueMessageSending(userPhrase);
        }
    }

    private void queueMessageSending(UserPhrase userPhrase) {
        sendQueue.add(userPhrase);
        if (sendQueue.size() == 1) {
            sendMessage(userPhrase);
        }
    }

    private void proceedSendingQueue() {
        sendQueue.remove(0);
        if (sendQueue.size() > 0) {
            sendMessage(sendQueue.get(0));
        }
    }

    void cleanAll() throws PushServerErrorException {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "cleanAll: ");
        }
        mDatabaseHolder.cleanDatabase();
        if (fragment != null) fragment.cleanChat();
        mConsultWriter.setCurrentConsultLeft();
        mConsultWriter.setSearchingConsult(false);
        currentOffset = 0;
        h.removeCallbacksAndMessages(null);
        if (appContext != null) {
            appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
            notifyUnreadMessagesCountChanged(appContext);
        }
    }

    public void setActivityIsForeground(final boolean isForeground) {
        this.isActive = isForeground;
        if (isForeground && fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                final ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null
                        && cm.getActiveNetworkInfo() != null
                        && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    final List<String> unreadProviderIds = mDatabaseHolder.getUnreadMessagesProviderIds();
                    if (unreadProviderIds != null && !unreadProviderIds.isEmpty()) {
                        firstUnreadProviderId = unreadProviderIds.get(0); // для скролла к первому непрочитанному сообщению
                    } else {
                        firstUnreadProviderId = null;
                    }
                    if (unreadProviderIds != null) {
                        for (final String providerId : unreadProviderIds) {
                            setConsultMessageRead(appContext, providerId);
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

    public String getFirstUnreadProviderId() {
        return firstUnreadProviderId;
    }

    void setMessageState(final UserPhrase up, final MessageState messageState) {
        up.setSentState(messageState);
        if (fragment != null) {
            fragment.setPhraseSentStatusByProviderId(up.getProviderId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhraseByProviderId(up.getProviderId(), up.getSentState());
    }

    void setSurveyState(final Survey survey, final MessageState messageState) {
        survey.setSentState(messageState);
        if (fragment != null) {
            fragment.setSurveySentStatus(survey.getSendingId(), survey.getSentState());
        }
        mDatabaseHolder.putChatItem(survey);
    }

    private UserPhrase convert(final UpcomingUserMessage message) {

        if (message == null)
            return new UserPhrase(null, null, System.currentTimeMillis(), null);

        final UserPhrase up = new UserPhrase(message.getText(), message.getQuote(),
                System.currentTimeMillis(), message.getFileDescription());

        up.setCopy(message.isCopyied());
        return up;
    }

    public void onSystemMessageFromServer(final Context ctx, final Bundle bundle, final String shortMessage) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onSystemMessageFromServer:");
        }
        boolean isCurrentClientId = IncomingMessageParser.checkId(bundle, PrefUtils.getClientID(ctx));
        String appMarker = bundle.getString(PushMessageAttributes.APP_MARKER_KEY);
        final long currentTimeMillis = System.currentTimeMillis();
        final PushMessageTypes pushMessageTypes = PushMessageTypes.getKnownType(bundle);
        switch (pushMessageTypes) {
            case TYPING:
                if (isCurrentClientId) {
                    addMessage(new ConsultTyping(mConsultWriter.getCurrentConsultId(), currentTimeMillis, mConsultWriter.getCurrentPhotoUrl()), ctx);
                }
                break;
            case MESSAGES_READ:
                final List<String> list = IncomingMessageParser.getReadIds(bundle);
                if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                    Log.i(TAG, "onSystemMessageFromServer: read messages " + list);
                }
                for (final String readMessageProviderId : list) {
                    if (fragment != null) {
                        fragment.setPhraseSentStatusByProviderId(readMessageProviderId, MessageState.STATE_WAS_READ);
                    }
                    if (mDatabaseHolder != null)
                        mDatabaseHolder.setStateOfUserPhraseByProviderId(readMessageProviderId, MessageState.STATE_WAS_READ);
                }
                break;
            case REMOVE_PUSHES:
                final Intent intent = new Intent(ctx, NotificationService.class);
                intent.setAction(NotificationService.ACTION_REMOVE_NOTIFICATION);
                ctx.startService(intent);
                break;
            case UNREAD_MESSAGE_NOTIFICATION:
                final Intent intent2 = new Intent(ctx, NotificationService.class);
                if (bundle != null && bundle.getString(PushMessageAttributes.OPERATOR_URL) != null) {
                    final String operatorPhotoUrl = bundle.getString(PushMessageAttributes.OPERATOR_URL);
                    intent2.putExtra(NotificationService.EXTRA_OPERATOR_URL, operatorPhotoUrl);
                }
                intent2.putExtra(NotificationService.ACTION_ADD_UNREAD_MESSAGE_TEXT, shortMessage);
                intent2.setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE_TEXT);
                intent2.putExtra(NotificationService.EXTRA_APP_MARKER, appMarker);
                ctx.startService(intent2);
                break;
        }
    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled)
            Log.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet(appContext));
        if (!PrefUtils.isClientIdNotEmpty(appContext)) {
            callback.onSuccess(new ArrayList<ChatItem>());
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (instance.fragment != null) {
                    final int[] currentOffset = {instance.fragment.getCurrentItemsCount()};
                    int count = (int) Transport.getHistoryLoadingCount(instance.fragment.getActivity());
                    try {
                        final HistoryResponse response = Transport.getHistorySync(instance.fragment.getActivity(), null, false);
                        final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
                        count = serverItems.size();
                        mDatabaseHolder.putMessagesSync(serverItems);
                        final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], count));
                        currentOffset[0] += chatItems.size();
                        final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(chatItems);
                            }
                        });
                    } catch (final Exception e) {
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
            }
        });
    }

    public void onImageDownloadRequest(final FileDescription fileDescription) {
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                final Intent i = new Intent(activity, DownloadService.class);
                i.setAction(DownloadService.START_DOWNLOAD_WITH_NO_STOP);
                i.putExtra(DownloadService.FD_TAG, fileDescription);
                activity.startService(i);
            }
        }
    }

    public void checkAndLoadOgData(List<ChatItem> chatItems) {

        for (ChatItem chatItem : chatItems) {
            checkAndLoadOgData(chatItem);
        }
    }

    private void checkAndLoadOgData(ChatItem chatItem) {

        String phrase = null;

        if (chatItem instanceof UserPhrase) {
            phrase = ((UserPhrase) chatItem).getPhrase();
        } else if (chatItem instanceof ConsultPhrase) {
            phrase = ((ConsultPhrase) chatItem).getPhrase();
        }

        if (phrase != null) {
            List<String> urls = UrlUtils.extractLinks(phrase);

            if (!urls.isEmpty()) {
                loadOgData(chatItem, urls);
            }
        }
    }

    public void loadOgData(final ChatItem chatItem, final List<String> urls) {

        final String url = urls.get(0);

        OGDataProvider.getOGData(url, new OGDataProvider.Callback<OGData>() {

            @Override
            public void onSuccess(OGData ogData) {

                if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                    Log.d(TAG, "OGData for url: " + url
                            + "\n received: " + String.valueOf(ogData));
                }

                if (ogData != null && !ogData.isEmpty()) {
                    if (chatItem instanceof UserPhrase) {
                        UserPhrase message = (UserPhrase) chatItem;
                        message.ogData = ogData;
                        message.ogUrl = url;
                    } else if (chatItem instanceof ConsultPhrase) {
                        ConsultPhrase message = (ConsultPhrase) chatItem;
                        message.ogData = ogData;
                        message.ogUrl = url;
                    }
                    updateChatItem(chatItem);
                }
            }

            @Override
            public void onError(Throwable error) {
                if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                    Log.w(TAG, "OpenGraph data load failed: ", error);
                }
            }
        });
    }

    private void updateChatItem(ChatItem chatItem) {
        if (fragment != null) {
            fragment.updateChatItem(chatItem, false);
        }
    }

    /**
     * @return true, если формат сообщения распознан и обработан чатом.
     * false, если push уведомление не относится к чату и никак им не обработано.
     */
    public synchronized PushMessageCheckResult onFullMessage(final PushMessage pushMessage, final Context ctx) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onFullMessage: " + pushMessage);
        }
        final ChatItem chatItem = IncomingMessageParser.format(pushMessage);
        final PushMessageCheckResult pushMessageCheckResult = new PushMessageCheckResult();

        if (chatItem == null) {
            return pushMessageCheckResult;
        }

        pushMessageCheckResult.setDetected(true);

        // if thread is closed by timeout
        // remove close request from the history
        if (chatItem instanceof EmptyChatItem) {
            if (isResolveRequestVisible &&
                    PushMessageTypes.THREAD_CLOSED.name().equalsIgnoreCase(((EmptyChatItem) chatItem).getType())) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        removeResolveRequest();
                    }
                });
            }
        }

        if (chatItem instanceof SaveThreadIdChatItem) {
            final Long threadId = ((SaveThreadIdChatItem) chatItem).getThreadId();
            PrefUtils.setThreadId(ctx, threadId);
        }

        if (chatItem instanceof ClearThreadIdChatItem) {
            PrefUtils.setThreadId(ctx, -1L);
        }

        boolean isCurrentClientId = IncomingMessageParser.checkId(pushMessage, PrefUtils.getClientID(ctx));

        if (isCurrentClientId) {
            if (chatItem instanceof Survey) {
                final Survey survey = (Survey) chatItem;
                final String ratingDoneMessage = OutgoingMessageCreator.createRatingReceivedMessage(
                        survey.getSendingId(),
                        PrefUtils.getClientID(appContext),
                        appContext
                );

                Transport.sendMessageMFMSAsync(ctx, ratingDoneMessage, true, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
                    @Override
                    public void onResult(InMessageSend.Response response) {
                        setSurveyLifetime(survey);
                    }

                    @Override
                    public void onError(final PushServerErrorException e) {

                    }
                }, null);
            }

            if (chatItem instanceof RequestResolveThread) {
                isResolveRequestVisible = true;
                final RequestResolveThread resolveThread = (RequestResolveThread) chatItem;
                // if thread is closed by timeout
                // remove close request from the history
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isResolveRequestVisible) {
                            removeResolveRequest();
                        }
                    }
                }, resolveThread.getHideAfter() * 1000);
            }

            if (chatItem instanceof ScheduleInfo) {
                final ScheduleInfo schedule = (ScheduleInfo) chatItem;
                updateInputEnable(schedule.isChatWorking() || schedule.isSendDuringInactive());
                isScheduleInfoReceived = true;
                if (null != mConsultWriter) mConsultWriter.setSearchingConsult(false);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != fragment) {
                            fragment.removeSearching();
                            fragment.setTitleStateDefault();
                        }
                    }
                });
            }

            if (chatItem instanceof UserPhrase || chatItem instanceof ConsultPhrase) {
                checkAndLoadOgData(chatItem);
            }

            final ConsultMessageReaction consultReactor = new ConsultMessageReaction(
                    mConsultWriter,
                    new ConsultMessageReactions() {
                        @Override
                        public void consultConnected(ConsultInfo consultInfo) {
                            if (fragment != null) {
                                fragment.setStateConsultConnected(consultInfo);
                            }
                        }

                        @Override
                        public void onConsultLeft() {
                            if (null != fragment) fragment.setTitleStateDefault();
                        }
                    });
            consultReactor.onPushMessage(chatItem);
            addMessage(chatItem, ctx);
        }

        if (chatItem instanceof ScheduleInfo || chatItem instanceof UserPhrase || chatItem instanceof EmptyChatItem) {
            // не показывать уведомление для расписания и сообщений пользователя
            pushMessageCheckResult.setNeedsShowIsStatusBar(false);
        } else {
            // не показывать уведомление, если shortMessage пустой
            pushMessageCheckResult.setNeedsShowIsStatusBar(!TextUtils.isEmpty(pushMessage.shortMessage));
        }

        return pushMessageCheckResult;
    }

    public void setSurveyLifetime(final Survey survey) {
        // delete survey after timeout if user doesn't vote
        activeSurveySendingId = survey.getSendingId();
        final Long hideAfter = survey.getHideAfter();
        final Handler closeActiveSurveyHandler = new Handler(Looper.getMainLooper());
        closeActiveSurveyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeActiveSurvey();
            }
        }, hideAfter * 1000);
    }

    public void onConsultChoose(final Activity activity, final String consultId) {

        if (consultId == null) {
            Log.w(TAG, "Can't show consult info: consultId == null");
        } else {
            ConsultInfo info = mDatabaseHolder.getConsultInfoSync(consultId);
            if (info == null) info = new ConsultInfo("", consultId, "", "", "");
            final Intent i = ConsultActivity.getStartIntent(activity, info.getPhotoUrl(), info.getName(), info.getStatus());
            activity.startActivity(i);
        }
    }

    public void getLastUnreadConsultPhrase(final CompletionHandler<ConsultPhrase> handler) {
        mDatabaseHolder.getLastUnreadPhrase(handler);
    }

    public void onSettingClientId(final Context ctx) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onSettingClientId:");
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {

                String newClientId = PrefUtils.getNewClientID(ctx);

                if (fragment != null && !TextUtils.isEmpty(newClientId)) {

                    try {
                        cleanAll();
                        PrefUtils.setClientId(ctx, newClientId);
                        PrefUtils.setClientIdWasSet(true, ctx);

                        Transport.getPushControllerInstance(ctx).resetCounterSync();

                        Transport.sendMessageMFMSSync(ctx, OutgoingMessageCreator.createEnvironmentMessage(PrefUtils.getUserName(ctx),
                                newClientId,
                                PrefUtils.getClientIDEncrypted(ctx),
                                PrefUtils.getData(ctx),
                                ctx), true);

                        final HistoryResponse response = Transport.getHistorySync(instance.fragment.getActivity(), null, true);
                        final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
                        mDatabaseHolder.putMessagesSync(serverItems);
                        if (fragment != null) {
                            List<ChatItem> itemsWithLastAvatars = (List<ChatItem>) setLastAvatars(serverItems);
                            fragment.addChatItems(itemsWithLastAvatars);
                            checkAndLoadOgData(itemsWithLastAvatars);
                            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                            if (info != null) {
                                fragment.setStateConsultConnected(info);
                            }
                        }
                        currentOffset = serverItems.size();

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String getConsultNameById(final String id) {
        return mConsultWriter.getName(id);
    }

    private void updateInputEnable(final boolean enabled) {
        h.post(new Runnable() {
            @Override
            public void run() {
                if (fragment != null) {
                    fragment.updateInputEnable(enabled);
                }
            }
        });
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
            public void onComplete(final Void data) {
                notifyUnreadMessagesCountChanged(finalContext);
            }

            @Override
            public void onError(final Throwable e, final String message, final Void data) {

            }
        });
        if (fragment != null) fragment.setAllMessagesWereRead();
    }

    public static UnreadMessagesCountListener getUnreadMessagesCountListener() {
        return unreadMessagesCountListener == null ? null : unreadMessagesCountListener.get();
    }

    public static void setPendingIntentCreator(final PendingIntentCreator pendingIntentCreator) {
        ChatController.pendingIntentCreator = pendingIntentCreator;
    }

    public static void setUnreadMessagesCountListener(final UnreadMessagesCountListener unreadMessagesCountListener) {
        ChatController.unreadMessagesCountListener = new WeakReference<>(unreadMessagesCountListener);
    }

    public static void removeUnreadMessagesCountListener() {
        unreadMessagesCountListener = null;
    }

    public static void setShortPushListener(final ShortPushListener shortPushListener) {
        ChatController.shortPushListener = shortPushListener;
    }

    public static void removeShortPushListener() {
        shortPushListener = null;
    }

    public static void setFullPushListener(final FullPushListener fullPushListener) {
        ChatController.fullPushListener = fullPushListener;
    }

    public static void removeFullPushListener() {
        fullPushListener = null;
    }

    public static ShortPushListener getShortPushListener() {
        return shortPushListener;
    }

    public static FullPushListener getFullPushListener() {
        return fullPushListener;
    }

    public interface PendingIntentCreator {
        PendingIntent createPendingIntent(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }

    private void setConsultMessageRead(final Context ctx, final String providerId) {
        try {
            Transport.getPushControllerInstance(ctx).notifyMessageRead(providerId);
            mDatabaseHolder.setMessageWereRead(providerId);
        } catch (final PushServerErrorException e) {
            e.printStackTrace();
        }
    }
}
