package im.threads.internal.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.threads.R;
import im.threads.databinding.ItemQuickReplyBinding;
import im.threads.internal.model.QuickReply;

public class QuickRepliesAdapter extends RecyclerView.Adapter<QuickRepliesAdapter.QuickReplyVH> {

    private final List<QuickReply> quickReplies;
    private final Consumer<QuickReply> quickReplyConsumer;

    public QuickRepliesAdapter(List<QuickReply> quickReplies, Consumer<QuickReply> quickReplyConsumer) {
        this.quickReplies = quickReplies;
        this.quickReplyConsumer = quickReplyConsumer;
    }

    @NonNull
    @Override
    public QuickReplyVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QuickReplyVH(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_quick_reply, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QuickReplyVH holder, int position) {
        final QuickReply bindQuickReply = quickReplies.get(position);
        holder.bind(bindQuickReply, () -> quickReplyConsumer.accept(bindQuickReply));
    }

    @Override
    public int getItemCount() {
        return quickReplies.size();
    }

    static class QuickReplyVH extends RecyclerView.ViewHolder {

        private final ItemQuickReplyBinding binding;

        QuickReplyVH(ItemQuickReplyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(QuickReply quickReply, Runnable clickRunnable) {
            binding.setQuickReply(quickReply);
            binding.getRoot().setOnClickListener(v -> clickRunnable.run());
            binding.quickReplyBtn.setOnClickListener(v -> clickRunnable.run());
            binding.executePendingBindings();
        }
    }

}
