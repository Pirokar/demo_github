package im.threads.internal.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.mfms.android.push_lite.RequestCallback;
import com.mfms.android.push_lite.exception.PushServerErrorException;
import com.mfms.android.push_lite.repo.push.remote.api.InMessageSend;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.R;
import im.threads.ThreadsLib;
import im.threads.internal.Config;
import im.threads.internal.activities.ConsultActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.formatters.OutgoingMessageCreator;
import im.threads.internal.formatters.PushMessageAttributes;
import im.threads.internal.formatters.PushMessageType;
import im.threads.view.ChatFragment;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.ClearThreadIdChatItem;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.EmptyChatItem;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageFromHistory;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.PushMessageCheckResult;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.SaveThreadIdChatItem;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.opengraph.OGData;
import im.threads.internal.opengraph.OGDataProvider;
import im.threads.internal.retrofit.ServiceGenerator;
import im.threads.internal.services.DownloadService;
import im.threads.internal.services.NotificationService;
import im.threads.internal.utils.Callback;
import im.threads.internal.utils.CallbackNoError;
import im.threads.internal.utils.ConsultWriter;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.DualFilePoster;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.Seeker;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.Transport;
import im.threads.internal.utils.UrlUtils;

/**
 * controller for chat Fragment. all bells and whistles in fragment,
 * all work here
 * don't forget to unbindFragment() in ChatFragment onDestroy, to avoid leaks;
 */
public class ChatController {
    private static final String TAG = "ChatController ";

    // Состояния консультанта
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;

    private static final int PER_PAGE_COUNT = 100;

    private static final int RESEND_MSG = 123;

    private static ChatController instance;
    // Некоторые операции производятся в отдельном потоке через Executor.
    // Чтобы отправить результат из него в UI Thread используется Handler.
    private static final Handler h = new Handler(Looper.getMainLooper());
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private Executor mMessagesExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private final Context appContext;

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
    private Long lastMessageTimestamp;
    private boolean isActive;
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
    private String firstUnreadProviderId;

    public static ChatController getInstance(@NonNull final Context ctx) {
        if (instance == null) {
            instance = new ChatController(ctx);
        }
        String newClientId = PrefUtils.getNewClientID();
        String oldClientId = PrefUtils.getClientID();
        ThreadsLogger.i(TAG, "getChatStyle newClientId = " + newClientId + ", oldClientClientId = " + oldClientId);
        if (TextUtils.isEmpty(newClientId) || newClientId.equals(oldClientId)) {
            // clientId has not changed
            PrefUtils.setNewClientId("");
        } else {
            final String clientId = newClientId;
            PrefUtils.setClientId(clientId);
            instance.mExecutor.execute(() -> instance.onClientIdChanged(clientId));
        }
        return instance;
    }

    @SuppressLint("all")
    private ChatController(@NonNull final Context ctx) {
        appContext = ctx;
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance();
        }
        if (mConsultWriter == null) {
            mConsultWriter = new ConsultWriter(ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        }
        ServiceGenerator.setUserAgent(OutgoingMessageCreator.getUserAgent(ctx));
        resendTimeInterval = ctx.getResources().getInteger(R.integer.check_internet_interval_ms);
        mUnsendMessageHandler = new Handler(msg -> {
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
        });
    }

    public void onRatingClick(@NonNull final Survey survey) {
//        final ChatItem chatItem = convertRatingItem(survey); //TODO THREADS-3395 Figure out what is this for
        addMessage(survey);
        String ratingDoneMessage = OutgoingMessageCreator.createRatingDoneMessage(survey,
                PrefUtils.getClientID(),
                PrefUtils.getAppMarker());
        Transport.sendMessageMFMSAsync(ratingDoneMessage, false,
                new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
                    @Override
                    public void onResult(InMessageSend.Response response) {
                        // Change survey view after 2 seconds
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            setSurveyState(survey, MessageState.STATE_SENT);
                            resetActiveSurvey();
                            updateUi();
                        }, Config.instance.surveyCompletionDelay);
                    }

                    @Override
                    public void onError(final PushServerErrorException e) {
                    }
                }, null);
    }

    public void onResolveThreadClick(final boolean approveResolve) {
        // if user approve to resolve the thread - send CLOSE_THREAD push
        // else if user doesn't approve to resolve the thread - send REOPEN_THREAD push
        // and then delete the request from the chat history
        final String clientID = PrefUtils.getClientID();
        final String resolveThreadMessage = approveResolve ?
                OutgoingMessageCreator.createResolveThreadMessage(clientID) :
                OutgoingMessageCreator.createReopenThreadMessage(clientID);

        Transport.sendMessageMFMSAsync(resolveThreadMessage, true,
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

    public void onUserTyping(String input) {
        Transport.sendMessageMFMSAsync(
                OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(), input),
                true, null, null);
    }

    public void onUserInput(@NonNull final UpcomingUserMessage upcomingUserMessage) {
        ThreadsLogger.i(TAG, "onUserInput: " + upcomingUserMessage);
        // If user has written a message while the request to resolve the thread is visible
        // we should make invisible the resolve request
        if (isResolveRequestVisible) {
            removeResolveRequest();
        }
        // If user has written a message while the active survey is visible
        // we should make invisible the survey
        removeActiveSurvey();
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um);
        if (!isScheduleInfoReceived && !mConsultWriter.isConsultConnected()) {
            if (fragment != null) {
                fragment.setStateSearchingConsult();
            }
            mConsultWriter.setSearchingConsult(true);
        }
        queueMessageSending(um);
        checkAndLoadOgData(um);
    }

    public void fancySearch(final String query,
                            final boolean forward,
                            final CallbackNoError<List<ChatItem>> callback) {
        if (!isAllMessagesDownloaded) {
            downloadMessagesTillEnd();
        }
        mExecutor.execute(() -> {
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
                ThreadsLogger.e(TAG, "fancySearch", e);
            }
        });
    }

    public void onFileClick(final FileDescription fileDescription) {
        ThreadsLogger.i(TAG, "onFileClick " + fileDescription);
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
                            setConsultMessageRead(providerId);
                        }
                    }
                }
            }
        }
        if (isForeground) h.postDelayed(() -> {
            appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
            notifyUnreadMessagesCountChanged();
        }, 1500);
    }

    public void onSystemMessageFromServer(@NonNull final Context ctx, @NonNull final Bundle bundle, final String shortMessage) {
        ThreadsLogger.i(TAG, "onSystemMessageFromServer:");
        boolean isCurrentClientId = IncomingMessageParser.checkId(bundle, PrefUtils.getClientID());
        String appMarker = bundle.getString(PushMessageAttributes.APP_MARKER_KEY);
        final long currentTimeMillis = System.currentTimeMillis();
        final PushMessageType pushMessageType = PushMessageType.getKnownType(bundle);
        switch (pushMessageType) {
            case TYPING:
                if (isCurrentClientId) {
                    addMessage(new ConsultTyping(mConsultWriter.getCurrentConsultId(), currentTimeMillis, mConsultWriter.getCurrentPhotoUrl()));
                }
                break;
            case MESSAGES_READ:
                final List<String> list = IncomingMessageParser.getReadIds(bundle);
                ThreadsLogger.i(TAG, "onSystemMessageFromServer: read messages " + list);
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
                if (bundle.getString(PushMessageAttributes.OPERATOR_URL) != null) {
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
        ThreadsLogger.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet());
        if (!PrefUtils.isClientIdNotEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        mExecutor.execute(() -> {
            if (instance.fragment != null) {
                final int[] currentOffset = {instance.fragment.getCurrentItemsCount()};
                int count = Config.instance.historyLoadingCount;
                try {
                    final HistoryResponse response = Transport.getHistorySync(null, false);
                    final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
                    count = serverItems.size();
                    mDatabaseHolder.putMessagesSync(serverItems);
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], count));
                    currentOffset[0] += chatItems.size();
                    h.post(() -> callback.onSuccess(chatItems));
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "requestItems", e);
                    final List<ChatItem> chatItems = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(currentOffset[0], count));
                    currentOffset[0] += chatItems.size();
                    h.post(() -> callback.onSuccess(chatItems));
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

    /**
     * @return true, если формат сообщения распознан и обработан чатом.
     * false, если push уведомление не относится к чату и никак им не обработано.
     */
    public synchronized PushMessageCheckResult onFullMessage(final PushMessage pushMessage) {
        ThreadsLogger.i(TAG, "onFullMessage: " + pushMessage);
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
                    PushMessageType.THREAD_CLOSED.name().equalsIgnoreCase(((EmptyChatItem) chatItem).getType())) {
                new Handler(Looper.getMainLooper()).post(this::removeResolveRequest);
            }
        }
        if (chatItem instanceof SaveThreadIdChatItem) {
            final Long threadId = ((SaveThreadIdChatItem) chatItem).getThreadId();
            PrefUtils.setThreadId(threadId);
        }
        if (chatItem instanceof ClearThreadIdChatItem) {
            PrefUtils.setThreadId(-1L);
        }
        boolean isCurrentClientId = IncomingMessageParser.checkId(pushMessage, PrefUtils.getClientID());
        if (isCurrentClientId) {
            if (chatItem instanceof Survey) {
                final Survey survey = (Survey) chatItem;
                final String ratingDoneMessage = OutgoingMessageCreator.createRatingReceivedMessage(
                        survey.getSendingId(),
                        PrefUtils.getClientID()
                );
                Transport.sendMessageMFMSAsync(ratingDoneMessage, true, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isResolveRequestVisible) {
                        removeResolveRequest();
                    }
                }, resolveThread.getHideAfter() * 1000);
            }
            if (chatItem instanceof ScheduleInfo) {
                final ScheduleInfo schedule = (ScheduleInfo) chatItem;
                updateInputEnable(schedule.isChatWorking() || schedule.isSendDuringInactive());
                isScheduleInfoReceived = true;
                if (null != mConsultWriter) mConsultWriter.setSearchingConsult(false);
                h.post(() -> {
                    if (null != fragment) {
                        fragment.removeSearching();
                        fragment.setTitleStateDefault();
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
            addMessage(chatItem);
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

    public void onConsultChoose(final Activity activity, final String consultId) {
        if (consultId == null) {
            ThreadsLogger.w(TAG, "Can't show consult info: consultId == null");
        } else {
            ConsultInfo info = mDatabaseHolder.getConsultInfoSync(consultId);
            if (info == null) info = new ConsultInfo("", consultId, "", "", "");
            final Intent i = ConsultActivity.getStartIntent(activity, info.getPhotoUrl(), info.getName(), info.getStatus());
            activity.startActivity(i);
        }
    }

    public boolean isNeedToShowWelcome() {
        return !(mDatabaseHolder.getMessagesCount() > 0);
    }

    public int getStateOfConsult() {
        if (mConsultWriter.istSearchingConsult()) return CONSULT_STATE_SEARCHING;
        else if (mConsultWriter.isConsultConnected()) return CONSULT_STATE_FOUND;
        else return CONSULT_STATE_DEFAULT;
    }

    public boolean isConsultFound() {
        return !isScheduleInfoReceived && mConsultWriter.isConsultConnected();
    }

    public ConsultInfo getCurrentConsultInfo() {
        return mConsultWriter.getCurrentConsultInfo();
    }

    public String getFirstUnreadProviderId() {
        return firstUnreadProviderId;
    }

    public void bindFragment(final ChatFragment f) {
        ThreadsLogger.i(TAG, "bindFragment:");
        fragment = f;
        final Activity activity = f.getActivity();
        if (mConsultWriter == null) {
            mConsultWriter = new ConsultWriter(appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        }
        if (mConsultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        }
        if (mDatabaseHolder == null) {
            mDatabaseHolder = DatabaseHolder.getInstance();
        }
        updateChatItemsOnBindAsync();
        Transport.sendMessageMFMSAsync(
                OutgoingMessageCreator.createInitChatMessage(PrefUtils.getClientID(), PrefUtils.getData()),
                true,
                null,
                null
        );

        final String environmentMessage = OutgoingMessageCreator.createEnvironmentMessage(PrefUtils.getUserName(),
                PrefUtils.getClientID(),
                PrefUtils.getClientIDEncrypted(),
                PrefUtils.getData(),
                appContext);
        Transport.sendMessageMFMSAsync(environmentMessage, true, null, null);

        if (mConsultWriter.isConsultConnected()) {
            fragment.setStateConsultConnected(mConsultWriter.getCurrentConsultInfo());
        } else if (mConsultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        } else {
            fragment.setTitleStateDefault();
        }
        mProgressReceiver = new ProgressReceiver(fragment, this::onSettingClientId);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DEVICE_ID_IS_SET_BROADCAST);
        activity.registerReceiver(mProgressReceiver, intentFilter);
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
    private void notifyUnreadMessagesCountChanged() {
        ThreadsLib.UnreadMessagesCountListener l = Config.instance.unreadMessagesCountListener;
        if (l != null) {
            DatabaseHolder.getInstance()
                    .getUnreadMessagesCount(false, l);
        }
    }

    void setAllMessagesWereRead() {
        Context cxt = null;
        if (fragment != null && fragment.isAdded()) {
            cxt = fragment.getActivity();
        }
        if (cxt == null) cxt = appContext;
        cxt.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        DatabaseHolder.getInstance().setAllMessagesRead(new CompletionHandler<Void>() {
            @Override
            public void onComplete(final Void data) {
                notifyUnreadMessagesCountChanged();
            }

            @Override
            public void onError(final Throwable e, final String message, final Void data) {
            }
        });
        if (fragment != null) fragment.setAllMessagesWereRead();
    }

    private void onClientIdChanged(final String finalClientId) {
        try {
            Transport.getPushControllerInstance();
        } catch (final PushServerErrorException e) {
            ThreadsLogger.e(TAG, "device address was not set, returning");
            return;
        }
        try {
            instance.cleanAll();
            if (instance.fragment != null) {
                instance.fragment.removeSearching();
            }
            instance.mConsultWriter.setCurrentConsultLeft();
            Transport.sendMessageMFMSSync(
                    OutgoingMessageCreator.createInitChatMessage(finalClientId, PrefUtils.getData()),
                    true
            );
            final String environmentMessage = OutgoingMessageCreator.createEnvironmentMessage(
                    PrefUtils.getUserName(),
                    finalClientId,
                    PrefUtils.getClientIDEncrypted(),
                    PrefUtils.getData(),
                    appContext
            );
            Transport.sendMessageMFMSSync(environmentMessage, true);
            Transport.getPushControllerInstance().resetCounterSync();
            final HistoryResponse response = Transport.getHistorySync(null, true);
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
            PrefUtils.setClientIdWasSet(true);
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "onClientIdChanged", e);
        }
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

    private void updateChatItemsOnBindAsync() {
        mExecutor.execute(() -> updateChatItemsOnBind());
    }

    private void updateChatItemsOnBind() {
        if (null != fragment) {
            final int historyLoadingCount = Config.instance.historyLoadingCount;
            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, historyLoadingCount));
            h.post(() -> {
                if (fragment != null) {
                    fragment.addChatItems(items);
                }
            });
            checkAndLoadOgData(items);

            final List<UserPhrase> unsendUserPhrase = mDatabaseHolder.getUnsendUserPhrase(historyLoadingCount);
            if (!unsendUserPhrase.isEmpty()) {
                unsendMessages.clear();
                unsendMessages.addAll(unsendUserPhrase);
                scheduleResend();
            }
        }
        loadHistory();
    }

    public void loadHistory() {
        if (!isDownloadingMessages) {
            isDownloadingMessages = true;
            mExecutor.execute(() -> {
                try {
                    final int count = Config.instance.historyLoadingCount;
                    final HistoryResponse response = Transport.getHistorySync(count, true);
                    final List<ChatItem> serverItems = Transport.getChatItemFromHistoryResponse(response);
                    final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                    final List<ChatItem> dbItems = mDatabaseHolder.getChatItems(0, count);
                    if (dbItems.size() != serverItems.size() || !dbItems.containsAll(serverItems)) {
                        ThreadsLogger.d(TAG, "Local and downloaded history are not same");
                        mDatabaseHolder.putMessagesSync(serverItems);
                        final int serverCount = serverItems.size();
                        h.post(() -> {
                            final List<ChatItem> items = (List<ChatItem>) setLastAvatars(mDatabaseHolder.getChatItems(0, serverCount));
                            if (fragment != null) {
                                fragment.addChatItems(items);
                                checkAndLoadOgData(items);
                                if (info != null) {
                                    fragment.setStateConsultConnected(info);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "loadHistory", e);
                } finally {
                    isDownloadingMessages = false;
                }
            });
        }
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

    private void sendMessage(final UserPhrase userPhrase) {
        ConsultInfo consultInfo = null;
        if (null != userPhrase.getQuote() && userPhrase.getQuote().isFromConsult()) {
            final String id = userPhrase.getQuote().getQuotedPhraseConsultId();
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
        h.postDelayed(() -> new Thread(() -> {
            if (fragment != null)
                try {
                    Transport.getPushControllerInstance().notifyMessageUpdateNeeded();
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "sendMessage", e);
                }
        }).start(), 60000);
    }

    private void sendTextMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        final String message = OutgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, null, null,
                PrefUtils.getClientID(),
                PrefUtils.getThreadID()
        );
        Transport.sendMessageMFMSAsync(message, false, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
            @Override
            public void onResult(InMessageSend.Response response) {
                long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
                onMessageSent(userPhrase, response.getMessageId(), sentAt);
            }

            @Override
            public void onError(final PushServerErrorException e) {
                onMessageSentError(userPhrase);
            }
        }, e -> onMessageSentError(userPhrase));
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
                ThreadsLogger.w(TAG, "File send failed", e);
            }
        };
    }

    private void onFileSent(final UserPhrase userPhrase, final ConsultInfo consultInfo, final String mfmsFilePath, final String mfmsQuoteFilePath) {
        ThreadsLogger.i(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
        final String message = OutgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, mfmsQuoteFilePath, mfmsFilePath,
                PrefUtils.getClientID(),
                PrefUtils.getThreadID()
        );
        Transport.sendMessageMFMSAsync(message, false, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
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
        ThreadsLogger.d(TAG, "server answer on pharse sent with id " + providerId);
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
        final Intent i = new Intent(appContext, NotificationService.class);
        i.setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE);
        i.putExtra(NotificationService.EXTRA_APP_MARKER, PrefUtils.getAppMarker());
        if (!isActive) appContext.startService(i);
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
        ThreadsLogger.e(TAG, "onSentMessageException", e);
        setMessageState(userPhrase, MessageState.STATE_NOT_SENT);
    }

    private void downloadMessagesTillEnd() {
        if (!isDownloadingMessages && !isAllMessagesDownloaded) {
            isDownloadingMessages = true;
            ThreadsLogger.d(TAG, "downloadMessagesTillEnd");
            mMessagesExecutor.execute(() -> {
                try {
                    final HistoryResponse response = Transport.getHistorySync(lastMessageTimestamp, PER_PAGE_COUNT);
                    final List<MessageFromHistory> items = response != null ? response.getMessages() : null;
                    if (items == null || items.isEmpty()) {
                        isDownloadingMessages = false;
                        isAllMessagesDownloaded = true;
                    } else {
                        lastMessageTimestamp = items.get(0).getTimeStamp();
                        isAllMessagesDownloaded = items.size() < PER_PAGE_COUNT; // Backend can give us more than chunk anytime, it will give less only on history end
                        final List<ChatItem> chatItems = IncomingMessageParser.formatNew(items);
                        mDatabaseHolder.putMessagesSync(chatItems);
                        isDownloadingMessages = false;
                        if (!isAllMessagesDownloaded) {
                            downloadMessagesTillEnd();
                        }
                    }
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "downloadMessagesTillEnd", e);
                }
            });
        }
    }

    private void addMessage(final ChatItem cm) {
        mDatabaseHolder.putChatItem(cm);
        h.post(() -> {
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
        });
        if (cm instanceof ConsultPhrase && isActive) {
            final String providerId = ((ConsultPhrase) cm).getProviderId();
            setConsultMessageRead(providerId);
        }
        h.postDelayed(() -> {
            if (isActive) {
                appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
                notifyUnreadMessagesCountChanged();
            }
        }, 1500);
        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (cm instanceof ConsultPhrase || cm instanceof ConsultConnectionMessage
                || (cm instanceof ScheduleInfo && ((ScheduleInfo) cm).isChatWorking())) {
            h.post(() -> {
                if (fragment != null && fragment.isAdded()) {
                    fragment.removeSchedule(false);
                }
            });
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

    private void cleanAll() {
        ThreadsLogger.i(TAG, "cleanAll: ");
        mDatabaseHolder.cleanDatabase();
        if (fragment != null) fragment.cleanChat();
        mConsultWriter.setCurrentConsultLeft();
        mConsultWriter.setSearchingConsult(false);
        h.removeCallbacksAndMessages(null);
        appContext.sendBroadcast(new Intent(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        notifyUnreadMessagesCountChanged();
    }

    private void setMessageState(final UserPhrase up, final MessageState messageState) {
        up.setSentState(messageState);
        if (fragment != null) {
            fragment.setPhraseSentStatusByProviderId(up.getProviderId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhraseByProviderId(up.getProviderId(), up.getSentState());
    }

    private void setSurveyState(final Survey survey, final MessageState messageState) {
        survey.setSentState(messageState);
        if (fragment != null) {
            fragment.setSurveySentStatus(survey.getSendingId(), survey.getSentState());
        }
        mDatabaseHolder.putChatItem(survey);
    }

    private UserPhrase convert(@Nullable final UpcomingUserMessage message) {
        if (message == null) {
            return new UserPhrase(null, null, System.currentTimeMillis(), null);
        }
        final UserPhrase up = new UserPhrase(
                message.text,
                message.quote,
                System.currentTimeMillis(),
                message.fileDescription
        );
        up.setCopy(message.copyied);
        return up;
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

    private void loadOgData(final ChatItem chatItem, final List<String> urls) {
        final String url = urls.get(0);
        OGDataProvider.getOGData(url, new Callback<OGData, Throwable>() {
            @Override
            public void onSuccess(OGData ogData) {
                ThreadsLogger.d(TAG, "OGData for url: " + url
                        + "\n received: " + ogData);
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
                ThreadsLogger.w(TAG, "OpenGraph data load failed: ", error);
            }
        });
    }

    private void updateChatItem(ChatItem chatItem) {
        if (fragment != null) {
            fragment.updateChatItem(chatItem, false);
        }
    }

    private void updateInputEnable(final boolean enabled) {
        h.post(() -> {
            if (fragment != null) {
                fragment.updateInputEnable(enabled);
            }
        });
    }

    private void setConsultMessageRead(final String providerId) {
        try {
            Transport.getPushControllerInstance().notifyMessageRead(providerId);
            mDatabaseHolder.setMessageWereRead(providerId);
        } catch (final PushServerErrorException e) {
            ThreadsLogger.e(TAG, "setConsultMessageRead", e);
        }
    }

    private void setSurveyLifetime(final Survey survey) {
        // delete survey after timeout if user doesn't vote
        activeSurveySendingId = survey.getSendingId();
        final Long hideAfter = survey.getHideAfter();
        final Handler closeActiveSurveyHandler = new Handler(Looper.getMainLooper());
        closeActiveSurveyHandler.postDelayed(this::removeActiveSurvey, hideAfter * 1000);
    }

    private void onSettingClientId(final Context ctx) {
        ThreadsLogger.i(TAG, "onSettingClientId:");
        mExecutor.execute(() -> {
            String newClientId = PrefUtils.getNewClientID();
            if (fragment != null && !TextUtils.isEmpty(newClientId)) {
                try {
                    cleanAll();
                    PrefUtils.setClientId(newClientId);
                    PrefUtils.setClientIdWasSet(true);
                    Transport.getPushControllerInstance().resetCounterSync();
                    Transport.sendMessageMFMSSync(
                            OutgoingMessageCreator.createEnvironmentMessage(PrefUtils.getUserName(),
                                    newClientId,
                                    PrefUtils.getClientIDEncrypted(),
                                    PrefUtils.getData(),
                                    ctx), true);
                    final HistoryResponse response = Transport.getHistorySync(null, true);
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
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "onSettingClientId", e);
                }
            }
        });
    }
}
