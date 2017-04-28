package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.MessageState;
import im.threads.model.RatingStars;
import im.threads.model.RatingThumbs;
import im.threads.utils.PrefUtils;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class RatingStarsSentViewHolder extends BaseHolder {

    private ImageView star;
    private TextView mHeader;
    private TextView rateStarsCount;
    private TextView from;
    private TextView totalStarsCount;
    private TextView mTimeStampTextView;
    private SimpleDateFormat sdf;
    private ChatStyle style;
    private ImageView mBubble;

    private static
    @ColorInt
    int messageColor;

    public RatingStarsSentViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_stars_sent, parent, false));

        star = (ImageView) itemView.findViewById(R.id.star);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mHeader = (TextView) itemView.findViewById(R.id.header);
        rateStarsCount = (TextView) itemView.findViewById(R.id.rate_stars_count);
        from = (TextView) itemView.findViewById(R.id.from);
        totalStarsCount = (TextView) itemView.findViewById(R.id.total_stars_count);
        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        mBubble = (ImageView) itemView.findViewById(R.id.bubble);

        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.outgoingMessageBubbleColor != ChatStyle.INVALID) {
                rateStarsCount.setTextColor(getColorInt(style.outgoingMessageBubbleColor));
                mBubble.getDrawable().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            }
            if (style.outgoingMessageTextColor != ChatStyle.INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                setTextColorToViews(new TextView[]{mHeader, mTimeStampTextView, from, totalStarsCount}, style.outgoingMessageTextColor);

                star.setImageResource(R.drawable.ic_star_grey600_24dp);
                star.setColorFilter(
                        ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor),
                        PorterDuff.Mode.SRC_ATOP
                );
            }
        }

    }

    public void bind(
            RatingStars ratingStars) {
        rateStarsCount.setText(String.valueOf(ratingStars.getRating()));
        if (style != null && style.ratingStarsCount != ChatStyle.INVALID) {
            totalStarsCount.setText(style.ratingStarsCount);
        }
        mTimeStampTextView.setText(sdf.format(new Date(ratingStars.getTimeStamp())));
        Drawable d;
        switch (ratingStars.getSentState()) {
            case STATE_WAS_READ:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_done_all_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_done_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_cached_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }
}
