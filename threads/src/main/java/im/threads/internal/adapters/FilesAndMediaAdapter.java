package im.threads.internal.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import im.threads.internal.holders.EmptyViewHolder;
import im.threads.internal.holders.FileAndMediaViewHolder;
import im.threads.internal.holders.FilesDateStampHolder;
import im.threads.internal.model.DateRow;
import im.threads.internal.model.FileAndMediaItem;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MediaAndFileItem;
import im.threads.internal.utils.FileUtils;

/**
 * Created by yuri on 01.07.2016.
 */
public final class FilesAndMediaAdapter extends RecyclerView.Adapter {
    private static final int TYPE_DATE_ROW = 1;
    private static final int TYPE_FILE_AND_MEDIA_ROW = 2;
    private OnFileClick mOnFileClick;

    private ArrayList<MediaAndFileItem> list;
    private ArrayList<MediaAndFileItem> backup;

    public FilesAndMediaAdapter(List<FileDescription> list, OnFileClick onFileClick) {
        this.list = new ArrayList<>();
        mOnFileClick = onFileClick;
        if (list.size() != 0) {
            addItems(list);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DATE_ROW:
                return new FilesDateStampHolder(parent);
            case TYPE_FILE_AND_MEDIA_ROW:
                return new FileAndMediaViewHolder(parent);
            default:
                return new EmptyViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_DATE_ROW) {
            FilesDateStampHolder h = (FilesDateStampHolder) holder;
            h.onBind(list.get(position).getTimeStamp());
        }
        if (getItemViewType(position) == TYPE_FILE_AND_MEDIA_ROW) {
            final FileAndMediaViewHolder h = (FileAndMediaViewHolder) holder;
            FileAndMediaItem item = (FileAndMediaItem) list.get(position);
            h.onBind(
                    item.getFileDescription(),
                    v -> {
                        if (null != mOnFileClick) {
                            FileAndMediaItem item1 = (FileAndMediaItem) list.get(h.getAdapterPosition());
                            mOnFileClick.onFileClick(item1.getFileDescription());
                        }
                    }
            );
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof DateRow) {
            return TYPE_DATE_ROW;
        }
        if (list.get(position) instanceof FileAndMediaItem) {
            return TYPE_FILE_AND_MEDIA_ROW;
        }
        return super.getItemViewType(position);
    }

    public void backupAndClear() {
        backup = new ArrayList<>(list);
        list.clear();
    }

    public void filter(String filter) {
        if (filter == null) filter = "";
        list.clear();
        ArrayList<FileDescription> filteredItems = new ArrayList<>();
        for (MediaAndFileItem maf : backup) {
            if (maf instanceof FileAndMediaItem) {
                FileAndMediaItem fileAndMediaItem = (FileAndMediaItem) maf;
                FileDescription fd = fileAndMediaItem.getFileDescription();
                if (fd != null) {
                    String lastPathSegment = fd.getFileUri() != null ? fd.getFileUri().getLastPathSegment() : null;
                    if (lastPathSegment != null && lastPathSegment.toLowerCase().contains(filter.toLowerCase())) {
                        filteredItems.add(fd);
                    } else if (fd.getIncomingName() != null) {
                        String name = fd.getIncomingName();
                        if (name.toLowerCase().contains(filter.toLowerCase())) {
                            filteredItems.add(fd);
                        }
                    } else if (fileAndMediaItem.getFileName().toLowerCase().contains(filter.toLowerCase())) {
                        filteredItems.add(fd);
                    }
                }
            }
        }
        addItems(filteredItems);
        notifyDataSetChanged();
    }

    public void undoClear() {
        list = new ArrayList<>(backup);
        backup.clear();
        notifyDataSetChanged();
    }

    private void addItems(List<FileDescription> fileDescriptionList) {
        if (fileDescriptionList.size() == 0) return;
        Calendar current = Calendar.getInstance();
        Calendar prev = Calendar.getInstance();
        if (list.size() == 0) {
            FileDescription fd = fileDescriptionList.get(0);
            this.list.add(new DateRow(fd.getTimeStamp()));
            this.list.add(new FileAndMediaItem(fd, fd.getFileUri() != null ? FileUtils.getFileName(fd.getFileUri()) : ""));

        }
        for (int i = 1; i < fileDescriptionList.size(); i++) {
            FileDescription fd = fileDescriptionList.get(i);
            current.setTimeInMillis(fd.getTimeStamp());
            prev.setTimeInMillis(fileDescriptionList.get(i - 1).getTimeStamp());
            this.list.add(new FileAndMediaItem(fd, fd.getFileUri() != null ? FileUtils.getFileName(fd.getFileUri()) : ""));
            if (current.get(Calendar.DAY_OF_YEAR) != prev.get(Calendar.DAY_OF_YEAR)) {
                this.list.add(new DateRow(fd.getTimeStamp()));
            }
        }
    }

    public interface OnFileClick {
        void onFileClick(FileDescription fileDescription);
    }
}
