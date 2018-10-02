package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.Survey;

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

        if (style == null) style = ChatStyle.getInstance();
        rateStarsCount.setTextColor(getColorInt(style.outgoingMessageBubbleColor));
        mBubble.setBackground(ContextCompat.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mHeader, from, totalStarsCount}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        star.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor), PorterDuff.Mode.SRC_ATOP);
        star.setImageResource(style.optionsSurveySelectedIconResId);
    }

    public void bind(Survey survey) {
        int rate = survey.getQuestions().get(0).getRate();
        int scale = survey.getQuestions().get(0).getScale();
        rateStarsCount.setText(String.valueOf(rate));
        totalStarsCount.setText(String.valueOf(scale));
        mTimeStampTextView.setText(sdf.format(new Date(survey.getTimeStamp())));
        mHeader.setText(survey.getQuestions().get(0).getText());
        Drawable d;
        switch (survey.getSentState()) {
            case STATE_WAS_READ:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = ContextCompat.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }
}
