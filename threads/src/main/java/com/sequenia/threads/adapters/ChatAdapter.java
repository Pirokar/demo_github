package com.sequenia.threads.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.holders.ConsultConnectedViewHolder;
import com.sequenia.threads.holders.ConsultIsTypingViewHolder;
import com.sequenia.threads.holders.ConsultPhraseHolder;
import com.sequenia.threads.holders.DateViewHolder;
import com.sequenia.threads.holders.ImageFromConsultViewHolder;
import com.sequenia.threads.holders.ImageFromUserViewHolder;
import com.sequenia.threads.holders.SearchingConsultViewHolder;
import com.sequenia.threads.holders.SpaceViewHolder;
import com.sequenia.threads.holders.UserPhraseViewHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.SearchingConsult;
import com.sequenia.threads.model.Space;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yuri on 09.06.2016.
 * main adapter of chat activity
 * consist of 6 types of rows
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
    private Calendar prev = Calendar.getInstance();
    private Calendar next = Calendar.getInstance();

    ArrayList<ChatItem> list;
    final Picasso picasso;
    private final Context ctx;
    private AdapterInterface mAdapterInterface;

    public ChatAdapter(ArrayList<ChatItem> list, Context ctx, AdapterInterface adapterInterface) {
        this.list = list;
        if (this.list == null) this.list = new ArrayList<>();
        picasso = Picasso.with(ctx);
        this.ctx = ctx;
        this.mAdapterInterface = adapterInterface;
    }

    public void setAdapterInterface(AdapterInterface mAdapterInterface) {
        this.mAdapterInterface = mAdapterInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONSULT_TYPING) return new ConsultIsTypingViewHolder(parent);
        if (viewType == TYPE_DATE) return new DateViewHolder(parent);
        if (viewType == TYPE_SEARCHING_CONSULT) return new SearchingConsultViewHolder(parent);
        if (viewType == TYPE_CONSULT_CONNECTED) return new ConsultConnectedViewHolder(parent);
        if (viewType == TYPE_CONSULT_PHRASE) return new ConsultPhraseHolder(parent);
        if (viewType == TYPE_USER_PHRASE) return new UserPhraseViewHolder(parent);
        if (viewType == TYPE_FREE_SPACE) return new SpaceViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_CONSULT) return new ImageFromConsultViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_USER) return new ImageFromUserViewHolder(parent);
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConsultConnectedViewHolder) {
            ConsultConnected cc = (ConsultConnected) list.get(position);
            ((ConsultConnectedViewHolder) holder).onBind(cc.getName(), cc.getTimeStamp(), cc.getSex());
            picasso
                    .load(cc.getAvatarPath())
                    .fit()
                    .centerInside()
                    .into(((ConsultConnectedViewHolder) holder)
                            .mConsultAvatar);
        }
        if (holder instanceof ConsultPhraseHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            ((ConsultPhraseHolder) holder)
                    .onBind(cp.getPhrase()
                            , cp.getTimeStamp()
                            , cp.isAvatarVisible()
                            , cp.getQuote()
                            , cp.getFileDescription()
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mAdapterInterface != null) {
                                        mAdapterInterface.onFileClick(cp.getFileDescription().getPath());
                                    }
                                }
                            }
                            , new View.OnLongClickListener() {

                                @Override
                                public boolean onLongClick(View v) {
                                    if (mAdapterInterface != null) {
                                        mAdapterInterface.onPhraseLongClick(cp, position);
                                        return true;
                                    }
                                    return false;
                                }
                            }
                            , cp.isChosen());
            picasso.
                    load(cp.getAvatarPath())
                    .fit()
                    .centerInside()
                    .into(((ConsultPhraseHolder) holder)
                            .mConsultAvatar);

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
                            if (mAdapterInterface != null && (up.getFileDescription() != null)) {
                                mAdapterInterface.onFileClick(up.getFileDescription().getPath());
                            }
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onUserPhraseClick(up, position);
                            }
                        }
                    }
                    , new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onPhraseLongClick(up, position);
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
        if (holder instanceof ConsultIsTypingViewHolder) {
            ConsultTyping ct = (ConsultTyping) list.get(position);
            picasso
                    .load(ct.getAvatarPath())
                    .fit()
                    .into(((ConsultIsTypingViewHolder) holder).mConsultImageView);
        }
        if (holder instanceof SpaceViewHolder) {
            Space space = (Space) list.get(position);
            float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space.getHeight(), ctx.getResources().getDisplayMetrics());
            ((SpaceViewHolder) holder).onBind((int) height);
        }
        if (holder instanceof ImageFromConsultViewHolder) {
            final ConsultPhrase cp = (ConsultPhrase) list.get(position);
            ((ImageFromConsultViewHolder) holder).onBind(
                    cp.getAvatarPath()
                    , Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + holder.itemView.getContext().getPackageName() + "/drawable/sample_card").toString()
                    , cp.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onFileClick(cp.getFileDescription().getPath());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onPhraseLongClick(cp, position);
                                return true;
                            }
                            return false;
                        }
                    }
                    , cp.isChosen()
                    , cp.isAvatarVisible());
        }

        if (holder instanceof ImageFromUserViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            ((ImageFromUserViewHolder) holder).onBind(
                    Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + holder.itemView.getContext().getPackageName() + "/drawable/sample_card").toString()
                    , up.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onFileClick(up.getFileDescription().getPath());
                            }
                        }
                    }
                    , new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (mAdapterInterface != null) {
                                mAdapterInterface.onPhraseLongClick(up, position);
                                return true;
                            }
                            return false;
                        }
                    }
                    , up.isChosen());
        }

    }


    public void addConsultTyping(ConsultTyping ct) {
        if (ct != null) {
            list.add(ct);
            notifyItemInserted(list.lastIndexOf(ct));
        }
    }

    public boolean isConsultTyping() {
        boolean isTyping = false;
        for (ChatItem ci : list) {
            if (ci instanceof ConsultTyping) {
                isTyping = true;
            }
        }
        return isTyping;
    }

    public void removeConsultIsTyping() {
        for (ChatItem cm : list) {
            if (cm instanceof ConsultTyping) {
                int i = list.indexOf(cm);
                list.remove(cm);
                notifyItemRemoved(i);
            }
        }
    }

    public void addConsultSearching(SearchingConsult sc) {
        list.add(sc);
        notifyItemInserted(list.lastIndexOf(sc));
    }

    public void removeConsultSearching() {
        for (ChatItem cm : list) {
            if (cm instanceof SearchingConsult) {
                int i = list.indexOf(cm);
                list.remove(cm);
                notifyItemRemoved(i);
            }
        }
    }

    public void addItem(ChatItem item) {
        if (list.size() == 0) {
            list.add(new DateRow(System.currentTimeMillis()));
            notifyItemInserted(0);
        }
        if (!(list.get(list.size() - 1) instanceof DateRow)) {
            prev.setTimeInMillis(list.get(list.size() - 1).getTimeStamp());
            next.setTimeInMillis(System.currentTimeMillis());
            int prevSum = prev.get(Calendar.DAY_OF_MONTH) + prev.get(Calendar.MONTH) + prev.get(Calendar.YEAR);
            int nextSum = next.get(Calendar.DAY_OF_MONTH) + next.get(Calendar.MONTH) + next.get(Calendar.YEAR);
            if (prevSum != nextSum) {
                list.add(new DateRow(System.currentTimeMillis()));
            }
        }
        list.add(item);
        notifyItemInserted(list.lastIndexOf(item));
        if (item instanceof ConsultPhrase && list.size() != 1) {
            int prev = list.size() - 2;
            if (list.get(prev) instanceof ConsultPhrase) {
                ((ConsultPhrase) list.get(prev)).setAvatarVisible(false);
                notifyItemChanged(prev);
            }
        }
        if (list.size() < 2) return;
        final ChatItem last = list.get(list.size() - 1);
        final ChatItem prev = list.get(list.size() - 2);

        if (prev instanceof UserPhrase && last instanceof ConsultConnected) {// spacing between Consult and Consult connected
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (prev instanceof ConsultPhrase && last instanceof UserPhrase) {// spacing between Consult and User phrase
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (prev instanceof ConsultConnected && last instanceof ConsultPhrase) {// spacing between Consult connected and Consult phrase
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (last instanceof ConsultPhrase && prev instanceof ConsultPhrase) {// spacing between Consult phrase connected and Consult phrase
            list.add(list.size() - 1, new Space(2, System.currentTimeMillis()));
        }
        if (last instanceof UserPhrase && prev instanceof UserPhrase) {// spacing between User phrase connected and User phrase
            list.add(list.size() - 1, new Space(2, System.currentTimeMillis()));
        }
        if (prev instanceof UserPhrase && last instanceof ConsultPhrase) {// spacing between User phrase connected and Consult phrase
            list.add(list.size() - 1, new Space(24, System.currentTimeMillis()));
        }
        notifyItemInserted(list.size() - 2);
    }

    public void addItems(List<ChatItem> items) {
        for (ChatItem ci : items) {
            addItem(ci);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = list.get(position);
        if (o instanceof ConsultPhrase) {
            ConsultPhrase cp = (ConsultPhrase) o;
            String extension = cp.getFileDescription() == null ? null : cp.getFileDescription().getPath().substring(cp.getFileDescription().getPath().lastIndexOf(".") + 1);
            if (TextUtils.isEmpty(cp.getPhrase()) && extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png"))) {
                return TYPE_IMAGE_FROM_CONSULT;
            } else {
                return TYPE_CONSULT_PHRASE;
            }
        }
        if (o instanceof ConsultConnected) return TYPE_CONSULT_CONNECTED;
        if (o instanceof ConsultTyping) return TYPE_CONSULT_TYPING;
        if (o instanceof DateRow) return TYPE_DATE;
        if (o instanceof SearchingConsult) return TYPE_SEARCHING_CONSULT;

        if (o instanceof UserPhrase) {
            UserPhrase up = (UserPhrase) o;
            String extension = up.getFileDescription() == null ? null : up.getFileDescription().getPath().substring(up.getFileDescription().getPath().lastIndexOf(".") + 1);
            if (TextUtils.isEmpty(up.getPhrase()) && extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png"))) {
                return TYPE_IMAGE_FROM_USER;
            } else {
                return TYPE_USER_PHRASE;
            }
        }
        if (o instanceof Space) return TYPE_FREE_SPACE;
        return super.getItemViewType(position);
    }

    public void changeStateOfMessage(String id, MessageState state) {
        for (ChatItem cm : list) {
            if (cm instanceof UserPhrase && ((((UserPhrase) cm).getMessageId()).equals(id))) {
                int position = list.lastIndexOf(cm);
                ((UserPhrase) cm).setSentState(state);
                notifyItemChanged(position);
            }
        }
    }

    public void changeDownloadProgress(String messageId, int progress) {
        for (ChatItem cm : list) {
            if (cm instanceof ConsultPhrase && (((ConsultPhrase) cm).getMessageId()).equals(messageId)) {
                int position = list.lastIndexOf(cm);
                ((ConsultPhrase) cm).getFileDescription().setDownloadProgress(progress);
                notifyItemChanged(position);
            }
        }
    }

    public void updateProgress(String path, int progress) {
        if (progress > 100) progress = 100;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if (cp.getFileDescription() != null && cp.getFileDescription().getPath().equals(path)) {
                    cp.getFileDescription().setDownloadProgress(progress);
                }
                notifyItemChanged(i);
            }
        }
    }

    public void clean() {
        list.clear();
        notifyItemRangeRemoved(0, list.size());
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

    public interface AdapterInterface {
        void onFileClick(String path);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onUserPhraseClick(UserPhrase userPhrase, int position);
    }
}
