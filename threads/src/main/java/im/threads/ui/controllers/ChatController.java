package im.threads.ui.controllers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.business.UserInfoBuilder;
import im.threads.business.broadcastReceivers.ProgressReceiver;
import im.threads.business.chat_updates.ChatUpdateProcessor;
import im.threads.business.config.BaseConfig;
import im.threads.business.controllers.UnreadMessagesController;
import im.threads.business.formatters.ChatItemType;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ChatPhrase;
import im.threads.business.models.ClientNotificationDisplayType;
import im.threads.business.models.ConsultChatPhrase;
import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultInfo;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.ConsultTyping;
import im.threads.business.models.FileDescription;
import im.threads.business.models.Hidable;
import im.threads.business.models.InputFieldEnableModel;
import im.threads.business.models.MessageRead;
import im.threads.business.models.MessageState;
import im.threads.business.models.QuickReplyItem;
import im.threads.business.models.RequestResolveThread;
import im.threads.business.models.ScheduleInfo;
import im.threads.business.models.SearchingConsult;
import im.threads.business.models.SimpleSystemMessage;
import im.threads.business.models.Survey;
import im.threads.business.models.SystemMessage;
import im.threads.business.models.UpcomingUserMessage;
import im.threads.business.models.UserPhrase;
import im.threads.business.preferences.Preferences;
import im.threads.business.preferences.PreferencesJava;
import im.threads.business.rest.models.HistoryResponse;
import im.threads.business.rest.models.SettingsResponse;
import im.threads.business.rest.queries.BackendApi;
import im.threads.business.rest.queries.ThreadsApi;
import im.threads.business.secureDatabase.DatabaseHolder;
import im.threads.business.transport.HistoryLoader;
import im.threads.business.transport.HistoryParser;
import im.threads.business.transport.TransportException;
import im.threads.business.transport.models.Attachment;
import im.threads.business.utils.ChatMessageSeeker;
import im.threads.business.utils.ClientInteractor;
import im.threads.business.utils.ConsultWriter;
import im.threads.business.utils.FileUtils;
import im.threads.business.utils.messenger.Messenger;
import im.threads.business.utils.messenger.MessengerImpl;
import im.threads.business.utils.preferences.PrefUtilsBase;
import im.threads.business.workers.FileDownloadWorker;
import im.threads.ui.ChatStyle;
import im.threads.ui.activities.ConsultActivity;
import im.threads.ui.activities.ImagesActivity;
import im.threads.ui.config.Config;
import im.threads.ui.fragments.ChatFragment;
import im.threads.ui.utils.ThreadRunnerKt;
import im.threads.ui.utils.preferences.PrefUtilsUi;
import im.threads.ui.utils.preferences.PreferencesMigrationUi;
import im.threads.ui.workers.NotificationWorker;
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
    private static final int PER_PAGE_COUNT = 100;
    private static final long UPDATE_SPEECH_STATUS_DEBOUNCE = 400L;

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

    private final ChatStyle chatStyle = Config.getInstance().getChatStyle();

    // this flag is keeping the visibility state of the request to resolve thread
    private boolean surveyCompletionInProgress = false;
    // Ссылка на фрагмент, которым управляет контроллер
    @Nullable
    private ChatFragment fragment;
    // Для приема сообщений из сервиса по скачиванию файлов
    private ProgressReceiver progressReceiver;
    // keep an active and visible for user survey id
    private Survey activeSurvey = null;
    private Long lastMessageTimestamp;
    private boolean isActive;
    @NonNull
    private List<ChatItem> lastItems = new ArrayList<>();
    // TODO: вынести в отдельный класс поиск сообщений
    private ChatMessageSeeker seeker = new ChatMessageSeeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;
    private String firstUnreadUuidId;
    // На основе этих переменных определяется возможность отправки сообщений в чат
    private ScheduleInfo currentScheduleInfo;
    // Если пользователь не ответил на вопрос (quickReply), то блокируем поле ввода
    private boolean hasQuickReplies = false;
    // Если пользователь не ответил на вопрос (quickReply), то блокируем поле ввода
    private boolean inputEnabledDuringQuickReplies;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Messenger messenger = new MessengerImpl(compositeDisposable);
    private final ArrayList<UserPhrase> localUserMessages = new ArrayList<>();

    private ChatController() {
        appContext = BaseConfig.instance.context;
        new PreferencesMigrationUi(appContext).migrateNamedPreferences(ChatController.class.getSimpleName());
        inputEnabledDuringQuickReplies = chatStyle.inputEnabledDuringQuickReplies;
        chatUpdateProcessor = ChatUpdateProcessor.getInstance();
        databaseHolder = DatabaseHolder.getInstance();

        consultWriter = new ConsultWriter(PrefUtilsBase.getDefaultSharedPreferences());
        subscribeToChatEvents();
        messenger.resendMessages();
    }

    public static ChatController getInstance() {
        ClientInteractor clientInteractor
                = new ClientInteractor(new Preferences(BaseConfig.instance.context)); //TODO: rewrite it to inject with kotlin
        if (instance == null) {
            instance = new ChatController();
        }
        clientInteractor.initClientId();
        subscribeOnClientIdChange();
        return instance;
    }

    private static void subscribeOnClientIdChange() {
        instance.subscribe(
                Single.fromCallable(() -> instance.onClientIdChanged())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                chatItems -> {
                                    if (instance.fragment != null) {
                                        instance.fragment.addChatItems(chatItems);
                                        instance.handleQuickReplies(chatItems);
                                    }
                                },
                                LoggerEdna::error
                        )
        );
    }

    public void onRatingClick(@NonNull final Survey survey) {
        if (!surveyCompletionInProgress) {
            surveyCompletionInProgress = true;
            subscribeToSurveyCompletion();
        }
        surveyCompletionProcessor.onNext(survey);
    }

    public void onResolveThreadClick(final boolean approveResolve) {
        BaseConfig.instance.transport.sendResolveThread(approveResolve);
    }

    public void onUserTyping(String input) {
        BaseConfig.instance.transport.sendUserTying(input);
    }

    public void onUserInput(@NonNull final UpcomingUserMessage upcomingUserMessage) {
        LoggerEdna.info("onUserInput: " + upcomingUserMessage);
        // If user has written a message while the request to resolve the thread is visible
        // we should make invisible the resolve request
        removeResolveRequest();
        // If user has written a message while the active survey is visible
        // we should make invisible the survey
        removeActiveSurvey();
        final UserPhrase um = convert(upcomingUserMessage);
        if (um.getFileDescription() != null) {
            localUserMessages.add(um);
        }
        addMessage(um);
        messenger.queueMessageSending(um);
    }

    public void fancySearch(final String query, final boolean forward, final Consumer<kotlin.Pair<List<ChatItem>, ChatItem>> consumer) {
        subscribe(
                Single.just(isAllMessagesDownloaded)
                        .flatMap(isAllMessagesDownloaded -> {
                            if (!isAllMessagesDownloaded) {
                                if (query.length() == 1) {
                                    ThreadRunnerKt.runOnUiThread(() -> {
                                        fragment.showProgressBar();
                                        Toast.makeText(appContext, appContext.getString(R.string.threads_history_loading_message), Toast.LENGTH_LONG).show();
                                    });
                                }
                                return messenger.downloadMessagesTillEnd();
                            } else {
                                return Single.fromCallable((Callable<Object>) ArrayList::new);
                            }
                        })
                        .flatMapCompletable(o -> Completable.fromAction(
                                () -> {
                                    if (System.currentTimeMillis() > (lastFancySearchDate + 3000)) {
                                        lastItems = databaseHolder.getChatItems(0, -1);
                                        lastFancySearchDate = System.currentTimeMillis();
                                    }
                                    if (query != null && (query.isEmpty() || !query.equals(lastSearchQuery))) {
                                        seeker = new ChatMessageSeeker();
                                    }
                                    lastSearchQuery = query;
                                    ThreadRunnerKt.runOnUiThread(() -> {
                                        fragment.hideProgressBar();
                                    });
                                })
                        )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> consumer.accept(seeker.searchMessages(lastItems, !forward, query)),
                                e -> {
                                    LoggerEdna.error(e);
                                    ThreadRunnerKt.runOnUiThread(() -> {
                                        fragment.hideProgressBar();
                                    });
                                }
                        )
        );
    }

    public void onFileClick(final FileDescription fileDescription) {
        LoggerEdna.info("onFileClick " + fileDescription);
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                if (fileDescription.getFileUri() == null) {
                    FileDownloadWorker.startDownloadFD(activity, fileDescription);
                } else if (FileUtils.isImage(fileDescription)) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, fileDescription));
                } else {
                    final Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(fileDescription.getFileUri(), FileUtils.getMimeType(fileDescription));
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

    public void setActivityIsForeground(final boolean isForeground) {
        LoggerEdna.info("setActivityIsForeground");
        this.isActive = isForeground;
        if (isForeground && fragment != null && fragment.isAdded()) {
            final ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null
                    && cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                final List<String> uuidList = databaseHolder.getUnreadMessagesUuid();
                if (!uuidList.isEmpty()) {
                    BaseConfig.instance.transport.markMessagesAsRead(uuidList);
                    firstUnreadUuidId = uuidList.get(0); // для скролла к первому непрочитанному сообщению
                } else {
                    firstUnreadUuidId = null;
                }
            }
        }
        subscribe(
                Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .filter(value -> isActive)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            removePushNotification();
                            UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
                        }, LoggerEdna::error)
        );
    }

    public Observable<List<ChatItem>> requestItems(int currentItemsCount, boolean fromBeginning) {
        return Observable
                .fromCallable(() -> {
                    if (instance.fragment != null && !PrefUtilsBase.isClientIdEmpty()) {
                        LoggerEdna.info(ThreadsApi.REST_TAG, "Requesting history items");
                        int count = BaseConfig.instance.historyLoadingCount;
                        try {
                            final HistoryResponse response = HistoryLoader.INSTANCE.getHistorySync(
                                    null,
                                    fromBeginning
                            );
                            List<ChatItem> serverItems = HistoryParser.getChatItems(response);
                            serverItems = addLocalUserMessages(serverItems);
                            messenger.saveMessages(serverItems);
                            clearUnreadPush();
                            processSystemMessages(serverItems);
                            return setLastAvatars(serverItems);
                        } catch (final Exception e) {
                            LoggerEdna.error(ThreadsApi.REST_TAG, "Requesting history items error", e);
                            return setLastAvatars(databaseHolder.getChatItems(currentItemsCount, count));
                        }
                    }
                    return new ArrayList<ChatItem>();
                })
                .subscribeOn(Schedulers.io());
    }

    public void onFileDownloadRequest(final FileDescription fileDescription) {
        if (fragment != null && fragment.isAdded()) {
            final Activity activity = fragment.getActivity();
            if (activity != null) {
                FileDownloadWorker.startDownloadWithNoStop(activity, fileDescription);
            }
        }
    }

    public void onConsultChoose(final Activity activity, final String consultId) {
        if (consultId == null) {
            LoggerEdna.warning("Can't show consult info: consultId == null");
        } else {
            ConsultInfo info = databaseHolder.getConsultInfo(consultId);
            if (info != null) {
                ConsultActivity.startActivity(activity, info);
            } else {
                ConsultActivity.startActivity(activity);
            }
        }
    }

    public boolean isNeedToShowWelcome() {
        return databaseHolder.getMessagesCount() == 0;
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

    public String getFirstUnreadUuidId() {
        return firstUnreadUuidId;
    }

    public void bindFragment(@NonNull final ChatFragment f) {
        LoggerEdna.info("bindFragment: " + f.toString());
        final Activity activity = f.getActivity();
        if (activity == null) {
            return;
        }
        fragment = f;
        fragment.showProgressBar();
        if (consultWriter.isSearchingConsult()) {
            fragment.setStateSearchingConsult();
        }
        if (PrefUtilsBase.isClientIdEmpty()) {
            fragment.showEmptyState();
        }
        subscribe(
                Single.fromCallable(() -> {
                            final int historyLoadingCount = BaseConfig.instance.historyLoadingCount;
                            final List<UserPhrase> unsentUserPhrase = databaseHolder.getUnsendUserPhrase(historyLoadingCount);
                            if (!unsentUserPhrase.isEmpty()) {
                                messenger.recreateUnsentMessagesWith(unsentUserPhrase);
                                messenger.scheduleResend();
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
                                LoggerEdna::error
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

    public void setMessagesInCurrentThreadAsReadInDB() {
        subscribe(DatabaseHolder.getInstance().setAllConsultMessagesWereReadInThread(PrefUtilsBase.getThreadId())
                .subscribe(UnreadMessagesController.INSTANCE::refreshUnreadMessagesCount,
                        error -> LoggerEdna.error("setAllMessagesWereRead() ", error))
        );
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
                        .throttleLast(BaseConfig.instance.surveyCompletionDelay, TimeUnit.MILLISECONDS)
                        .firstElement()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(survey -> BaseConfig.instance.transport.sendRatingDone(survey),
                                error -> LoggerEdna.error("subscribeToSurveyCompletion: ", error)
                        )
        );
    }

    void setAllMessagesWereRead() {
        removePushNotification();
        subscribe(DatabaseHolder.getInstance().setAllConsultMessagesWereRead()
                .subscribe(UnreadMessagesController.INSTANCE::refreshUnreadMessagesCount,
                        error -> LoggerEdna.error("setAllMessagesWereRead() ", error))
        );
        if (fragment != null) {
            fragment.setAllMessagesWereRead();
        }
    }

    private boolean isChatWorking() {
        return currentScheduleInfo == null || currentScheduleInfo.isChatWorking();
    }

    private List<ChatItem> onClientIdChanged() throws Exception {
        LoggerEdna.info(ThreadsApi.REST_TAG, "Client id changed. Loading history.");
        cleanAll();
        if (fragment != null) {
            fragment.removeSearching();
        }
        final HistoryResponse response = HistoryLoader.INSTANCE.getHistorySync(
                null,
                true
        );
        List<ChatItem> serverItems = HistoryParser.getChatItems(response);
        serverItems = addLocalUserMessages(serverItems);
        messenger.saveMessages(serverItems);
        clearUnreadPush();
        processSystemMessages(serverItems);
        if (fragment != null) {
            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
            if (info != null) {
                fragment.setStateConsultConnected(info);
            }
        }
        return setLastAvatars(serverItems);
    }

    public void loadHistory() {
        if (!isDownloadingMessages) {
            if (fragment != null && fragment.isAdded()) {
                fragment.showProgressBar();
            }
            LoggerEdna.info(ThreadsApi.REST_TAG, "Loading history from " + ChatController.class.getSimpleName());
            isDownloadingMessages = true;
            subscribe(
                    Single.fromCallable(() -> {
                                final int count = BaseConfig.instance.historyLoadingCount;
                                final HistoryResponse response = HistoryLoader.INSTANCE.getHistorySync(
                                        count,
                                        true
                                );
                                List<ChatItem> serverItems = HistoryParser.getChatItems(response);
                                serverItems = addLocalUserMessages(serverItems);
                                messenger.saveMessages(serverItems);
                                clearUnreadPush();
                                processSystemMessages(serverItems);
                                if (fragment != null && isActive) {
                                    final List<String> uuidList = databaseHolder.getUnreadMessagesUuid();
                                    if (!uuidList.isEmpty()) {
                                        BaseConfig.instance.transport.markMessagesAsRead(uuidList);
                                    }
                                }
                                return new Pair<>(response == null ? null : response.getConsultInfo(), serverItems);
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    pair -> {
                                        isDownloadingMessages = false;
                                        final List<ChatItem> items = setLastAvatars(pair.second);
                                        if (fragment != null) {
                                            fragment.addChatItems(items);
                                            handleQuickReplies(items);
                                            final ConsultInfo info = pair.first;
                                            if (info != null) {
                                                fragment.setStateConsultConnected(info);
                                            }
                                            fragment.hideProgressBar();
                                        }
                                    },
                                    e -> {
                                        isDownloadingMessages = false;
                                        if (fragment != null) {
                                            fragment.hideProgressBar();
                                        }
                                        LoggerEdna.error(e);
                                    }
                            )
            );
        } else {
            LoggerEdna.info(ThreadsApi.REST_TAG, "Loading history cancelled. isDownloadingMessages = true");
        }
    }

    public void getSettings() {
        subscribe(
                Single.fromCallable(() -> BackendApi.get().settings().execute())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    final SettingsResponse responseBody = response.body();
                                    if (responseBody != null) {
                                        LoggerEdna.info("getting settings : " + responseBody);
                                        String clientNotificationType = responseBody.getClientNotificationDisplayType();
                                        if (clientNotificationType != null && !clientNotificationType.isEmpty()) {
                                            final ClientNotificationDisplayType type = ClientNotificationDisplayType.fromString(clientNotificationType);
                                            PrefUtilsUi.setClientNotificationDisplayType(type);
                                            chatUpdateProcessor.postClientNotificationDisplayType(type);
                                        }
                                    }
                                },
                                e -> {
                                    LoggerEdna.info("error on getting settings : " + e.getMessage());
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )

        );
    }

    public Single<List<ChatItem>> downloadMessagesTillEnd()  {
        return messenger.downloadMessagesTillEnd();
    }

    public void forceResend(UserPhrase userPhrase) {
        messenger.forceResend(userPhrase);
    }

    private List<ChatItem> addLocalUserMessages(List<ChatItem> serverItems)  {
        ArrayList<UserPhrase> localMessagesToDelete = new ArrayList<>();
        for (UserPhrase localUserMessage : localUserMessages) {
            for (ChatItem serverItem : serverItems) {
                if (serverItem.isTheSameItem(localUserMessage)) {
                    localMessagesToDelete.add(localUserMessage);
                    break;
                }
            }
        }
        for (UserPhrase localMessageToDelete : localMessagesToDelete) {
            localUserMessages.remove(localMessageToDelete);
        }
        serverItems.addAll(localUserMessages);

        return serverItems;
    }

    private List<ChatItem> setLastAvatars(final List<ChatItem> list) {
        for (final ChatItem ci : list) {
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

    public void hideEmptyState() {
        if (!PrefUtilsBase.isClientIdEmpty()) {
            if (fragment != null) {
                fragment.hideEmptyState();
            }
        }
    }

    private void subscribeToChatEvents() {
        subscribeToTyping();
        subscribeToOutgoingMessageRead();
        subscribeToIncomingMessageRead();
        subscribeToNewMessage();
        subscribeToUpdateAttachments();
        subscribeToMessageSendSuccess();
        subscribeToCampaignMessageReplySuccess();
        subscribeToMessageSendError();
        subscribeToSurveySendSuccess();
        subscribeToRemoveChatItem();
        subscribeToDeviceAddressChanged();
        subscribeToQuickReplies();
        subscribeToAttachAudioFiles();
        subscribeToClientNotificationDisplayTypeProcessor();
        subscribeSpeechMessageUpdated();
        subscribeForResendMessage();
    }

    private void subscribeToCampaignMessageReplySuccess() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getCampaignMessageReplySuccessProcessor())
                        .observeOn(Schedulers.io())
                        .delay(1000, TimeUnit.MILLISECONDS)
                        .subscribe(chatItem -> loadHistory(),
                                onError -> LoggerEdna.error("subscribeToCampaignMessageReplySuccess ", onError))
        );
    }

    private void subscribeToTyping() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getTypingProcessor())
                        .map(clientId ->
                                new ConsultTyping(
                                        consultWriter.getCurrentConsultId(),
                                        System.currentTimeMillis(),
                                        consultWriter.getCurrentPhotoUrl()
                                )
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::addMessage,
                                error -> LoggerEdna.error("subscribeToTyping ", error)
                        )
        );
    }

    private void subscribeToOutgoingMessageRead() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getOutgoingMessageReadProcessor())
                        .observeOn(Schedulers.io())
                        .doOnNext(providerId -> databaseHolder.setStateOfUserPhraseByProviderId(providerId, MessageState.STATE_WAS_READ))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                providerId -> {
                                    if (fragment != null) {
                                        fragment.setMessageState(providerId, MessageState.STATE_WAS_READ);
                                    }
                                },
                                error -> LoggerEdna.error("subscribeToOutgoingMessageRead ", error)
                        )
        );
    }

    private void subscribeToIncomingMessageRead() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getIncomingMessageReadProcessor())
                        .observeOn(Schedulers.io())
                        .subscribe(id -> {
                                    databaseHolder.setMessageWasRead(id);
                                    UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
                                },
                                error -> LoggerEdna.error("subscribeToIncomingMessageRead ", error)
                        )
        );
    }

    private void subscribeSpeechMessageUpdated() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getSpeechMessageUpdateProcessor())
                        .debounce(UPDATE_SPEECH_STATUS_DEBOUNCE, TimeUnit.MILLISECONDS)
                        .map(speechMessageUpdate -> {
                            databaseHolder.saveSpeechMessageUpdate(speechMessageUpdate);
                            ChatItem itemFromDb = databaseHolder.getChatItem(speechMessageUpdate.getUuid());
                            if (itemFromDb == null) {
                                return speechMessageUpdate;
                            } else {
                                return itemFromDb;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItem -> {
                                    if (fragment != null) {
                                        fragment.addChatItem(chatItem);
                                    }
                                },
                                error -> LoggerEdna.error("subscribeSpeechMessageUpdated " + error)
                        )
        );
    }

    private void subscribeToUpdateAttachments() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getUpdateAttachmentsProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(attachments -> {
                                    for (Attachment attachment : attachments) {
                                        for (ChatItem item : databaseHolder.getChatItems(0, PER_PAGE_COUNT)) {
                                            if (item instanceof ChatPhrase) {
                                                ChatPhrase phrase = (ChatPhrase) item;
                                                if (phrase.getFileDescription() != null) {
                                                    boolean incomingNameEquals = phrase.getFileDescription().getIncomingName() != null
                                                            && phrase.getFileDescription().getIncomingName().equals(attachment.getName());
                                                    boolean isUrlHashFileName = phrase.getFileDescription().getFileUri() != null
                                                            && phrase.getFileDescription().getFileUri().toString().contains(attachment.getName());
                                                    if ((incomingNameEquals || isUrlHashFileName)
                                                            && fragment != null && fragment.isAdded()) {
                                                        phrase.getFileDescription().setState(attachment.getState());
                                                        phrase.getFileDescription().setErrorCode(attachment.getErrorCodeState());
                                                        phrase.getFileDescription().setDownloadPath(attachment.getResult());
                                                        fragment.updateProgress(phrase.getFileDescription());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                e -> LoggerEdna.error(e)
                        )
        );
    }

    private void subscribeToNewMessage() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getNewMessageProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(chatItem -> {
                            if (chatItem instanceof MessageRead) {
                                List<String> readMessagesIds = ((MessageRead) chatItem).getMessageId();
                                for (String readId : readMessagesIds) {
                                    UserPhrase userPhrase = (UserPhrase) databaseHolder.getChatItem(readId);
                                    if (userPhrase != null) {
                                        chatUpdateProcessor.postOutgoingMessageWasRead(userPhrase.getProviderId());
                                    }
                                }
                                return;
                            }
                            if (chatItem instanceof ScheduleInfo) {
                                currentScheduleInfo = (ScheduleInfo) chatItem;
                                currentScheduleInfo.calculateServerTimeDiff();
                                refreshUserInputState();
                                if (!isChatWorking()) {
                                    consultWriter.setSearchingConsult(false);
                                    if (fragment != null) {
                                        fragment.removeSearching();
                                        fragment.setTitleStateDefault();
                                    }
                                }
                            }
                            if (chatItem instanceof ConsultConnectionMessage) {
                                processConsultConnectionMessage((ConsultConnectionMessage) chatItem);
                            }
                            if (chatItem instanceof SearchingConsult) {
                                if (fragment != null) {
                                    fragment.setStateSearchingConsult();
                                }
                                consultWriter.setSearchingConsult(true);
                                return;
                            }
                            if (chatItem instanceof SimpleSystemMessage) {
                                processSimpleSystemMessage((SimpleSystemMessage) chatItem);
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
                                },
                                LoggerEdna::error
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
                                LoggerEdna.debug("server answer on phrase sent with id " + chatItemSent.messageId);
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                userPhrase.setProviderId(chatItemSent.messageId);
                                if (chatItemSent.sentAt > 0) {
                                    userPhrase.setTimeStamp(chatItemSent.sentAt);
                                }
                                userPhrase.setSentState(MessageState.STATE_SENT);
                                databaseHolder.putChatItem(userPhrase);
                            }
                            if (chatItem == null) {
                                LoggerEdna.error("chatItem not found");
                            }
                            return Maybe.fromCallable(() -> chatItem);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItem -> {
                                    if (chatItem instanceof UserPhrase) {
                                        UserPhrase userPhrase = (UserPhrase) chatItem;
                                        if (fragment != null) {
                                            fragment.addChatItem(userPhrase);
                                        }
                                        messenger.proceedSendingQueue(userPhrase);
                                    }
                                },
                                error -> LoggerEdna.error("subscribeToMessageSendSuccess ", error)
                        )
        );
    }

    private void subscribeToMessageSendError() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getMessageSendErrorProcessor())
                        .observeOn(Schedulers.io())
                        .flatMapMaybe(chatItemSendErrorModel -> {
                            String phraseUuid = chatItemSendErrorModel.getUserPhraseUuid();
                            ChatItem chatItem = databaseHolder.getChatItem(phraseUuid);
                            if (chatItem instanceof UserPhrase) {
                                LoggerEdna.debug("server answer on phrase sent with id " + phraseUuid);
                                UserPhrase userPhrase = (UserPhrase) chatItem;
                                userPhrase.setSentState(MessageState.STATE_NOT_SENT);
                                databaseHolder.putChatItem(userPhrase);
                            }
                            if (chatItem == null) {
                                LoggerEdna.error("chatItem not found");
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
                                        messenger.addMsgToResendQueue(userPhrase);
                                        if (fragment != null && isActive) {
                                            fragment.showConnectionError();
                                        }
                                        if (!isActive) {
                                            PreferencesJava preferences = new PreferencesJava();
                                            UserInfoBuilder userInfo = preferences.getUserInfo();
                                            NotificationWorker.addUnsentMessage(appContext, userInfo.getAppMarker());
                                        }
                                        messenger.proceedSendingQueue(userPhrase);
                                    }
                                },
                                error -> LoggerEdna.error("subscribeToMessageSendError ", error)
                        )
        );
    }

    private void subscribeToSurveySendSuccess() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getSurveySendSuccessProcessor())
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(survey -> {
                                    surveyCompletionInProgress = false;
                                    setSurveyStateSent(survey);
                                    resetActiveSurvey();
                                },
                                error -> LoggerEdna.error("subscribeToSurveySendSuccess ", error)
                        )
        );
    }

    private void subscribeToRemoveChatItem() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getRemoveChatItemProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(chatItemType -> chatItemType.equals(ChatItemType.REQUEST_CLOSE_THREAD))
                        .subscribe(chatItemType -> removeResolveRequest(),
                                error -> LoggerEdna.error("subscribeToRemoveChatItem ", error)
                        )
        );
    }

    private void subscribeToDeviceAddressChanged() {
        subscribe(
                Flowable.fromPublisher(chatUpdateProcessor.getDeviceAddressChangedProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chatItemType -> onDeviceAddressChanged(),
                                error -> LoggerEdna.error("subscribeToDeviceAddressChanged ", error)
                        )
        );
    }

    private void subscribeToQuickReplies() {
        subscribe(ChatUpdateProcessor.getInstance().getQuickRepliesProcessor()
                .subscribe(quickReplies -> {
                            hasQuickReplies = quickReplies != null && !quickReplies.getItems().isEmpty();
                            refreshUserInputState();
                        },
                        error -> LoggerEdna.error("subscribeToQuickReplies ", error)
                )
        );
    }

    private void subscribeToAttachAudioFiles() {
        subscribe(ChatUpdateProcessor.getInstance().getAttachAudioFilesProcessor()
                .subscribe(hasFile -> {
                            refreshUserInputState();
                        },
                        error -> LoggerEdna.error("subscribeToAttachAudioFiles ", error)
                )
        );
    }

    private void subscribeToClientNotificationDisplayTypeProcessor() {
        subscribe(ChatUpdateProcessor.getInstance().getClientNotificationDisplayTypeProcessor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        type -> fragment.setClientNotificationDisplayType(type),
                        LoggerEdna::error
                )
        );
    }

    private void subscribeForResendMessage() {
        subscribe(messenger.getResendStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(providerId -> {
                    if (fragment != null) {
                        fragment.setMessageState(providerId, MessageState.STATE_SENDING);
                    }
                }, LoggerEdna::error));
    }

    private void removeResolveRequest() {
        LoggerEdna.info("removeResolveRequest");
        subscribe(
                databaseHolder.setOldRequestResolveThreadDisplayMessageToFalse()
                        .subscribe(
                                () -> LoggerEdna.info("removeResolveRequest"),
                                LoggerEdna::error
                        )
        );
        if (fragment != null) {
            fragment.removeResolveRequest();
        }
    }

    private void removeActiveSurvey() {
        LoggerEdna.info("removeActiveSurvey");
        subscribe(
                databaseHolder.setNotSentSurveyDisplayMessageToFalse()
                        .subscribe(
                                () -> LoggerEdna.info("setOldSurveyDisplayMessageToFalse"),
                                LoggerEdna::error
                        )
        );
        if (activeSurvey != null && fragment != null) {
            fragment.removeSurvey(activeSurvey.getSendingId());
            resetActiveSurvey();
        }
    }

    private void resetActiveSurvey() {
        LoggerEdna.info("resetActiveSurvey");
        activeSurvey = null;
    }

    /*
    Вызывается когда получено новое сообщение из канала (TG/PUSH)
     */
    private void addMessage(final ChatItem chatItem) {
        LoggerEdna.info("addMessage: " + chatItem);
        databaseHolder.putChatItem(chatItem);
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
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
            BaseConfig.instance.transport.markMessagesAsRead(Collections.singletonList(consultPhrase.getId()));
        }
        if (chatItem instanceof SimpleSystemMessage && isActive) {
            hideQuickReplies();
        }
        if (chatItem instanceof Survey && isActive) {
            BaseConfig.instance.transport.markMessagesAsRead(Collections.singletonList(((Survey) chatItem).getUuid()));
        }
        if (chatItem instanceof RequestResolveThread && isActive) {
            BaseConfig.instance.transport.markMessagesAsRead(Collections.singletonList(((RequestResolveThread) chatItem).getUuid()));
        }
        subscribe(
                Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .filter(value -> isActive)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                                    removePushNotification();
                                    UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
                                },
                                error -> LoggerEdna.error("addMessage ", error)
                        )
        );
        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (
                chatItem instanceof ConsultPhrase ||
                        (chatItem instanceof ConsultConnectionMessage && ChatItemType.OPERATOR_JOINED.name().equals(((ConsultConnectionMessage) chatItem).getType())) ||
                        (chatItem instanceof ScheduleInfo && ((ScheduleInfo) chatItem).isChatWorking())
        ) {
            if (fragment != null && fragment.isAdded()) {
                fragment.removeSchedule(false);
            }
        }
    }

    public void cleanAll() {
        LoggerEdna.info("cleanAll: ");
        isAllMessagesDownloaded = false;
        messenger.clearSendQueue();
        databaseHolder.cleanDatabase();
        if (fragment != null) {
            fragment.cleanChat();
        }
        PrefUtilsBase.setThreadId(-1);
        consultWriter.setCurrentConsultLeft();
        consultWriter.setSearchingConsult(false);
        removePushNotification();
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount();
    }

    private void removePushNotification() {
        NotificationWorker.removeNotification(appContext);
    }

    private void setSurveyStateSent(final Survey survey) {
        survey.setSentState(MessageState.STATE_SENT);
        survey.setDisplayMessage(true);
        if (fragment != null) {
            fragment.setSurveySentStatus(survey.getSendingId(), survey.getSentState());
        }
        databaseHolder.putChatItem(survey);
    }

    private UserPhrase convert(@NonNull final UpcomingUserMessage message) {
        final UserPhrase up = new UserPhrase(
                message.getText(),
                message.getQuote(),
                System.currentTimeMillis(),
                message.getFileDescription(),
                null
        );
        up.setCopy(message.getCopied());
        up.setCampaignMessage(message.getCampaignMessage());
        return up;
    }

    private void onDeviceAddressChanged() {
        LoggerEdna.info(ThreadsApi.REST_TAG, "onDeviceAddressChanged. Loading history.");
        String clientId = PrefUtilsBase.getClientID();
        if (fragment != null && !TextUtils.isEmpty(clientId)) {
            subscribe(
                    Single.fromCallable(() -> {
                                BaseConfig.instance.transport.sendInit();
                                final HistoryResponse response = HistoryLoader.INSTANCE.getHistorySync(
                                        null,
                                        true
                                );
                                List<ChatItem> chatItems = HistoryParser.getChatItems(response);
                                chatItems = addLocalUserMessages(chatItems);
                                messenger.saveMessages(chatItems);
                                clearUnreadPush();
                                processSystemMessages(chatItems);
                                return new Pair<>(response != null ? response.getConsultInfo() : null, setLastAvatars(chatItems));
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    pair -> {
                                        List<ChatItem> chatItems = pair.second;
                                        if (fragment != null) {
                                            fragment.addChatItems(chatItems);
                                            handleQuickReplies(chatItems);
                                            final ConsultInfo info = pair.first;
                                            if (info != null) {
                                                fragment.setStateConsultConnected(info);
                                            }
                                        }
                                    },
                                    LoggerEdna::error
                            )
            );
        } else {
            LoggerEdna.info(
                    ThreadsApi.REST_TAG,
                    "Loading history cancelled in onDeviceAddressChanged. " +
                            "fragment != null && !TextUtils.isEmpty(clientId) == false"
            );
        }
    }

    private void clearUnreadPush() {
        UnreadMessagesController.INSTANCE.clearUnreadPush();
    }

    private void processSystemMessages(List<ChatItem> chatItems) {
        if (!isChatWorking()) {
            return;
        }

        ChatItem latestSystemMessage = null;
        for (ChatItem chatItem : chatItems) {
            if (chatItem instanceof SystemMessage) {
                final Long threadId = chatItem.getThreadId();
                if (threadId != null && threadId >= PrefUtilsBase.getThreadId()) {
                    final String type = ((SystemMessage) chatItem).getType();
                    if (ChatItemType.OPERATOR_JOINED.toString().equalsIgnoreCase(type) ||
                            ChatItemType.THREAD_ENQUEUED.toString().equalsIgnoreCase(type) ||
                            ChatItemType.THREAD_WILL_BE_REASSIGNED.toString().equalsIgnoreCase(type) ||
                            ChatItemType.AVERAGE_WAIT_TIME.toString().equalsIgnoreCase(type) ||
                            ChatItemType.THREAD_CLOSED.toString().equalsIgnoreCase(type)) {
                        if (latestSystemMessage == null || latestSystemMessage.getTimeStamp() <= chatItem.getTimeStamp()) {
                            latestSystemMessage = chatItem;
                        }
                    }
                }
            }
        }
        if (latestSystemMessage != null) {
            final ChatItem systemMessage = latestSystemMessage;
            ThreadRunnerKt.runOnUiThread(() -> {
                if (systemMessage instanceof ConsultConnectionMessage) {
                    processConsultConnectionMessage((ConsultConnectionMessage) systemMessage);
                } else {
                    processSimpleSystemMessage((SimpleSystemMessage) systemMessage);
                }
            });
        }
    }

    @MainThread
    private void processConsultConnectionMessage(ConsultConnectionMessage ccm) {
        if (ccm.getType().equalsIgnoreCase(ChatItemType.OPERATOR_JOINED.name())) {
            if (ccm.getThreadId() != null) {
                PrefUtilsBase.setThreadId(ccm.getThreadId());
                if (fragment != null) {
                    fragment.setCurrentThreadId(ccm.getThreadId());
                }
            }
            consultWriter.setSearchingConsult(false);
            consultWriter.setCurrentConsultInfo(ccm);
            if (fragment != null) {
                fragment.setStateConsultConnected(
                        new ConsultInfo(
                                ccm.getName(),
                                ccm.getConsultId(),
                                ccm.getStatus(),
                                ccm.getOrgUnit(),
                                ccm.getAvatarPath(),
                                ccm.getRole()
                        )
                );
            }
        }
    }

    @MainThread
    private void processSimpleSystemMessage(SimpleSystemMessage systemMessage) {
        final String type = systemMessage.getType();
        if (ChatItemType.THREAD_CLOSED.name().equalsIgnoreCase(type)) {
            PrefUtilsBase.setThreadId(-1);
            removeResolveRequest();
            consultWriter.setCurrentConsultLeft();
            if (fragment != null && !consultWriter.isSearchingConsult()) {
                fragment.setTitleStateDefault();
            }
        } else {
            if (systemMessage.getThreadId() != null) {
                PrefUtilsBase.setThreadId(systemMessage.getThreadId());
                if (fragment != null) {
                    fragment.setCurrentThreadId(systemMessage.getThreadId());
                }
            }
            if (ChatItemType.THREAD_ENQUEUED.name().equalsIgnoreCase(type) ||
                    ChatItemType.THREAD_WILL_BE_REASSIGNED.name().equalsIgnoreCase(type) ||
                    ChatItemType.AVERAGE_WAIT_TIME.name().equalsIgnoreCase(type)) {
                if (fragment != null) {
                    fragment.setStateSearchingConsult();
                }
                consultWriter.setSearchingConsult(true);
            }
        }
    }

    private void refreshUserInputState() {
        chatUpdateProcessor.postUserInputEnableChanged(new InputFieldEnableModel(isInputFieldEnabled(), isSendButtonEnabled()));
    }

    public boolean isInputFieldEnabled() {
        if (fragment != null && fragment.getFileDescription() != null && FileUtils.isVoiceMessage(fragment.getFileDescription())) {
            return false;
        }
        return isSendButtonEnabled();
    }

    public boolean isSendButtonEnabled() {
        if (hasQuickReplies && !inputEnabledDuringQuickReplies) {
            return false;
        }
        return enableInputBySchedule();
    }

    private boolean enableInputBySchedule() {
        //todo
        // Это было до меня. Временное решение пока нет ответа по https://track.brooma.ru/issue/THREADS-7708
        if (currentScheduleInfo == null) {
            return true;
        } else {
            return currentScheduleInfo.isChatWorking() || currentScheduleInfo.isSendDuringInactive();
        }
    }

    private void handleQuickReplies(List<ChatItem> chatItems) {
        ConsultPhrase quickReplyMessageCandidate = getQuickReplyMessageCandidate(chatItems);
        if (quickReplyMessageCandidate != null) {
            if (quickReplyMessageCandidate.isBlockInput() != null) {
                inputEnabledDuringQuickReplies = Boolean.FALSE.equals(quickReplyMessageCandidate.isBlockInput());
            } else {
                inputEnabledDuringQuickReplies = chatStyle.inputEnabledDuringQuickReplies;
            }
            chatUpdateProcessor.postQuickRepliesChanged(
                    new QuickReplyItem(quickReplyMessageCandidate.getQuickReplies(), quickReplyMessageCandidate.getTimeStamp() + 1));
        } else {
            hideQuickReplies();
        }
    }

    public void hideQuickReplies() {
        chatUpdateProcessor.postQuickRepliesChanged(new QuickReplyItem(new ArrayList<>(), 0));
    }

    @Nullable
    private ConsultPhrase getQuickReplyMessageCandidate(List<ChatItem> chatItems) {
        if (chatItems != null && !chatItems.isEmpty()) {
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
                    return ((ConsultPhrase) chatItem);
                }
                break;
            }
        }
        return null;
    }
}
