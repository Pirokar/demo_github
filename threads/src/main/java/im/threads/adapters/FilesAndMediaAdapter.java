package im.threads.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import im.threads.holders.FileAndMediaViewHolder;
import im.threads.holders.FilesDateStampHolder;
import im.threads.model.DateRow;
import im.threads.model.FileAndMediaItem;
import im.threads.model.FileDescription;
import im.threads.model.MediaAndFileItem;
import im.threads.utils.FileUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesAndMediaAdapter extends RecyclerView.Adapter {
    private static final String TAG = "FilesAndMediaAdapter ";
    private static final int TYPE_DATE_ROW = 1;
    private static final int TYPE_FILE_AND_MEDIA_ROW = 2;
    private OnFileClick mOnFileClick;

    private ArrayList<MediaAndFileItem> list;
    private ArrayList<MediaAndFileItem> backup;

    public FilesAndMediaAdapter(List<FileDescription> list, OnFileClick onFileClick) {
        this.list = new ArrayList<>();
        mOnFileClick = onFileClick;
        if (list.size() == 0) return;
        addItems(list);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_ROW) return new FilesDateStampHolder(parent);
        if (viewType == TYPE_FILE_AND_MEDIA_ROW) return new FileAndMediaViewHolder(parent);
        throw new IllegalStateException("unknown view type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_DATE_ROW) {
            FilesDateStampHolder h = (FilesDateStampHolder) holder;
            h.onBind(list.get(position).getTimeStamp());
        }
        if (getItemViewType(position) == TYPE_FILE_AND_MEDIA_ROW) {
            final FileAndMediaViewHolder h = (FileAndMediaViewHolder) holder;
            FileAndMediaItem item = (FileAndMediaItem) list.get(position);
            h.onBind(item.getFileDescription()
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mOnFileClick) {
                                FileAndMediaItem item = (FileAndMediaItem) list.get(h.getAdapterPosition());
                                mOnFileClick.onFileClick(item.getFileDescription());
                            }
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
        if (list.get(position) instanceof DateRow) return TYPE_DATE_ROW;
        if (list.get(position) instanceof FileAndMediaItem) return TYPE_FILE_AND_MEDIA_ROW;
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

    public interface OnFileClick {
        void onFileClick(FileDescription fileDescription);
    }
}
