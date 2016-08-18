package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.NetworkPolicy;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.utils.MaskedTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 30.06.2016.
 */
public class ImageFromConsultViewHolder extends RecyclerView.ViewHolder {
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private ImageView mConsultAvatar;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private View filter;
    private View filterSecond;

    public ImageFromConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_from_consult, parent, false));
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.consult_avatar);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
    }

    public void onBind(String avatarPath
            , FileDescription fileDescription
            , long timestamp
            , View.OnClickListener listener
            , View.OnLongClickListener longListener
            , boolean isChosen
            , boolean isAvatarVisible) {
        Picasso p = Picasso.with(itemView.getContext());
        mTimeStampTextView.setOnClickListener(listener);
        mTimeStampTextView.setOnLongClickListener(longListener);
        mImage.setOnClickListener(listener);
        mImage.setOnLongClickListener(longListener);
        mConsultAvatar.setOnClickListener(listener);
        mConsultAvatar.setOnLongClickListener(longListener);
        filter.setOnClickListener(listener);
        filter.setOnLongClickListener(longListener);
        p
                .load(avatarPath)
                .fit()
                .transform(new CircleTransform())
                .centerInside()
                .into(mConsultAvatar);
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        if (fileDescription.getFilePath() != null) {
            p
                    .load(fileDescription.getFilePath())
                    .fit()
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .transform(new MaskedTransformer(itemView.getContext(), MaskedTransformer.TYPE_CONSULT))
                    .into(mImage);
        } else if (fileDescription.getDownloadPath() != null) {
            p
                    .load(fileDescription.getDownloadPath())
                    .fit()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .transform(new MaskedTransformer(itemView.getContext(), MaskedTransformer.TYPE_CONSULT))
                    .into(mImage);
        }
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }
    }
}
