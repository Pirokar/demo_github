package im.threads.holders;

import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.MaskedTransformer;
import im.threads.utils.PrefUtils;

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
    private static ChatStyle style;

    public ImageFromConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_from_consult, parent, false));
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.consult_avatar);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        if (null == style) {
            style = PrefUtils.getIncomingStyle(itemView.getContext());
        }
        if (null != style && style.incomingMessageTextColor!= ChatStyle.INVALID) {
            mTimeStampTextView.setTextColor(ContextCompat.getColor(itemView.getContext(),style.incomingMessageTextColor));
        }
        if (style != null && style.chatHighlightingColor != ChatStyle.INVALID) {
            filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
            filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        }
    }

    public void onBind(String avatarPath
            , FileDescription fileDescription
            , long timestamp
            , View.OnClickListener listener
            , View.OnLongClickListener longListener
            , boolean isDownloadError
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
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        mImage.setImageResource(0);
        if (fileDescription.getFilePath() != null && !isDownloadError) {
            p
                    .load(fileDescription.getFilePath())
                    .fit()
                    .centerCrop()
                    .transform(new MaskedTransformer(itemView.getContext(), MaskedTransformer.TYPE_CONSULT))
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            if (style!=null && style.imagePlaceholder!= ChatStyle.INVALID){
                                mImage.setImageResource(style.imagePlaceholder);
                            }else {
                                mImage.setImageResource(R.drawable.no_image);
                            }
                        }
                    });
        } else if (isDownloadError) {
            if (style!=null && style.imagePlaceholder!= ChatStyle.INVALID){
                mImage.setImageResource(style.imagePlaceholder);
            }else {
                mImage.setImageResource(R.drawable.no_image);
            }
        }
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
        @DrawableRes int resId = R.drawable.blank_avatar_round;
        if (style!=null && style.defaultIncomingMessageAvatar!= ChatStyle.INVALID)resId = style.defaultIncomingMessageAvatar;
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
            if (avatarPath != null) {
                final int finalResId = resId;
                p
                        .load(avatarPath)
                        .fit()
                        .transform(new CircleTransform())
                        .centerInside()
                        .noPlaceholder()
                        .into(mConsultAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso
                                        .with(itemView.getContext())
                                        .load(finalResId)
                                        .fit()
                                        .noPlaceholder()
                                        .transform(new CircleTransform())
                                        .into(mConsultAvatar);
                            }
                        });
            } else {
                p
                        .load(resId)
                        .fit()
                        .noPlaceholder()
                        .transform(new CircleTransform())
                        .centerInside()
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }
    }
}
