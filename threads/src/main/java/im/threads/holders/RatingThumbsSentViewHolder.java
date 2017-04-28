package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.adapters.ChatAdapter;
import im.threads.model.ChatStyle;
import im.threads.model.MessageState;
import im.threads.model.RatingThumbs;
import im.threads.utils.PrefUtils;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class RatingThumbsSentViewHolder extends BaseHolder {

    private ImageView thumb;
    private TextView mHeader;
    private TextView mTimeStampTextView;
    private SimpleDateFormat sdf;
    private ChatStyle style;
    private ImageView mBubble;

    private static
    @ColorInt
    int messageColor;

    public RatingThumbsSentViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_thumbs_sent, parent, false));

        thumb = (ImageView) itemView.findViewById(R.id.thumb);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mHeader = (TextView) itemView.findViewById(R.id.header);
        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        mBubble = (ImageView) itemView.findViewById(R.id.bubble);

        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.outgoingMessageBubbleColor != ChatStyle.INVALID)
                mBubble.getDrawable().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            if (style.outgoingMessageTextColor != ChatStyle.INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                setTextColorToViews(new TextView[]{mHeader, mTimeStampTextView}, style.outgoingMessageTextColor);
            }
        }

    }

    public void bind(
            RatingThumbs ratingThumbs) {
        if (ratingThumbs.getRating() != null) {
            if (ratingThumbs.getRating()) {
                if (style.outgoingMessageTextColor != ChatStyle.INVALID) {
                    thumb.setImageResource(R.drawable.ic_like_full_36dp);
                    thumb.setColorFilter(
                            ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor),
                            PorterDuff.Mode.SRC_ATOP
                    );
                }
            } else {
                if (style.outgoingMessageTextColor != ChatStyle.INVALID) {
                    thumb.setImageResource(R.drawable.ic_dislike_full_36dp);
                    thumb.setColorFilter(
                            ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor),
                            PorterDuff.Mode.SRC_ATOP
                    );
                }
            }
        }
        mTimeStampTextView.setText(sdf.format(new Date(ratingThumbs.getTimeStamp())));
        Drawable d;
        switch (ratingThumbs.getSentState()) {
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
