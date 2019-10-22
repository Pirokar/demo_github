package im.threads.internal.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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

    private ArrayList<ChatItem> list;
    private ArrayList<ChatItem> backupList = new ArrayList<>();
    private final Context ctx;
    private AdapterInterface mAdapterInterface;
    private boolean isInSearchMode = false;
    private Handler viewHandler = new Handler(Looper.getMainLooper());
    private MaskedTransformation outgoingImageMaskTransformation;
    private MaskedTransformation incomingImageMaskTransformation;

    public ChatAdapter(final ArrayList<ChatItem> list, final Context ctx, final AdapterInterface adapterInterface) {
        this.list = list;
        if (this.list == null) this.list = new ArrayList<>();
        this.ctx = ctx;
        ChatStyle style = Config.instance.getChatStyle();
        this.mAdapterInterface = adapterInterface;
        outgoingImageMaskTransformation = new MaskedTransformation(ctx.getResources().getDrawable(style.outgoingImageBubbleMask));
        incomingImageMaskTransformation = new MaskedTransformation(ctx.getResources().getDrawable(style.incomingImageBubbleMask));
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
                        if (null != mAdapterInterface) {
                            final ConsultConnectionMessage cc1 = (ConsultConnectionMessage) list.get(holder.getAdapterPosition());
                            if (null != mAdapterInterface)
                                mAdapterInterface.onConsultConnectionClick(cc1);
                        }
                    }
            );
        }
        if (holder instanceof ConsultPhraseHolder) {
            final ConsultPhrase consultPhrase = (ConsultPhrase) list.get(position);
            if (mAdapterInterface != null && consultPhrase.getFileDescription() != null && consultPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(consultPhrase.getFileDescription());
            }
            ((ConsultPhraseHolder) holder)
                    .onBind(consultPhrase,
                            consultPhrase.getPhrase(),
                            consultPhrase.getAvatarPath(),
                            consultPhrase.getTimeStamp(),
                            consultPhrase.isAvatarVisible(),
                            consultPhrase.getQuote(),
                            consultPhrase.getFileDescription(),
                            v -> {
                                if (mAdapterInterface != null) {
                                    mAdapterInterface.onImageClick(consultPhrase);
                                }
                            },
                            v -> {
                                if (mAdapterInterface != null && consultPhrase.getQuote() != null && consultPhrase.getQuote().getFileDescription() != null) {
                                    mAdapterInterface.onFileClick(consultPhrase.getQuote().getFileDescription());
                                }
                                if (mAdapterInterface != null && consultPhrase.getFileDescription() != null) {
                                    mAdapterInterface.onFileClick(consultPhrase.getFileDescription());
                                }
                            },
                            v -> {
                                if (mAdapterInterface != null) {
                                    mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                                    return true;
                                }
                                return false;
                            },
                            v -> {
                                if (null != mAdapterInterface) {
                                    mAdapterInterface.onConsultAvatarClick(consultPhrase.getConsultId());
                                }
                            },
                            v -> {
                                if (mAdapterInterface != null) {
                                    mAdapterInterface.onOpenGraphClicked(consultPhrase.ogUrl, holder.getAdapterPosition());
                                }
                            },
                            consultPhrase.isChosen()
                    );
        }
        if (holder instanceof UserPhraseViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            if (mAdapterInterface != null && userPhrase.getFileDescription() != null && userPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(userPhrase.getFileDescription());
            }
            ((UserPhraseViewHolder) holder).onBind(userPhrase,
                    userPhrase.getPhrase(),
                    userPhrase.getTimeStamp(),
                    userPhrase.getSentState(),
                    userPhrase.getQuote(),
                    userPhrase.getFileDescription(),
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onImageClick(userPhrase);
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null && (userPhrase.getFileDescription() != null)) {
                            mAdapterInterface.onFileClick(userPhrase.getFileDescription());
                        } else if (mAdapterInterface != null && userPhrase.getQuote() != null && userPhrase.getQuote().getFileDescription() != null) {
                            mAdapterInterface.onFileClick(userPhrase.getQuote().getFileDescription());
                        }
                    },
                    v -> {

                        if (mAdapterInterface != null) {
                            mAdapterInterface.onUserPhraseClick(userPhrase, holder.getAdapterPosition());
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                            return true;
                        }
                        return false;
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onOpenGraphClicked(userPhrase.ogUrl, holder.getAdapterPosition());
                        }

                    },
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
            ((ConsultIsTypingViewHolderNew) holder).onBind(v -> {
                if (null != mAdapterInterface) {
                    mAdapterInterface.onConsultAvatarClick(consultTyping.getConsultId());
                }
            });
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
            if (mAdapterInterface != null && consultPhrase.getFileDescription() != null && consultPhrase.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(consultPhrase.getFileDescription());
            }
            ((ImageFromConsultViewHolder) holder).onBind(
                    consultPhrase.getAvatarPath(),
                    consultPhrase.getFileDescription(),
                    consultPhrase.getTimeStamp(),
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onImageClick(consultPhrase);
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                            return true;
                        }
                        return false;
                    },
                    consultPhrase.getFileDescription().isDownloadError(),
                    consultPhrase.isChosen(),
                    consultPhrase.isAvatarVisible()
            );

        }
        if (holder instanceof ImageFromUserViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            if (mAdapterInterface != null && userPhrase.getFileDescription() != null && userPhrase.getFileDescription().getFilePath() == null)
                mAdapterInterface.onImageDownloadRequest(userPhrase.getFileDescription());
            ((ImageFromUserViewHolder) holder).onBind(
                    userPhrase.getFileDescription(),
                    userPhrase.getTimeStamp(),
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onImageClick(userPhrase);
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                            return true;
                        }
                        return false;
                    },
                    userPhrase.getFileDescription().isDownloadError(),
                    userPhrase.isChosen(),
                    userPhrase.getSentState()
            );
        }
        if (holder instanceof UserFileViewHolder) {
            final UserPhrase userPhrase = (UserPhrase) list.get(position);
            ((UserFileViewHolder) holder).onBind(
                    userPhrase.getTimeStamp(),
                    userPhrase.getFileDescription(),
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onFileClick(userPhrase.getFileDescription());
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onUserPhraseClick(userPhrase, holder.getAdapterPosition());
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onPhraseLongClick(userPhrase, holder.getAdapterPosition());
                            return true;
                        }
                        return false;
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
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onFileClick(consultPhrase.getFileDescription());
                        }
                    },
                    v -> {
                        if (mAdapterInterface != null) {
                            mAdapterInterface.onPhraseLongClick(consultPhrase, holder.getAdapterPosition());
                            return true;
                        }
                        return false;
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
        final ArrayList<ChatItem> list = getOriginalList();
        for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            if (item instanceof ConsultPhrase) {
                if (!((ConsultPhrase) item).isRead()) ((ConsultPhrase) item).setRead(true);
            }
            if (item instanceof UnreadMessages) {
                iter.remove();
                notifyItemRemoved(item);
            }
        }
    }

    public void setAvatar(final String consultId, String newAvatarImageUrl) {
        if (TextUtils.isEmpty(consultId)) {
            return;
        }
        for (int i = 0; i < getOriginalList().size(); i++) {
            final ChatItem item = getOriginalList().get(i);
            if (item instanceof ConsultChatPhrase) {
                final ConsultChatPhrase p = (ConsultChatPhrase) item;
                if (p.getConsultId().equals(consultId) && TextUtils.isEmpty(newAvatarImageUrl) && !p.hasAvatar()) {
                    continue;
                }
                if (p.getConsultId().equals(consultId)
                        && !p.hasAvatar()) {
                    p.setAvatarPath(newAvatarImageUrl);
                    notifyItemChanged(i);
                    continue;
                }
                if (newAvatarImageUrl == null) newAvatarImageUrl = "";
                if (p.getConsultId().equals(consultId)
                        && (!p.getAvatarPath().equals(newAvatarImageUrl))) {
                    p.setAvatarPath(newAvatarImageUrl);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void removeHighlight() {
        for (int i = 0; i < getOriginalList().size(); i++) {
            if (getOriginalList().get(i) instanceof ChatPhrase &&
                    ((ChatPhrase) getOriginalList().get(i)).isHighlight()) {
                ((ChatPhrase) getOriginalList().get(i)).setHighLighted(false);
                notifyItemChanged(i);
            }
        }
    }

    public int setItemHighlighted(final ChatPhrase chatPhrase) {
        int index = -1;
        for (int i = 0; i < getOriginalList().size(); i++) {
            if (getOriginalList().get(i).equals(chatPhrase)) {
                ((ChatPhrase) getOriginalList().get(i)).setHighLighted(true);
                index = i;
                notifyItemChanged(index);
            }
        }
        return index;
    }

    private void removeConsultIsTyping() {
        final ArrayList<ChatItem> list = getOriginalList();
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
        final ArrayList<ChatItem> list = getOriginalList();
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
        final ArrayList<ChatItem> list = getOriginalList();
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
        final ArrayList<ChatItem> list = getOriginalList();
        boolean containsSearch = false;
        for (final ChatItem ci : list) {
            if (ci instanceof SearchingConsult) containsSearch = true;
        }
        if (containsSearch) return;
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        final SearchingConsult sc = new SearchingConsult(c.getTimeInMillis());
        list.add(sc);
        if (!isInSearchMode) notifyItemInserted(list.lastIndexOf(sc));
    }

    public void removeConsultSearching() {
        final ArrayList<ChatItem> list = getOriginalList();
        for (final Iterator<ChatItem> iter = list.iterator();
             iter.hasNext(); ) {
            final ChatItem ch = iter.next();
            if (ch instanceof SearchingConsult) {
                iter.remove();
            }
        }

    }

    public int getCurrentItemCount() {
        final ArrayList<ChatItem> list = getOriginalList();
        int count = 0;
        for (final ChatItem item : list) {
            if (item instanceof UserPhrase || item instanceof ConsultPhrase || item instanceof ConsultConnectionMessage || item instanceof Survey)
                count++;
        }
        return count;
    }

    public void addItems(final List<ChatItem> items) {
        boolean withTyping = false;
        for (final ChatItem ci : items) {
            if (ci instanceof ConsultTyping) withTyping = true;
        }
        if (withTyping) {
            removeConsultIsTyping();
            viewHandler.removeCallbacksAndMessages(null);
            viewHandler.postDelayed(this::removeConsultIsTyping, 8000);
        }
        if (items.size() == 1
                && items.get(0) instanceof ConsultPhrase) {
            removeConsultIsTyping();
        }
        ChatMessagesOrderer.addAndOrder(getOriginalList(), items);
        if (!isInSearchMode) {
            notifyDataSetChangedOnUi();
        }
    }

    public int getUnreadCount() {
        final long lastUnreadStamp = getLastUnreadStamp(getOriginalList());
        return getUnreadCount(getOriginalList(), lastUnreadStamp);
    }

    public boolean hasSchedule() {
        final List<ChatItem> list = getOriginalList();

        if (list == null) {
            return false;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ScheduleInfo) {
                return true;
            }
        }

        return false;
    }

    public void removeSchedule(final boolean checkSchedule) {
        final List<ChatItem> list = getOriginalList();
        boolean scheduleRemoved = false;

        if (list != null) {
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
        }

        if (scheduleRemoved && !isInSearchMode) {
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
        if (o instanceof ConsultConnectionMessage) return TYPE_CONSULT_CONNECTED;
        if (o instanceof ConsultTyping) return TYPE_CONSULT_TYPING;
        if (o instanceof DateRow) return TYPE_DATE;
        if (o instanceof SearchingConsult) return TYPE_SEARCHING_CONSULT;
        if (o instanceof UserPhrase) {
            final UserPhrase up = (UserPhrase) o;
            if (up.getFileDescription() == null) return TYPE_USER_PHRASE;
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
        if (o instanceof Space) return TYPE_FREE_SPACE;
        if (o instanceof UnreadMessages) return TYPE_UNREAD_MESSAGES;
        if (o instanceof ScheduleInfo) return TYPE_SCHEDULE;
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
        if (o instanceof RequestResolveThread) {
            return TYPE_REQ_RESOLVE_THREAD;
        }
        return super.getItemViewType(position);
    }

    public void changeStateOfSurvey(long sendingId, MessageState sentState) {
        for (final ChatItem cm : getOriginalList()) {
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
        for (final ChatItem cm : getOriginalList()) {
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

    public void setUserPhraseProviderId(final String uuid, final String newProviderId) {
        final ArrayList<ChatItem> list = getOriginalList();
        for (final ChatItem cm : list) {
            if (cm instanceof UserPhrase && ((((UserPhrase) cm).getUuid()).equals(uuid))) {
                ((UserPhrase) cm).setProviderId(newProviderId);
            }
        }
    }

    public void updateProgress(final FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (fileDescription.getFilePath() == null
                    && (getItemViewType(i) == TYPE_IMAGE_FROM_USER
                    || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT) && !isInSearchMode)
                continue;
            if (list.get(i) instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if (cp.getFileDescription() != null && cp.getFileDescription().equals(fileDescription)) {
                    cp.setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(cp));
                } else if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null && cp.getQuote().getFileDescription().equals(fileDescription)) {
                    cp.getQuote().setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(cp));
                }
            } else if (list.get(i) instanceof UserPhrase) {
                final UserPhrase up = (UserPhrase) list.get(i);
                if (up.getFileDescription() != null && up.getFileDescription().equals(fileDescription)) {
                    up.setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(up));
                } else if (up.getQuote() != null && up.getQuote().getFileDescription() != null && up.getQuote().getFileDescription().equals(fileDescription)) {
                    up.getQuote().setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(up));
                }
            }
        }
        if (backupList != null && backupList.size() > 0 && isInSearchMode)
            for (int i = 0; i < backupList.size(); i++) {
                if (fileDescription.getFilePath() == null
                        && (getItemViewType(i) == TYPE_IMAGE_FROM_USER || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT))
                    continue;
                if (backupList.get(i) instanceof ConsultPhrase) {
                    final ConsultPhrase cp = (ConsultPhrase) backupList.get(i);
                    if (cp.getFileDescription() != null && cp.getFileDescription().equals(fileDescription)) {
                        cp.setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(cp));
                    } else if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null && cp.getQuote().getFileDescription().equals(fileDescription)) {
                        cp.getQuote().setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(cp));
                    }
                } else if (backupList.get(i) instanceof UserPhrase) {
                    final UserPhrase up = (UserPhrase) backupList.get(i);

                    if (up.getFileDescription() != null && up.getFileDescription().equals(fileDescription)) {
                        up.setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(up));
                    } else if (up.getQuote() != null && up.getQuote().getFileDescription() != null && up.getQuote().getFileDescription().equals(fileDescription)) {
                        up.getQuote().setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(up));
                    }
                }
            }
    }

    public void setItemChosen(final boolean isChosen, final ChatPhrase cp) {
        if (cp == null) return;
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
        final ArrayList<ChatItem> list = getOriginalList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                final ChatPhrase cp = (ChatPhrase) list.get(i);
                if (cp.getFileDescription() != null
                        && cp.getFileDescription().equals(fileDescription)
                        && getItemViewType(i) == TYPE_IMAGE_FROM_USER
                        || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT) {
                    cp.getFileDescription().setDownloadError(true);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void reorder() {
        ChatMessagesOrderer.updateOrder(getOriginalList());
    }

    public void notifyAvatarChanged(final String newUrl, final String consultId) {
        if (newUrl == null || consultId == null) return;
        final List<ChatItem> list = getOriginalList();
        for (final ChatItem ci : list) {
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = (ConsultPhrase) ci;
                if (!cp.getConsultId().equals(consultId)) continue;
                final String oldUrl = cp.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    cp.setAvatarPath(newUrl);
                    if (!isInSearchMode) notifyItemChanged(list.lastIndexOf(cp));
                }
            }
            if (ci instanceof ConsultConnectionMessage) {
                final ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                if (!ccm.getConsultId().equals(consultId)) continue;
                final String oldUrl = ccm.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    ccm.setAvatarPath(newUrl);
                    if (!isInSearchMode) notifyItemChanged(list.lastIndexOf(ci));
                }
            }
        }
    }

    public ArrayList<ChatItem> getList() {
        return list;
    }

    public void notifyDataSetChangedOnUi() {
        ThreadUtils.runOnUiThread(this::notifyDataSetChanged);
    }

    public void notifyItemChangedOnUi(final ChatItem chatItem) {
        ThreadUtils.runOnUiThread(() -> {
            int position = list.indexOf(chatItem);
            notifyItemChanged(position);
        });
    }

    private ArrayList<ChatItem> getOriginalList() {
        final ArrayList<ChatItem> list;
        if (isInSearchMode) {
            list = this.backupList;
        } else {
            list = this.list;
        }
        return list;
    }

    private void notifyItemRemoved(final ChatItem chatItem) {
        notifyItemRemoved(list.indexOf(chatItem));
    }

    private static int getUnreadCount(final List<ChatItem> listToInsertTo, final long lastUnreadStamp) {
        int counter = 0;
        for (final ChatItem ci : listToInsertTo) {
            if (ci instanceof ConsultPhrase) {
                final ConsultPhrase cp = ((ConsultPhrase) ci);
                if (cp.getTimeStamp() > (lastUnreadStamp - 1)) {
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

    private static void removeUnreadMessagesTitle(final List<ChatItem> list) {
        if (list != null) {
            for (final Iterator<ChatItem> iterator = list.iterator(); iterator.hasNext(); ) {
                final ChatItem item = iterator.next();
                if (item instanceof UnreadMessages) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static class ChatMessagesOrderer {

        static void addAndOrder(final List<ChatItem> listToInsertTo, final List<ChatItem> listToAdd) {
            if (listToInsertTo.containsAll(listToAdd)) return;
            for (int i = 0; i < listToAdd.size(); i++) {
                if (!listToInsertTo.contains(listToAdd.get(i)))
                    addItemInternal(listToInsertTo, listToAdd.get(i));
            }
            updateOrder(listToInsertTo);
        }

        static void updateOrder(List<ChatItem> items) {
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
                if (ci instanceof SearchingConsult) sc = (SearchingConsult) ci;
            }
            if (sc != null) {
                items.remove(sc);
                items.add(sc);
            }
            boolean hasUnread = false;
            for (final ChatItem ci : items) {
                if (ci instanceof ConsultPhrase) {
                    if (!((ConsultPhrase) ci).isRead()) hasUnread = true;
                }
            }
            if (hasUnread) {
                final long lastUnreadStamp = getLastUnreadStamp(items);
                final int counter = getUnreadCount(items, lastUnreadStamp);
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
            for (int i = 0; i < items.size(); i++) {
                if (i == 0) continue;
                final ChatItem prev = items.get(i - 1);
                final ChatItem current = items.get(i);
                if (prev instanceof ConsultPhrase
                        && current instanceof ConsultPhrase) {
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
                    if (item instanceof ConsultTyping) iter.remove();
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
            if (listToInsertTo.size() < 2) return;
            for (int i = 1; i < listToInsertTo.size(); i++) {
                final ChatItem last = listToInsertTo.get(i);
                final ChatItem prev = listToInsertTo.get(i - 1);
                if (prev instanceof UserPhrase && last instanceof ConsultConnectionMessage) {// spacing between Consult and Consult connected
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultPhrase && last instanceof UserPhrase) {// spacing between Consult and User phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof ConsultConnectionMessage && last instanceof ConsultPhrase) {// spacing between Consult connected and Consult phrase
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultPhrase && prev instanceof ConsultPhrase) {// spacing between Consult phrase  and Consult phrase
                    listToInsertTo.add(i, new Space(0, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof UserPhrase && prev instanceof UserPhrase) {// spacing between User phrase  and User phrase
                    listToInsertTo.add(i, new Space(0, prev.getTimeStamp() + 1));
                    continue;
                }
                if (prev instanceof UserPhrase && last instanceof ConsultPhrase) {// spacing between User phrase  and Consult phrase
                    listToInsertTo.add(i, new Space(24, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof UserPhrase && prev instanceof ConsultConnectionMessage) {
                    listToInsertTo.add(i, new Space(12, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultConnectionMessage && prev instanceof ConsultConnectionMessage) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultConnectionMessage && prev instanceof ConsultPhrase) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultTyping && !(prev instanceof Space)) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                }
            }
        }

        private static void removeAllSpacings(final List<ChatItem> list) {
            for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                if (iter.next() instanceof Space) iter.remove();
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

        void onOpenGraphClicked(String ogUrl, int adapterPosition);
    }
}
