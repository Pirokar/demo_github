package im.threads.ui.adapters;

import static im.threads.business.utils.FileUtils.isImage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import im.threads.business.config.BaseConfig;
import im.threads.business.formatters.ChatItemType;
import im.threads.business.imageLoading.ImageModifications;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.media.FileDescriptionMediaPlayer;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ChatPhrase;
import im.threads.business.models.ClientNotificationDisplayType;
import im.threads.business.models.ConsultChatPhrase;
import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.ConsultTyping;
import im.threads.business.models.DateRow;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageStatus;
import im.threads.business.models.NoChatItem;
import im.threads.business.models.QuestionDTO;
import im.threads.business.models.QuickReply;
import im.threads.business.models.QuickReplyItem;
import im.threads.business.models.Quote;
import im.threads.business.models.RequestResolveThread;
import im.threads.business.models.ScheduleInfo;
import im.threads.business.models.SearchingConsult;
import im.threads.business.models.SimpleSystemMessage;
import im.threads.business.models.Space;
import im.threads.business.models.Survey;
import im.threads.business.models.SystemMessage;
import im.threads.business.models.UnreadMessages;
import im.threads.business.models.UserPhrase;
import im.threads.business.models.enums.ModificationStateEnum;
import im.threads.business.ogParser.OpenGraphParser;
import im.threads.business.ogParser.OpenGraphParserJsoupImpl;
import im.threads.business.utils.ChatItemListFinder;
import im.threads.business.utils.FileUtils;
import im.threads.business.workers.FileDownloadWorker;
import im.threads.ui.ChatStyle;
import im.threads.ui.adapters.utils.SendingStatusObserver;
import im.threads.ui.config.Config;
import im.threads.ui.holders.BaseHolder;
import im.threads.ui.holders.ConsultFileViewHolder;
import im.threads.ui.holders.ConsultIsTypingViewHolderNew;
import im.threads.ui.holders.ConsultPhraseHolder;
import im.threads.ui.holders.ConsultVoiceMessageViewHolder;
import im.threads.ui.holders.DateViewHolder;
import im.threads.ui.holders.EmptyViewHolder;
import im.threads.ui.holders.ImageFromConsultViewHolder;
import im.threads.ui.holders.ImageFromUserViewHolder;
import im.threads.ui.holders.QuickRepliesViewHolder;
import im.threads.ui.holders.RatingStarsSentViewHolder;
import im.threads.ui.holders.RatingStarsViewHolder;
import im.threads.ui.holders.RatingThumbsSentViewHolder;
import im.threads.ui.holders.RatingThumbsViewHolder;
import im.threads.ui.holders.RequestResolveThreadViewHolder;
import im.threads.ui.holders.ScheduleInfoViewHolder;
import im.threads.ui.holders.SearchingConsultViewHolder;
import im.threads.ui.holders.SpaceViewHolder;
import im.threads.ui.holders.SystemMessageViewHolder;
import im.threads.ui.holders.UnreadMessageViewHolder;
import im.threads.ui.holders.UserFileViewHolder;
import im.threads.ui.holders.UserPhraseViewHolder;
import im.threads.ui.holders.VoiceMessageBaseHolder;
import im.threads.ui.holders.helper.SurveySplitterKt;
import im.threads.ui.preferences.PreferencesJavaUI;
import im.threads.ui.utils.ThreadRunnerKt;
import io.reactivex.subjects.PublishSubject;
import kotlin.jvm.Synchronized;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_UNDEFINED = 0;
    private static final int TYPE_CONSULT_TYPING = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_SEARCHING_CONSULT = 3;
    private static final int TYPE_SYSTEM_MESSAGE = 4;
    private static final int TYPE_CONSULT_PHRASE = 5;
    private static final int TYPE_USER_PHRASE = 6;
    private static final int TYPE_FREE_SPACE = 7;
    private static final int TYPE_IMAGE_FROM_CONSULT = 8;
    private static final int TYPE_IMAGE_FROM_USER = 9;
    private static final int TYPE_FILE_FROM_USER = 10;
    private static final int TYPE_FILE_FROM_CONSULT = 11;
    private static final int TYPE_UNREAD_MESSAGES = 12;
    private static final int TYPE_SCHEDULE = 13;
    private static final int TYPE_RATING_THUMBS = 14;
    private static final int TYPE_RATING_THUMBS_SENT = 15;
    private static final int TYPE_RATING_STARS = 16;
    private static final int TYPE_RATING_STARS_SENT = 17;
    private static final int TYPE_REQ_RESOLVE_THREAD = 18;
    private static final int TYPE_VOICE_MESSAGE_FROM_CONSULT = 19;
    private static final int TYPE_QUICK_REPLIES = 20;

    private final Handler viewHandler = new Handler(Looper.getMainLooper());

    private final ArrayList<ChatItem> list = new ArrayList<>();
    @NonNull
    private final Callback mCallback;
    @NonNull
    private final FileDescriptionMediaPlayer fdMediaPlayer;
    @NonNull
    private final MediaMetadataRetriever mediaMetadataRetriever;
    @NonNull
    private final PublishSubject<Long> messageErrorProcessor;
    private final ChatMessagesOrderer chatMessagesOrderer;
    private final SendingStatusObserver sendingStatusObserver = new SendingStatusObserver(
            new WeakReference<>(this),
            Config.getInstance().getRequestConfig().getSocketClientSettings().getResendIntervalMillis()
    );
    @NonNull
    PublishSubject<ChatItem> highlightingStream = PublishSubject.create();
    @NonNull
    OpenGraphParser openGraphParser = new OpenGraphParserJsoupImpl();
    private Context ctx;
    private ImageModifications.MaskedModification outgoingImageMaskTransformation;
    private ImageModifications.MaskedModification incomingImageMaskTransformation;
    @Nullable
    private ChatItem highlightedItem = null;
    @NonNull
    private ClientNotificationDisplayType clientNotificationDisplayType;
    private long currentThreadId;
    private boolean ignorePlayerUpdates = false;
    @Nullable
    private VoiceMessageBaseHolder playingHolder = null;

    public ChatAdapter(
            @NonNull Callback callback,
            @NonNull FileDescriptionMediaPlayer fdMediaPlayer,
            @NonNull MediaMetadataRetriever mediaMetadataRetriever,
            @NonNull PublishSubject<Long> messageErrorProcessor
    ) {
        this.mCallback = callback;
        this.fdMediaPlayer = fdMediaPlayer;
        this.mediaMetadataRetriever = mediaMetadataRetriever;
        this.messageErrorProcessor = messageErrorProcessor;

        PreferencesJavaUI preferences = new PreferencesJavaUI();
        clientNotificationDisplayType = preferences.getClientNotificationDisplayType();
        currentThreadId = preferences.getThreadId() == null ? 0L : preferences.getThreadId();
        chatMessagesOrderer = new ChatMessagesOrderer();
    }

    public void onResumeView() {
        sendingStatusObserver.startObserving();
    }

    public void onPauseView() {
        sendingStatusObserver.pauseObserving();
    }

    public void onDestroyView() {
        sendingStatusObserver.finishObserving();
    }

    @Synchronized
    public ArrayList<ChatItem> getList() {
        return list;
    }

    private void setupMaskTransformations() {
        if (outgoingImageMaskTransformation == null || incomingImageMaskTransformation == null) {
            ChatStyle style = Config.getInstance().getChatStyle();
            outgoingImageMaskTransformation = new ImageModifications.MaskedModification(
                    Objects.requireNonNull(ContextCompat.getDrawable(ctx, style.outgoingImageBubbleMask))
            );
            incomingImageMaskTransformation = new ImageModifications.MaskedModification(
                    Objects.requireNonNull(ContextCompat.getDrawable(ctx, style.incomingImageBubbleMask))
            );
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        ctx = parent.getContext();
        switch (viewType) {
            case TYPE_CONSULT_TYPING:
                return new ConsultIsTypingViewHolderNew(parent);
            case TYPE_DATE:
                return new DateViewHolder(parent);
            case TYPE_SEARCHING_CONSULT:
                return new SearchingConsultViewHolder(parent);
            case TYPE_SYSTEM_MESSAGE:
                return new SystemMessageViewHolder(parent);
            case TYPE_CONSULT_PHRASE:
                return new ConsultPhraseHolder(parent, incomingImageMaskTransformation, highlightingStream, openGraphParser);
            case TYPE_USER_PHRASE:
                return new UserPhraseViewHolder(
                        parent,
                        outgoingImageMaskTransformation,
                        highlightingStream,
                        openGraphParser,
                        fdMediaPlayer,
                        messageErrorProcessor
                );
            case TYPE_FREE_SPACE:
                return new SpaceViewHolder(parent);
            case TYPE_IMAGE_FROM_CONSULT:
                return new ImageFromConsultViewHolder(parent, incomingImageMaskTransformation, highlightingStream, openGraphParser);
            case TYPE_IMAGE_FROM_USER:
                return new ImageFromUserViewHolder(
                        parent,
                        outgoingImageMaskTransformation,
                        highlightingStream,
                        openGraphParser,
                        messageErrorProcessor
                );
            case TYPE_FILE_FROM_CONSULT:
                return new ConsultFileViewHolder(parent, highlightingStream, openGraphParser);
            case TYPE_FILE_FROM_USER:
                return new UserFileViewHolder(parent, highlightingStream, openGraphParser, messageErrorProcessor);
            case TYPE_UNREAD_MESSAGES:
                return new UnreadMessageViewHolder(parent);
            case TYPE_SCHEDULE:
                return new ScheduleInfoViewHolder(parent);
            case TYPE_RATING_THUMBS:
                return new RatingThumbsViewHolder(parent);
            case TYPE_RATING_THUMBS_SENT:
                return new RatingThumbsSentViewHolder(parent);
            case TYPE_RATING_STARS:
                return new RatingStarsViewHolder(parent);
            case TYPE_RATING_STARS_SENT:
                return new RatingStarsSentViewHolder(parent);
            case TYPE_REQ_RESOLVE_THREAD:
                return new RequestResolveThreadViewHolder(parent);
            case TYPE_VOICE_MESSAGE_FROM_CONSULT:
                return new ConsultVoiceMessageViewHolder(parent, highlightingStream, openGraphParser, fdMediaPlayer);
            case TYPE_QUICK_REPLIES:
                return new QuickRepliesViewHolder(parent);
            default:
                return new EmptyViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        setupMaskTransformations();

        if (holder instanceof SystemMessageViewHolder) {
            bindSystemMessageVH((SystemMessageViewHolder) holder, (SystemMessage) getList().get(position));
        } else if (holder instanceof ConsultPhraseHolder) {
            ConsultPhrase phrase = (ConsultPhrase) getList().get(position);
            bindConsultPhraseVH((ConsultPhraseHolder) holder, phrase);
            updateReadStateForConsultPhrase(phrase);
        } else if (holder instanceof UserPhraseViewHolder) {
            bindUserPhraseVH((UserPhraseViewHolder) holder, (UserPhrase) getList().get(position));
        } else if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).onBind(getList().get(position).getTimeStamp());
        } else if (holder instanceof ConsultIsTypingViewHolderNew) {
            bindConsultIsTypingVH((ConsultIsTypingViewHolderNew) holder);
        } else if (holder instanceof SpaceViewHolder) {
            final Space space = (Space) getList().get(position);
            ((SpaceViewHolder) holder).onBind((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics()));
        } else if (holder instanceof ImageFromConsultViewHolder) {
            ConsultPhrase phrase = (ConsultPhrase) getList().get(position);
            bindImageFromConsultVH((ImageFromConsultViewHolder) holder, phrase);
            updateReadStateForConsultPhrase(phrase);
        } else if (holder instanceof ImageFromUserViewHolder) {
            bindImageFromUserVH((ImageFromUserViewHolder) holder, (UserPhrase) getList().get(position));
        } else if (holder instanceof UserFileViewHolder) {
            bindFileFromUserVH((UserFileViewHolder) holder, (UserPhrase) getList().get(position));
        } else if (holder instanceof ConsultFileViewHolder) {
            ConsultPhrase phrase = (ConsultPhrase) getList().get(position);
            bindFileFromConsultVH((ConsultFileViewHolder) holder, phrase);
            updateReadStateForConsultPhrase(phrase);
        } else if (holder instanceof UnreadMessageViewHolder) {
            ((UnreadMessageViewHolder) holder).onBind((UnreadMessages) getList().get(holder.getAdapterPosition()));
        } else if (holder instanceof ScheduleInfoViewHolder) {
            ((ScheduleInfoViewHolder) holder).bind((ScheduleInfo) getList().get(holder.getAdapterPosition()));
        } else if (holder instanceof RatingThumbsViewHolder) {
            final Survey survey = (Survey) getList().get(holder.getAdapterPosition());
            ((RatingThumbsViewHolder) holder).bind(survey, mCallback);
        } else if (holder instanceof RatingThumbsSentViewHolder) {
            final Survey survey = (Survey) getList().get(holder.getAdapterPosition());
            ((RatingThumbsSentViewHolder) holder).bind(survey);
        } else if (holder instanceof RatingStarsViewHolder) {
            final Survey survey = (Survey) getList().get(holder.getAdapterPosition());
            ((RatingStarsViewHolder) holder).bind(
                    survey,
                    ratingCount -> mCallback.onRatingClick(survey, ratingCount)
            );
        } else if (holder instanceof RatingStarsSentViewHolder) {
            ((RatingStarsSentViewHolder) holder).bind((Survey) getList().get(holder.getAdapterPosition()));
        } else if (holder instanceof RequestResolveThreadViewHolder) {
            ((RequestResolveThreadViewHolder) holder).bind(mCallback);
        } else if (holder instanceof ConsultVoiceMessageViewHolder) {
            ConsultPhrase phrase = (ConsultPhrase) getList().get(position);
            bindVoiceMessageFromConsultVH((ConsultVoiceMessageViewHolder) holder, phrase);
            updateReadStateForConsultPhrase(phrase);
        } else if (holder instanceof QuickRepliesViewHolder) {
            ((QuickRepliesViewHolder) holder).bind((QuickReplyItem) getList().get(position), mCallback);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        Object o;
        try {
            o = getList().get(position);
        } catch (final IndexOutOfBoundsException e) {
            LoggerEdna.error("getItemViewType", e);
            return 0;
        }
        if (o instanceof SystemMessage) {
            return TYPE_SYSTEM_MESSAGE;
        } else if (o instanceof ConsultTyping) {
            return TYPE_CONSULT_TYPING;
        } else if (o instanceof DateRow) {
            return TYPE_DATE;
        } else if (o instanceof SearchingConsult) {
            return TYPE_SEARCHING_CONSULT;
        } else if (o instanceof Space) {
            return TYPE_FREE_SPACE;
        } else if (o instanceof UnreadMessages) {
            return TYPE_UNREAD_MESSAGES;
        } else if (o instanceof ScheduleInfo) {
            return TYPE_SCHEDULE;
        } else if (o instanceof RequestResolveThread) {
            return TYPE_REQ_RESOLVE_THREAD;
        } else if (o instanceof ConsultPhrase) {
            final ConsultPhrase cp = (ConsultPhrase) o;
            if (cp.isVoiceMessage() && cp.getModified() != ModificationStateEnum.DELETED) {
                return TYPE_VOICE_MESSAGE_FROM_CONSULT;
            }
            if (cp.isOnlyImage() && cp.getModified() != ModificationStateEnum.DELETED) {
                return TYPE_IMAGE_FROM_CONSULT;
            }
            if (cp.isOnlyDoc() && cp.getModified() != ModificationStateEnum.DELETED) {
                return TYPE_FILE_FROM_CONSULT;
            }
            return TYPE_CONSULT_PHRASE;
        } else if (o instanceof UserPhrase) {
            final UserPhrase up = (UserPhrase) o;
            if (up.isOnlyImage()) {
                return TYPE_IMAGE_FROM_USER;
            }
            if (up.isOnlyDoc()) {
                return TYPE_FILE_FROM_USER;
            }
            return TYPE_USER_PHRASE;
        } else if (o instanceof Survey && !((Survey) o).getQuestions().isEmpty()) {
            final Survey survey = (Survey) o;
            final QuestionDTO questionDTO = survey.getQuestions().get(0);
            if (questionDTO.getSimple()) {
                if (survey.isCompleted()) {
                    return TYPE_RATING_THUMBS_SENT;
                } else {
                    return TYPE_RATING_THUMBS;
                }
            } else {
                if (survey.isCompleted()) {
                    return TYPE_RATING_STARS_SENT;
                } else {
                    return TYPE_RATING_STARS;
                }
            }
        } else if (o instanceof QuickReplyItem) {
            return TYPE_QUICK_REPLIES;
        } else {
            return TYPE_UNDEFINED;
        }
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof BaseHolder) {
            ((BaseHolder) holder).onClear();
        }
    }

    public void setAllMessagesRead() {
        ArrayList<String> readMessages = new ArrayList<>();
        for (final Iterator<ChatItem> iter = getList().iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ConsultPhrase) {
                if (!((ConsultPhrase) item).getRead()) {
                    ((ConsultPhrase) item).setRead(true);
                    String id = ((ConsultPhrase) item).getId();
                    if (!TextUtils.isEmpty(id)) readMessages.add(id);
                }
            }
            if (item instanceof Survey) {
                if (!((Survey) item).isRead()) {
                    ((Survey) item).setRead(true);
                }
            }
            if (item instanceof UnreadMessages) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), item));
                } catch (final Exception e) {
                    LoggerEdna.error("setAllMessagesRead", e);
                }
                iter.remove();
            }
        }

        if (!readMessages.isEmpty()) {
            BaseConfig.Companion.getInstance().transport.markMessagesAsRead(readMessages);
        }
    }

    public void setAvatar(final String consultId, String newAvatarImageUrl) {
        if (TextUtils.isEmpty(consultId)) {
            return;
        }
        for (int i = 0; i < getList().size(); i++) {
            final ChatItem item = getList().get(i);
            if (item instanceof ConsultChatPhrase) {
                final ConsultChatPhrase p = (ConsultChatPhrase) item;
                if (Objects.equals(p.getConsultId(), consultId) && !ObjectsCompat.equals(p.getAvatarPath(), newAvatarImageUrl)) {
                    p.setAvatarPath(TextUtils.isEmpty(newAvatarImageUrl) ? "" : newAvatarImageUrl);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void removeHighlight() {
        highlightingStream.onNext(new NoChatItem());
        highlightedItem = null;
    }

    public int setItemHighlighted(@NonNull final ChatItem chatItem) {
        int index = -1;
        highlightingStream.onNext(chatItem);

        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i).isTheSameItem(chatItem)) {
                highlightedItem = getList().get(i);
                index = i;
            }
        }

        return index;
    }

    public int setItemHighlighted(final String uuid) {
        for (ChatItem chatItem : getList()) {
            if (chatItem instanceof ChatPhrase && Objects.equals(((ChatPhrase) chatItem).getId(), uuid)) {
                setItemHighlighted(chatItem);
                return ChatItemListFinder.lastIndexOf(getList(), chatItem);
            }
        }
        return -1;
    }

    private void removeConsultIsTyping() {
        for (final ListIterator<ChatItem> iter = getList().listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof ConsultTyping) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), cm));
                } catch (final Exception e) {
                    LoggerEdna.error("removeConsultIsTyping", e);
                }
                iter.remove();
            }
        }
    }

    private void updateReadStateForConsultPhrase(ConsultPhrase phrase) {
        if (!phrase.getRead() && phrase.getId() != null) {
            BaseConfig.Companion.getInstance().transport.markMessagesAsRead(List.of(phrase.getId()));
        }
    }

    /**
     * Remove close request from the thread history
     *
     * @return true - if deletion occurred, false - if RequestResolveThread item wasn't found in the history
     */
    public boolean removeResolveRequest() {
        boolean removed = false;
        for (final ListIterator<ChatItem> iter = getList().listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof RequestResolveThread) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), cm));
                } catch (final Exception e) {
                    LoggerEdna.error("removeResolveRequest", e);
                }
                iter.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Remove survey from the thread history
     *
     * @return true - if deletion occurred, false - if Survey item wasn't found in the history
     */
    public boolean removeSurvey(final long sendingId) {
        boolean removed = false;
        for (final ListIterator<ChatItem> iter = getList().listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof Survey) {
                final Survey survey = (Survey) cm;
                if (sendingId == survey.getSendingId()) {
                    try {
                        notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), cm));
                    } catch (final Exception e) {
                        LoggerEdna.error("removeSurvey", e);
                    }
                    iter.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    public void setSearchingConsult() {
        for (final ChatItem ci : getList()) {
            if (ci instanceof SearchingConsult) {
                return;
            }
        }
        final SearchingConsult sc = new SearchingConsult();
        getList().add(sc);
        notifyItemInserted(ChatItemListFinder.lastIndexOf(getList(), sc));
    }

    public void removeConsultSearching() {
        for (final Iterator<ChatItem> iter = getList().iterator();
             iter.hasNext(); ) {
            final ChatItem ch = iter.next();
            if (ch instanceof SearchingConsult) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), ch));
                } catch (final Exception e) {
                    LoggerEdna.error("removeConsultSearching", e);
                }
                iter.remove();
            }
        }
    }

    public void addItems(
            @NonNull List<ChatItem> items,
            ListUpdateCallback listUpdateCallback,
            boolean withAnimation
    ) {
        boolean withTyping = false;
        boolean withRequestResolveThread = false;
        checkIdsForReplacingToNull(items);
        items = SurveySplitterKt.splitSurveyQuestions(items);
        for (final ChatItem ci : items) {
            if (ci instanceof ConsultTyping) {
                withTyping = true;
                break;
            }
            if (ci instanceof RequestResolveThread) {
                withRequestResolveThread = true;
                break;
            }
        }
        if (withTyping) {
            removeConsultIsTyping();
            viewHandler.removeCallbacksAndMessages(null);
            viewHandler.postDelayed(this::removeConsultIsTyping, 8000);
        }
        if (items.size() == 1 && items.get(0) instanceof ConsultPhrase) {
            removeConsultIsTyping();
        }
        if (withRequestResolveThread) {
            removeResolveRequest();
        }
        ArrayList<ChatItem> newList = new ArrayList<>(getList());
        chatMessagesOrderer.addAndOrder(newList, items, clientNotificationDisplayType, currentThreadId);
        if (withAnimation) {
            notifyDatasetChangedWithDiffUtil(newList, listUpdateCallback);
        } else {
            int oldSize = getItemCount();
            int oldPosition = oldSize - 1;
            if (oldPosition < 0) oldPosition = 0;
            getList().clear();
            getList().addAll(newList);
            notifyDataSetChanged();
            int newSize = newList.size();
            if (oldSize != newSize && listUpdateCallback != null) {
                listUpdateCallback.onInserted(oldPosition, newSize);
            }
        }
    }

    private void checkIdsForReplacingToNull(@NonNull List<ChatItem> items) {
        List<ChatItem> currentItems = getList();
        for (ChatItem newItem : items) {
            for (ChatItem currentItem : currentItems) {
                if (newItem.isTheSameItem(currentItem) && newItem instanceof UserPhrase) {
                    UserPhrase currentPhrase = (UserPhrase) currentItem;
                    UserPhrase newPhrase = (UserPhrase) newItem;

                    if (!TextUtils.isEmpty(currentPhrase.getBackendMessageId()) && TextUtils.isEmpty(newPhrase.getBackendMessageId())) {
                        newPhrase.setBackendMessageId(currentPhrase.getBackendMessageId());
                    }
                }
            }
        }
    }

    public void notifyDatasetChangedWithDiffUtil(ArrayList<ChatItem> newList, ListUpdateCallback listUpdateCallback) {
        removeSurveyIfNotLatest(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatDiffCallback(getList(), newList));
        getList().clear();
        getList().addAll(newList);
        if (listUpdateCallback != null) {
            diffResult.dispatchUpdatesTo(listUpdateCallback);
        } else {
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public void modifyImageInItem(FileDescription newFileDescription) {
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i) instanceof ChatPhrase) {
                ChatPhrase chatPhrase = (ChatPhrase) getList().get(i);
                FileDescription file = chatPhrase.getFileDescription();
                String originalUrl = newFileDescription.getOriginalPath();
                boolean isFileNotNull = file != null;
                boolean isDownloadPathNotNull = isFileNotNull && file.getDownloadPath() != null;
                boolean isDownloadPathMatch =
                        isDownloadPathNotNull && file.getDownloadPath().equals(originalUrl);
                boolean isFileUriNotNull = isFileNotNull && file.getFileUri() != null;
                boolean isFileUriMatch = isFileUriNotNull && file.getFileUri().toString().equals(originalUrl);

                if (isDownloadPathMatch || isFileUriMatch) {
                    FileDescription currentFD = chatPhrase.getFileDescription();
                    currentFD.setDownloadPath(newFileDescription.getDownloadPath());
                    currentFD.setState(newFileDescription.getState());
                    currentFD.setIncomingName(newFileDescription.getIncomingName());
                    currentFD.setMimeType(newFileDescription.getMimeType());
                    currentFD.setSize(newFileDescription.getSize());

                    getList().set(i, chatPhrase);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public boolean hasSchedule() {
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i) instanceof ScheduleInfo) {
                return true;
            }
        }
        return false;
    }

    public void removeSchedule(final boolean checkSchedule) {
        for (final Iterator<ChatItem> iter = getList().iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ScheduleInfo) {
                final ScheduleInfo scheduleInfo = (ScheduleInfo) item;
                if (!checkSchedule || scheduleInfo.isChatWorking()) {
                    try {
                        notifyItemRemoved(ChatItemListFinder.lastIndexOf(getList(), scheduleInfo));
                    } catch (final Exception e) {
                        LoggerEdna.error("removeSchedule", e);
                    }
                    iter.remove();
                }
            }
        }
    }

    public void changeStateOfSurvey(Survey updatedSurvey) {
        for (final ChatItem cm : getList()) {
            if (cm instanceof Survey) {
                final Survey survey = (Survey) cm;
                if (updatedSurvey.getSendingId() == survey.getSendingId()) {
                    boolean questionsAreEmpty = survey.getQuestions() == null || survey.getQuestions().size() == 0;
                    boolean questionsOfUpdatedAreEmpty = updatedSurvey.getQuestions() == null || updatedSurvey.getQuestions().size() == 0;

                    if (questionsAreEmpty || questionsOfUpdatedAreEmpty) {
                        changeSurveyState(cm, updatedSurvey.getSentState());
                    } else {
                        boolean breakUpperLoop = false;
                        for (QuestionDTO updatableQuestion : updatedSurvey.getQuestions()) {
                            if (breakUpperLoop) break;
                            for (QuestionDTO questionToUpdate : survey.getQuestions()) {
                                if (questionToUpdate.getId() == updatableQuestion.getId() &&
                                        questionToUpdate.getCorrelationId().equals(updatableQuestion.getCorrelationId())) {
                                    changeSurveyState(cm, updatedSurvey.getSentState());
                                    breakUpperLoop = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void changeSurveyState(ChatItem message, MessageStatus sentState) {
        ((Survey) message).setSentState(sentState);
        notifyItemChangedOnUi(message);
    }

    public void changeStateOfMessageByMessageId(
            String correlationId,
            final String backendMessageId,
            final MessageStatus status
    ) {
        for (final ChatItem cm : getList()) {
            if (cm instanceof UserPhrase) {
                UserPhrase up = (UserPhrase) cm;

                if (correlationId != null) {
                    String[] split = correlationId.split(":");
                    if (split.length > 1) {
                        correlationId = split[1];
                    }
                }

                if (ObjectsCompat.equals(correlationId, up.getId()) || ObjectsCompat.equals(backendMessageId, up.getBackendMessageId())) {
                    if (backendMessageId != null) {
                        ((UserPhrase) cm).setBackendMessageId(backendMessageId);
                    }
                    if (up.getSentState().ordinal() < status.ordinal()) {
                        ((UserPhrase) cm).setSentState(status);
                        notifyItemChangedOnUi(cm);
                    }
                }
            }
        }
    }

    public void updateQuotesIfNeed(ConsultPhrase consultPhrase) {
        String consultItemId = consultPhrase.getId();
        for(int i = 0; i < getList().size(); i ++) {
            ChatItem item = getList().get(i);
            if (item instanceof ConsultPhrase) {
                Quote quote = ((ConsultPhrase) item).getQuote();
                if (quote != null && Objects.requireNonNull(quote.getUuid()).equals(consultItemId)) {
                    quote.setFileDescription(consultPhrase.getFileDescription());
                    quote.setModified(consultPhrase.getModified());
                    quote.setText(consultPhrase.getPhraseText());
                    notifyItemChanged(i);
                    break;
                }
            }
            if (item instanceof UserPhrase) {
                Quote quote = ((UserPhrase) item).getQuote();
                if (quote != null && Objects.requireNonNull(quote.getUuid()).equals(consultItemId)) {
                    quote.setFileDescription(consultPhrase.getFileDescription());
                    quote.setModified(consultPhrase.getModified());
                    quote.setText(consultPhrase.getPhraseText());
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void updateProgress(final FileDescription fileDescription) {
        for (int i = 0; i < getList().size(); i++) {
            if (fileDescription.getFileUri() == null && fileDescription.getDownloadPath() == null
                    && (getItemViewType(i) == TYPE_IMAGE_FROM_USER
                    || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT
                    || getItemViewType(i) == TYPE_USER_PHRASE
                    || getItemViewType(i) == TYPE_VOICE_MESSAGE_FROM_CONSULT))
                continue;
            if (getList().get(i) instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) getList().get(i);
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)) {
                    if (cp.getFileDescription() != null &&
                            cp.getFileDescription().getState().ordinal() > fileDescription.getState().ordinal()) {
                        break;
                    }
                    cp.setFileDescription(fileDescription);
                    if (!isImage(fileDescription)
                            || isImageChanged(cp.getFileDescription(), fileDescription)) {
                        notifyItemChanged(ChatItemListFinder.indexOf(getList(), cp));
                    }
                } else if (cp.getQuote() != null && ObjectsCompat.equals(cp.getQuote().getFileDescription(), fileDescription)) {
                    cp.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(getList(), cp));
                }
            } else if (getList().get(i) instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) getList().get(i);
                if (ObjectsCompat.equals(up.getFileDescription(), fileDescription)) {
                    if (up.getFileDescription() != null &&
                            up.getFileDescription().getState().ordinal() > fileDescription.getState().ordinal()) {
                        break;
                    }
                    up.setFileDescription(fileDescription);
                    if (!isImage(fileDescription)
                            || isImageChanged(up.getFileDescription(), fileDescription)) {
                        notifyItemChanged(ChatItemListFinder.indexOf(getList(), up));
                    }
                } else if (up.getQuote() != null && ObjectsCompat.equals(up.getQuote().getFileDescription(), fileDescription)) {
                    up.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(getList(), up));
                }
            }
        }
    }

    private boolean isImageChanged(FileDescription oldImage, FileDescription newImage) {
        boolean stateChanged = oldImage.getState() != newImage.getState();
        return stateChanged
                || newImage.getDownloadProgress() == 0
                || newImage.getDownloadProgress() == 100;
    }

    public void onDownloadError(final FileDescription fileDescription) {
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i) instanceof ChatPhrase) {
                final ChatPhrase cp = (ChatPhrase) getList().get(i);
                int itemViewType = getItemViewType(i);
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)
                        && (itemViewType == TYPE_IMAGE_FROM_USER
                        || itemViewType == TYPE_IMAGE_FROM_CONSULT
                        || itemViewType == TYPE_USER_PHRASE
                        || itemViewType == TYPE_VOICE_MESSAGE_FROM_CONSULT)) {
                    if (cp.getFileDescription() != null) {
                        cp.getFileDescription().setDownloadError(true);
                        notifyItemChanged(i);
                    }
                }
            }
        }
    }

    public void notifyAvatarChanged(final String newUrl, final String consultId) {
        if (newUrl == null || consultId == null) {
            return;
        }
        for (final ChatItem ci : getList()) {
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                if (!Objects.equals(cp.getConsultId(), consultId)) continue;
                final String oldUrl = cp.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    cp.setAvatarPath(newUrl);
                    notifyItemChanged(ChatItemListFinder.lastIndexOf(getList(), cp));
                }
            }
        }
    }

    private void notifyItemChangedOnUi(final ChatItem chatItem) {
        ThreadRunnerKt.runOnUiThread(() -> {
            int position = ChatItemListFinder.indexOf(getList(), chatItem);
            notifyItemChanged(position);
        });
    }

    private void onVoiceMessagePlayClick(VoiceMessageBaseHolder holder) {
        if (holder.getFileDescription() == null) {
            return;
        }
        if (holder.getFileDescription().getFileUri() == null) {
            fdMediaPlayer.setClickedDownloadPath(holder.getFileDescription().getDownloadPath());
            holder.startLoader();
            FileDownloadWorker.startDownload(ctx, holder.getFileDescription(), false, false);
        } else {
            fdMediaPlayer.clearClickedDownloadPath();
            holder.stopLoader();
            fdMediaPlayer.processPlayPause(holder.getFileDescription());
        }
    }

    private void bindSystemMessageVH(@NonNull final SystemMessageViewHolder holder, SystemMessage sm) {
        holder.onBind(
                sm,
                v -> {
                    final SystemMessage cc1 = (SystemMessage) getList().get(holder.getAdapterPosition());
                    mCallback.onSystemMessageClick(cc1);
                }
        );
    }

    private void bindConsultPhraseVH(@NonNull final ConsultPhraseHolder holder, ConsultPhrase consultPhrase) {
        downloadImageIfNeeded(consultPhrase.getFileDescription());
        holder
                .onBind(
                        consultPhrase,
                        consultPhrase.equals(highlightedItem),
                        v -> mCallback.onImageClick(consultPhrase),
                        v -> {
                            if (consultPhrase.getQuote() != null && consultPhrase.getQuote().getFileDescription() != null) {
                                mCallback.onFileClick(consultPhrase.getQuote().getFileDescription());
                            }
                            if (consultPhrase.getFileDescription() != null) {
                                mCallback.onFileClick(consultPhrase.getFileDescription());
                            }
                        },
                        v -> mCallback.onQuoteClick(consultPhrase.getQuote()),
                        v -> {
                            phraseLongClick(consultPhrase, holder.getAdapterPosition());
                            return true;
                        },
                        v -> mCallback.onConsultAvatarClick(consultPhrase.getConsultId())
                );
    }

    @SuppressLint("RestrictedApi")
    private void bindUserPhraseVH(@NonNull final UserPhraseViewHolder holder, UserPhrase userPhrase) {
        downloadImageIfNeeded(userPhrase.getFileDescription());
        downloadVoiceIfNeeded(userPhrase.getFileDescription());
        String voiceFormattedDuration = "";
        if (userPhrase.getFileDescription() != null) {
            voiceFormattedDuration = userPhrase.getFileDescription().getVoiceFormattedDuration();
        }
        holder.onBind(
                userPhrase,
                voiceFormattedDuration,
                v -> mCallback.onImageClick(userPhrase),
                v -> {
                    if (userPhrase.getFileDescription() != null) {
                        mCallback.onFileClick(userPhrase.getFileDescription());
                    } else if (userPhrase.getQuote() != null && userPhrase.getQuote().getFileDescription() != null) {
                        mCallback.onFileClick(userPhrase.getQuote().getFileDescription());
                    }
                },
                v -> onVoiceMessagePlayClick(holder),
                v -> mCallback.onUserPhraseClick(userPhrase, holder.getAdapterPosition(), holder.itemView),
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                        if (mediaPlayer != null) {
                            mediaPlayer.seekTo((int) value);
                        }
                    }
                },
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        ignorePlayerUpdates = true;
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        ignorePlayerUpdates = false;
                    }
                },
                v -> mCallback.onQuoteClick(userPhrase.getQuote()),
                v -> {
                    phraseLongClick(userPhrase, holder.getAdapterPosition());
                    return true;
                },
                userPhrase.equals(highlightedItem)
        );
        if (ObjectsCompat.equals(holder.getFileDescription(), fdMediaPlayer.getFileDescription())) {
            MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
            if (mediaPlayer != null) {
                int duration = fdMediaPlayer.getDuration();
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition < 0) currentPosition = 0;

                holder.init(duration, currentPosition, mediaPlayer.isPlaying());
            }
            playingHolder = holder;
        } else {
            holder.resetProgress();
        }
    }

    private void bindConsultIsTypingVH(@NonNull final ConsultIsTypingViewHolderNew holder) {
        final ConsultTyping consultTyping = (ConsultTyping) getList().get(holder.getAdapterPosition());
        holder.onBind(
                consultTyping,
                v -> mCallback.onConsultAvatarClick(consultTyping.getConsultId())
        );
    }

    private void bindImageFromConsultVH(@NonNull final ImageFromConsultViewHolder holder, ConsultPhrase consultPhrase) {
        downloadImageIfNeeded(consultPhrase.getFileDescription());
        holder.onBind(
                consultPhrase,
                consultPhrase.equals(highlightedItem),
                v -> mCallback.onImageClick(consultPhrase),
                v -> {
                    mCallback.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                    return true;
                },
                v -> mCallback.onConsultAvatarClick(consultPhrase.getConsultId())
        );
    }

    private void bindImageFromUserVH(@NonNull final ImageFromUserViewHolder holder, UserPhrase userPhrase) {
        downloadImageIfNeeded(userPhrase.getFileDescription());
        if (userPhrase.getFileDescription() != null) {
            holder.onBind(userPhrase,
                    userPhrase.equals(highlightedItem),
                    () -> mCallback.onImageClick(userPhrase),
                    () -> mCallback.onPhraseLongClick(userPhrase, holder.getAdapterPosition())
            );
        }
    }

    private void downloadVoiceIfNeeded(@Nullable FileDescription fileDescription) {
        if (fileDescription != null) {
            if (FileUtils.isVoiceMessage(fileDescription) && fileDescription.getFileUri() == null) {
                mCallback.onFileDownloadRequest(fileDescription, false);
            }
        }
    }

    private void bindFileFromUserVH(@NonNull final UserFileViewHolder holder, UserPhrase userPhrase) {
        holder.onBind(
                userPhrase,
                v -> mCallback.onFileClick(userPhrase.getFileDescription()),
                v -> mCallback.onUserPhraseClick(userPhrase, holder.getAdapterPosition(), holder.itemView),
                v -> {
                    phraseLongClick(userPhrase, holder.getAdapterPosition());
                    return true;
                },
                userPhrase.equals(highlightedItem)
        );
    }

    private void bindFileFromConsultVH(@NonNull ConsultFileViewHolder holder, @NonNull ConsultPhrase consultPhrase) {
        holder.onBind(
                consultPhrase,
                consultPhrase.equals(highlightedItem),
                v -> mCallback.onFileClick(consultPhrase.getFileDescription()),
                v -> {
                    phraseLongClick(consultPhrase, holder.getAdapterPosition());
                    return true;
                },
                v -> mCallback.onQuoteClick(consultPhrase.getQuote()),
                v -> mCallback.onConsultAvatarClick(consultPhrase.getConsultId())
        );
    }

    @SuppressLint("RestrictedApi")
    private void bindVoiceMessageFromConsultVH(@NonNull final ConsultVoiceMessageViewHolder holder, ConsultPhrase consultPhrase) {
        downloadVoiceIfNeeded(consultPhrase.getFileDescription());
        String voiceFormattedDuration = "";
        if (consultPhrase.getFileDescription() != null) {
            voiceFormattedDuration = consultPhrase.getFileDescription().getVoiceFormattedDuration();
        }
        holder.onBind(
                consultPhrase,
                consultPhrase.equals(highlightedItem),
                voiceFormattedDuration,
                v -> {
                    phraseLongClick(consultPhrase, holder.getAdapterPosition());
                    return true;
                },
                v -> mCallback.onQuoteClick(consultPhrase.getQuote()),
                v -> mCallback.onConsultAvatarClick(consultPhrase.getConsultId()),
                v -> onVoiceMessagePlayClick(holder),
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                        if (mediaPlayer != null) {
                            mediaPlayer.seekTo((int) value);
                        }
                    }
                },
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        ignorePlayerUpdates = true;
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        ignorePlayerUpdates = false;
                    }
                }
        );
        if (ObjectsCompat.equals(holder.getFileDescription(), fdMediaPlayer.getFileDescription())) {
            MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
            if (mediaPlayer != null) {
                holder.init(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), mediaPlayer.isPlaying());
            }
            playingHolder = holder;
        } else {
            holder.resetProgress();
        }
    }

    private void phraseLongClick(ChatPhrase chatPhrase, int position) {
        mCallback.onPhraseLongClick(chatPhrase, position);
    }

    public void setClientNotificationDisplayType(ClientNotificationDisplayType type) {
        this.clientNotificationDisplayType = type;
        addItems(new ArrayList<>(), null, true);
    }

    public void setCurrentThreadId(long threadId) {
        this.currentThreadId = threadId;
        addItems(new ArrayList<>(), null, true);
    }

    public void playerUpdate() {
        if (ignorePlayerUpdates) {
            return;
        }
        FileDescription fileDescription = fdMediaPlayer.getFileDescription();
        if (fileDescription != null) {
            if (playingHolder != null && ObjectsCompat.equals(playingHolder.getFileDescription(), fileDescription)) {
                MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                if (mediaPlayer != null) {
                    playingHolder.updateProgress(mediaPlayer.getCurrentPosition());
                    playingHolder.updateIsPlaying(mediaPlayer.isPlaying());
                }
            } else {
                resetPlayingHolder();
                ChatItem chatItem = findByFileDescription(fileDescription);
                if (chatItem != null) {
                    notifyItemChanged(ChatItemListFinder.lastIndexOf(getList(), chatItem));
                }
            }
        } else {
            resetPlayingHolder();
        }
    }

    public void resetPlayingHolder() {
        if (playingHolder != null) {
            ignorePlayerUpdates = false;
            playingHolder.resetProgress();
            playingHolder = null;
        }
    }


    @Nullable
    private ChatItem findByFileDescription(FileDescription fileDescription) {
        for (ChatItem chatPhrase : getList()) {
            if (chatPhrase instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) chatPhrase;
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)) {
                    return cp;
                }
            } else if (chatPhrase instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) chatPhrase;
                if (ObjectsCompat.equals(up.getFileDescription(), fileDescription)) {
                    return up;
                }
            }
        }
        return null;
    }

    public void removeItem(ChatItem chatItem) {
        final int index = ChatItemListFinder.lastIndexOf(getList(), chatItem);
        if (index != -1) {
            getList().remove(index);
            notifyItemRemoved(index);
        }
    }

    public void removeItem(int index) {
        if (index != -1 && getList().size() > index) {
            getList().remove(index);
            notifyItemRemoved(index);
        }
    }

    private void removeSurveyIfNotLatest(ArrayList<ChatItem> list) {
        boolean isListSizeMoreThat1Element = list != null && list.size() > 1;
        boolean isPreviousItemSurvey = isListSizeMoreThat1Element &&
                list.get(list.size() - 2) instanceof Survey;
        boolean isLatestItemSurvey = isListSizeMoreThat1Element && list.get(list.size() - 1) instanceof Survey;
        if (isPreviousItemSurvey && !isLatestItemSurvey) {
            for (int i = list.size() - 2; i >= 0; i --) {
                if (list.get(i) instanceof Survey) {
                    Survey itemForDelete = (Survey) list.get(i);
                    if (!itemForDelete.isCompleted()) {
                        list.remove(i);
                    }
                } else {
                    return;
                }
            }
        }
    }

    public int getPositionByTimeStamp(long timeStamp) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTimeStamp() == timeStamp)
                return i;
        }
        return -1;
    }

    private void downloadImageIfNeeded(@Nullable FileDescription fileDescription) {
        if (fileDescription != null) {
            FileDescription previewFileDescription = fileDescription.getPreviewFileDescription();
            if (previewFileDescription != null) {
                if (isImage(previewFileDescription) && previewFileDescription.getFileUri() == null) {
                    mCallback.onFileDownloadRequest(previewFileDescription, true);
                }
            }
        }
    }

    public interface Callback {
        void onFileClick(FileDescription fileDescription);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onQuoteClick(Quote quote);

        void onUserPhraseClick(UserPhrase userPhrase, int position, View view);

        void onConsultAvatarClick(String consultId);

        void onImageClick(ChatPhrase chatPhrase);

        void onFileDownloadRequest(FileDescription fileDescription, boolean isPreview);

        void onSystemMessageClick(SystemMessage systemMessage);

        void onRatingClick(@NonNull Survey survey, int rating);

        void onResolveThreadClick(boolean approveResolve);

        void onQuickReplyClick(QuickReply quickReply);
    }

    private class ChatMessagesOrderer {

        void addAndOrder(@NonNull final List<ChatItem> listToInsertTo, @NonNull final List<ChatItem> listToAdd, ClientNotificationDisplayType type, long currentThreadId) {
            for (int i = 0; i < listToAdd.size(); i++) {
                ChatItem currentItem = listToAdd.get(i);
                int index = indexOf(listToInsertTo, currentItem);
                if (index == -1) {
                    addItemInternal(listToInsertTo, currentItem);
                } else {
                    listToInsertTo.set(index, currentItem);
                }
            }
            orderAndFilter(listToInsertTo, type, currentThreadId);
        }

        void orderAndFilter(@NonNull List<ChatItem> items, ClientNotificationDisplayType type, long currentThreadId) {
            sortItemsByTimeStamp(items);
            if (items.size() == 0) {
                return;
            }
            filter(items, type, currentThreadId);
            items.add(0, new DateRow(items.get(0).getTimeStamp() - 2));
            final Calendar currentTimeStamp = Calendar.getInstance();
            final Calendar nextTimeStamp = Calendar.getInstance();
            final List<DateRow> daterows = new ArrayList<>();
            for (final ChatItem ci : items) {
                if (ci instanceof DateRow) {
                    daterows.add((DateRow) ci);
                }
            }
            for (int i = 0; i < daterows.size(); i++) {
                if (i == (daterows.size() - 1)) continue;
                currentTimeStamp.setTimeInMillis(daterows.get(i).getTimeStamp());
                nextTimeStamp.setTimeInMillis(daterows.get(i + 1).getTimeStamp());
                if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) == nextTimeStamp.get(Calendar.DAY_OF_YEAR)) {
                    items.remove(daterows.get(i + 1));
                }
                //Removing daterow if it is a last item - may happen when message order has changed
                DateRow lastDateRow = daterows.get(daterows.size() - 1);
                if (lastDateRow == items.get(items.size() - 1)) {
                    items.remove(lastDateRow);
                }
            }
            SearchingConsult sc = null;
            for (final ChatItem ci : items) {
                if (ci instanceof SearchingConsult) {
                    sc = (SearchingConsult) ci;
                }
            }
            if (sc != null) {
                items.remove(sc);
                items.add(sc);
            }
            sortItemsByTimeStamp(items);
            boolean isWithTyping = false;
            ConsultTyping ct = null;
            for (final ChatItem ci : items) {
                if (ci instanceof ConsultTyping) {
                    isWithTyping = true;
                    ct = (ConsultTyping) ci;
                }
            }
            if (isWithTyping && !(items.get(items.size() - 1) instanceof ConsultTyping)) {
                ct.setTimeStamp(items.get(items.size() - 1).getTimeStamp() + 1);
            }
            sortItemsByTimeStamp(items);
            removeAllSpacings(items);
            for (int i = 1; i < items.size(); i++) {
                updateConsultAvatarIfNeed(items, i - 1, i);
            }
            insertSpacing(items);
        }

        private void sortItemsByTimeStamp(@NonNull List<ChatItem> items) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(items, Comparator.comparingLong(ChatItem::getTimeStamp));
            } else {
                Collections.sort(items, (lhs, rhs) -> Long.compare(lhs.getTimeStamp(), rhs.getTimeStamp()));
            }
        }

        private void updateConsultAvatarIfNeed(List<ChatItem> list, int firstItemIdx, int lastItemIdx) {
            ChatItem firstItem = list.get(firstItemIdx);
            ChatItem lastItem = list.get(lastItemIdx);
            if (firstItem instanceof ConsultPhrase && lastItem instanceof ConsultPhrase) {
                String lastItemOperatorId = ((ConsultPhrase) lastItem).getConsultId();
                String firstItemOperatorId = ((ConsultPhrase) firstItem).getConsultId();
                if (firstItemOperatorId != null && firstItemOperatorId.equals(lastItemOperatorId)) {
                    ConsultPhrase newFirstItem = ((ConsultPhrase) firstItem).copy();
                    ConsultPhrase newLastItem = ((ConsultPhrase) lastItem).copy();
                    newFirstItem.setAvatarVisible(false);
                    newLastItem.setAvatarVisible(true);
                    list.set(firstItemIdx, newFirstItem);
                    list.set(lastItemIdx, newLastItem);
                }
            }
        }

        private void filter(@NonNull List<ChatItem> items, ClientNotificationDisplayType type, long currentThreadId) {
            if (type == ClientNotificationDisplayType.ALL) {
                return;
            }
            Set<String> systemMessagesTypes = new HashSet<>();
            for (int i = items.size() - 1; i >= 0; i--) {
                final ChatItem chatItem = items.get(i);
                if (chatItem instanceof SystemMessage) {
                    boolean isMessageUserBlocked = ((SystemMessage) chatItem).getType().equals(ChatItemType.CLIENT_BLOCKED.name());
                    if (!ObjectsCompat.equals(chatItem.getThreadId(), currentThreadId)
                            && !isMessageUserBlocked) {
                        items.remove(chatItem);
                    }
                    if (type == ClientNotificationDisplayType.CURRENT_THREAD_WITH_GROUPING) {
                        final String itemType = ((SystemMessage) chatItem).getType();
                        if (systemMessagesTypes.contains(itemType)) {
                            items.remove(chatItem);
                        } else {
                            systemMessagesTypes.add(itemType);
                        }
                    }
                }
            }
        }

        private void addItemInternal(final List<ChatItem> listToInsertTo, final ChatItem itemToInsert) {
            if (listToInsertTo.size() == 0) {
                listToInsertTo.add(new DateRow(itemToInsert.getTimeStamp() - 2));
            }
            if (itemToInsert instanceof ConsultTyping) {
                for (final Iterator<ChatItem> iter = listToInsertTo.listIterator(); iter.hasNext(); ) {
                    final ChatItem item = iter.next();
                    if (item instanceof ConsultTyping) {
                        iter.remove();
                    }
                }
            }
            if (listToInsertTo.contains(itemToInsert)) {
                return;
            }
            if (itemToInsert instanceof ConsultConnectionMessage && !((ConsultConnectionMessage) itemToInsert).getDisplay()) {
                return;
            }
            if (itemToInsert instanceof Survey) {
                final Survey survey = (Survey) itemToInsert;
                if (!survey.isCompleted() && (!survey.isDisplayMessage() ||
                        (survey.getHideAfter() != null && survey.getHideAfter() * 1000 + survey.getTimeStamp() <= System.currentTimeMillis()))) {
                    return;
                }
            }
            if (itemToInsert instanceof SimpleSystemMessage && TextUtils.isEmpty(((SimpleSystemMessage) itemToInsert).getText())) {
                return;
            }
            listToInsertTo.add(itemToInsert);
            final Calendar currentTimeStamp = Calendar.getInstance();
            final Calendar prevTimeStamp = Calendar.getInstance();
            currentTimeStamp.setTimeInMillis(itemToInsert.getTimeStamp());
            final boolean insertingToStart = lastIndexOf(listToInsertTo, itemToInsert) == 0;
            if (!insertingToStart) {//if we are not inserting to the start
                final int prevIndex = lastIndexOf(listToInsertTo, itemToInsert) - 1;
                prevTimeStamp.setTimeInMillis(listToInsertTo.get(prevIndex).getTimeStamp());
                if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) != prevTimeStamp.get(Calendar.DAY_OF_YEAR)) {
                    listToInsertTo.add(new DateRow(itemToInsert.getTimeStamp() - 2));
                }
            }
        }

        private void insertSpacing(final List<ChatItem> listToInsertTo) {
            if (listToInsertTo.size() < 2) {
                return;
            }
            for (int i = 1; i < listToInsertTo.size(); i++) {
                final ChatItem current = listToInsertTo.get(i);
                final ChatItem prev = listToInsertTo.get(i - 1);
                if (prev instanceof UserPhrase && current instanceof ConsultPhrase) {// spacing between User phrase  and Consult phrase
                    listToInsertTo.add(i, new Space(24, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof UserPhrase && current instanceof SystemMessage) {// spacing between Consult and Consult connected
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && current instanceof UserPhrase) {// spacing between Consult and User phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof SystemMessage && current instanceof ConsultPhrase) {// spacing between Consult connected and Consult phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof SystemMessage && current instanceof UserPhrase) {
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof SystemMessage && current instanceof SystemMessage) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && current instanceof SystemMessage) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (!(prev instanceof Space) && current instanceof ConsultTyping) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && current instanceof ConsultPhrase) {// spacing between Consult phrase  and Consult phrase
                    listToInsertTo.add(i, new Space(0, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof UserPhrase && current instanceof UserPhrase) {// spacing between User phrase  and User phrase
                    listToInsertTo.add(i, new Space(0, prev.getTimeStamp() + 1));
                }
            }
        }

        private void removeAllSpacings(final List<ChatItem> list) {
            for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                if (iter.next() instanceof Space) {
                    iter.remove();
                }
            }
        }

        private int indexOf(@NonNull List<ChatItem> list, @NonNull ChatItem chatItem) {
            for (int i = 0; i < list.size(); i++) {
                if (chatItem.isTheSameItem(list.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        private int lastIndexOf(@NonNull List<ChatItem> list, @NonNull ChatItem chatItem) {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (chatItem.isTheSameItem(list.get(i))) {
                    return i;
                }
            }
            return -1;
        }
    }
}