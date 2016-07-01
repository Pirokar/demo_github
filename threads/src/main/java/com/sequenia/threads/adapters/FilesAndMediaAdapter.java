package com.sequenia.threads.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.holders.FileAndMediaViewHolder;
import com.sequenia.threads.holders.FilesDateStampHolder;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.FileAndMediaItem;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MediaAndFileItem;

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

    public FilesAndMediaAdapter(List<FileDescription> list, OnFileClick onFileClick) {
        this.list = new ArrayList<>();
        mOnFileClick = onFileClick;
        if (list.size() == 0) return;
        this.list.add(new DateRow(list.get(0).getTimeStamp()));
        this.list.add(new FileAndMediaItem(list.get(0)));
        Calendar current = Calendar.getInstance();
        Calendar prev = Calendar.getInstance();

        for (int i = 1; i < list.size(); i++) {
            current.setTimeInMillis(list.get(i).getTimeStamp());
            prev.setTimeInMillis(list.get(i - 1).getTimeStamp());
            this.list.add(new FileAndMediaItem(list.get(i)));
            if (current.get(Calendar.DAY_OF_YEAR) != prev.get(Calendar.DAY_OF_YEAR)) {
                this.list.add(new DateRow(list.get(i).getTimeStamp()));
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

    public interface OnFileClick {
        void onFileClick(FileDescription fileDescription);
    }
}
