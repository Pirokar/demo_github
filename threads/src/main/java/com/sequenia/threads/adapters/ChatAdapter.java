package com.sequenia.threads.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.holders.ConsultConnectionMessageViewHolder;
import com.sequenia.threads.holders.ConsultFileViewHolder;
import com.sequenia.threads.holders.ConsultIsTypingViewHolder;
import com.sequenia.threads.holders.ConsultPhraseHolder;
import com.sequenia.threads.holders.DateViewHolder;
import com.sequenia.threads.holders.ImageFromConsultViewHolder;
import com.sequenia.threads.holders.ImageFromUserViewHolder;
import com.sequenia.threads.holders.SearchingConsultViewHolder;
import com.sequenia.threads.holders.SpaceViewHolder;
import com.sequenia.threads.holders.UserFileViewHolder;
import com.sequenia.threads.holders.UserPhraseViewHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.SearchingConsult;
import com.sequenia.threads.model.Space;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.utils.FileUtils;

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
    private static final int TYPE_FILE_FROM_USER = 10;
    private static final int TYPE_FILE_FROM_CONSULT = 11;
    private Calendar prev = Calendar.getInstance();
    private Calendar next = Calendar.getInstance();

    ArrayList<ChatItem> list;
    ArrayList<ChatItem> backupList;
    final Picasso picasso;
    private final Context ctx;
    private AdapterInterface mAdapterInterface;

    public ChatAdapter(ArrayList<ChatItem> list, Context ctx, AdapterInterface adapterInterface) {
        this.list = list;
        if (this.list == null) this.list = new ArrayList<>();
        backupList = new ArrayList<>(this.list);
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
        if (viewType == TYPE_CONSULT_CONNECTED)
            return new ConsultConnectionMessageViewHolder(parent);
        if (viewType == TYPE_CONSULT_PHRASE) return new ConsultPhraseHolder(parent);
        if (viewType == TYPE_USER_PHRASE) return new UserPhraseViewHolder(parent);
        if (viewType == TYPE_FREE_SPACE) return new SpaceViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_CONSULT) return new ImageFromConsultViewHolder(parent);
        if (viewType == TYPE_IMAGE_FROM_USER) return new ImageFromUserViewHolder(parent);
        if (viewType == TYPE_FILE_FROM_CONSULT) return new ConsultFileViewHolder(parent);
        if (viewType == TYPE_FILE_FROM_USER) return new UserFileViewHolder(parent);
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConsultConnectionMessageViewHolder) {
            ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(position);
            ((ConsultConnectionMessageViewHolder) holder).onBind(
                    cc.getName()
                    , cc.getTimeStamp()
                    , cc.getSex()
                    , cc.getType().equals(ConsultConnectionMessage.TYPE_JOINED)
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mAdapterInterface) {
                                ConsultConnectionMessage cc = (ConsultConnectionMessage) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onConsultAvatarClick(cc.getConsultId());
                            }
                        }
                    });
            picasso
                    .load(cc.getAvatarPath())
                    .fit()
                    .centerInside()
                    .transform(new CircleTransform())
                    .into(((ConsultConnectionMessageViewHolder) holder)
                            .mConsultAvatar);
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
                                mAdapterInterface.onFileClick(up.getFileDescription());// TODO: 29.07.2016 implemet download fron quote file description
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
        if (holder instanceof ConsultIsTypingViewHolder) {
            ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
            ((ConsultIsTypingViewHolder) holder).onBind(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mAdapterInterface) {
                        ConsultTyping ct = (ConsultTyping) list.get(holder.getAdapterPosition());
                        mAdapterInterface.onConsultAvatarClick(ct.getConsultId());
                    }
                }
            });
            picasso
                    .load(ct.getAvatarPath())
                    .fit()
                    .transform(new CircleTransform())
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
                    , cp.getFileDescription()
                    , cp.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                final ConsultPhrase cp = (ConsultPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onFileClick(cp.getFileDescription());
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
                    }
                    , cp.isChosen()
                    , cp.isAvatarVisible());
        }

        if (holder instanceof ImageFromUserViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            ((ImageFromUserViewHolder) holder).onBind(
                    up.getFileDescription().getFilePath()
                    , up.getTimeStamp()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onUserPhraseClick(up, holder.getAdapterPosition());
                            }
                        }
                    }
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mAdapterInterface != null) {
                                UserPhrase up = (UserPhrase) list.get(holder.getAdapterPosition());
                                mAdapterInterface.onFileClick(up.getFileDescription());
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
                    , up.getSentState());
        }

        if (holder instanceof UserFileViewHolder) {
            final UserPhrase up = (UserPhrase) list.get(position);
            ((UserFileViewHolder) holder).onBind(
                    up.getTimeStamp()
                    , "1,2 mb"
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

    public void addItem(ChatItem item, boolean isBulk) {
        addItem(item, true, isBulk);
    }

    public void addItem(ChatItem item, boolean withBackup, boolean isBulk) {
        if (list.size() == 0) {
            list.add(new DateRow(item.getTimeStamp()));
            if (withBackup) backupList.add(new DateRow(item.getTimeStamp()));
            if (!isBulk) notifyItemInserted(0);
        }
        Calendar currentTimeStamp = Calendar.getInstance();
        Calendar prevTimeStamp = Calendar.getInstance();
        currentTimeStamp.setTimeInMillis(item.getTimeStamp());
        prevTimeStamp.setTimeInMillis(list.get(list.size() - 1).getTimeStamp());
        if (currentTimeStamp.get(Calendar.DAY_OF_YEAR) != prevTimeStamp.get(Calendar.DAY_OF_YEAR)) {
            this.list.add(new DateRow(item.getTimeStamp()));
            if (withBackup) this.backupList.add(new DateRow(item.getTimeStamp()));
        }
        list.add(item);
        if (withBackup) backupList.add(item);
        if (!isBulk) notifyItemInserted(list.size() - 1);
        if (item instanceof ConsultPhrase && list.size() != 1) {
            int prev = list.size() - 2;
            if (list.get(prev) instanceof ConsultPhrase) {
                ((ConsultPhrase) list.get(prev)).setAvatarVisible(false);
                if (!isBulk) notifyItemChanged(prev);
            }
        }
        if (list.size() < 2) return;
        final ChatItem last = list.get(list.size() - 1);
        final ChatItem prev = list.get(list.size() - 2);
        if (prev instanceof UserPhrase && last instanceof ConsultConnectionMessage) {// spacing between Consult and Consult connected
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (prev instanceof ConsultPhrase && last instanceof UserPhrase) {// spacing between Consult and User phrase
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (prev instanceof ConsultConnectionMessage && last instanceof ConsultPhrase) {// spacing between Consult connected and Consult phrase
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
        if (last instanceof UserPhrase && prev instanceof ConsultConnectionMessage) {
            list.add(list.size() - 1, new Space(12, System.currentTimeMillis()));
        }
        if (last instanceof ConsultConnectionMessage && prev instanceof ConsultConnectionMessage) {
            list.add(list.size() - 1, new Space(8, System.currentTimeMillis()));
        }
        if (last instanceof UserPhrase && prev instanceof ConsultConnectionMessage) {
            list.add(list.size() - 1, new Space(8, System.currentTimeMillis()));
        }
        if (last instanceof ConsultConnectionMessage && prev instanceof ConsultPhrase) {
            list.add(list.size() - 1, new Space(8, System.currentTimeMillis()));
        }
        if (!isBulk) notifyItemInserted(list.size() - 2);
    }


    public void addItems(final List<ChatItem> items) {
        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i), true);
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
            FileDescription fileDescription = cp.getFileDescription();
            if (TextUtils.isEmpty(cp.getPhrase())
                    && (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.JPEG
                    || FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PNG
                    || FileUtils.getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.JPEG
                    || FileUtils.getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.PNG)) {
                return TYPE_IMAGE_FROM_CONSULT;
            } else if (TextUtils.isEmpty(cp.getPhrase())
                    && (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PDF || FileUtils.getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.PDF)) {
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
            String extension = up.getFileDescription() == null ? null : up.getFileDescription().getFilePath().substring(up.getFileDescription().getFilePath().lastIndexOf(".") + 1);
            if (TextUtils.isEmpty(up.getPhrase()) && extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png"))) {
                return TYPE_IMAGE_FROM_USER;
            } else if (TextUtils.isEmpty(up.getPhrase()) && extension != null && (extension.equalsIgnoreCase("pdf"))) {
                return TYPE_FILE_FROM_USER;
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

    public void updateProgress(FileDescription fileDescription) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ConsultPhrase) {
                ConsultPhrase cp = (ConsultPhrase) list.get(i);
                if ((cp.getFileDescription() != null && cp.getFileDescription().equals(fileDescription))) {
                    notifyItemChanged(list.indexOf(cp));
                    continue;
                }
                if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null && cp.getQuote().getFileDescription().equals(fileDescription)) {
                    notifyItemChanged(list.indexOf(cp));
                }
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

    public void filterItems(String query) {
        if (query == null || (query.length() == 0)) {
            list = new ArrayList<>(backupList);
            notifyDataSetChanged();
            return;
        }
        list.clear();
        notifyDataSetChanged();
        for (ChatItem ci : backupList) {
            if (ci instanceof ChatPhrase) {
                if (((ChatPhrase) ci).getPhraseText() != null
                        && ((ChatPhrase) ci).getPhraseText().toLowerCase().contains(query.toLowerCase())
                        && !(ci instanceof ConsultConnectionMessage)) {
                    addItem(ci, false, true);
                    continue;
                }
            }
        }
    }

    public interface AdapterInterface {
        void onFileClick(FileDescription description);

        void onPhraseLongClick(ChatPhrase chatPhrase, int position);

        void onUserPhraseClick(UserPhrase userPhrase, int position);

        void onConsultAvatarClick(String consultId);
    }
}
