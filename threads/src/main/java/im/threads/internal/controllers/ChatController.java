package im.threads.internal.controllers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.ThreadsLib;
import im.threads.internal.Config;
import im.threads.internal.activities.ConsultActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.ChatMessageType;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.opengraph.OGData;
import im.threads.internal.opengraph.OGDataProvider;
import im.threads.internal.services.DownloadService;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.HistoryFormatter;
import im.threads.internal.transport.HistoryLoader;
import im.threads.internal.utils.Callback;
import im.threads.internal.utils.CallbackNoError;
import im.threads.internal.utils.ConsultWriter;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.DualFilePoster;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.Seeker;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.UrlUtils;
import im.threads.view.ChatFragment;
import io.reactivex.Completable;
import io.reactivex.Flowable;
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
    private static final String TAG = "ChatController ";

    // Состояния консультанта
    public static final int CONSULT_STATE_FOUND = 1;
    public static final int CONSULT_STATE_SEARCHING = 2;
    public static final int CONSULT_STATE_DEFAULT = 3;

    private static final int PER_PAGE_COUNT = 100;

    private static final int RESEND_MSG = 123;

    private static ChatController instance;

    private final PublishProcessor<Survey> surveyCompletionProcessor = PublishProcessor.create();
    private boolean surveyCompletionInProgress = false;

    @NonNull
    private final Context appContext;
    @NonNull
    private final DatabaseHolder databaseHolder;
    @NonNull
    private final ConsultWriter consultWriter;

    // Ссылка на фрагмент, которым управляет контроллер
    private ChatFragment fragment;

    // Для приема сообщений из сервиса по скачиванию файлов
    private ProgressReceiver mProgressReceiver;
    // Было получено сообщение, что чат не работает
    private boolean isChatWorking;
    // this flag is keeping the visibility state of the request to resolve thread
    private boolean isResolveRequestVisible;

    // keep an active and visible for user survey id
    private long activeSurveySendingId;
    private Long lastMessageTimestamp;
    private boolean isActive;
    private List<ChatItem> lastItems = new ArrayList<>();
    private Seeker seeker = new Seeker();
    private long lastFancySearchDate = 0;
    private String lastSearchQuery = "";
    private boolean isAllMessagesDownloaded = false;
    private boolean isDownloadingMessages;
    private List<UserPhrase> unsendMessages = new ArrayList<>();
    private int resendTimeInterval;
    private List<UserPhrase> sendQueue = new ArrayList<>();

    private Handler unsendMessageHandler;
    private String firstUnreadProviderId;

    private CompositeDisposable compositeDisposable;

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
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
            instance.subscribe(
                    Completable.fromAction(() -> instance.onClientIdChanged(clientId))
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                    },
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            )
            );
        }
        return instance;
    }

    private ChatController() {
        appContext = Config.instance.context;
        databaseHolder = DatabaseHolder.getInstance();
        consultWriter = new ConsultWriter(appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE));
        resendTimeInterval = appContext.getResources().getInteger(R.integer.check_internet_interval_ms);
        unsendMessageHandler = new Handler(msg -> {
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
        });
        subscribe(
                Flowable.fromPublisher(ChatUpdateProcessor.getInstance().getTypingProcessor())
                        .filter(clientId -> ObjectsCompat.equals(PrefUtils.getClientID(), clientId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                clientId -> addMessage(
                                        new ConsultTyping(
                                                consultWriter.getCurrentConsultId(),
                                                System.currentTimeMillis(),
                                                consultWriter.getCurrentPhotoUrl()
                                        )
                                )
                        )
        );
        subscribe(
                Flowable.fromPublisher(ChatUpdateProcessor.getInstance().getMessageReadProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                providerId -> {
                                    if (fragment != null) {
                                        fragment.setMessageState(providerId, MessageState.STATE_WAS_READ);
                                    }
                                    databaseHolder.setStateOfUserPhraseByProviderId(providerId, MessageState.STATE_WAS_READ);
                                }
                        )
        );
        subscribe(
                Flowable.fromPublisher(ChatUpdateProcessor.getInstance().getNewMessageProcessor())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::onNewMessage
                        )
        );
    }

    public void onRatingClick(@NonNull final Survey survey) {
//        final ChatItem chatItem = convertRatingItem(survey); //TODO THREADS-3395 Figure out what is this for
        if (!surveyCompletionInProgress) {
            surveyCompletionInProgress = true;
            subscribeToSurveyCompletion();
            addMessage(survey);
        }
        surveyCompletionProcessor.onNext(survey);
    }

    public void onResolveThreadClick(final boolean approveResolve) {
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendResolveThread(approveResolve))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::removeResolveRequest,
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
    }

    public void onUserTyping(String input) {
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendUserTying(input))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
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
        if (isChatWorking && !consultWriter.isConsultConnected()) {
            if (fragment != null) {
                fragment.setStateSearchingConsult();
            }
            consultWriter.setSearchingConsult(true);
        }
        queueMessageSending(um);
        checkAndLoadOgData(um);
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
                    final List<String> unreadProviderIds = databaseHolder.getUnreadMessagesProviderIds();
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
        if (isActive) {
            subscribe(
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
                                notifyUnreadMessagesCountChanged();
                            })
            );
        }
    }

    public void requestItems(final Callback<List<ChatItem>, Throwable> callback) {
        ThreadsLogger.i(TAG, "isClientIdSet = " + PrefUtils.isClientIdSet());
        if (!PrefUtils.isClientIdNotEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        if (instance.fragment != null) {
            subscribe(
                    Single.fromCallable(() -> {
                        final int[] currentOffset = {instance.fragment.getCurrentItemsCount()};
                        int count = Config.instance.historyLoadingCount;
                        try {
                            final HistoryResponse response = HistoryLoader.getHistorySync(null, false);
                            final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
                            count = serverItems.size();
                            databaseHolder.putMessagesSync(serverItems);
                            final List<ChatItem> chatItems = setLastAvatars(databaseHolder.getChatItems(currentOffset[0], count));
                            currentOffset[0] += chatItems.size();
                            return chatItems;
                        } catch (final Exception e) {
                            ThreadsLogger.e(TAG, "requestItems", e);
                            final List<ChatItem> chatItems = setLastAvatars(databaseHolder.getChatItems(currentOffset[0], count));
                            currentOffset[0] += chatItems.size();
                            return chatItems;
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    callback::onSuccess,
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            ));
        }
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

    public void onConsultChoose(final Activity activity, final String consultId) {
        if (consultId == null) {
            ThreadsLogger.w(TAG, "Can't show consult info: consultId == null");
        } else {
            ConsultInfo info = databaseHolder.getConsultInfoSync(consultId);
            if (info == null) info = new ConsultInfo("", consultId, "", "", "");
            final Intent i = ConsultActivity.getStartIntent(activity, info.getPhotoUrl(), info.getName(), info.getStatus());
            activity.startActivity(i);
        }
    }

    public boolean isNeedToShowWelcome() {
        return databaseHolder.getMessagesCount() <= 0;
    }

    public int getStateOfConsult() {
        if (consultWriter.istSearchingConsult()) {
            return CONSULT_STATE_SEARCHING;
        } else if (consultWriter.isConsultConnected()) {
            return CONSULT_STATE_FOUND;
        } else {
            return CONSULT_STATE_DEFAULT;
        }
    }

    public boolean isConsultFound() {
        return isChatWorking && consultWriter.isConsultConnected();
    }

    public ConsultInfo getCurrentConsultInfo() {
        return consultWriter.getCurrentConsultInfo();
    }

    public String getFirstUnreadProviderId() {
        return firstUnreadProviderId;
    }

    public void bindFragment(final ChatFragment f) {
        ThreadsLogger.i(TAG, "bindFragment:");
        final Activity activity = f.getActivity();
        if (activity == null) {
            return;
        }
        fragment = f;
        if (consultWriter.istSearchingConsult()) {
            fragment.setStateSearchingConsult();
        }
        subscribe(
                Single.fromCallable(this::updateChatItemsOnBind)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                items -> {
                                    if (fragment != null) {
                                        fragment.addChatItems(items);
                                    }
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendInitChatMessage())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                },
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendEnvironmentMessage(PrefUtils.getClientID()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, e -> ThreadsLogger.e(TAG, e.getMessage()))
        );
        if (consultWriter.isConsultConnected()) {
            fragment.setStateConsultConnected(consultWriter.getCurrentConsultInfo());
        } else if (consultWriter.istSearchingConsult()) {
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
     * NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ.
     * Данный тип сообщения отправляется в Сервис пуш уведомлений при прочтении сообщений.
     * <p>
     * Можно было бы поместить оповещение в точку прихода NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ,
     * но иногда в этот момент в сообщения еще не помечены, как прочитанные.
     */
    private void notifyUnreadMessagesCountChanged() {
        ThreadsLib.UnreadMessagesCountListener l = Config.instance.unreadMessagesCountListener;
        if (l != null) {
            DatabaseHolder.getInstance().getUnreadMessagesCount(false, l);
        }
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
                        .subscribe(survey -> {
                                    Config.instance.transport.sendRatingDone(survey);
                                    surveyCompletionInProgress = false;
                                    setSurveyStateSent(survey);
                                    resetActiveSurvey();
                                    updateUi();
                                }
                        )
        );
    }

    void setAllMessagesWereRead() {
        appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
        DatabaseHolder.getInstance().setAllMessagesRead(new CompletionHandler<Void>() {
            @Override
            public void onComplete(final Void data) {
                notifyUnreadMessagesCountChanged();
            }

            @Override
            public void onError(final Throwable e, final String message, final Void data) {
            }
        });
        if (fragment != null) {
            fragment.setAllMessagesWereRead();
        }
    }

    private void onClientIdChanged(final String finalClientId) throws Exception {
        instance.cleanAll();
        if (instance.fragment != null) {
            instance.fragment.removeSearching();
        }
        instance.consultWriter.setCurrentConsultLeft();
        Config.instance.transport.onClientIdChanged(finalClientId);
        final HistoryResponse response = HistoryLoader.getHistorySync(null, true);
        final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
        instance.databaseHolder.putMessagesSync(serverItems);
        final List<ChatItem> phrases = instance.setLastAvatars(serverItems);
        if (instance.fragment != null) {
            instance.fragment.addChatItems(phrases);
            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
            if (info != null) {
                instance.fragment.setStateConsultConnected(info);
            }
        }
        instance.checkAndLoadOgData(phrases);
        PrefUtils.setClientIdWasSet(true);
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

    private List<ChatItem> updateChatItemsOnBind() {
        List<ChatItem> items = new ArrayList<>();
        if (fragment != null) {
            final int historyLoadingCount = Config.instance.historyLoadingCount;
            items = setLastAvatars(databaseHolder.getChatItems(0, historyLoadingCount));
            checkAndLoadOgData(items);
            final List<UserPhrase> unsendUserPhrase = databaseHolder.getUnsendUserPhrase(historyLoadingCount);
            if (!unsendUserPhrase.isEmpty()) {
                unsendMessages.clear();
                unsendMessages.addAll(unsendUserPhrase);
                scheduleResend();
            }
        }
        loadHistory();
        return items;
    }

    public void logoutClient(@NonNull final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            subscribe(
                    Completable.fromAction(() -> Config.instance.transport.sendClientOffline(clientId))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                    },
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            )
            );
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
                        final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
                        final List<ChatItem> dbItems = databaseHolder.getChatItems(0, count);
                        if (dbItems.size() != serverItems.size() || !dbItems.containsAll(serverItems)) {
                            ThreadsLogger.d(TAG, "Local and downloaded history are not same");
                            databaseHolder.putMessagesSync(serverItems);
                        }
                        return response;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                        isDownloadingMessages = false;
                                        final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
                                        final int serverCount = serverItems.size();
                                        final List<ChatItem> items = setLastAvatars(databaseHolder.getChatItems(0, serverCount));
                                        if (fragment != null) {
                                            fragment.addChatItems(items);
                                            checkAndLoadOgData(items);
                                            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
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
                ccm.setAvatarPath(databaseHolder.getLastConsultAvatarPathSync(ccm.getConsultId()));
            }
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                cp.setAvatarPath(databaseHolder.getLastConsultAvatarPathSync(cp.getConsultId()));
            }
        }
        return list;
    }

    private void sendMessage(final UserPhrase userPhrase) {
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
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendEnvironmentMessage(PrefUtils.getClientID()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, e -> ThreadsLogger.e(TAG, e.getMessage()))
        );
    }

    private void sendTextMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        sendMessage(userPhrase, consultInfo, null, null);
    }

    private void sendFileMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo) {
        final FileDescription fileDescription = userPhrase.getFileDescription();
        final FileDescription quoteFileDescription = userPhrase.getQuote() != null ? userPhrase.getQuote().getFileDescription() : null;
        new DualFilePoster(fileDescription, quoteFileDescription, appContext) {
            @Override
            public void onResult(final String mfmsFilePath, final String mfmsQuoteFilePath) {
                ThreadsLogger.i(TAG, "onResult mfmsFilePath =" + mfmsFilePath + " mfmsQuoteFilePath = " + mfmsQuoteFilePath);
                sendMessage(userPhrase, consultInfo, mfmsFilePath, mfmsQuoteFilePath);
            }

            @Override
            public void onError(final Throwable e) {
                ThreadsLogger.w(TAG, "File send failed", e);
                onMessageSentError(userPhrase);
            }
        };
    }

    private void sendMessage(final UserPhrase userPhrase, final ConsultInfo consultInfo, @Nullable final String mfmsFilePath, @Nullable final String mfmsQuoteFilePath) {
        subscribe(
                Single.fromCallable(() -> Config.instance.transport.sendMessage(userPhrase, consultInfo, mfmsFilePath, mfmsQuoteFilePath))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(response -> {
                            onMessageSent(userPhrase, response.messageId, response.sentAt);
                            return response;
                        })
                        .observeOn(Schedulers.io())
                        .delay(60000, TimeUnit.MILLISECONDS)
                        .subscribe(
                                response -> Config.instance.transport.notifyMessageUpdateNeeded(),
                                e -> {
                                    ThreadsLogger.e(TAG, e.getMessage());
                                    onMessageSentError(userPhrase);
                                }
                        )
        );
    }

    private void onMessageSent(final UserPhrase userPhrase, String providerId, long sentAtTimestamp) {
        ThreadsLogger.d(TAG, "server answer on pharse sent with id " + providerId);
        userPhrase.setProviderId(providerId);
        if (sentAtTimestamp > 0) {
            userPhrase.setTimeStamp(sentAtTimestamp);
        }
        databaseHolder.putChatItem(userPhrase);
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
        if (!isActive) {
            NotificationService.addUnsentMessage(appContext, PrefUtils.getAppMarker());
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
                        final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
                        if (serverItems.isEmpty()) {
                            isDownloadingMessages = false;
                            isAllMessagesDownloaded = true;
                        } else {
                            lastMessageTimestamp = serverItems.get(0).getTimeStamp();
                            isAllMessagesDownloaded = serverItems.size() < PER_PAGE_COUNT; // Backend can give us more than chunk anytime, it will give less only on history end
                            databaseHolder.putMessagesSync(serverItems);
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

    private void onNewMessage(ChatItem chatItem) {
        if (chatItem instanceof Survey) {
            final Survey survey = (Survey) chatItem;
            activeSurveySendingId = survey.getSendingId();
            subscribe(
                    Completable.fromAction(() -> Config.instance.transport.sendRatingReceived(survey.getSendingId()))
                            .delay(survey.getHideAfter(), TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    this::removeActiveSurvey,
                                    e -> ThreadsLogger.e(TAG, e.getMessage())
                            )
            );
        }
        if (chatItem instanceof RequestResolveThread) {
            isResolveRequestVisible = true;
            final RequestResolveThread resolveThread = (RequestResolveThread) chatItem;
            // if thread is closed by timeout
            // remove close request from the history
            subscribe(
                    Observable.timer(resolveThread.getHideAfter(), TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                if (isResolveRequestVisible) {
                                    removeResolveRequest();
                                }
                            })
            );
        }
        if (chatItem instanceof ScheduleInfo) {
            final ScheduleInfo schedule = (ScheduleInfo) chatItem;
            isChatWorking = schedule.isChatWorking();
            updateInputEnable(isChatWorking || schedule.isSendDuringInactive());
            consultWriter.setSearchingConsult(false);
            fragment.removeSearching();
            fragment.setTitleStateDefault();
        }
        if (chatItem instanceof UserPhrase || chatItem instanceof ConsultPhrase) {
            checkAndLoadOgData(chatItem);
        }
        if (chatItem instanceof ConsultConnectionMessage) {
            ConsultConnectionMessage ccm = (ConsultConnectionMessage) chatItem;
            if (ccm.getType().equalsIgnoreCase(ChatMessageType.OPERATOR_JOINED.name())) {
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
                if (fragment != null) {
                    fragment.setTitleStateDefault();
                }
            }
        }
        addMessage(chatItem);
    }

    private void addMessage(final ChatItem cm) {
        databaseHolder.putChatItem(cm);
        if (fragment != null) {
            final ChatItem ci = setLastAvatars(Collections.singletonList(cm)).get(0);
            if (!(ci instanceof ConsultConnectionMessage) || ((ConsultConnectionMessage) ci).isDisplayMessage()) {
                fragment.addChatItem(ci);
                checkAndLoadOgData(ci);
            }
            if (ci instanceof ConsultChatPhrase) {
                fragment.notifyConsultAvatarChanged(((ConsultChatPhrase) ci).getAvatarPath(), ((ConsultChatPhrase) ci).getConsultId());
            }
        }
        if (cm instanceof ConsultPhrase && isActive) {
            final String providerId = ((ConsultPhrase) cm).getProviderId();
            setConsultMessageRead(providerId);
        }
        if (isActive) {
            subscribe(
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
                                notifyUnreadMessagesCountChanged();
                            })
            );
        }
        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (cm instanceof ConsultPhrase || cm instanceof ConsultConnectionMessage || (cm instanceof ScheduleInfo && ((ScheduleInfo) cm).isChatWorking())) {
            if (fragment != null && fragment.isAdded()) {
                fragment.removeSchedule(false);
            }
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
        databaseHolder.cleanDatabase();
        if (fragment != null) {
            fragment.cleanChat();
        }
        consultWriter.setCurrentConsultLeft();
        consultWriter.setSearchingConsult(false);
        appContext.sendBroadcast(new Intent(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
        notifyUnreadMessagesCountChanged();
    }

    private void setMessageState(final UserPhrase up, final MessageState messageState) {
        up.setSentState(messageState);
        if (fragment != null) {
            fragment.setMessageState(up.getProviderId(), up.getSentState());
        }
        databaseHolder.setStateOfUserPhraseByProviderId(up.getProviderId(), up.getSentState());
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
        if (fragment != null) {
            fragment.updateInputEnable(enabled);
        }
    }

    private void setConsultMessageRead(final String providerId) {
        subscribe(
                Completable.fromAction(() -> Config.instance.transport.sendMessageRead(providerId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> databaseHolder.setMessageWereRead(providerId),
                                e -> ThreadsLogger.e(TAG, e.getMessage())
                        )
        );
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
                        Config.instance.transport.onSettingClientId(newClientId);
                        final HistoryResponse response = HistoryLoader.getHistorySync(null, true);
                        final List<ChatItem> serverItems = HistoryFormatter.getChatItemFromHistoryResponse(response);
                        databaseHolder.putMessagesSync(serverItems);
                        if (fragment != null) {
                            List<ChatItem> itemsWithLastAvatars = setLastAvatars(serverItems);
                            fragment.addChatItems(itemsWithLastAvatars);
                            checkAndLoadOgData(itemsWithLastAvatars);
                            final ConsultInfo info = response != null ? response.getConsultInfo() : null;
                            if (info != null) {
                                fragment.setStateConsultConnected(info);
                            }
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
