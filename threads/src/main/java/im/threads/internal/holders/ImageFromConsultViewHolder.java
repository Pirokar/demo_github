package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
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
        style = Config.instance.getChatStyle();
        this.maskedTransformation = maskedTransformation;
        mImage = itemView.findViewById(R.id.image);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mConsultAvatar = itemView.findViewById(R.id.consult_avatar);
        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mTimeStampTextView.setTextColor(getColorInt(style.incomingImageTimeColor));
        mTimeStampTextView.getBackground().setColorFilter(getColorInt(style.incomingImageTimeBackgroundColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(ConsultPhrase consultPhrase, Runnable clickRunnable, Runnable longClickRunnable) {
        FileDescription fileDescription = consultPhrase.getFileDescription();
        mTimeStampTextView.setOnClickListener(v -> clickRunnable.run());
        mTimeStampTextView.setOnLongClickListener(v -> {
            longClickRunnable.run();
            return true;
        });
        mTimeStampTextView.setText(sdf.format(new Date(consultPhrase.getTimeStamp())));
        mConsultAvatar.setOnClickListener(v -> clickRunnable.run());
        mConsultAvatar.setOnLongClickListener(v -> {
            longClickRunnable.run();
            return true;
        });
        filter.setOnClickListener(v -> clickRunnable.run());
        filter.setOnLongClickListener(v -> {
            longClickRunnable.run();
            return true;
        });
        mImage.setOnClickListener(v -> clickRunnable.run());
        mImage.setOnLongClickListener(v -> {
            longClickRunnable.run();
            return true;
        });
        mImage.setImageResource(0);
        if (fileDescription.getFilePath() != null && !fileDescription.isDownloadError()) {
            Picasso.get()
                    .load(new File(fileDescription.getFilePath()))
                    .fit()
                    .centerCrop()
                    .transform(maskedTransformation)
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            mImage.setImageResource(style.imagePlaceholder);
                        }
                    });
        } else if (fileDescription.isDownloadError()) {
            mImage.setImageResource(style.imagePlaceholder);
        }
        if (consultPhrase.isChosen()) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
        @DrawableRes int resId = style.defaultOperatorAvatar;
        String avatarPath = consultPhrase.getAvatarPath();
        if (consultPhrase.isAvatarVisible()) {
            float bubbleLeftMarginDp = itemView.getContext().getResources().getDimension(R.dimen.margin_quarter);
            int bubbleLeftMarginPx = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bubbleLeftMarginDp, itemView.getResources().getDisplayMetrics()));
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImage.getLayoutParams();
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mImage.setLayoutParams(lp);
            mConsultAvatar.setVisibility(View.VISIBLE);
            if (avatarPath != null) {
                Picasso.get()
                        .load(FileUtils.convertRelativeUrlToAbsolute(avatarPath))
                        .error(style.defaultOperatorAvatar)
                        .fit()
                        .transform(new CircleTransformation())
                        .centerInside()
                        .noPlaceholder()
                        .into(mConsultAvatar);
            } else {
                Picasso.get()
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
