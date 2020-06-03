package im.threads.internal.controllers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.activities.ConsultActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.ConsultChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.Hidable;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuickReply;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.SearchingConsult;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.services.FileDownloadService;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.HistoryLoader;
import im.threads.internal.transport.HistoryParser;
import im.threads.internal.utils.CallbackNoError;
import im.threads.internal.utils.ConsultWriter;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.FilePoster;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.Seeker;
import im.threads.internal.utils.ThreadUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.view.ChatFragment;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * controller for chat Fragment. all bells and whistles in fragment,
 * all work here
 * don't forget to unbindFragment() in ChatFragment onDestroy, to avoid leaks;
 */
public final class ChatController {
    // Состояния консультанта
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;
    private static final String TAG = "ChatController ";
    private static final int PER_PAGE_COUNT = 100;

    private static final int RESEND_MSG = 123;

    private static ChatController instance;

    private final PublishProcessor<Survey> surveyCompletionProcessor = PublishProcessor.create();
    @NonNull
    private final Context appContext;
    @NonNull
    private final ChatUpdateProcessor chatUpdateProcessor;
    @NonNull
    private final DatabaseHolder databaseHolder;
    @NonNull
    private final ConsultWriter consultWriter;
    private boolean surveyCompletionInProgress = false;
    // Ссылка на фрагмент, которым управляет контроллер
    private ChatFragment fragment;

    // Для приема сообщений из сервиса по скачиванию файлов
    private ProgressReceiver progressReceiver;
    // this flag is keeping the visibility state of the request to resolve thread
    private boolean isResolveRequestVisible;

    // keep an active and visible for user survey id
    private Survey activeSurvey = null;
    private Long lastMessageTimestamp;
    private boolean isActive;
    private List<ChatItem> lastItems = new ArrayList<>();

    // TODO: вынести в отдельный класс поиск сообщений
    private Seeker seeker = new Seeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;

    // TODO: вынести в отдельный класс отправку сообщений
    private List<UserPhrase> unsendMessages = new ArrayList<>();
    private int resendTimeInterval;
    private List<UserPhrase> sendQueue = new ArrayList<>();
    private Handler unsendMessageHandler;
    private String firstUnreadProviderId;

    // На основе этих переменных определяется возможность отправки сообщений в чат
    private ScheduleInfo currentScheduleInfo;
    private boolean hasQuickReplies = false; // Если пользователь не ответил на вопрос (quickReply), то блокируем поле ввода

    private CompositeDisposable compositeDisposable;

    private ChatController() {
        appContext = Config.instance.context;
        chatUpdateProcessor = ChatUpdateProcessor.getInstance();
        databaseHolder = DatabaseHolder.getInstance();
        consultWriter = new ConsultWriter(appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        resendTimeInterval = appContext.getResources().getInteger(R.integer.check_internet_interval_ms);
        ThreadUtils.runOnUiThread(() -> unsendMessageHandler = new Handler(msg -> {
                    if (msg.what == RESEND_MSG) {
                        if (!unsendMessages.isEmpty()) {
                            if (DeviceInfoHelper.hasNoInternet(appContext)) {
                                scheduleResend();
                            } else {
                                // try to send all unsent messages
                                unsendMessageHandler.removeMessages(RESEND_MSG);
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
                })
        );
        subscribeToChatEvents();
        subscribe(
                Completable.fromAction(PrefUtils::migrateTransportIfNeeded)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage()))
        );
    }

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        initClientId();
        return instance;
    }

    private static void initClientId() {
        String newClientId = PrefUtils.getNewClientID();
        String oldClientId = PrefUtils.getClientID();
        ThreadsLogger.i(TAG, "getInstance newClientId = " + newClientId + ", oldClientId = " + oldClientId);
        if (TextUtils.isEmpty(newClientId) || newClientId.equals(oldClientId)) {
            // clientId has not changed
            PrefUtils.setNewClientId("");
        } else {
            PrefUtils.setClientId(newClientId);
            instance.subscribe(
                    Completable.fromAction(() -> instance.onClientIdChanged())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                    },
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            )
            );
        }
    }

    public void onRatingClick(@NonNull final Survey survey) {
//        final ChatItem chatItem = convertRatingItem(survey); //TODO THREADS-3395 Figure out what is this for
        if (!surveyCompletionInProgress) {
            surveyCompletionInProgress = true;
            subscribeToSurveyCompletion();
            addMessage(survey);
        }
        surveyCompletionProcessor.onNext(survey);
        addMessage(survey);
    }

    public void onResolveThreadClick(final boolean approveResolve) {
        Config.instance.transport.sendResolveThread(approveResolve);
    }

    public void onUserTyping(String input) {
        Config.instance.transport.sendUserTying(input);
    }

    public void onUserInput(@NonNull final UpcomingUserMessage upcomingUserMessage) {
        ThreadsLogger.i(TAG, "onUserInput: " + upcomingUserMessage);
        // If user has written a message while the request to resolve the thread is visible
        // we should make invisible the resolve request
        removeResolveRequest();
        // If user has written a message while the active survey is visible
        // we should make invisible the survey
        removeActiveSurvey();
        final UserPhrase um = convert(upcomingUserMessage);
        addMessage(um);
        queueMessageSending(um);
    }

    public void fancySearch(final String query, final boolean forward, final CallbackNoError<List<ChatItem>> callback) {
        if (!isAllMessagesDownloaded) {
            downloadMessagesTillEnd();
        }
        subscribe(Completable.fromAction(() -> {
                    if (System.currentTimeMillis() > (lastFancySearchDate + 3000)) {
                        final List<ChatItem> fromDb = databaseHolder.getChatItems(0, -1);
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
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
    }

    public void onFileClick(final FileDescription fileDescription) {
        ThreadsLogger.i(TAG, "onFileClick " + fileDescription);
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                if (fileDescription.getFilePath() == null) {
                    FileDownloadService.startDownloadFD(activity, fileDescription);
                } else if (FileUtils.isImage(fileDescription)) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
                } else if (FileUtils.isDoc(fileDescription)) {
                    final Intent target = new Intent(Intent.ACTION_VIEW);
                    final File file = new File(fileDescription.getFilePath());
                    target.setDataAndType(FileProviderHelper.getUriForFile(activity, file), FileUtils.getMimeType(file));
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
                    final List<String> unreadProviderIds = databaseHolder.getUnreadMessagesProviderIds();
                    if (unreadProviderIds != null && !unreadProviderIds.isEmpty()) {
                        firstUnreadProviderId = unreadProviderIds.get(0); // для скролла к первому непрочитанному сообщению
                        for (final String providerId : unreadProviderIds) {
                            Config.instance.transport.sendMessageRead(providerId);
                        }
                    } else {
                        firstUnreadProviderId = null;
                    }
                }
            }
        }
        if (isActive) {
            subscribe(
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
                                UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
                            })
            );
        }
    }

    public Observable<List<ChatItem>> requestItems() {
        ThreadsLogger.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet());
        return Observable
                .fromCallable(() -> {
                    if (instance.fragment != null && PrefUtils.isClientIdNotEmpty()) {
                        int currentOffset = instance.fragment.getCurrentItemsCount();
                        int count = Config.instance.historyLoadingCount;
                        try {
                            final HistoryResponse response = HistoryLoader.getHistorySync(null, false);
                            final List<ChatItem> serverItems = HistoryParser.getChatItems(response);
                            count = serverItems.size();
                            databaseHolder.putChatItems(serverItems);
                            return setLastAvatars(databaseHolder.getChatItems(currentOffset, count));
                        } catch (final Exception e) {
                            ThreadsLogger.e(TAG, "requestItems", e);
                            return setLastAvatars(databaseHolder.getChatItems(currentOffset, count));
                        }
                    }
                    return new ArrayList<ChatItem>();
                })
                .subscribeOn(Schedulers.io());
    }

    public void onImageDownloadRequest(final FileDescription fileDescription) {
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                FileDownloadService.startDownloadWithNoStop(activity, fileDescription);
            }
        }
    }

    public void onConsultChoose(final Activity activity, final String consultId) {
        if (consultId == null) {
            ThreadsLogger.w(TAG, "Can't show consult info: consultId == null");
        } else {
            ConsultInfo info = databaseHolder.getConsultInfo(consultId);
            if (info != null) {
                activity.startActivity(ConsultActivity.getStartIntent(activity, info.getPhotoUrl(), info.getName(), info.getStatus()));
            } else {
                activity.startActivity(ConsultActivity.getStartIntent(activity));
            }
        }
    }

    public boolean isNeedToShowWelcome() {
        return databaseHolder.getMessagesCount() <= 0;
    }

    public int getStateOfConsult() {
        if (consultWriter.isSearchingConsult()) {
            return CONSULT_STATE_SEARCHING;
        } else if (consultWriter.isConsultConnected()) {
            return CONSULT_STATE_FOUND;
        } else {
            return CONSULT_STATE_DEFAULT;
        }
    }

    public boolean isConsultFound() {
        return isChatWorking() && consultWriter.isConsultConnected();
    }

    public ConsultInfo getCurrentConsultInfo() {
        return consultWriter.getCurrentConsultInfo();
    }

    public String getFirstUnreadProviderId() {
        return firstUnreadProviderId;
    }

    public void bindFragment(final ChatFragment f) {
        ThreadsLogger.i(TAG, "bindFragment: " + f.toString());
        final Activity activity = f.getActivity();
        if (activity == null) {
            return;
        }
        fragment = f;
        if (consultWriter.isSearchingConsult()) {
            fragment.setStateSearchingConsult();
        }
        Config.instance.transport.setLifecycle(fragment.getLifecycle());
        subscribe(
                Single.fromCallable(() -> {
                    final int historyLoadingCount = Config.instance.historyLoadingCount;
                    final List<UserPhrase> unsendUserPhrase = databaseHolder.getUnsendUserPhrase(historyLoadingCount);
                    if (!unsendUserPhrase.isEmpty()) {
                        unsendMessages.clear();
                        unsendMessages.addAll(unsendUserPhrase);
                        scheduleResend();
                    }
                    return setLastAvatars(databaseHolder.getChatItems(0, historyLoadingCount));
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                chatItems -> {
                                    if (fragment != null) {
                                        fragment.addChatItems(chatItems);
                                        handleQuickReplies(chatItems);
                                        loadHistory();
                                    }
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
        if (consultWriter.isConsultConnected()) {
            fragment.setStateConsultConnected(consultWriter.getCurrentConsultInfo());
        } else if (consultWriter.isSearchingConsult()) {
            fragment.setStateSearchingConsult();
        } else {
            fragment.setTitleStateDefault();
        }
        progressReceiver = new ProgressReceiver(fragment);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST);
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST);
        LocalBroadcastManager.getInstance(activity).registerReceiver(progressReceiver, intentFilter);
    }

    public void unbindFragment() {
        if (fragment != null) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(progressReceiver);
            }
        }
        fragment = null;
    }

    private boolean subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(event);
    }

    private void subscribeToSurveyCompletion() {
        subscribe(
                Flowable.fromPublisher(surveyCompletionProcessor)
                        .throttleLast(Config.instance.surveyCompletionDelay, TimeUnit.MILLISECONDS)
                        .firstElement()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(survey -> Config.instance.transport.sendRatingDone(survey))
        );
    }

    void setAllMessagesWereRead() {
        appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
        subscribe(DatabaseHolder.getInstance().setAllConsultMessagesWereRead()
                .subscribe(UnreadMessagesController.INSTANCE::refreshUnreadMessagesCount));
        if (fragment != null) {
            fragment.setAllMessagesWereRead();
        }
    }

    private boolean isChatWorking() {
        return currentScheduleInfo == null || currentScheduleInfo.isChatWorking();
    }

    private void onClientIdChanged() throws Exception {
        cleanAll();
        if (fragment != null) {
            fragment.removeSearching();
        }
        consultWriter.setCurrentConsultLeft();
        final HistoryResponse response = HistoryLoader.getHistorySync(null, true);
        final List<ChatItem> serverItems = HistoryParser.getChatItems(response);
        databaseHolder.putChatItems(serverItems);
        final List<ChatItem> chatItems = setLastAvatars(serverItems);
        if (fragment != null) {
            fragment.addChatItems(chatItems);
            handleQuickReplies(chatItems);
            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
            if (info != null) {
                fragment.setStateConsultConnected(info);
            }
        }
        PrefUtils.setClientIdWasSet(true);
    }

    private void updateUi() {
        ThreadsLogger.i(TAG, "updateUi");
        if (fragment != null) {
            fragment.updateUi();
        }
    }

    public void logoutClient(@NonNull final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            Config.instance.transport.sendClientOffline(clientId);
        } else {
            ThreadsLogger.i(getClass().getSimpleName(), "clientId must not be empty");
        }
    }

    public void loadHistory() {
        if (!isDownloadingMessages) {
            isDownloadingMessages = true;
            subscribe(
                    Single.fromCallable(() -> {
                        final int count = Config.instance.historyLoadingCount;
                        final HistoryResponse response = HistoryLoader.getHistorySync(count, true);
                        final List<ChatItem> serverItems = HistoryParser.getChatItems(response);
                        databaseHolder.putChatItems(serverItems);
                        if (fragment != null && isActive) {
                            final List<String> unreadProviderIds = databaseHolder.getUnreadMessagesProviderIds();
                            if (unreadProviderIds != null) {
                                for (final String providerId : unreadProviderIds) {
                                    Config.instance.transport.sendMessageRead(providerId);
                                }
                            }
                        }
                        return new Pair<>(response == null ? null : response.getConsultInfo(), serverItems.size());
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    pair -> {
                                        isDownloadingMessages = false;
                                        final int serverCount = pair.second == null ? 0 : pair.second;
                                        final List<ChatItem> items = setLastAvatars(databaseHolder.getChatItems(0, serverCount));
                                        if (fragment != null) {
                                            fragment.addChatItems(items);
                                            handleQuickReplies(items);
                                            final ConsultInfo info = pair.first;
                                            if (info != null) {
                                                fragment.setStateConsultConnected(info);
                                            }
                                        }
                                    },
                                    e -> {
                                        isDownloadingMessages = false;
                                        ThreadsLogger.e(TAG, e.getMessage());
                                    }
                            )
            );
        }
    }

    private List<ChatItem> setLastAvatars(final List<ChatItem> list) {
        for (final ChatItem ci : list) {
            if (ci instanceof ConsultConnectionMessage) {
                final ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                ConsultInfo consultInfo = databaseHolder.getConsultInfo(ccm.getConsultId());
                if (consultInfo != null) {
                    ccm.setAvatarPath(consultInfo.getPhotoUrl());
                }
            }
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                ConsultInfo consultInfo = databaseHolder.getConsultInfo(cp.getConsultId());
                if (consultInfo != null) {
                    cp.setAvatarPath(consultInfo.getPhotoUrl());
                }
            }
        }
        return list;
    }

    private void sendMessage(final UserPhrase userPhrase) {
        ThreadsLogger.i(TAG, "sendMessage: " + userPhrase);
        ConsultInfo consultInfo = null;
        if (null != userPhrase.getQuote() && userPhrase.getQuote().isFromConsult()) {
            final String id = userPhrase.getQuote().getQuotedPhraseConsultId();
            consultInfo = consultWriter.getConsultInfo(id);
        }
        if (!userPhrase.hasFile()) {
            sendTextMessage(userPhrase, consultInfo);
        } else {
            sendFileMessage(userPhrase, consultInfo);
        }
    }

    private void sendTextMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        ThreadsLogger.i(TAG, "sendTextMessage: " + userPhrase + ", " + consultInfo);
        Config.instance.transport.sendMessage(userPhrase, consultInfo, null, null);
    }

    private void sendFileMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        ThreadsLogger.i(TAG, "sendFileMessage: " + userPhrase + ", " + consultInfo);
        final FileDescription fileDescription = userPhrase.getFileDescription();
        final FileDescription quoteFileDescription = userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() : null;
        subscribe(
                Completable.fromAction(() -> {
                    String filePath = null;
                    String quoteFilePath = null;
                    if (fileDescription != null) {
                        filePath = FilePoster.post(fileDescription);
                    }
                    if (quoteFileDescription != null) {
                        quoteFilePath = FilePoster.post(quoteFileDescription);
                    }
                    Config.instance.transport.sendMessage(userPhrase, consultInfo, filePath, quoteFilePath);
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                },
                                e -> {
                                    chatUpdateProcessor.postChatItemSendError(userPhrase.getUuid());
                                    ThreadsLogger.e(TAG, e.getMessage());
                                }
                        )
        );
    }

    private void subscribeToChatEvents() {
        subscribeToTyping();
        subscribeToUserMessageRead();
        subscribeToConsultMessageRead();
        subscribeToNewMessage();
        subscribeToMessageSendSuccess();
        subscribeToMessageSendError();
        subscribeToSurveySendSuccess();
        subscribeToRemoveChatItem();
        subscribeToDeviceAddressChanged();
        subscribeToQuickReplies();
    }

    private void subscribeToTyping() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getTypingProcessor())
                        .filter(clientId -> ObjectsCompat.equals(PrefUtils.getClientID(), clientId))
                        .map(clientId ->
                                new ConsultTyping(
                                        consultWriter.getCurrentConsultId(),
                                        System.currentTimeMillis(),
                                        consultWriter.getCurrentPhotoUrl()
                                )
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::addMessage
                        )
        );
    }

    private void subscribeToUserMessageRead() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getUserMessageReadProcessor())
                        .observeOn(Schedulers.io())
                        .doOnNext(providerId -> databaseHolder.setStateOfUserPhraseByProviderId(providerId, MessageState.STATE_WAS_READ))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                providerId -> {
                                    if (fragment != null) {
                                        fragment.setMessageState(providerId, MessageState.STATE_WAS_READ);
                                    }
                                }
                        )
        );
    }

    private void subscribeToConsultMessageRead() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getConsultMessageReadProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                databaseHolder::setConsultMessageWasRead
                        )
        );
    }

    private void subscribeToNewMessage() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getNewMessageProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(chatItem -> {
                            if (chatItem instanceof Survey) {
                                activeSurvey = (Survey) chatItem;
                                Config.instance.transport.sendRatingReceived(activeSurvey.getSendingId());
                            }
                            if (chatItem instanceof RequestResolveThread) {
                                isResolveRequestVisible = true;
                            }
                            if (chatItem instanceof ScheduleInfo) {
                                currentScheduleInfo = (ScheduleInfo) chatItem;
                                refreshUserInputState();
                                consultWriter.setSearchingConsult(false);
                                if (fragment != null) {
                                    fragment.removeSearching();
                                    fragment.setTitleStateDefault();
                                }
                            }
                            if (chatItem instanceof ConsultConnectionMessage) {
                                ConsultConnectionMessage ccm = (ConsultConnectionMessage) chatItem;
                                if (ccm.getType().equalsIgnoreCase(ChatItemType.OPERATOR_JOINED.name())) {
                                    consultWriter.setSearchingConsult(false);
                                    consultWriter.setCurrentConsultInfo(ccm);
                                    if (fragment != null) {
                                        fragment.setStateConsultConnected(
                                                new ConsultInfo(
                                                        ccm.getName(),
                                                        ccm.getConsultId(),
                                                        ccm.getStatus(),
                                                        ccm.getOrgUnit(),
                                                        ccm.getAvatarPath()
                                                )
                                        );
                                    }
                                } else {
                                    consultWriter.setCurrentConsultLeft();
                                    if (fragment != null && !consultWriter.isSearchingConsult()) {
                                        fragment.setTitleStateDefault();
                                    }
                                }
                            }
                            if (chatItem instanceof SearchingConsult) {
                                if (fragment != null) {
                                    fragment.setStateSearchingConsult();
                                }
                                consultWriter.setSearchingConsult(true);
                                return;
                            }
                            addMessage(chatItem);
                        })
                        .filter(chatItem -> chatItem instanceof Hidable)
                        .map(chatItem -> (Hidable) chatItem)
                        .delay(item -> Flowable.timer(item.getHideAfter(), TimeUnit.SECONDS))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                hidable -> {
                                    if (hidable instanceof Survey) {
                                        removeActiveSurvey();
                                    }
                                    if (hidable instanceof RequestResolveThread) {
                                        removeResolveRequest();
                                    }
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
    }

    private void subscribeToMessageSendSuccess() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getMessageSendSuccessProcessor())
                        .observeOn(Schedulers.io())
                        .flatMapMaybe(chatItemSent -> {
                            ChatItem chatItem = databaseHolder.getChatItem(chatItemSent.uuid);
                            if (chatItem instanceof UserPhrase) {
                                ThreadsLogger.d(TAG, "server answer on phrase sent with id " + chatItemSent.messageId);
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                userPhrase.setProviderId(chatItemSent.messageId);
                                if (chatItemSent.sentAt > 0) {
                                    userPhrase.setTimeStamp(chatItemSent.sentAt);
                                }
                                userPhrase.setSentState(MessageState.STATE_SENT);
                                databaseHolder.putChatItem(userPhrase);
                            }
                            if (chatItem == null) {
                                ThreadsLogger.e(TAG, "chatItem not found");
                            }
                            return Maybe.fromCallable(() -> chatItem);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItem -> {
                            if (chatItem instanceof UserPhrase) {
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                if (fragment != null) {
                                    fragment.updateChatItem(userPhrase, true);
                                }
                                proceedSendingQueue();
                            }
                        })
        );
    }

    private void subscribeToMessageSendError() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getMessageSendErrorProcessor())
                        .observeOn(Schedulers.io())
                        .flatMapMaybe(uuid -> {
                            ChatItem chatItem = databaseHolder.getChatItem(uuid);
                            if (chatItem instanceof UserPhrase) {
                                ThreadsLogger.d(TAG, "server answer on phrase sent with id " + uuid);
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                userPhrase.setSentState(MessageState.STATE_NOT_SENT);
                                databaseHolder.putChatItem(userPhrase);
                            }
                            if (chatItem == null) {
                                ThreadsLogger.e(TAG, "chatItem not found");
                            }
                            return Maybe.fromCallable(() -> chatItem);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItem -> {
                            if (chatItem instanceof UserPhrase) {
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                if (fragment != null) {
                                    fragment.setMessageState(userPhrase.getProviderId(), userPhrase.getSentState());
                                }
                                addMsgToResendQueue(userPhrase);
                                if (fragment != null && isActive) {
                                    fragment.showConnectionError();
                                }
                                if (!isActive) {
                                    NotificationService.addUnsentMessage(appContext, PrefUtils.getAppMarker());
                                }
                                proceedSendingQueue();
                            }
                        })
        );
    }

    private void subscribeToSurveySendSuccess() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getSurveySendSuccessProcessor())
                        .observeOn(Schedulers.io())
                        .flatMapMaybe(sendingId -> {
                            Survey survey = databaseHolder.getSurvey(sendingId);
                            if (survey == null) {
                                ThreadsLogger.e(TAG, "survey not found");
                            }
                            return Maybe.fromCallable(() -> survey);
                        })
                        .delay(Config.instance.surveyCompletionDelay, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(survey -> {
                            surveyCompletionInProgress = false;
                            setSurveyStateSent(survey);
                            resetActiveSurvey();
                            updateUi();
                        })
        );
    }

    private void subscribeToRemoveChatItem() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getRemoveChatItemProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(chatItemType -> chatItemType.equals(ChatItemType.REQUEST_CLOSE_THREAD))
                        .subscribe(chatItemType -> removeResolveRequest())
        );
    }


    private void subscribeToDeviceAddressChanged() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getDeviceAddressChangedProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItemType -> onSettingClientId())
        );
    }

    private void subscribeToQuickReplies() {
        subscribe(ChatUpdateProcessor.getInstance().getQuickRepliesProcessor()
                .subscribe(quickReplies -> {
                    hasQuickReplies = !quickReplies.isEmpty();
                    refreshUserInputState();
                }));

    }

    private void removeResolveRequest() {
        ThreadsLogger.i(TAG, "removeResolveRequest");
        if (isResolveRequestVisible && fragment != null) {
            if (fragment.removeResolveRequest()) {
                updateUi();
            }
            isResolveRequestVisible = false;
        }
        ThreadsLogger.i(TAG, "removeResolveRequest: " + isResolveRequestVisible);
    }

    private void removeActiveSurvey() {
        ThreadsLogger.i(TAG, "removeActiveSurvey");
        if (activeSurvey != null && fragment != null) {
            final boolean removed = fragment.removeSurvey(activeSurvey.getSendingId());
            if (removed) {
                updateUi();
            }
            resetActiveSurvey();
        }
    }

    private void resetActiveSurvey() {
        ThreadsLogger.i(TAG, "resetActiveSurvey");
        activeSurvey = null;
    }

    private void addMsgToResendQueue(final UserPhrase userPhrase) {
        if (unsendMessages.indexOf(userPhrase) == -1) {
            unsendMessages.add(userPhrase);
            scheduleResend();
        }
    }

    private void scheduleResend() {
        if (!unsendMessageHandler.hasMessages(RESEND_MSG)) {
            unsendMessageHandler.sendEmptyMessageDelayed(RESEND_MSG, resendTimeInterval);
        }
    }

    private void downloadMessagesTillEnd() {
        if (!isDownloadingMessages && !isAllMessagesDownloaded) {
            isDownloadingMessages = true;
            ThreadsLogger.d(TAG, "downloadMessagesTillEnd");
            subscribe(
                    Completable.fromAction(() -> {
                        final HistoryResponse response = HistoryLoader.getHistorySync(lastMessageTimestamp, PER_PAGE_COUNT);
                        final List<ChatItem> serverItems = HistoryParser.getChatItems(response);
                        if (serverItems.isEmpty()) {
                            isDownloadingMessages = false;
                            isAllMessagesDownloaded = true;
                        } else {
                            lastMessageTimestamp = serverItems.get(0).getTimeStamp();
                            isAllMessagesDownloaded = serverItems.size() < PER_PAGE_COUNT; // Backend can give us more than chunk anytime, it will give less only on history end
                            databaseHolder.putChatItems(serverItems);
                            isDownloadingMessages = false;
                            if (!isAllMessagesDownloaded) {
                                downloadMessagesTillEnd();
                            }
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                    },
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            )
            );
        }
    }

    /*
    Вызывается когда получено новое сообщение из канала (TG/PUSH)
     */
    private void addMessage(final ChatItem chatItem) {
        ThreadsLogger.i(TAG, "addMessage: " + chatItem);
        databaseHolder.putChatItem(chatItem);
        if (fragment != null) {
            final ChatItem ci = setLastAvatars(Collections.singletonList(chatItem)).get(0);
            if (!(ci instanceof ConsultConnectionMessage) || ((ConsultConnectionMessage) ci).isDisplayMessage()) {
                fragment.addChatItem(ci);
            }
            if (ci instanceof ConsultChatPhrase) {
                fragment.notifyConsultAvatarChanged(((ConsultChatPhrase) ci).getAvatarPath(), ((ConsultChatPhrase) ci).getConsultId());
            }
        }
        if (chatItem instanceof ConsultPhrase && isActive) {
            ConsultPhrase consultPhrase = (ConsultPhrase) chatItem;
            handleQuickReplies(Collections.singletonList(consultPhrase));
            Config.instance.transport.sendMessageRead(consultPhrase.getProviderId());
        }
        if (isActive) {
            subscribe(
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
                                UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
                            })
            );
        }
        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (chatItem instanceof ConsultPhrase || chatItem instanceof ConsultConnectionMessage || (chatItem instanceof ScheduleInfo && ((ScheduleInfo) chatItem).isChatWorking())) {
            if (fragment != null && fragment.isAdded()) {
                fragment.removeSchedule(false);
            }
        }
    }

    private void queueMessageSending(UserPhrase userPhrase) {
        ThreadsLogger.i(TAG, "queueMessageSending: " + userPhrase);
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
        sendQueue.clear();
        databaseHolder.cleanDatabase();
        if (fragment != null) {
            fragment.cleanChat();
        }
        consultWriter.setCurrentConsultLeft();
        consultWriter.setSearchingConsult(false);
        appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
    }

    private void setSurveyStateSent(final Survey survey) {
        survey.setSentState(MessageState.STATE_SENT);
        if (fragment != null) {
            fragment.setSurveySentStatus(survey.getSendingId(), survey.getSentState());
        }
        databaseHolder.putChatItem(survey);
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

    private void onSettingClientId() {
        ThreadsLogger.i(TAG, "onSettingClientId:");
        subscribe(
                Completable.fromAction(() -> {
                    String newClientId = PrefUtils.getNewClientID();
                    if (fragment != null && !TextUtils.isEmpty(newClientId)) {
                        cleanAll();
                        PrefUtils.setClientId(newClientId);
                        PrefUtils.setClientIdWasSet(true);
                        Config.instance.transport.sendInitChatMessage();
                        Config.instance.transport.sendEnvironmentMessage();
                        final HistoryResponse response = HistoryLoader.getHistorySync(null, true);
                        List<ChatItem> chatItems = HistoryParser.getChatItems(response);
                        databaseHolder.putChatItems(chatItems);
                        setLastAvatars(chatItems);
                        if (fragment != null) {
                            fragment.addChatItems(chatItems);
                            handleQuickReplies(chatItems);
                            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                            if (info != null) {
                                fragment.setStateConsultConnected(info);
                            }
                        }
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                        }, e -> ThreadsLogger.e(TAG, e.getMessage()))
        );
    }

    private void refreshUserInputState() {
        if (hasQuickReplies) {
            chatUpdateProcessor.postUserInputEnableChanged(false);
        } else {
            // Временное решение пока нет ответа по https://track.brooma.ru/issue/THREADS-7708
            if (currentScheduleInfo == null) {
                chatUpdateProcessor.postUserInputEnableChanged(true);
            } else {
                chatUpdateProcessor.postUserInputEnableChanged(
                        currentScheduleInfo.isChatWorking() || currentScheduleInfo.isSendDuringInactive());
            }
        }
    }

    private void handleQuickReplies(List<ChatItem> chatItems) {
        chatUpdateProcessor.postQuickRepliesChanged(getQuickReplies(chatItems));
    }

    public void quickReplyIsSent() {
        chatUpdateProcessor.postQuickRepliesChanged(new ArrayList<>());
    }

    @NonNull
    private List<QuickReply> getQuickReplies(List<ChatItem> chatItems) {
        if (!chatItems.isEmpty()) {
            ListIterator<ChatItem> listIterator = chatItems.listIterator(chatItems.size());
            while (listIterator.hasPrevious()) {
                ChatItem chatItem = listIterator.previous();
                // При некоторых ситуациях (пока неизвестно каких) последнее сообщение в истории ConsultConnectionMessage, который не отображается, его нужно игнорировать
                if (chatItem instanceof ConsultConnectionMessage) {
                    ConsultConnectionMessage consultConnectionMessage = (ConsultConnectionMessage) chatItem;
                    if (!consultConnectionMessage.isDisplayMessage()) {
                        continue;
                    }
                } else if (chatItem instanceof ConsultPhrase) {
                    return ((ConsultPhrase) chatItem).getQuickReplies();
                }
                break;
            }
        }
        return new ArrayList<>();
    }
}
