package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.model.FileDescription;
import im.threads.internal.picasso_url_connection_only.Callback;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.MaskedTransformation;

public final class ImageFromConsultViewHolder extends BaseHolder {

    private TextView mTimeStampTextView;
    private ImageView mImage;
    private MaskedTransformation maskedTransformation;
    private ImageView mConsultAvatar;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private View filter;
    private View filterSecond;
    private ChatStyle style;

    public ImageFromConsultViewHolder(ViewGroup parent, MaskedTransformation maskedTransformation) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_from_consult, parent, false));
        this.maskedTransformation = maskedTransformation;
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mImage = itemView.findViewById(R.id.image);
        mConsultAvatar = itemView.findViewById(R.id.consult_avatar);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        style = Config.instance.getChatStyle();

        filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));

        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
        mTimeStampTextView.setTextColor(getColorInt(style.incomingImageTimeColor));
        mTimeStampTextView.getBackground().setColorFilter(getColorInt(style.incomingImageTimeBackgroundColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(String avatarPath
            , FileDescription fileDescription
            , long timestamp
            , View.OnClickListener listener
            , View.OnLongClickListener longListener
            , boolean isDownloadError
            , boolean isChosen
            , boolean isAvatarVisible) {
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
            Picasso.with(itemView.getContext())
                    .load(new File(fileDescription.getFilePath()))
                    .fit()
                    .centerCrop()
                    .transform(maskedTransformation)
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            mImage.setImageResource(style.imagePlaceholder);
                        }
                    });
        } else if (isDownloadError) {
            mImage.setImageResource(style.imagePlaceholder);
        }
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
        @DrawableRes int resId = style.defaultOperatorAvatar;
        if (isAvatarVisible) {
            float bubbleLeftMarginDp = itemView.getContext().getResources().getDimension(R.dimen.margin_quarter);
            int bubbleLeftMarginPx = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bubbleLeftMarginDp, itemView.getResources().getDisplayMetrics()));
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImage.getLayoutParams();
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mImage.setLayoutParams(lp);
            mConsultAvatar.setVisibility(View.VISIBLE);
            if (avatarPath != null) {
                avatarPath = FileUtils.convertRelativeUrlToAbsolute(avatarPath);
                Picasso.with(itemView.getContext())
                        .load(avatarPath)
                        .error(style.defaultOperatorAvatar)
                        .fit()
                        .transform(new CircleTransformation())
                        .centerInside()
                        .noPlaceholder()
                        .into(mConsultAvatar);
            } else {
                Picasso.with(itemView.getContext())
                        .load(resId)
                        .fit()
                        .noPlaceholder()
                        .transform(new CircleTransformation())
                        .centerInside()
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
            int avatarSizeRes = style.operatorAvatarSize;
            int avatarSizePx = itemView.getContext().getResources().getDimensionPixelSize(avatarSizeRes);
            int bubbleLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);
            int avatarLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImage.getLayoutParams();
            lp.setMargins(avatarSizePx + bubbleLeftMarginPx + avatarLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mImage.setLayoutParams(lp);
        }
    }
}
