package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 30.06.2016.
 */
public class ImageFromUserViewHolder extends RecyclerView.ViewHolder {
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private View filter;
    private View filterSecond;

    public ImageFromUserViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_from_user, parent, false));
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
    }

    public void onBind(String imagePath, long timestamp, View.OnClickListener listener, View.OnLongClickListener longListener, boolean isChosen) {
        Picasso p = Picasso.with(itemView.getContext());
        mTimeStampTextView.setOnClickListener(listener);
        mTimeStampTextView.setOnLongClickListener(longListener);
        mImage.setOnClickListener(listener);
        mImage.setOnLongClickListener(longListener);
        filter.setOnClickListener(listener);
        filter.setOnLongClickListener(longListener);
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        p
                .load(imagePath)
                .into(mImage);
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
    }
}
