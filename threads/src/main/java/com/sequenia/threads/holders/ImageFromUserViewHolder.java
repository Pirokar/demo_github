package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Callback;
import com.sequenia.threads.utils.MaskedTransformer;
import com.sequenia.threads.R;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 30.06.2016.
 */
public class ImageFromUserViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ImageFromUserViewHolde ";
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private View filter;
    private View filterSecond;

    public ImageFromUserViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_imager_from, parent, false));
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
    }

    public void onBind(
            final FileDescription fileDescription
            , long timestamp
            , final View.OnClickListener rowClickListener
            , View.OnLongClickListener longListener
            , boolean isDownloadError
            , boolean isChosen
            , MessageState sentState) {

        final Picasso p = Picasso.with(itemView.getContext());
        mTimeStampTextView.setOnLongClickListener(longListener);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(rowClickListener);
        }
        mImage.setOnLongClickListener(longListener);
        filter.setOnLongClickListener(longListener);
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        mImage.setImageResource(0);
        if (fileDescription.getFilePath() != null && !isDownloadError) {
            p
                    .load(fileDescription.getFilePath())
                    .fit()
                    .centerCrop()
                    .transform(new MaskedTransformer(itemView.getContext(), MaskedTransformer.TYPE_USER))
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            mImage.setImageResource(R.drawable.no_image);
                        }
                    });
        } else if (isDownloadError) {
            mImage.setImageResource(R.drawable.no_image);
        }
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }

        switch (sentState) {
            case STATE_WAS_READ:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_white_18dp, 0);
                break;
            case STATE_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_white_18dp, 0);
                break;
            case STATE_NOT_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cached_white_18dp, 0);
                break;
            case STATE_SENDING:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.empty_space_24dp, 0);
        }
    }
}
