package im.threads.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

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
import im.threads.business.models.MessageState;
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
import im.threads.business.ogParser.OpenGraphParser;
import im.threads.business.ogParser.OpenGraphParserJsoupImpl;
import im.threads.business.utils.ChatItemListFinder;
import im.threads.business.utils.FileUtils;
import im.threads.business.utils.FileUtilsKt;
import im.threads.business.utils.preferences.PrefUtilsBase;
import im.threads.business.workers.FileDownloadWorker;
import im.threads.ui.ChatStyle;
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
import im.threads.ui.utils.ThreadRunnerKt;
import im.threads.ui.utils.preferences.PrefUtilsUi;
import im.threads.ui.views.VoiceTimeLabelFormatterKt;
import io.reactivex.subjects.PublishSubject;

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
    private final ChatMessagesOrderer chatMessagesOrderer;
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
            @NonNull MediaMetadataRetriever mediaMetadataRetriever) {
        this.mCallback = callback;
        this.fdMediaPlayer = fdMediaPlayer;
        this.mediaMetadataRetriever = mediaMetadataRetriever;

        clientNotificationDisplayType = PrefUtilsUi.getClientNotificationDisplayType();
        currentThreadId = PrefUtilsBase.getThreadId();
        chatMessagesOrderer = new ChatMessagesOrderer();
    }

    private static int getUnreadCount(final List<ChatItem> list) {
        int counter = 0;
        for (final ChatItem ci : list) {
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = ((ConsultPhrase) ci);
                if (!cp.isRead()) {
                    counter++;
                }
            }
            if (ci instanceof Survey) {
                final Survey survey = (Survey) ci;
                if (!survey.isRead()) {
                    counter++;
                }
            }
        }
        return counter;
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
                return new ConsultPhraseHolder(parent, highlightingStream, openGraphParser);
            case TYPE_USER_PHRASE:
                return new UserPhraseViewHolder(parent, highlightingStream, openGraphParser, fdMediaPlayer);
            case TYPE_FREE_SPACE:
                return new SpaceViewHolder(parent);
            case TYPE_IMAGE_FROM_CONSULT:
                return new ImageFromConsultViewHolder(parent, incomingImageMaskTransformation, highlightingStream, openGraphParser);
            case TYPE_IMAGE_FROM_USER:
                return new ImageFromUserViewHolder(parent, outgoingImageMaskTransformation, highlightingStream, openGraphParser);
            case TYPE_FILE_FROM_CONSULT:
                return new ConsultFileViewHolder(parent, highlightingStream, openGraphParser);
            case TYPE_FILE_FROM_USER:
                return new UserFileViewHolder(parent, highlightingStream, openGraphParser);
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
            bindSystemMessageVH((SystemMessageViewHolder) holder, (SystemMessage) list.get(position));
        }
        if (holder instanceof ConsultPhraseHolder) {
            bindConsultPhraseVH((ConsultPhraseHolder) holder, (ConsultPhrase) list.get(position));
        }
        if (holder instanceof UserPhraseViewHolder) {
            bindUserPhraseVH((UserPhraseViewHolder) holder, (UserPhrase) list.get(position));
        }
        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).onBind(list.get(position).getTimeStamp());
        }
        if (holder instanceof ConsultIsTypingViewHolderNew) {
            bindConsultIsTypingVH((ConsultIsTypingViewHolderNew) holder);
        }
        if (holder instanceof SpaceViewHolder) {
            final Space space = (Space) list.get(position);
            ((SpaceViewHolder) holder).onBind((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics()));
        }
        if (holder instanceof ImageFromConsultViewHolder) {
            bindImageFromConsultVH((ImageFromConsultViewHolder) holder, (ConsultPhrase) list.get(position));
        }
        if (holder instanceof ImageFromUserViewHolder) {
            bindImageFromUserVH((ImageFromUserViewHolder) holder, (UserPhrase) list.get(position));
        }
        if (holder instanceof UserFileViewHolder) {
            bindFileFromUserVH((UserFileViewHolder) holder, (UserPhrase) list.get(position));
        }
        if (holder instanceof ConsultFileViewHolder) {
            bindFileFromConsultVH((ConsultFileViewHolder) holder, (ConsultPhrase) list.get(position));
        }
        if (holder instanceof UnreadMessageViewHolder) {
            ((UnreadMessageViewHolder) holder).onBind((UnreadMessages) list.get(holder.getAdapterPosition()));
        }
        if (holder instanceof ScheduleInfoViewHolder) {
            ((ScheduleInfoViewHolder) holder).bind((ScheduleInfo) list.get(holder.getAdapterPosition()));
        }
        if (holder instanceof RatingThumbsViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingThumbsViewHolder) holder).bind(survey, mCallback);
        }
        if (holder instanceof RatingThumbsSentViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingThumbsSentViewHolder) holder).bind(survey);
        }
        if (holder instanceof RatingStarsViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingStarsViewHolder) holder).bind(
                    survey,
                    ratingCount -> mCallback.onRatingClick(survey, ratingCount)
            );
        }
        if (holder instanceof RatingStarsSentViewHolder) {
            ((RatingStarsSentViewHolder) holder).bind((Survey) list.get(holder.getAdapterPosition()));
        }
        if (holder instanceof RequestResolveThreadViewHolder) {
            ((RequestResolveThreadViewHolder) holder).bind(mCallback);
        }
        if (holder instanceof ConsultVoiceMessageViewHolder) {
            bindVoiceMessageFromConsultVH((ConsultVoiceMessageViewHolder) holder, (ConsultPhrase) list.get(position));
        }
        if (holder instanceof QuickRepliesViewHolder) {
            ((QuickRepliesViewHolder) holder).bind((QuickReplyItem) list.get(position), mCallback);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        Object o;
        try {
            o = list.get(position);
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
            if (cp.isVoiceMessage()) {
                return TYPE_VOICE_MESSAGE_FROM_CONSULT;
            }
            if (cp.isOnlyImage()) {
                return TYPE_IMAGE_FROM_CONSULT;
            }
            if (cp.isOnlyDoc()) {
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
            if (questionDTO.isSimple()) {
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
        return list.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof BaseHolder) {
            ((BaseHolder) holder).onClear();
        }
    }

    public void setAllMessagesRead() {
        for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ConsultPhrase) {
                if (!((ConsultPhrase) item).isRead()) {
                    ((ConsultPhrase) item).setRead(true);
                }
            }
            if (item instanceof Survey) {
                if (!((Survey) item).isRead()) {
                    ((Survey) item).setRead(true);
                }
            }
            if (item instanceof UnreadMessages) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, item));
                } catch (final Exception e) {
                    LoggerEdna.error("setAllMessagesRead", e);
                }
                iter.remove();
            }
        }
    }

    public void setAvatar(final String consultId, String newAvatarImageUrl) {
        if (TextUtils.isEmpty(consultId)) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            final ChatItem item = list.get(i);
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

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isTheSameItem(chatItem)) {
                highlightedItem = list.get(i);
                index = i;
            }
        }

        return index;
    }

    public int setItemHighlighted(final String uuid) {
        for (ChatItem chatItem : list) {
            if (chatItem instanceof ChatPhrase && Objects.equals(((ChatPhrase) chatItem).getId(), uuid)) {
                setItemHighlighted(chatItem);
                return ChatItemListFinder.lastIndexOf(list, chatItem);
            }
        }
        return -1;
    }

    private void removeConsultIsTyping() {
        for (final ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof ConsultTyping) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, cm));
                } catch (final Exception e) {
                    LoggerEdna.error("removeConsultIsTyping", e);
                }
                iter.remove();
            }
        }
    }

    /**
     * Remove close request from the thread history
     *
     * @return true - if deletion occurred, false - if RequestResolveThread item wasn't found in the history
     */
    public boolean removeResolveRequest() {
        boolean removed = false;
        for (final ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof RequestResolveThread) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, cm));
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
        for (final ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof Survey) {
                final Survey survey = (Survey) cm;
                if (sendingId == survey.getSendingId()) {
                    try {
                        notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, cm));
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
        for (final ChatItem ci : list) {
            if (ci instanceof SearchingConsult) {
                return;
            }
        }
        final SearchingConsult sc = new SearchingConsult();
        list.add(sc);
        notifyItemInserted(ChatItemListFinder.lastIndexOf(list, sc));
    }

    public void removeConsultSearching() {
        for (final Iterator<ChatItem> iter = list.iterator();
             iter.hasNext(); ) {
            final ChatItem ch = iter.next();
            if (ch instanceof SearchingConsult) {
                try {
                    notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, ch));
                } catch (final Exception e) {
                    LoggerEdna.error("removeConsultSearching", e);
                }
                iter.remove();
            }
        }
    }

    public int getCurrentItemCount() {
        int count = 0;
        for (final ChatItem item : list) {
            if (item instanceof UserPhrase || item instanceof ConsultPhrase || item instanceof SystemMessage || item instanceof Survey) {
                count++;
            }
        }
        return count;
    }

    public void addItems(@NonNull final List<ChatItem> items) {
        boolean withTyping = false;
        boolean withRequestResolveThread = false;
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
        ArrayList<ChatItem> newList = new ArrayList<>(list);
        chatMessagesOrderer.addAndOrder(newList, items, clientNotificationDisplayType, currentThreadId);
        removeSurveyIfNotLatest(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatDiffCallback(list, newList));
        list.clear();
        list.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void modifyImageInItem(FileDescription newFileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                ChatPhrase chatPhrase = (ChatPhrase) list.get(i);
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

                    list.set(i, chatPhrase);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public int getUnreadCount() {
        return getUnreadCount(list);
    }

    public boolean hasSchedule() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ScheduleInfo) {
                return true;
            }
        }
        return false;
    }

    public void removeSchedule(final boolean checkSchedule) {
        for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ScheduleInfo) {
                final ScheduleInfo scheduleInfo = (ScheduleInfo) item;
                if (!checkSchedule || scheduleInfo.isChatWorking()) {
                    try {
                        notifyItemRemoved(ChatItemListFinder.lastIndexOf(list, scheduleInfo));
                    } catch (final Exception e) {
                        LoggerEdna.error("removeSchedule", e);
                    }
                    iter.remove();
                }
            }
        }
    }

    public void changeStateOfSurvey(long sendingId, MessageState sentState) {
        for (final ChatItem cm : list) {
            if (cm instanceof Survey) {
                final Survey survey = (Survey) cm;
                if (sendingId == survey.getSendingId()) {
                    LoggerEdna.info("changeStateOfMessageByProviderId: changing read state");
                    ((Survey) cm).setSentState(sentState);
                    notifyItemChangedOnUi(survey);
                }
            }
        }
    }

    public void changeStateOfMessageByProviderId(final String providerId, final MessageState state) {
        for (final ChatItem cm : list) {
            if (cm instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) cm;
                if (ObjectsCompat.equals(providerId, up.getProviderId())) {
                    LoggerEdna.info("changeStateOfMessageByProviderId: changing read state");
                    ((UserPhrase) cm).setSentState(state);
                    notifyItemChangedOnUi(cm);
                }
            }
        }
    }

    public void updateProgress(final FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (fileDescription.getFileUri() == null && fileDescription.getDownloadPath() == null
                    && (getItemViewType(i) == TYPE_IMAGE_FROM_USER
                    || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT
                    || getItemViewType(i) == TYPE_USER_PHRASE
                    || getItemViewType(i) == TYPE_VOICE_MESSAGE_FROM_CONSULT))
                continue;
            if (list.get(i) instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)) {
                    cp.setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(list, cp));
                } else if (cp.getQuote() != null && ObjectsCompat.equals(cp.getQuote().getFileDescription(), fileDescription)) {
                    cp.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(list, cp));
                }
            } else if (list.get(i) instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) list.get(i);
                if (ObjectsCompat.equals(up.getFileDescription(), fileDescription)) {
                    up.setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(list, up));
                } else if (up.getQuote() != null && ObjectsCompat.equals(up.getQuote().getFileDescription(), fileDescription)) {
                    up.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(ChatItemListFinder.indexOf(list, up));
                }
            }
        }
    }

    public void onDownloadError(final FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                final ChatPhrase cp = (ChatPhrase) list.get(i);
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
        for (final ChatItem ci : list) {
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                if (!Objects.equals(cp.getConsultId(), consultId)) continue;
                final String oldUrl = cp.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    cp.setAvatarPath(newUrl);
                    notifyItemChanged(ChatItemListFinder.lastIndexOf(list, cp));
                }
            }
        }
    }

    public ArrayList<ChatItem> getList() {
        return list;
    }

    private void notifyItemChangedOnUi(final ChatItem chatItem) {
        ThreadRunnerKt.runOnUiThread(() -> {
            int position = ChatItemListFinder.indexOf(list, chatItem);
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
            FileDownloadWorker.startDownloadFD(ctx, holder.getFileDescription());
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
                    final SystemMessage cc1 = (SystemMessage) list.get(holder.getAdapterPosition());
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
        holder.onBind(
                userPhrase,
                getFormattedDuration(userPhrase.getFileDescription()),
                v -> mCallback.onImageClick(userPhrase),
                v -> {
                    if (userPhrase.getFileDescription() != null) {
                        mCallback.onFileClick(userPhrase.getFileDescription());
                    } else if (userPhrase.getQuote() != null && userPhrase.getQuote().getFileDescription() != null) {
                        mCallback.onFileClick(userPhrase.getQuote().getFileDescription());
                    }
                },
                v -> onVoiceMessagePlayClick(holder),
                v -> mCallback.onUserPhraseClick(userPhrase, holder.getAdapterPosition()),
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
        final ConsultTyping consultTyping = (ConsultTyping) list.get(holder.getAdapterPosition());
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

    private void downloadImageIfNeeded(@Nullable FileDescription fileDescription) {
        if(fileDescription != null) {
            if (FileUtils.isImage(fileDescription) && fileDescription.getFileUri() == null) {
                mCallback.onFileDownloadRequest(fileDescription);
            }
        }
    }

    private void downloadVoiceIfNeeded(@Nullable FileDescription fileDescription) {
        if(fileDescription != null) {
            if (FileUtils.isVoiceMessage(fileDescription) && fileDescription.getFileUri() == null) {
                mCallback.onFileDownloadRequest(fileDescription);
            }
        }
    }

    private void bindFileFromUserVH(@NonNull final UserFileViewHolder holder, UserPhrase userPhrase) {
        holder.onBind(
                userPhrase,
                v -> mCallback.onFileClick(userPhrase.getFileDescription()),
                v -> mCallback.onUserPhraseClick(userPhrase, holder.getAdapterPosition()),
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
                v -> mCallback.onConsultAvatarClick(consultPhrase.getConsultId())
        );
    }

    @SuppressLint("RestrictedApi")
    private void bindVoiceMessageFromConsultVH(@NonNull final ConsultVoiceMessageViewHolder holder, ConsultPhrase consultPhrase) {
        downloadVoiceIfNeeded(consultPhrase.getFileDescription());
        holder.onBind(
                consultPhrase,
                consultPhrase.equals(highlightedItem),
                getFormattedDuration(consultPhrase.getFileDescription()),
                v -> {
                    phraseLongClick(consultPhrase, holder.getAdapterPosition());
                    return true;
                },
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
        addItems(new ArrayList<>());
    }

    public void setCurrentThreadId(long threadId) {
        this.currentThreadId = threadId;
        addItems(new ArrayList<>());
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
                    notifyItemChanged(ChatItemListFinder.lastIndexOf(list, chatItem));
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
        for (ChatItem chatPhrase : list) {
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

    private String getFormattedDuration(@Nullable FileDescription fileDescription) {
        long duration = 0L;
        if (fileDescription != null && FileUtils.isVoiceMessage(fileDescription) && fileDescription.getFileUri() != null) {
            duration = FileUtilsKt.getDuration(mediaMetadataRetriever, fileDescription.getFileUri());
        }
        return VoiceTimeLabelFormatterKt.formatAsDuration(duration);
    }

    public void removeItem(ChatItem chatItem) {
        final int index = ChatItemListFinder.lastIndexOf(list, chatItem);
        if (index != -1) {
            notifyItemRemoved(index);
            list.remove(index);
        }
    }

    private void removeSurveyIfNotLatest(ArrayList<ChatItem> list) {
        boolean isListSizeMoreThat1Element = list != null && list.size() > 1;
        boolean isPreviousItemSurvey = isListSizeMoreThat1Element &&
                list.get(list.size() - 2) instanceof Survey;

        if (isPreviousItemSurvey) {
            list.remove(list.size() - 2);
        }
    }

    public interface Callback {
        void onFileClick(FileDescription fileDescription);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onQuoteClick(Quote quote);

        void onUserPhraseClick(UserPhrase userPhrase, int position);

        void onConsultAvatarClick(String consultId);

        void onImageClick(ChatPhrase chatPhrase);

        void onFileDownloadRequest(FileDescription fileDescription);

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
                ct.setDate(items.get(items.size() - 1).getTimeStamp() + 1);
            }
            sortItemsByTimeStamp(items);
            removeAllSpacings(items);
            for (int i = 1; i < items.size(); i++) {
                updateConsultAvatarIfNeed(items.get(i - 1), items.get(i));
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

        private void updateConsultAvatarIfNeed(ChatItem firstItem, ChatItem lastItem) {
            if (firstItem instanceof ConsultPhrase && lastItem instanceof ConsultPhrase) {
                String lastItemOperatorId = ((ConsultPhrase) lastItem).getConsultId();
                String firstItemOperatorId = ((ConsultPhrase) firstItem).getConsultId();
                if (firstItemOperatorId != null && firstItemOperatorId.equals(lastItemOperatorId)) {
                    ((ConsultPhrase) firstItem).setAvatarVisible(false);
                    ((ConsultPhrase) lastItem).setAvatarVisible(true);
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
            if (itemToInsert instanceof ConsultConnectionMessage && !((ConsultConnectionMessage) itemToInsert).isDisplayMessage()) {
                return;
            }
            if (itemToInsert instanceof Survey) {
                final Survey survey = (Survey) itemToInsert;
                if (!survey.isCompleted() && (!survey.isDisplayMessage() || survey.getHideAfter() * 1000 + survey.getTimeStamp() <= System.currentTimeMillis())) {
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
