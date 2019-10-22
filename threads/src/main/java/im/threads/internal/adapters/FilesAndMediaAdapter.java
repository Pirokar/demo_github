package im.threads.internal.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

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
            h.onBind(item.getFileDescription()
                    , v -> {
                        if (null != mOnFileClick) {
                            FileAndMediaItem item1 = (FileAndMediaItem) list.get(h.getAdapterPosition());
                            mOnFileClick.onFileClick(item1.getFileDescription());
                        }
                    });
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
                if (((FileAndMediaItem) maf).getFileDescription() != null) {
                    FileDescription fd = ((FileAndMediaItem) maf).getFileDescription();
                    if (FileUtils.getLastPathSegment(fd.getFilePath()).toLowerCase().contains(filter.toLowerCase())
                    ) {
                        filteredItems.add(fd);
                    } else if (((FileAndMediaItem) maf).getFileDescription() != null && ((FileAndMediaItem) maf).getFileDescription().getIncomingName() != null) {
                        String name = ((FileAndMediaItem) maf).getFileDescription().getIncomingName();
                        if (name.toLowerCase().contains(filter.toLowerCase()))
                            filteredItems.add(((FileAndMediaItem) maf).getFileDescription());
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

    private void addItems(List<FileDescription> fileDescription) {
        if (fileDescription.size() == 0) return;
        Calendar current = Calendar.getInstance();
        Calendar prev = Calendar.getInstance();
        if (list.size() == 0) {
            this.list.add(new DateRow(fileDescription.get(0).getTimeStamp()));
            this.list.add(new FileAndMediaItem(fileDescription.get(0)));

        }
        for (int i = 1; i < fileDescription.size(); i++) {
            current.setTimeInMillis(fileDescription.get(i).getTimeStamp());
            prev.setTimeInMillis(fileDescription.get(i - 1).getTimeStamp());
            this.list.add(new FileAndMediaItem(fileDescription.get(i)));
            if (current.get(Calendar.DAY_OF_YEAR) != prev.get(Calendar.DAY_OF_YEAR)) {
                this.list.add(new DateRow(fileDescription.get(i).getTimeStamp()));
            }
        }
    }

    public interface OnFileClick {
        void onFileClick(FileDescription fileDescription);
    }
}
