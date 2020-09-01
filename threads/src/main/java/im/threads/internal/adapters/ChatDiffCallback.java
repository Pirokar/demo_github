package im.threads.internal.adapters;

import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import im.threads.internal.model.ChatItem;

public class ChatDiffCallback extends DiffUtil.Callback {

    private final List<ChatItem> oldList;
    private final List<ChatItem> newList;

    public ChatDiffCallback(List<ChatItem> oldList, List<ChatItem> newList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).isTheSameItem(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return ObjectsCompat.equals(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }
}
