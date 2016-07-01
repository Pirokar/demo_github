package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 01.07.2016.
 */
public class ConsultFileViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ConsultFileViewHolder ";
    private CircularProgressButton mCircularProgressButton;
    private TextView mFileHeader;
    private TextView mSizeTextView;
    private TextView mTimeStampTextView;
    private ImageView mConsultAvatar;
    private View mFilterView;
    private View mFilterSecond;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public ConsultFileViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_chat_file, parent, false));
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.circ_button);
        mFileHeader = (TextView) itemView.findViewById(R.id.header);
        mSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterSecond = itemView.findViewById(R.id.filter_second);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.consult_avatar);
    }

    public void onBind(
            long timeStamp
            , String fileSize
            , FileDescription fileDescription
            , String avatarPath
            , View.OnClickListener buttonClickListener
            , View.OnLongClickListener onLongClick
            , boolean isAvatarVisible
            , boolean isFilterVisible) {
        mFileHeader.setText(fileDescription.getPath());
        mSizeTextView.setText(fileSize);
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
        ViewGroup vg = (ViewGroup) itemView;
        Log.e(TAG, "onBIND!!!!!!! progress = " + fileDescription.getDownloadProgress());// TODO: 01.07.2016
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick);
        }
        mCircularProgressButton.setOnClickListener(buttonClickListener);
        Picasso.with(itemView.getContext()).load(avatarPath).fit().into(mConsultAvatar);
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }
        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }
    }
}
