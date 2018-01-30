package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.Survey;
import im.threads.utils.PrefUtils;

import static im.threads.model.ChatStyle.INVALID;

/**
 * ViewHolder для результатов опроса с рейтингом
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
    private View mBubble;

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
        mBubble = itemView.findViewById(R.id.bubble);

        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.outgoingMessageBubbleColor != INVALID) {
                rateStarsCount.setTextColor(getColorInt(style.outgoingMessageBubbleColor));
                mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            }
            else {
                rateStarsCount.setTextColor(getColorInt(R.color.threads_chat_outgoing_message_bubble));
                mBubble.getBackground().setColorFilter(getColorInt(R.color.threads_chat_outgoing_message_bubble), PorterDuff.Mode.SRC_ATOP);

            }
            if (style.outgoingMessageBubbleBackground != INVALID) {
                mBubble.setBackground(ContextCompat.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
            }
            if (style.outgoingMessageTextColor != INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                setTextColorToViews(new TextView[]{mHeader, mTimeStampTextView, from, totalStarsCount}, style.outgoingMessageTextColor);
                star.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor),PorterDuff.Mode.SRC_ATOP);
            }
            else {
                star.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_text),PorterDuff.Mode.SRC_ATOP);
            }

            if (style.optionsSurveySelectedIconResId != INVALID) {
                star.setImageResource(style.optionsSurveySelectedIconResId);
            }
            else {
                star.setImageResource(R.drawable.threads_options_survey_selected);
            }
        }
    }

    public void bind(Survey survey) {
        int rate = survey.getQuestions().get(0).getRate();
        int scale = survey.getQuestions().get(0).getScale();
        rateStarsCount.setText(String.valueOf(rate));
        totalStarsCount.setText(String.valueOf(scale));
        mTimeStampTextView.setText(sdf.format(new Date(survey.getTimeStamp())));
        Drawable d;
        switch (survey.getSentState()) {
            case STATE_WAS_READ:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                if (messageColor != INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                if (messageColor != INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                if (messageColor != INVALID) {
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
