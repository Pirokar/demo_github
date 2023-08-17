package im.threads.ui.adapters;

import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.business.config.BaseConfig;
import im.threads.business.utils.FileUtils;
import im.threads.ui.config.Config;
import im.threads.ui.holders.BottomGalleryImageHolder;
import im.threads.ui.models.BottomGalleryItem;
import im.threads.ui.utils.FileHelper;
import im.threads.business.utils.Balloon;

public final class BottomGalleryAdapter extends RecyclerView.Adapter<BottomGalleryImageHolder> {
    private List<BottomGalleryItem> list;
    private List<Uri> mChosenItems = new ArrayList<>();
    private OnChooseItemsListener mOnChooseItemsListener;
    private Config config = Config.getInstance();

    public BottomGalleryAdapter(List<BottomGalleryItem> list, OnChooseItemsListener listener) {
        this.list = list;
        this.mOnChooseItemsListener = listener;
    }

    @NonNull
    @Override
    public BottomGalleryImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BottomGalleryImageHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final BottomGalleryImageHolder holder, int position) {
        final BottomGalleryItem item = list.get(position);
        holder.onBind(list.get(position), v -> {
            if (!isSendingAllowed(item, holder)) return;
            if (!item.isChosen() &&
                    mChosenItems.size() >= config.getChatStyle().getMaxGalleryImagesCount(BaseConfig.Companion.getInstance().context)) {
                Balloon.show(
                        holder.itemView.getContext(),
                        holder.itemView.getContext().getString(R.string.ecc_achieve_images_count_limit_message)
                );
                return;
            }
            item.setChosen(!item.isChosen());
            notifyItemChanged(holder.getAdapterPosition());
            mChosenItems.clear();
            for (BottomGalleryItem item1 : list) {
                if (item1.isChosen()) {
                    mChosenItems.add(item1.getImagePath());
                }
            }
            if (mOnChooseItemsListener != null) {
                mOnChooseItemsListener.onChosenItems(mChosenItems);
            }
        });
    }

    private boolean isSendingAllowed(@NonNull BottomGalleryItem item,
                                     @NonNull BottomGalleryImageHolder holder) {
        Uri uri = item.getImagePath();
        if (uri != null) {
            if (FileHelper.INSTANCE.isAllowedFileExtension(
                    FileUtils.getExtensionFromMediaStore(BaseConfig.Companion.getInstance().context, uri))
            ) {
                if (FileHelper.INSTANCE.isAllowedFileSize(
                        FileUtils.getFileSizeFromMediaStore(BaseConfig.Companion.getInstance().context, uri))
                ) {
                    return true;
                } else {
                    // Недопустимый размер файла
                    Balloon.show(
                            holder.itemView.getContext(),
                            holder.itemView.getContext().getString(
                                    R.string.ecc_not_allowed_file_size,
                                    FileHelper.INSTANCE.getMaxAllowedFileSize()
                            )
                    );
                    return false;
                }
            } else {
                // Недопустимое расширение файла
                Balloon.show(
                        holder.itemView.getContext(),
                        holder.itemView.getContext().getString(R.string.ecc_not_allowed_file_extension)
                );
                return false;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnChooseItemsListener {
        void onChosenItems(List<Uri> items);
    }
}
