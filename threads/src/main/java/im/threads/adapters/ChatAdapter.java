package im.threads.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import im.threads.holders.ConsultConnectionMessageViewHolder;
import im.threads.holders.ConsultFileViewHolder;
import im.threads.holders.ConsultIsTypingViewHolderNew;
import im.threads.holders.ConsultPhraseHolder;
import im.threads.holders.DateViewHolder;
import im.threads.holders.EmptyViewHolder;
import im.threads.holders.ImageFromConsultViewHolder;
import im.threads.holders.ImageFromUserViewHolder;
import im.threads.holders.RatingStarsSentViewHolder;
import im.threads.holders.RatingStarsViewHolder;
import im.threads.holders.RatingThumbsSentViewHolder;
import im.threads.holders.RatingThumbsViewHolder;
import im.threads.holders.RequestResolveThreadViewHolder;
import im.threads.holders.ScheduleInfoViewHolder;
import im.threads.holders.SearchingConsultViewHolder;
import im.threads.holders.SpaceViewHolder;
import im.threads.holders.UnreadMessageViewHolder;
import im.threads.holders.UserFileViewHolder;
import im.threads.holders.UserPhraseViewHolder;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultChatPhrase;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.DateRow;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.QuestionDTO;
import im.threads.model.RequestResolveThread;
import im.threads.model.ScheduleInfo;
import im.threads.model.SearchingConsult;
import im.threads.model.Space;
import im.threads.model.Survey;
import im.threads.model.UnreadMessages;
import im.threads.model.UserPhrase;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.ThreadUtils;
import im.threads.widget.Rating;

import static android.text.TextUtils.isEmpty;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

    private boolean isRemovingTyping = false;


    ArrayList<ChatItem> list;
    ArrayList<ChatItem> backupList = new ArrayList<>();
    private final Context ctx;
    private AdapterInterface mAdapterInterface;
    public static final String ACTION_CHANGED = "im.threads.adapters.ACTION_CHANGED";
    private boolean isInSearchMode = false;
    private Handler viewHandler = new Handler(Looper.getMainLooper());

    public ChatAdapter(final ArrayList<ChatItem> list, final Context ctx, final AdapterInterface adapterInterface) {
        this.list = list;
        if (this.list == null) this.list = new ArrayList<>();
        this.ctx = ctx;
        this.mAdapterInterface = adapterInterface;
        final BroadcastReceiver br = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(ctx).registerReceiver(br, new IntentFilter(ACTION_CHANGED));
    }

    public void setAdapterInterface(final AdapterInterface mAdapterInterface) {
        this.mAdapterInterface = mAdapterInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == TYPE_CONSULT_TYPING) return new ConsultIsTypingViewHolderNew(parent);
        if (viewType == TYPE_DATE) return new DateViewHolder(parent);
        if (viewType == TYPE_SEARCHING_CONSULT) return new SearchingConsultViewHolder(parent);
        if (viewType == TYPE_CONSULT_CONNECTED)
            return new ConsultConnectionMessageViewHolder(parent);
        if (viewType == TYPE_CONSULT_PHRASE) return new ConsultPhraseHolder(parent);
        if (viewType == TYPE_USER_PHRASE) return new UserPhraseViewHolder(parent);
        if (viewType == TYPE_FREE_SPACE) return new SpaceViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_CONSULT) return new ImageFromConsultViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_USER) return new ImageFromUserViewHolder(parent);
        if (viewType == TYPE_FILE_FROM_CONSULT) return new ConsultFileViewHolder(parent);
        if (viewType == TYPE_FILE_FROM_USER) return new UserFileViewHolder(parent);
        if (viewType == TYPE_UNREAD_MESSAGES) return new UnreadMessageViewHolder(parent);
        if (viewType == TYPE_SCHEDULE) return new ScheduleInfoViewHolder(parent);
        if (viewType == TYPE_RATING_THUMBS) return new RatingThumbsViewHolder(parent);
        if (viewType == TYPE_RATING_THUMBS_SENT) return new RatingThumbsSentViewHolder(parent);
        if (viewType == TYPE_RATING_STARS) return new RatingStarsViewHolder(parent);
        if (viewType == TYPE_RATING_STARS_SENT) return new RatingStarsSentViewHolder(parent);
        if (viewType == TYPE_REQ_RESOLVE_THREAD) return new RequestResolveThreadViewHolder(parent);
        return new EmptyViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConsultConnectionMessageViewHolder) {
            final ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(position);
            ((ConsultConnectionMessageViewHolder) holder).onBind(
                    cc
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (null != mAdapterInterface) {
                                final ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(holder.getAdapterPosition());
                                if (null != mAdapterInterface)
                                    mAdapterInterface.onConsultConnectionClick(cc);
                            }
                        }
                    });
        }
        if (holder instanceof ConsultPhraseHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            if (mAdapterInterface != null && cp.getFileDescription() != null && cp.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(cp.getFileDescription());
            }

            ((ConsultPhraseHolder) holder)
                    .onBind(cp, cp.getPhrase()
                            , cp.getAvatarPath()
                            , cp.getTimeStamp()
                            , cp.isAvatarVisible()
                            , cp.getQuote()
                            , cp.getFileDescription()
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    if (mAdapterInterface != null) {
                                        final ChatPhrase up = (ChatPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onImageClick(up);
                                    }
                                }
                            }
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                    if (mAdapterInterface != null && cp.getQuote() != null && cp.getQuote().getFileDescription() != null) {
                                        mAdapterInterface.onFileClick(cp.getQuote().getFileDescription());
                                    }
                                    if (mAdapterInterface != null && cp.getFileDescription() != null) {
                                        mAdapterInterface.onFileClick(cp.getFileDescription());
                                    }
                                }
                            }
                            , new View.OnLongClickListener() {

                                @Override
                                public boolean onLongClick(final View v) {
                                    if (mAdapterInterface != null) {
                                        final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onPhraseLongClick(cp, holder.getAdapterPosition());
                                        return true;
                                    }
                                    return false;
                                }
                            }
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    if (null != mAdapterInterface) {
                                        final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onConsultAvatarClick(cp.getConsultId());
                                    }
                                }
                            }
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mAdapterInterface != null) {
                                        final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onOpenGraphClicked(cp.ogUrl, holder.getAdapterPosition());
                                    }
                                }
                            }
                            , cp.isChosen());
        }

        if (holder instanceof UserPhraseViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            if (mAdapterInterface != null && up.getFileDescription() != null && up.getFileDescription().getFilePath() == null) {
                mAdapterInterface.onImageDownloadRequest(up.getFileDescription());
            }

            ((UserPhraseViewHolder) holder).onBind(up,
                    up.getPhrase()
                    , up.getTimeStamp()
                    , up.getSentState()
                    , up.getQuote()
                    , up.getFileDescription()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (mAdapterInterface != null) {
                                final ChatPhrase up = (ChatPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onImageClick(up);
                            }
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null && (up.getFileDescription() != null)) {
                                mAdapterInterface.onFileClick(up.getFileDescription());
                            } else if (mAdapterInterface != null && up.getFileDescription() == null && up.getQuote() != null && up.getQuote().getFileDescription() != null) {
                                mAdapterInterface.onFileClick(up.getQuote().getFileDescription());
                            }
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onUserPhraseClick(up, holder.getAdapterPosition());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            if (mAdapterInterface != null) {
                                final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(up, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mAdapterInterface != null) {
                                final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onOpenGraphClicked(up.ogUrl, holder.getAdapterPosition());
                            }
                        }
                    }
                    , up.isChosen());
        }

        if (holder instanceof DateViewHolder) {
            final DateRow dr = (DateRow) list.get(position);
            ((DateViewHolder) holder).onBind(dr.getDate());
        }
        if (holder instanceof ConsultIsTypingViewHolderNew) {
            final ChatStyle style = ChatStyle.getInstance();

            final ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
            ((ConsultIsTypingViewHolderNew) holder).onBind(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != mAdapterInterface) {
                        final ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
                        mAdapterInterface.onConsultAvatarClick(ct.getConsultId());
                    }
                }
            });
            final String avatarPath = FileUtils.convertRelativeUrlToAbsolute(ctx, ct.getAvatarPath());
            Picasso.with(ctx)
                    .load(avatarPath)
                    .fit()
                    .error(style.defaultOperatorAvatar)
                    .placeholder(style.defaultOperatorAvatar)
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(((ConsultIsTypingViewHolderNew) holder).mConsultAvatar);
        }
        if (holder instanceof SpaceViewHolder) {
            final Space space = (Space) list.get(position);
            final float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics());
            ((SpaceViewHolder) holder).onBind((int) height);
        }
        if (holder instanceof ImageFromConsultViewHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            if (mAdapterInterface != null && cp.getFileDescription() != null && cp.getFileDescription().getFilePath() == null)
                mAdapterInterface.onImageDownloadRequest(cp.getFileDescription());
            ((ImageFromConsultViewHolder) holder).onBind(
                    cp.getAvatarPath()
                    , cp.getFileDescription()
                    , cp.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (mAdapterInterface != null) {
                                final ChatPhrase cp = (ChatPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onImageClick(cp);
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(final View v) {
                            if (mAdapterInterface != null) {
                                final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(cp, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }, cp.getFileDescription().isDownlodadError()
                    , cp.isChosen()
                    , cp.isAvatarVisible());

        }

        if (holder instanceof ImageFromUserViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            if (mAdapterInterface != null && up.getFileDescription() != null && up.getFileDescription().getFilePath() == null)
                mAdapterInterface.onImageDownloadRequest(up.getFileDescription());
            ((ImageFromUserViewHolder) holder).onBind(
                    up.getFileDescription()
                    , up.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (mAdapterInterface != null) {
                                final ChatPhrase up = (ChatPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onImageClick(up);
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(final View v) {
                            if (mAdapterInterface != null) {
                                final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(up, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }
                    , up.getFileDescription().isDownlodadError()
                    , up.isChosen()
                    , up.getSentState());
        }

        if (holder instanceof UserFileViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            ((UserFileViewHolder) holder).onBind(
                    up.getTimeStamp()
                    , up.getFileDescription()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (mAdapterInterface != null) {
                                final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onFileClick(up.getFileDescription());
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onUserPhraseClick((UserPhrase) list.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            if (mAdapterInterface != null) {
                                final UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(up, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }
                    , up.isChosen()
                    , up.getSentState()
            );
        }

        if (holder instanceof ConsultFileViewHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            ((ConsultFileViewHolder) holder).onBind(
                    cp.getTimeStamp()
                    , cp.getFileDescription()
                    , cp.getAvatarPath()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onFileClick(cp.getFileDescription());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            if (mAdapterInterface != null) {
                                final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(cp, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }
                    , cp.isAvatarVisible()
                    , cp.isChosen()
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
                    new Rating.CallBackListener() {
                        @Override
                        public void onStarClick(final int ratingCount) {
                            mAdapterInterface.onRatingClick(survey, ratingCount);
                        }
                    });
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
        if (isEmpty(consultId)) return;
        for (int i = 0; i < getOriginalList().size(); i++) {
            final ChatItem item = getOriginalList().get(i);
            if (item instanceof ConsultChatPhrase) {
                final ConsultChatPhrase p = (ConsultChatPhrase) item;
                if (p.getConsultId().equals(consultId) && isEmpty(newAvatarImageUrl) && !p.hasAvatar())
                    continue;
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

    public boolean isConsultTyping() {
        final ArrayList<ChatItem> list = getOriginalList();
        for (int i = list.size() - 1; i > 0; i++) {
            if (list.get(i) instanceof ConsultTyping) return true;
        }
        return false;
    }

    private void removeConsultIsTyping() {
        final ArrayList<ChatItem> list = getOriginalList();
        for (final ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            final ChatItem cm = iter.next();
            if (cm instanceof ConsultTyping) {
                try {
                    notifyItemRemoved(list.lastIndexOf(cm));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                iter.remove();

            }
        }
    }

    /**
     * Remove close request from the thread history
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
                    e.printStackTrace();
                }
                iter.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Remove survey from the thread history
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
                        e.printStackTrace();
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

    private void addItem(final List<? extends ChatItem> listoInsert, final boolean isBulk, final boolean forceNotSearchMode) {
        final ArrayList<ChatItem> list;
        if (isInSearchMode && !forceNotSearchMode) {
            list = this.backupList;
        } else {
            list = this.list;
        }
        final ArrayList<ChatItem> l2 = new ArrayList<>(listoInsert);
        new ChatMessagesOrderer().addAndOrder(list, l2);
        if (!isBulk && !isInSearchMode) notifyItemInserted(list.size() - 1);
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
            viewHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeConsultIsTyping();
                }
            }, 8000);
        }
        if (items.size() == 1
                && items.get(0) instanceof ConsultPhrase) {
            removeConsultIsTyping();
        }
        ChatMessagesOrderer.addAndOrder(getOriginalList(), items);
        if (!isInSearchMode) notifyDataSetChangedOnUi();
    }

    public int getUnreadCount() {
        final long lastUnreadStamp = getLastUnreadStamp(getOriginalList());
        return getUnreadCount(getOriginalList(), lastUnreadStamp);
    }

    public static int getUnreadCount(final List<ChatItem> listToInsertTo, final long lastUnreadStamp) {
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

    public static long getLastUnreadStamp(final List<ChatItem> listToInsertTo) {
        long lastUnreadStamp = Long.MAX_VALUE;
        for (final Iterator<ChatItem> iter = listToInsertTo.iterator(); iter.hasNext(); ) {
            final ChatItem item = iter.next();
            //if (item instanceof UnreadMessages) iter.remove();
            if (item instanceof ConsultPhrase) {
                final ConsultPhrase cp = ((ConsultPhrase) item);
                if (!cp.isRead() && cp.getTimeStamp() < lastUnreadStamp) {
                    lastUnreadStamp = cp.getTimeStamp();
                }
            }
        }
        return lastUnreadStamp;
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


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(final int position) {
        Object o = null;
        try {
            o = list.get(position);
        } catch (final IndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
        if (o instanceof ConsultPhrase) {
            final ConsultPhrase cp = (ConsultPhrase) o;
            final FileDescription fileDescription = cp.getFileDescription();
            if (isEmpty(cp.getPhrase())
                    && (FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.JPEG
                    || FileUtils.getExtensionFromFileDescription(fileDescription) == FileUtils.PNG)) {
                return TYPE_IMAGE_FROM_CONSULT;
            } else if (isEmpty(cp.getPhrase())
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
            if ((extension == FileUtils.PDF || extension == FileUtils.OTHER_DOC_FORMATS) && isEmpty(up.getPhrase())) {
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
                    if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                        Log.i(TAG, "changeStateOfMessageByProviderId: changing read state");
                    }
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
                    if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                        Log.i(TAG, "changeStateOfMessageByProviderId: changing read state");
                    }
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

   /* public void backupAndClear() {
        backupList = new ArrayList<>(list);
        list.clear();
        notifyDataSetChanged();
        isInSearchMode = true;
    }*/

   /* public void undoClear() {
        isInSearchMode = false;
        list = new ArrayList<>(backupList);
        Collections.sort(list, new Comparator<ChatItem>() {
            @Override
            public int compare(ChatItem lhs, ChatItem rhs) {
                return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
            }
        });
        notifyDataSetChanged();
    }*/

    /* public void swapItems(List<ChatPhrase> list) {
         this.list.clear();
         addItem(list, true, true);
         notifyDataSetChanged();
     }
 */
    public void onDownloadError(final FileDescription fileDescription) {
        final ArrayList<ChatItem> list = getOriginalList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                final ChatPhrase cp = (ChatPhrase) list.get(i);
                if (cp.getFileDescription() != null
                        && cp.getFileDescription().equals(fileDescription)
                        && getItemViewType(i) == TYPE_IMAGE_FROM_USER
                        || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT) {
                    cp.getFileDescription().setDownlodadError(true);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void reorder(ChatItem chatItem) {
        ChatMessagesOrderer.updateOrder(getOriginalList());
    }

    public interface AdapterInterface {
        void onFileClick(FileDescription description);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onUserPhraseClick(UserPhrase userPhrase, int position);

        void onConsultAvatarClick(String consultId);

        void onImageClick(ChatPhrase chatPhrase);

        void onImageDownloadRequest(FileDescription fileDescription);

        void onConsultConnectionClick(ConsultConnectionMessage consultConnectionMessage);

        void onRatingClick(Survey survey, int rating);

        void onResolveThreadClick(boolean approveResolve);

        void onOpenGraphClicked(String ogUrl, int adapterPosition);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver ";
        int last = -1;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int index = intent.getIntExtra(ACTION_CHANGED, -1);
            if (last == index) return;
            last = index;
            notifyItemChanged(last);
        }
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

    public ConsultPhrase getLastConsultPhrase() {
        for (int i = getOriginalList().size() - 1; i > 0; i--) {
            if (getOriginalList().get(i) instanceof ConsultPhrase) {
                return (ConsultPhrase) getOriginalList().get(i);
            }
        }
        return null;
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
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void notifyItemChangedOnUi(final ChatItem chatItem) {
        ThreadUtils.runOnUiThread(new Runnable() {
            final int position = list.indexOf(chatItem);
            @Override
            public void run() {
                notifyItemChanged(position);
            }
        });
    }

    public void notifyItemRemovedOnUi(final ChatItem chatItem) {
        ThreadUtils.runOnUiThread(() -> {
            final int position = list.indexOf(chatItem);
            notifyItemRemoved(position);
        });
    }

    public void notifyItemRemoved(final ChatItem chatItem) {
        viewHandler.post(() -> notifyItemRemoved(list.indexOf(chatItem)));
    }

    public static class ChatMessagesOrderer {

        public static void addAndOrder(final List<ChatItem> listToInsertTo, final List<ChatItem> listToAdd) {
            if (listToInsertTo.containsAll(listToAdd)) return;
            for (int i = 0; i < listToAdd.size(); i++) {
                if (!listToInsertTo.contains(listToAdd.get(i)))
                    addItemInternal(listToInsertTo, listToAdd.get(i));
            }
            updateOrder(listToInsertTo);
        }

        public static void updateOrder(List<ChatItem> items) {

            Collections.sort(items, new Comparator<ChatItem>() {
                @Override
                public int compare(final ChatItem lhs, final ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            if (items.size() == 0) return;
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
                final int prevPos = items.lastIndexOf(sc);
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
            Collections.sort(items, new Comparator<ChatItem>() {
                @Override
                public int compare(final ChatItem lhs, final ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            boolean isWithTyping = false;
            ConsultTyping ct = null;
            for (final ChatItem ci : items) {
                if (ci instanceof ConsultTyping) {
                    isWithTyping = true;
                    ct = (ConsultTyping) ci;
                }
            }
            if (isWithTyping
                    && items.size() != 0
                    && !(items.get(items.size() - 1) instanceof ConsultTyping)) {
                ct.setDate(items.get(items.size() - 1).getTimeStamp() + 1);
            }

            Collections.sort(items, new Comparator<ChatItem>() {
                @Override
                public int compare(final ChatItem lhs, final ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            removeAllSpacings(items);
            for (int i = 0; i < items.size(); i++) {
                if (i == 0) continue;
                if (items.size() == 1) return;
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
                if (last instanceof UserPhrase && prev instanceof ConsultConnectionMessage) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultConnectionMessage && prev instanceof ConsultPhrase) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
                if (last instanceof ConsultTyping && !(prev instanceof Space)) {
                    listToInsertTo.add(i, new Space(8, prev.getTimeStamp() + 1));
                    continue;
                }
            }
        }

        private static void removeAllSpacings(final List<ChatItem> list) {
            for (final Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                if (iter.next() instanceof Space) iter.remove();
            }
        }
    }
}
