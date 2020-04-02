package im.threads.internal.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.ObjectsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import im.threads.ChatStyle;
import im.threads.internal.Config;
import im.threads.internal.holders.ConsultConnectionMessageViewHolder;
import im.threads.internal.holders.ConsultFileViewHolder;
import im.threads.internal.holders.ConsultIsTypingViewHolderNew;
import im.threads.internal.holders.ConsultPhraseHolder;
import im.threads.internal.holders.DateViewHolder;
import im.threads.internal.holders.EmptyViewHolder;
import im.threads.internal.holders.ImageFromConsultViewHolder;
import im.threads.internal.holders.ImageFromUserViewHolder;
import im.threads.internal.holders.RatingStarsSentViewHolder;
import im.threads.internal.holders.RatingStarsViewHolder;
import im.threads.internal.holders.RatingThumbsSentViewHolder;
import im.threads.internal.holders.RatingThumbsViewHolder;
import im.threads.internal.holders.RequestResolveThreadViewHolder;
import im.threads.internal.holders.ScheduleInfoViewHolder;
import im.threads.internal.holders.SearchingConsultViewHolder;
import im.threads.internal.holders.SpaceViewHolder;
import im.threads.internal.holders.UnreadMessageViewHolder;
import im.threads.internal.holders.UserFileViewHolder;
import im.threads.internal.holders.UserPhraseViewHolder;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.ConsultChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.DateRow;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.SearchingConsult;
import im.threads.internal.model.Space;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UnreadMessages;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.MaskedTransformation;
import im.threads.internal.utils.ThreadUtils;
import im.threads.internal.utils.ThreadsLogger;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatAdapter ";

    private static final int TYPE_CONSULT_TYPING = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_SEARCHING_CONSULT = 3;
    private static final int TYPE_CONSULT_CONNECTED = 4;
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

    private final Handler viewHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<ChatItem> list = new ArrayList<>();
    @NonNull
    private final Context ctx;
    @NonNull
    private final AdapterInterface mAdapterInterface;
    @NonNull
    private final MaskedTransformation outgoingImageMaskTransformation;
    @NonNull
    private final MaskedTransformation incomingImageMaskTransformation;

    public ChatAdapter(@NonNull final Context ctx, @NonNull final AdapterInterface adapterInterface) {
        this.ctx = ctx;
        ChatStyle style = Config.instance.getChatStyle();
        this.mAdapterInterface = adapterInterface;
        this.outgoingImageMaskTransformation = new MaskedTransformation(ctx.getResources().getDrawable(style.outgoingImageBubbleMask));
        this.incomingImageMaskTransformation = new MaskedTransformation(ctx.getResources().getDrawable(style.incomingImageBubbleMask));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case TYPE_CONSULT_TYPING:
                return new ConsultIsTypingViewHolderNew(parent);
            case TYPE_DATE:
                return new DateViewHolder(parent);
            case TYPE_SEARCHING_CONSULT:
                return new SearchingConsultViewHolder(parent);
            case TYPE_CONSULT_CONNECTED:
                return new ConsultConnectionMessageViewHolder(parent);
            case TYPE_CONSULT_PHRASE:
                return new ConsultPhraseHolder(parent);
            case TYPE_USER_PHRASE:
                return new UserPhraseViewHolder(parent);
            case TYPE_FREE_SPACE:
                return new SpaceViewHolder(parent);
            case TYPE_IMAGE_FROM_CONSULT:
                return new ImageFromConsultViewHolder(parent, incomingImageMaskTransformation);
            case TYPE_IMAGE_FROM_USER:
                return new ImageFromUserViewHolder(parent, outgoingImageMaskTransformation);
            case TYPE_FILE_FROM_CONSULT:
                return new ConsultFileViewHolder(parent);
            case TYPE_FILE_FROM_USER:
                return new UserFileViewHolder(parent);
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
            default:
                return new EmptyViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConsultConnectionMessageViewHolder) {
            final ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(position);
            ((ConsultConnectionMessageViewHolder) holder).onBind(
                    cc,
                    v -> {
                        final ConsultConnectionMessage cc1 = (ConsultConnectionMessage) list.get(holder.getAdapterPosition());
                        mAdapterInterface.onConsultConnectionClick(cc1);
                    }
            );
        }
        if (holder instanceof ConsultPhraseHolder) {
            final ConsultPhrase consultPhrase = (ConsultPhrase) list.get(position);
            if (consultPhrase.getFileDescription() != null && consultPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(consultPhrase.getFileDescription());
            }
            ((ConsultPhraseHolder) holder)
                    .onBind(
                            consultPhrase,
                            consultPhrase.getPhrase(),
                            consultPhrase.getAvatarPath(),
                            consultPhrase.getTimeStamp(),
                            consultPhrase.isAvatarVisible(),
                            consultPhrase.getQuote(),
                            consultPhrase.getFileDescription(),
                            v -> mAdapterInterface.onImageClick(consultPhrase),
                            v -> {
                                if (consultPhrase.getQuote() != null && consultPhrase.getQuote().getFileDescription() != null) {
                                    mAdapterInterface.onFileClick(consultPhrase.getQuote().getFileDescription());
                                }
                                if (consultPhrase.getFileDescription() != null) {
                                    mAdapterInterface.onFileClick(consultPhrase.getFileDescription());
                                }
                            },
                            v -> {
                                mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                                return true;
                            },
                            v -> mAdapterInterface.onConsultAvatarClick(consultPhrase.getConsultId()),
                            () -> notifyItemChangedOnUi(consultPhrase),
                            consultPhrase.isChosen()
                    );
        }
        if (holder instanceof UserPhraseViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            if (userPhrase.getFileDescription() != null && userPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(userPhrase.getFileDescription());
            }
            ((UserPhraseViewHolder) holder).onBind(
                    userPhrase,
                    userPhrase.getPhrase(),
                    userPhrase.getTimeStamp(),
                    userPhrase.getSentState(),
                    userPhrase.getQuote(),
                    userPhrase.getFileDescription(),
                    v -> mAdapterInterface.onImageClick(userPhrase),
                    v -> {
                        if (userPhrase.getFileDescription() != null) {
                            mAdapterInterface.onFileClick(userPhrase.getFileDescription());
                        } else if (userPhrase.getQuote() != null && userPhrase.getQuote().getFileDescription() != null) {
                            mAdapterInterface.onFileClick(userPhrase.getQuote().getFileDescription());
                        }
                    },
                    v -> mAdapterInterface.onUserPhraseClick(userPhrase, holder.getAdapterPosition()),
                    v -> {
                        mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                        return true;
                    },
                    () -> notifyItemChangedOnUi(userPhrase),
                    userPhrase.isChosen()
            );
        }
        if (holder instanceof DateViewHolder) {
            final DateRow dateRow = (DateRow) list.get(position);
            ((DateViewHolder) holder).onBind(dateRow.getDate());
        }
        if (holder instanceof ConsultIsTypingViewHolderNew) {
            final ChatStyle style = Config.instance.getChatStyle();
            final ConsultTyping consultTyping = (ConsultTyping) list.get(holder.getAdapterPosition());
            ((ConsultIsTypingViewHolderNew) holder).onBind(v -> mAdapterInterface.onConsultAvatarClick(consultTyping.getConsultId()));
            final String avatarPath = FileUtils.convertRelativeUrlToAbsolute(consultTyping.getAvatarPath());
            Picasso.with(ctx)
                    .load(avatarPath)
                    .fit()
                    .error(style.defaultOperatorAvatar)
                    .placeholder(style.defaultOperatorAvatar)
                    .centerCrop()
                    .transform(new CircleTransformation())
                    .into(((ConsultIsTypingViewHolderNew) holder).mConsultAvatar);
        }
        if (holder instanceof SpaceViewHolder) {
            final Space space = (Space) list.get(position);
            final float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics());
            ((SpaceViewHolder) holder).onBind((int) height);
        }
        if (holder instanceof ImageFromConsultViewHolder) {
            final ConsultPhrase consultPhrase = (ConsultPhrase) list.get(position);
            if (consultPhrase.getFileDescription() != null && consultPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(consultPhrase.getFileDescription());
            }
            ((ImageFromConsultViewHolder) holder).onBind(
                    consultPhrase.getAvatarPath(),
                    consultPhrase.getFileDescription(),
                    consultPhrase.getTimeStamp(),
                    v -> mAdapterInterface.onImageClick(consultPhrase),
                    v -> {
                        mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                        return true;
                    },
                    consultPhrase.getFileDescription().isDownloadError(),
                    consultPhrase.isChosen(),
                    consultPhrase.isAvatarVisible()
            );

        }
        if (holder instanceof ImageFromUserViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            if (userPhrase.getFileDescription() != null) {
                if (userPhrase.getFileDescription().getFilePath() == null) {
                    mAdapterInterface.onImageDownloadRequest(userPhrase.getFileDescription());
                }
                ((ImageFromUserViewHolder) holder).onBind(
                        userPhrase.getFileDescription(),
                        userPhrase.getTimeStamp(),
                        v -> mAdapterInterface.onImageClick(userPhrase),
                        v -> {
                            mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                            return true;
                        },
                        userPhrase.getFileDescription().isDownloadError(),
                        userPhrase.isChosen(),
                        userPhrase.getSentState()
                );
            }
        }
        if (holder instanceof UserFileViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            ((UserFileViewHolder) holder).onBind(
                    userPhrase.getTimeStamp(),
                    userPhrase.getFileDescription(),
                    v -> mAdapterInterface.onFileClick(userPhrase.getFileDescription()),
                    v -> mAdapterInterface.onUserPhraseClick(userPhrase, holder.getAdapterPosition()),
                    v -> {
                        mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                        return true;
                    }, userPhrase.isChosen(),
                    userPhrase.getSentState()
            );
        }
        if (holder instanceof ConsultFileViewHolder) {
            final ConsultPhrase consultPhrase = (ConsultPhrase) list.get(position);
            ((ConsultFileViewHolder) holder).onBind(
                    consultPhrase.getTimeStamp(),
                    consultPhrase.getFileDescription(),
                    consultPhrase.getAvatarPath(),
                    v -> mAdapterInterface.onFileClick(consultPhrase.getFileDescription()),
                    v -> {
                        mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                        return true;
                    },
                    consultPhrase.isAvatarVisible(),
                    consultPhrase.isChosen()
            );
        }
        if (holder instanceof UnreadMessageViewHolder) {
            ((UnreadMessageViewHolder) holder).onBind((UnreadMessages) list.get(holder.getAdapterPosition()));
        }
        if (holder instanceof ScheduleInfoViewHolder) {
            ((ScheduleInfoViewHolder) holder).bind((ScheduleInfo) list.get(holder.getAdapterPosition()));
        }
        if (holder instanceof RatingThumbsViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingThumbsViewHolder) holder).bind(survey, mAdapterInterface);
        }
        if (holder instanceof RatingThumbsSentViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingThumbsSentViewHolder) holder).bind(survey);
        }
        if (holder instanceof RatingStarsViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingStarsViewHolder) holder).bind(
                    survey,
                    ratingCount -> mAdapterInterface.onRatingClick(survey, ratingCount)
            );
        }
        if (holder instanceof RatingStarsSentViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingStarsSentViewHolder) holder).bind(survey);
        }
        if (holder instanceof RequestResolveThreadViewHolder) {
            ((RequestResolveThreadViewHolder) holder).bind(mAdapterInterface);
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
            if (item instanceof UnreadMessages) {
                try {
                    notifyItemRemoved(list.lastIndexOf(item));
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "setAllMessagesRead", e);
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
                if (p.getConsultId().equals(consultId) && !ObjectsCompat.equals(p.getAvatarPath(), newAvatarImageUrl)) {
                    p.setAvatarPath(TextUtils.isEmpty(newAvatarImageUrl) ? "" : newAvatarImageUrl);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void removeHighlight() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase && ((ChatPhrase) list.get(i)).isHighlight()) {
                ((ChatPhrase) list.get(i)).setHighLighted(false);
                notifyItemChanged(i);
            }
        }
    }

    public int setItemHighlighted(final ChatPhrase chatPhrase) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(chatPhrase)) {
                ((ChatPhrase) list.get(i)).setHighLighted(true);
                index = i;
                notifyItemChanged(index);
            }
        }
        return index;
    }

    private void removeConsultIsTyping() {
        for (final ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof ConsultTyping) {
                try {
                    notifyItemRemoved(list.lastIndexOf(cm));
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "removeConsultIsTyping", e);
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
                    notifyItemRemoved(list.lastIndexOf(cm));
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "removeResolveRequest", e);
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
                        notifyItemRemoved(list.lastIndexOf(cm));
                    } catch (final Exception e) {
                        ThreadsLogger.e(TAG, "removeSurvey", e);
                    }
                    iter.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    public void setSearchingConsult() {
        boolean containsSearch = false;
        for (final ChatItem ci : list) {
            if (ci instanceof SearchingConsult) {
                containsSearch = true;
            }
        }
        if (containsSearch) {
            return;
        }
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        final SearchingConsult sc = new SearchingConsult(c.getTimeInMillis());
        list.add(sc);
        notifyItemInserted(list.lastIndexOf(sc));
    }

    public void removeConsultSearching() {
        for (final Iterator<ChatItem> iter = list.iterator();
             iter.hasNext(); ) {
            final ChatItem ch = iter.next();
            if (ch instanceof SearchingConsult) {
                try {
                    notifyItemRemoved(list.lastIndexOf(ch));
                } catch (final Exception e) {
                    ThreadsLogger.e(TAG, "removeConsultSearching", e);
                }
                iter.remove();
            }
        }
    }

    public int getCurrentItemCount() {
        int count = 0;
        for (final ChatItem item : list) {
            if (item instanceof UserPhrase || item instanceof ConsultPhrase || item instanceof ConsultConnectionMessage || item instanceof Survey) {
                count++;
            }
        }
        return count;
    }

    public void addItems(@NonNull final List<ChatItem> items) {
        boolean withTyping = false;
        for (final ChatItem ci : items) {
            if (ci instanceof ConsultTyping) {
                withTyping = true;
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
        ChatMessagesOrderer.addAndOrder(list, items);
        notifyDataSetChangedOnUi();
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
        boolean scheduleRemoved = false;
        for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ScheduleInfo) {
                final ScheduleInfo scheduleInfo = (ScheduleInfo) item;
                if (!checkSchedule || scheduleInfo.isChatWorking()) {
                    iter.remove();
                    scheduleRemoved = true;
                }
            }
        }
        if (scheduleRemoved) {
            notifyDataSetChangedOnUi();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(final int position) {
        Object o;
        try {
            o = list.get(position);
        } catch (final IndexOutOfBoundsException e) {
            ThreadsLogger.e(TAG, "getItemViewType", e);
            return 0;
        }
        if (o instanceof ConsultConnectionMessage) {
            return TYPE_CONSULT_CONNECTED;
        }
        if (o instanceof ConsultTyping) {
            return TYPE_CONSULT_TYPING;
        }
        if (o instanceof DateRow) {
            return TYPE_DATE;
        }
        if (o instanceof SearchingConsult) {
            return TYPE_SEARCHING_CONSULT;
        }
        if (o instanceof Space) {
            return TYPE_FREE_SPACE;
        }
        if (o instanceof UnreadMessages) {
            return TYPE_UNREAD_MESSAGES;
        }
        if (o instanceof ScheduleInfo) {
            return TYPE_SCHEDULE;
        }
        if (o instanceof RequestResolveThread) {
            return TYPE_REQ_RESOLVE_THREAD;
        }
        if (o instanceof ConsultPhrase) {
            final ConsultPhrase cp = (ConsultPhrase) o;
            final FileDescription fileDescription = cp.getFileDescription();
            if (TextUtils.isEmpty(cp.getPhrase())
                    && (FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.JPEG
                    || FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.PNG)) {
                return TYPE_IMAGE_FROM_CONSULT;
            } else if (TextUtils.isEmpty(cp.getPhrase())
                    && (FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.PDF
                    || FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.PDF)) {
                return TYPE_FILE_FROM_CONSULT;
            } else {
                return TYPE_CONSULT_PHRASE;
            }
        }
        if (o instanceof UserPhrase) {
            final UserPhrase up = (UserPhrase) o;
            if (up.getFileDescription() == null) {
                return TYPE_USER_PHRASE;
            }
            if (up.isOnlyImage()) {
                return TYPE_IMAGE_FROM_USER;
            }
            int extension = -1;
            if (up.getFileDescription().getFilePath() != null) {
                extension = FileUtils.getExtensionFromPath(up.getFileDescription().getFilePath());
            } else if (up.getFileDescription().getIncomingName() != null) {
                extension = FileUtils.getExtensionFromPath(up.getFileDescription().getIncomingName());
            }
            if ((extension == FileUtils.PDF || extension == FileUtils.OTHER_DOC_FORMATS) && TextUtils.isEmpty(up.getPhrase())) {
                return TYPE_FILE_FROM_USER;
            } else {
                return TYPE_USER_PHRASE;
            }
        }
        if (o instanceof Survey) {
            final Survey survey = (Survey) o;
            final QuestionDTO questionDTO = survey.getQuestions().get(0);
            if (questionDTO.isSimple()) {
                if (survey.getSentState() == MessageState.STATE_SENT || survey.getSentState() == MessageState.STATE_WAS_READ) {
                    return TYPE_RATING_THUMBS_SENT;
                } else {
                    return TYPE_RATING_THUMBS;
                }
            } else {
                if (survey.getSentState() == MessageState.STATE_SENT || survey.getSentState() == MessageState.STATE_WAS_READ) {
                    return TYPE_RATING_STARS_SENT;
                } else {
                    return TYPE_RATING_STARS;
                }
            }
        }
        return super.getItemViewType(position);
    }

    public void changeStateOfSurvey(long sendingId, MessageState sentState) {
        for (final ChatItem cm : list) {
            if (cm instanceof Survey) {
                final Survey survey = (Survey) cm;
                if (sendingId == survey.getSendingId()) {
                    ThreadsLogger.i(TAG, "changeStateOfMessageByProviderId: changing read state");
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
                if (providerId.equals(up.getProviderId())) {
                    ThreadsLogger.i(TAG, "changeStateOfMessageByProviderId: changing read state");
                    ((UserPhrase) cm).setSentState(state);
                }
            }
        }
        notifyDataSetChangedOnUi();
    }

    public void updateProgress(final FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (fileDescription.getFilePath() == null
                    && (getItemViewType(i) == TYPE_IMAGE_FROM_USER || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT))
                continue;
            if (list.get(i) instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)) {
                    cp.setFileDescription(fileDescription);
                    notifyItemChanged(list.indexOf(cp));
                } else if (cp.getQuote() != null && ObjectsCompat.equals(cp.getQuote().getFileDescription(), fileDescription)) {
                    cp.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(list.indexOf(cp));
                }
            } else if (list.get(i) instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) list.get(i);
                if (ObjectsCompat.equals(up.getFileDescription(), fileDescription)) {
                    up.setFileDescription(fileDescription);
                    notifyItemChanged(list.indexOf(up));
                } else if (up.getQuote() != null && ObjectsCompat.equals(up.getQuote().getFileDescription(), fileDescription)) {
                    up.getQuote().setFileDescription(fileDescription);
                    notifyItemChanged(list.indexOf(up));
                }
            }
        }
    }

    public void setItemChosen(final boolean isChosen, final ChatPhrase cp) {
        if (cp == null) {
            return;
        }
        if (cp instanceof UserPhrase) {
            ((UserPhrase) cp).setChosen(isChosen);
            notifyItemChanged(list.indexOf(cp));
        }
        if (cp instanceof ConsultPhrase) {
            ((ConsultPhrase) cp).setChosen(isChosen);
            notifyItemChanged(list.indexOf(cp));
        }
    }

    public void onDownloadError(final FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                final ChatPhrase cp = (ChatPhrase) list.get(i);
                if (ObjectsCompat.equals(cp.getFileDescription(), fileDescription)
                        && getItemViewType(i) == TYPE_IMAGE_FROM_USER
                        || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT) {
                    cp.getFileDescription().setDownloadError(true);
                    notifyItemChanged(i);
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
                if (!cp.getConsultId().equals(consultId)) continue;
                final String oldUrl = cp.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    cp.setAvatarPath(newUrl);
                    notifyItemChanged(list.lastIndexOf(cp));
                }
            }
            if (ci instanceof ConsultConnectionMessage) {
                final ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                if (!ccm.getConsultId().equals(consultId)) continue;
                final String oldUrl = ccm.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    ccm.setAvatarPath(newUrl);
                    notifyItemChanged(list.lastIndexOf(ci));
                }
            }
        }
    }

    public ArrayList<ChatItem> getList() {
        return list;
    }

    /**
     * TODO THREADS-6290:
     * It might be not that obvious what is happening here.
     * Why do we look for an item and then replacing it with itself?
     * Well, look no further than at equals implementation of some ChatItems (i.e. UserPhrase, ConsultPhrase, Survey).
     * They are equal if their ids are equal. So we look for the item with the same id to replace it with the updated version of itself.
     */
    public void updateChatItem(ChatItem chatItem, boolean needsReordering) {
        int i = list.indexOf(chatItem);
        if (i != -1) {
            list.set(i, chatItem);
            if (needsReordering) {
                reorder();
                notifyDataSetChangedOnUi();
            } else {
                notifyItemChangedOnUi(chatItem);
            }
        }
    }

    public void notifyDataSetChangedOnUi() {
        ThreadUtils.runOnUiThread(this::notifyDataSetChanged);
    }

    private void reorder() {
        ChatMessagesOrderer.updateOrder(list);
    }

    private void notifyItemChangedOnUi(final ChatItem chatItem) {
        ThreadUtils.runOnUiThread(() -> {
            int position = list.indexOf(chatItem);
            notifyItemChanged(position);
        });
    }

    private void notifyItemRemoved(final ChatItem chatItem) {
        notifyItemRemoved(list.indexOf(chatItem));
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
        }
        return counter;
    }

    private static long getLastUnreadStamp(final List<ChatItem> listToInsertTo) {
        long lastUnreadStamp = Long.MAX_VALUE;
        for (final ChatItem item : listToInsertTo) {
            if (item instanceof ConsultPhrase) {
                final ConsultPhrase cp = ((ConsultPhrase) item);
                if (!cp.isRead() && cp.getTimeStamp() < lastUnreadStamp) {
                    lastUnreadStamp = cp.getTimeStamp();
                }
            }
        }
        return lastUnreadStamp;
    }

    private static void removeUnreadMessagesTitle(@NonNull final List<ChatItem> list) {
        for (final Iterator<ChatItem> iterator = list.iterator(); iterator.hasNext(); ) {
            final ChatItem item = iterator.next();
            if (item instanceof UnreadMessages) {
                iterator.remove();
                break;
            }
        }
    }

    private static class ChatMessagesOrderer {

        static void addAndOrder(@NonNull final List<ChatItem> listToInsertTo, @NonNull final List<ChatItem> listToAdd) {
            for (int i = 0; i < listToAdd.size(); i++) {
                ChatItem currentItem = listToAdd.get(i);
                if (!listToInsertTo.contains(currentItem)) {
                    addItemInternal(listToInsertTo, currentItem);
                } else {
                    listToInsertTo.set(listToInsertTo.indexOf(currentItem), currentItem);
                }
            }
            updateOrder(listToInsertTo);
        }

        static void updateOrder(@NonNull List<ChatItem> items) {
            Collections.sort(items, (lhs, rhs) -> Long.compare(lhs.getTimeStamp(), rhs.getTimeStamp()));
            if (items.size() == 0) {
                return;
            }
            items.add(0, new DateRow(items.get(0).getTimeStamp() - 2));
            final Calendar currentTimeStamp = Calendar.getInstance();
            final Calendar nextTimeStamp = Calendar.getInstance();
            final List<DateRow> daterows = new ArrayList<>();
            for (final ChatItem ci : items) {
                if (ci instanceof DateRow) {
                    daterows.add((DateRow) ci);
                    continue;
                }
                final int index = items.indexOf(ci);
                if (index == (items.size() - 1)) continue;//removing dups of date rows
                if (ci instanceof ConsultPhrase && items.get(index + 1) instanceof ConsultPhrase) {
                    ((ConsultPhrase) ci).setAvatarVisible(false);
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
            for (final ChatItem ci : items) {
                final int index = items.indexOf(ci);
                if (index == (items.size() - 1))
                    continue;//removing wrong avatar visibility of consult of date rows
                if (ci instanceof ConsultPhrase && items.get(index + 1) instanceof ConsultPhrase) {
                    ((ConsultPhrase) ci).setAvatarVisible(false);
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
            boolean hasUnread = false;
            for (final ChatItem ci : items) {
                if (ci instanceof ConsultPhrase) {
                    if (!((ConsultPhrase) ci).isRead()) {
                        hasUnread = true;
                    }
                }
            }
            if (hasUnread) {
                final long lastUnreadStamp = getLastUnreadStamp(items);
                final int counter = getUnreadCount(items);
                removeUnreadMessagesTitle(items);
                items.add(new UnreadMessages(lastUnreadStamp - 1, counter));
            }
            Collections.sort(items, (lhs, rhs) -> Long.compare(lhs.getTimeStamp(), rhs.getTimeStamp()));
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
            Collections.sort(items, (lhs, rhs) -> Long.compare(lhs.getTimeStamp(), rhs.getTimeStamp()));
            removeAllSpacings(items);
            for (int i = 1; i < items.size(); i++) {
                final ChatItem prev = items.get(i - 1);
                final ChatItem current = items.get(i);
                if (prev instanceof ConsultPhrase && current instanceof ConsultPhrase) {
                    ((ConsultPhrase) prev).setAvatarVisible(false);//setting proper visibility of consult avatars
                    ((ConsultPhrase) current).setAvatarVisible(true);
                }
            }
            insertSpacing(items);
        }

        private static void addItemInternal(final List<ChatItem> listToInsertTo, final ChatItem itemToInsert) {
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
            listToInsertTo.add(itemToInsert);
            final Calendar currentTimeStamp = Calendar.getInstance();
            final Calendar prevTimeStamp = Calendar.getInstance();
            currentTimeStamp.setTimeInMillis(itemToInsert.getTimeStamp());
            final boolean insertingToStart = listToInsertTo.lastIndexOf(itemToInsert) == 0;
            if (!insertingToStart) {//if we are not inserting to the start
                final int prevIndex = listToInsertTo.lastIndexOf(itemToInsert) - 1;
                prevTimeStamp.setTimeInMillis(listToInsertTo.get(prevIndex).getTimeStamp());
                if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) != prevTimeStamp.get(Calendar.DAY_OF_YEAR)) {
                    listToInsertTo.add(new DateRow(itemToInsert.getTimeStamp() - 2));
                }
                if (itemToInsert instanceof ConsultPhrase && listToInsertTo.size() != 1) {
                    final int prev = listToInsertTo.size() - 2;
                    if (listToInsertTo.get(prev) instanceof ConsultPhrase) {
                        ((ConsultPhrase) listToInsertTo.get(prev)).setAvatarVisible(false);
                    }
                }
            }
        }

        private static void insertSpacing(final List<ChatItem> listToInsertTo) {
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
                if (prev instanceof UserPhrase && current instanceof ConsultConnectionMessage) {// spacing between Consult and Consult connected
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && current instanceof UserPhrase) {// spacing between Consult and User phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultConnectionMessage && current instanceof ConsultPhrase) {// spacing between Consult connected and Consult phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultConnectionMessage && current instanceof UserPhrase) {
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultConnectionMessage && current instanceof ConsultConnectionMessage) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && current instanceof ConsultConnectionMessage) {
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

        private static void removeAllSpacings(final List<ChatItem> list) {
            for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                if (iter.next() instanceof Space) {
                    iter.remove();
                }
            }
        }
    }

    public interface AdapterInterface {
        void onFileClick(FileDescription description);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onUserPhraseClick(UserPhrase userPhrase, int position);

        void onConsultAvatarClick(String consultId);

        void onImageClick(ChatPhrase chatPhrase);

        void onImageDownloadRequest(FileDescription fileDescription);

        void onConsultConnectionClick(ConsultConnectionMessage consultConnectionMessage);

        void onRatingClick(@NonNull Survey survey, int rating);

        void onResolveThreadClick(boolean approveResolve);
    }
}
