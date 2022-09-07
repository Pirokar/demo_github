package im.threads.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.business.config.BaseConfig;
import im.threads.business.utils.FileUtils;
import im.threads.internal.holders.BottomGalleryImageHolder;
import im.threads.ui.config.Config;
import im.threads.ui.models.BottomGalleryItem;
import im.threads.ui.utils.FileHelper;

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
            if (!isSendingAllowed(item, holder.itemView.getContext())) return;
            if (!item.isChosen() &&
                    mChosenItems.size() >= config.getChatStyle().getMaxGalleryImagesCount(BaseConfig.instance.context)) {
                Toast.makeText(BaseConfig.instance.context, BaseConfig.instance.context.getString(R.string.threads_achieve_images_count_limit_message), Toast.LENGTH_SHORT)
                        .show();
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
                                     @NonNull Context context) {
        Uri uri = item.getImagePath();
        if (uri != null) {
            if (FileHelper.INSTANCE.isAllowedFileExtension(
                    FileUtils.getExtensionFromMediaStore(BaseConfig.instance.context, uri))
            ) {
                if (FileHelper.INSTANCE.isAllowedFileSize(
                        FileUtils.getFileSizeFromMediaStore(BaseConfig.instance.context, uri))
                ) {
                    return true;
                } else {
                    // Недопустимый размер файла
                    showToast(context.getString(R.string.threads_not_allowed_file_size,
                            FileHelper.INSTANCE.getMaxAllowedFileSize()), context);
                    return false;
                }
            } else {
                // Недопустимое расширение файла
                showToast(context.getString(R.string.threads_not_allowed_file_extension), context);
                return false;
            }
        }
        return false;
    }

    private void showToast(final String message, @NonNull Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnChooseItemsListener {
        void onChosenItems(List<Uri> items);
    }
}
