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

import im.threads.R;
import im.threads.holders.ConsultConnectionMessageViewHolder;
import im.threads.holders.ConsultFileViewHolder;
import im.threads.holders.ConsultIsTypingViewHolderNew;
import im.threads.holders.ConsultPhraseHolder;
import im.threads.holders.DateViewHolder;
import im.threads.holders.ImageFromConsultViewHolder;
import im.threads.holders.ImageFromUserViewHolder;
import im.threads.holders.RatingStarsSentViewHolder;
import im.threads.holders.RatingStarsViewHolder;
import im.threads.holders.RatingThumbsSentViewHolder;
import im.threads.holders.RatingThumbsViewHolder;
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
import im.threads.model.ScheduleInfo;
import im.threads.model.SearchingConsult;
import im.threads.model.Space;
import im.threads.model.Survey;
import im.threads.model.UnreadMessages;
import im.threads.model.UserPhrase;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;
import im.threads.widget.Rating;

import static android.text.TextUtils.isEmpty;

/**

 */
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
    private boolean isRemovingTyping = false;


    ArrayList<ChatItem> list;
    ArrayList<ChatItem> backupList = new ArrayList<>();
    final Picasso picasso;
    private final Context ctx;
    private AdapterInterface mAdapterInterface;
    public static final String ACTION_CHANGED = "im.threads.adapters.ACTION_CHANGED";
    private boolean isInSearchMode = false;
    private Handler typingHandler = new Handler(Looper.getMainLooper());

    public ChatAdapter(ArrayList<ChatItem> list, Context ctx, AdapterInterface adapterInterface) {
        this.list = list;
        if (this.list == null) this.list = new ArrayList<>();
        picasso = Picasso.with(ctx);
        this.ctx = ctx;
        this.mAdapterInterface = adapterInterface;
        BroadcastReceiver br = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(ctx).registerReceiver(br, new IntentFilter(ACTION_CHANGED));
    }

    public void setAdapterInterface(AdapterInterface mAdapterInterface) {
        this.mAdapterInterface = mAdapterInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConsultConnectionMessageViewHolder) {
            ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(position);
            ((ConsultConnectionMessageViewHolder) holder).onBind(
                    cc
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mAdapterInterface) {
                                ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(holder.getAdapterPosition());
                                if (null != mAdapterInterface)
                                    mAdapterInterface.onConsultConnectionClick(cc);
                            }
                        }
                    });
        }
        if (holder instanceof ConsultPhraseHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            ((ConsultPhraseHolder) holder)
                    .onBind(cp.getPhrase()
                            , cp.getAvatarPath()
                            , cp.getTimeStamp()
                            , cp.isAvatarVisible()
                            , cp.getQuote()
                            , cp.getFileDescription()
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
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
                                public boolean onLongClick(View v) {
                                    if (mAdapterInterface != null) {
                                        ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onPhraseLongClick(cp, holder.getAdapterPosition());
                                        return true;
                                    }
                                    return false;
                                }
                            }
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (null != mAdapterInterface) {
                                        ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                        mAdapterInterface.onConsultAvatarClick(cp.getConsultId());
                                    }
                                }
                            }
                            , cp.isChosen());
        }

        if (holder instanceof UserPhraseViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            ((UserPhraseViewHolder) holder).onBind(
                    up.getPhrase()
                    , up.getTimeStamp()
                    , up.getSentState()
                    , up.getQuote()
                    , up.getFileDescription()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null && (up.getFileDescription() != null)) {
                                mAdapterInterface.onFileClick(up.getFileDescription());
                            } else if (mAdapterInterface != null && up.getFileDescription() == null && up.getQuote() != null && up.getQuote().getFileDescription() != null) {
                                mAdapterInterface.onFileClick(up.getQuote().getFileDescription());
                            }
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onUserPhraseClick(up, holder.getAdapterPosition());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onPhraseLongClick(up, holder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        }
                    }
                    , up.isChosen());
        }

        if (holder instanceof DateViewHolder) {
            DateRow dr = (DateRow) list.get(position);
            ((DateViewHolder) holder).onBind(dr.getDate());
        }
        if (holder instanceof ConsultIsTypingViewHolderNew) {
            ChatStyle style = PrefUtils.getIncomingStyle(ctx);
            int defaultImageResId;
            if (style != null && style.imagePlaceholder != ChatStyle.INVALID) {
                defaultImageResId = style.imagePlaceholder;
            } else {
                defaultImageResId = R.drawable.blank_avatar_round;
            }
            ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
            ((ConsultIsTypingViewHolderNew) holder).onBind(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mAdapterInterface) {
                        ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
                        mAdapterInterface.onConsultAvatarClick(ct.getConsultId());
                    }
                }
            });
            if (!isEmpty(ct.getAvatarPath())) {
                final int finalDefaultImageRes = defaultImageResId;
                picasso
                        .load(ct.getAvatarPath())
                        .fit()
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(((ConsultIsTypingViewHolderNew) holder).mConsultImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                picasso
                                        .load(finalDefaultImageRes)
                                        .fit()
                                        .noPlaceholder()
                                        .centerCrop()
                                        .transform(new CircleTransform())
                                        .into(((ConsultIsTypingViewHolderNew) holder).mConsultImageView);
                            }
                        });
            } else {
                picasso
                        .load(defaultImageResId)
                        .fit()
                        .noPlaceholder()
                        .transform(new CircleTransform())
                        .into(((ConsultIsTypingViewHolderNew) holder).mConsultImageView);
            }
        }
        if (holder instanceof SpaceViewHolder) {
            Space space = (Space) list.get(position);
            float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics());
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
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onImageClick(cp);
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
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
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onImageClick(up);
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
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
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onFileClick(up.getFileDescription());
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onUserPhraseClick((UserPhrase) list.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
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
                        public void onClick(View v) {
                            ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onFileClick(cp.getFileDescription());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
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
                        public void onStarClick(int ratingCount) {
                            mAdapterInterface.onRatingClick(survey, ratingCount);
                        }
                    });
        }

        if (holder instanceof RatingStarsSentViewHolder) {
            final Survey survey = (Survey) list.get(holder.getAdapterPosition());
            ((RatingStarsSentViewHolder) holder).bind(survey);
        }

    }


    public void setAllMessagesRead() {
        ArrayList<ChatItem> list = getOriginalList();
        for (Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
            ChatItem item = iter.next();
            if (item instanceof ConsultPhrase) {
                if (!((ConsultPhrase) item).isRead()) ((ConsultPhrase) item).setRead(true);
            }
            if (item instanceof UnreadMessages) {
                iter.remove();
                notifyItemRemoved(list.indexOf(item));
            }
        }
    }

    public void setAvatar(String consultId, String newAvatarImageUrl) {
        if (isEmpty(consultId)) return;
        for (int i = 0; i < getOriginalList().size(); i++) {
            ChatItem item = getOriginalList().get(i);
            if (item instanceof ConsultChatPhrase) {
                ConsultChatPhrase p = (ConsultChatPhrase) item;
                if (p.getConsultId().equals(consultId) && isEmpty(newAvatarImageUrl) && isEmpty(p.getAvatarPath()))
                    continue;
                if (p.getConsultId().equals(consultId)
                        && isEmpty(p.getAvatarPath())) {
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

    public int setItemHighlighted(ChatPhrase chatPhrase) {
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
        ArrayList<ChatItem> list = getOriginalList();
        for (int i = list.size() - 1; i > 0; i++) {
            if (list.get(i) instanceof ConsultTyping) return true;
        }
        return false;
    }

    private void removeConsultIsTyping() {
        ArrayList<ChatItem> list = getOriginalList();
        for (ListIterator<ChatItem> iter = list.listIterator(); iter.hasNext(); ) {
            ChatItem cm = iter.next();
            if (cm instanceof ConsultTyping) {
                try {
                    notifyItemRemoved(list.lastIndexOf(cm));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                iter.remove();

            }
        }
    }

    public void setSearchingConsult() {
        ArrayList<ChatItem> list = getOriginalList();
        boolean containsSearch = false;
        for (ChatItem ci : list) {
            if (ci instanceof SearchingConsult) containsSearch = true;
        }
        if (containsSearch) return;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        SearchingConsult sc = new SearchingConsult(c.getTimeInMillis());
        list.add(sc);
        if (!isInSearchMode) notifyItemInserted(list.lastIndexOf(sc));
    }

    public void removeConsultSearching() {
        ArrayList<ChatItem> list = getOriginalList();
        for (Iterator<ChatItem> iter = list.iterator();
             iter.hasNext(); ) {
            ChatItem ch = iter.next();
            if (ch instanceof SearchingConsult) {
                iter.remove();
            }
        }

    }

    private void addItem(List<? extends ChatItem> listoInsert, boolean isBulk, boolean forceNotSearchMode) {
        ArrayList<ChatItem> list;
        if (isInSearchMode && !forceNotSearchMode) {
            list = this.backupList;
        } else {
            list = this.list;
        }
        ArrayList<ChatItem> l2 = new ArrayList<>(listoInsert);
        new ChatMessagesOrderer().addAndOrder(list, l2);
        if (!isBulk && !isInSearchMode) notifyItemInserted(list.size() - 1);
    }

    public int getCurrentItemCount() {
        ArrayList<ChatItem> list = getOriginalList();
        int count = 0;
        for (ChatItem item : list) {
            if (item instanceof UserPhrase || item instanceof ConsultPhrase || item instanceof ConsultConnectionMessage)
                count++;
        }
        return count;
    }

    public void addItems(final List<ChatItem> items) {
        boolean withTyping = false;
        for (ChatItem ci : items) {
            if (ci instanceof ConsultTyping) withTyping = true;
        }
        if (withTyping) {
            removeConsultIsTyping();
            typingHandler.removeCallbacksAndMessages(null);
            typingHandler.postDelayed(new Runnable() {
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
        new ChatMessagesOrderer().addAndOrder(getOriginalList(), items);
        if (!isInSearchMode) notifyDataSetChanged();
    }

    public boolean hasSchedule() {
        List<ChatItem> list = getOriginalList();

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

    public void removeSchedule(boolean checkSchedule) {
        List<ChatItem> list = getOriginalList();
        boolean scheduleRemoved = false;

        if (list != null) {
            for (Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                ChatItem item = iter.next();
                if (item instanceof ScheduleInfo) {
                    ScheduleInfo scheduleInfo = (ScheduleInfo) item;
                    if (!checkSchedule || scheduleInfo.isChatWorking()) {
                        iter.remove();
                        scheduleRemoved = true;
                    }
                }
            }
        }

        if (scheduleRemoved && !isInSearchMode) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        if (holder instanceof ConsultIsTypingViewHolder) {
//            ((ConsultIsTypingViewHolder) holder).beginTyping();
//        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        if (holder instanceof ConsultIsTypingViewHolder)
//            ((ConsultIsTypingViewHolder) holder).stopTyping();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = null;
        try {
            o = list.get(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
        if (o instanceof ConsultPhrase) {
            ConsultPhrase cp = (ConsultPhrase) o;
            FileDescription fileDescription = cp.getFileDescription();
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
            UserPhrase up = (UserPhrase) o;
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
//        if (o instanceof RatingThumbs) {
//            RatingThumbs ratingThumbs = (RatingThumbs) o;
//            if (ratingThumbs.getSentState() == MessageState.STATE_SENT || ratingThumbs.getSentState() == MessageState.STATE_WAS_READ) {
//                return TYPE_RATING_THUMBS_SENT;
//            } else {
//                return TYPE_RATING_THUMBS;
//            }
//        }
//        if (o instanceof RatingStars) {
//            RatingStars ratingStars = (RatingStars) o;
//            if (ratingStars.getSentState() == MessageState.STATE_SENT || ratingStars.getSentState() == MessageState.STATE_WAS_READ) {
//                return TYPE_RATING_STARS_SENT;
//            } else {
//                return TYPE_RATING_STARS;
//            }
//        }

        if (o instanceof Survey) {
            Survey survey = (Survey) o;
            QuestionDTO questionDTO = survey.getQuestions().get(0);

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

    public void changeStateOfMessage(String id, MessageState state) {
        for (ChatItem cm : getOriginalList()) {
            if (cm instanceof UserPhrase) {
                UserPhrase up = (UserPhrase) cm;
                if (up.getMessageId().equals(id) || (up.getBackendId() != null && up.getBackendId().equals(id))) {
                    Log.i(TAG, "changeStateOfMessage: changing read state");
                    ((UserPhrase) cm).setSentState(state);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setUserPhraseMessageId(String oldId, String newId) {
        ArrayList<ChatItem> list = getOriginalList();
        for (ChatItem cm : list) {
            if (cm instanceof UserPhrase && ((((UserPhrase) cm).getMessageId()).equals(oldId))) {
                ((UserPhrase) cm).setMessageId(newId);
            }
        }
    }


    public void updateProgress(FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (fileDescription.getFilePath() == null
                    && (getItemViewType(i) == TYPE_IMAGE_FROM_USER
                    || getItemViewType(i) == TYPE_IMAGE_FROM_CONSULT) && !isInSearchMode)
                continue;
            if (list.get(i) instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if (cp.getFileDescription() != null && cp.getFileDescription().equals(fileDescription)) {
                    cp.setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(cp));
                } else if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null && cp.getQuote().getFileDescription().equals(fileDescription)) {
                    cp.getQuote().setFileDescription(fileDescription);
                    if (!isInSearchMode) notifyItemChanged(list.indexOf(cp));
                }
            } else if (list.get(i) instanceof UserPhrase) {
                UserPhrase up = (UserPhrase) list.get(i);
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
                    ConsultPhrase cp = (ConsultPhrase) backupList.get(i);
                    if (cp.getFileDescription() != null && cp.getFileDescription().equals(fileDescription)) {
                        cp.setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(cp));
                    } else if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null && cp.getQuote().getFileDescription().equals(fileDescription)) {
                        cp.getQuote().setFileDescription(fileDescription);
                        notifyItemChanged(backupList.indexOf(cp));
                    }
                } else if (backupList.get(i) instanceof UserPhrase) {
                    UserPhrase up = (UserPhrase) backupList.get(i);

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

    public void setItemChosen(boolean isChosen, ChatPhrase cp) {
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
    public void onDownloadError(FileDescription fileDescription) {
        ArrayList<ChatItem> list = getOriginalList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ChatPhrase) {
                ChatPhrase cp = (ChatPhrase) list.get(i);
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

    public interface AdapterInterface {
        void onFileClick(FileDescription description);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onUserPhraseClick(UserPhrase userPhrase, int position);

        void onConsultAvatarClick(String consultId);

        void onImageClick(ChatPhrase chatPhrase);

        void onImageDownloadRequest(FileDescription fileDescription);

        void onConsultConnectionClick(ConsultConnectionMessage consultConnectionMessage);

        void onRatingClick(Survey survey, int rating);

//        void onRatingStarsClick(Survey survey, int rating);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver ";
        int last = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int index = intent.getIntExtra(ACTION_CHANGED, -1);
            if (last == index) return;
            last = index;
            notifyItemChanged(last);
        }
    }

    private ArrayList<ChatItem> getOriginalList() {
        ArrayList<ChatItem> list;
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

    public void notifyAvatarChanged(String newUrl, String consultId) {
        if (newUrl == null || consultId == null) return;
        List<ChatItem> list = getOriginalList();
        for (ChatItem ci : list) {
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) ci;
                if (!cp.getConsultId().equals(consultId)) continue;
                String oldUrl = cp.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    cp.setAvatarPath(newUrl);
                    if (!isInSearchMode) notifyItemChanged(list.lastIndexOf(cp));
                }
            }
            if (ci instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                if (!ccm.getConsultId().equals(consultId)) continue;
                String oldUrl = ccm.getAvatarPath();
                if (oldUrl == null || !oldUrl.equals(newUrl)) {
                    ccm.setAvatarPath(newUrl);
                    if (!isInSearchMode) notifyItemChanged(list.lastIndexOf(ci));
                }
            }
        }
    }

    public static class ChatMessagesOrderer {

        private void addItemInternal(List<ChatItem> listToInsertTo, ChatItem itemToInsert) {
            if (listToInsertTo.size() == 0) {
                listToInsertTo.add(new DateRow(itemToInsert.getTimeStamp() - 2));
            }
            if (itemToInsert instanceof ConsultTyping) {
                for (Iterator<ChatItem> iter = listToInsertTo.listIterator(); iter.hasNext(); ) {
                    ChatItem item = iter.next();
                    if (item instanceof ConsultTyping) iter.remove();
                }
            }
            if (listToInsertTo.contains(itemToInsert)) return;
            listToInsertTo.add(itemToInsert);
            Calendar currentTimeStamp = Calendar.getInstance();
            Calendar prevTimeStamp = Calendar.getInstance();
            currentTimeStamp.setTimeInMillis(itemToInsert.getTimeStamp());
            boolean insertingToStart = listToInsertTo.lastIndexOf(itemToInsert) == 0;
            if (!insertingToStart) {//if we are not inserting to the start
                int prevIndex = listToInsertTo.lastIndexOf(itemToInsert) - 1;
                prevTimeStamp.setTimeInMillis(listToInsertTo.get(prevIndex).getTimeStamp());
                if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) != prevTimeStamp.get(Calendar.DAY_OF_YEAR)) {
                    listToInsertTo.add(new DateRow(itemToInsert.getTimeStamp() - 2));
                }
                if (itemToInsert instanceof ConsultPhrase && listToInsertTo.size() != 1) {
                    int prev = listToInsertTo.size() - 2;
                    if (listToInsertTo.get(prev) instanceof ConsultPhrase) {
                        ((ConsultPhrase) listToInsertTo.get(prev)).setAvatarVisible(false);
                    }
                }
            }

        }

        private void insertSpacing(List<ChatItem> listToInsertTo) {
            if (listToInsertTo.size() < 2) return;
            for (int i = 1; i < listToInsertTo.size(); i++) {
                ChatItem last = listToInsertTo.get(i);
                ChatItem prev = listToInsertTo.get(i - 1);
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

        private void removeAllSpacings(List<ChatItem> list) {
            for (Iterator<ChatItem> iter = list.iterator(); iter.hasNext(); ) {
                if (iter.next() instanceof Space) iter.remove();
            }
        }

        public void addAndOrder(List<ChatItem> listToInsertTo, List<ChatItem> listToAdd) {
            if (listToInsertTo.containsAll(listToAdd)) return;
            for (int i = 0; i < listToAdd.size(); i++) {
                if (!listToInsertTo.contains(listToAdd.get(i)))
                    addItemInternal(listToInsertTo, listToAdd.get(i));
            }
            Collections.sort(listToInsertTo, new Comparator<ChatItem>() {
                @Override
                public int compare(ChatItem lhs, ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            if (listToInsertTo.size() == 0) return;
            listToInsertTo.add(0, new DateRow(listToInsertTo.get(0).getTimeStamp() - 2));
            Calendar currentTimeStamp = Calendar.getInstance();
            Calendar nextTimeStamp = Calendar.getInstance();
            List<DateRow> daterows = new ArrayList<>();
            for (ChatItem ci : listToInsertTo) {
                if (ci instanceof DateRow) {
                    daterows.add((DateRow) ci);
                    continue;
                }
                int index = listToInsertTo.indexOf(ci);
                if (index == (listToInsertTo.size() - 1)) continue;//removing dups of date rows
                if (ci instanceof ConsultPhrase && listToInsertTo.get(index + 1) instanceof ConsultPhrase) {
                    ((ConsultPhrase) ci).setAvatarVisible(false);
                }
            }
            for (int i = 0; i < daterows.size(); i++) {
                if (i == (daterows.size() - 1)) continue;
                currentTimeStamp.setTimeInMillis(daterows.get(i).getTimeStamp());
                nextTimeStamp.setTimeInMillis(daterows.get(i + 1).getTimeStamp());
                if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) == nextTimeStamp.get(Calendar.DAY_OF_YEAR)) {
                    listToInsertTo.remove(daterows.get(i + 1));
                }
            }
            for (ChatItem ci : listToInsertTo) {
                int index = listToInsertTo.indexOf(ci);
                if (index == (listToInsertTo.size() - 1))
                    continue;//removing wrong avatar visibility of consult of date rows
                if (ci instanceof ConsultPhrase && listToInsertTo.get(index + 1) instanceof ConsultPhrase) {
                    ((ConsultPhrase) ci).setAvatarVisible(false);
                }
            }
            SearchingConsult sc = null;
            for (ChatItem ci : listToInsertTo) {
                if (ci instanceof SearchingConsult) sc = (SearchingConsult) ci;
            }
            if (sc != null) {
                int prevPos = listToInsertTo.lastIndexOf(sc);
                listToInsertTo.remove(sc);
                listToInsertTo.add(sc);
            }
            boolean hasUnread = false;
            for (ChatItem ci : listToInsertTo) {
                if (ci instanceof ConsultPhrase) {
                    if (!((ConsultPhrase) ci).isRead()) hasUnread = true;
                }
            }
            if (hasUnread) {
                long lastUnreadStamp = Long.MAX_VALUE;
                int counter = 0;
                for (Iterator<ChatItem> iter = listToInsertTo.iterator(); iter.hasNext(); ) {
                    ChatItem item = iter.next();
                    if (item instanceof UnreadMessages) iter.remove();
                    if (item instanceof ConsultPhrase) {
                        ConsultPhrase cp = ((ConsultPhrase) item);
                        if (!cp.isRead() && cp.getTimeStamp() < lastUnreadStamp) {
                            lastUnreadStamp = cp.getTimeStamp();
                        }
                    }
                }
                for (ChatItem ci : listToInsertTo) {
                    if (ci instanceof ConsultPhrase) {
                        ConsultPhrase cp = ((ConsultPhrase) ci);
                        if (cp.getTimeStamp() > (lastUnreadStamp - 1)) {
                            counter++;
                        }
                    }
                }
                listToInsertTo.add(new UnreadMessages(lastUnreadStamp - 1, counter));
            }
            Collections.sort(listToInsertTo, new Comparator<ChatItem>() {
                @Override
                public int compare(ChatItem lhs, ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            boolean isWithTyping = false;
            ConsultTyping ct = null;
            for (ChatItem ci : listToInsertTo) {
                if (ci instanceof ConsultTyping) {
                    isWithTyping = true;
                    ct = (ConsultTyping) ci;
                }
            }
            if (isWithTyping
                    && listToInsertTo.size() != 0
                    && !(listToInsertTo.get(listToInsertTo.size() - 1) instanceof ConsultTyping)) {
                ct.setDate(listToInsertTo.get(listToInsertTo.size() - 1).getTimeStamp() + 1);
            }

            Collections.sort(listToInsertTo, new Comparator<ChatItem>() {
                @Override
                public int compare(ChatItem lhs, ChatItem rhs) {
                    return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
                }
            });
            removeAllSpacings(listToInsertTo);
            for (int i = 0; i < listToInsertTo.size(); i++) {
                if (i == 0) continue;
                if (listToInsertTo.size() == 1) return;
                ChatItem prev = listToInsertTo.get(i - 1);
                ChatItem current = listToInsertTo.get(i);
                if (prev instanceof ConsultPhrase
                        && current instanceof ConsultPhrase) {
                    ((ConsultPhrase) prev).setAvatarVisible(false);//setting proper visibility of consult avatars
                    ((ConsultPhrase) current).setAvatarVisible(true);
                }
            }

            insertSpacing(listToInsertTo);
        }
    }

    public ArrayList<ChatItem> getList() {
        return list;
    }
}
