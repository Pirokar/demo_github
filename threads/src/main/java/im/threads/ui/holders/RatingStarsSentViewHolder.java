package im.threads.ui.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.business.models.Survey;

/**
 * ViewHolder для результатов опроса с рейтингом
 */
public final class RatingStarsSentViewHolder extends BaseHolder {

    private ImageView star;
    private TextView mHeader;
    private TextView rateStarsCount;
    private TextView from;
    private TextView totalStarsCount;
    private TextView mTimeStampTextView;
    private SimpleDateFormat sdf;
    private ViewGroup mBubble;

    public RatingStarsSentViewHolder(ViewGroup parent) {
        super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_stars_sent, parent, false),
                null,
                null
        );
        star = itemView.findViewById(R.id.star);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mHeader = itemView.findViewById(R.id.header);
        rateStarsCount = itemView.findViewById(R.id.rate_stars_count);
        from = itemView.findViewById(R.id.from);
        totalStarsCount = itemView.findViewById(R.id.total_stars_count);
        sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        mBubble = itemView.findViewById(R.id.bubble);
        rateStarsCount.setTextColor(getColorInt(getStyle().outgoingMessageBubbleColor));
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), getStyle().outgoingMessageBubbleBackground));

        setPaddings(false, mBubble);
        setLayoutMargins(false, mBubble);
        mBubble.getBackground().setColorFilter(getColorInt(getStyle().outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);

        setTextColorToViews(new TextView[]{mHeader, from, totalStarsCount}, getStyle().outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(getStyle().outgoingMessageTimeColor));
        star.setColorFilter(ContextCompat.getColor(itemView.getContext(), getStyle().outgoingMessageTextColor), PorterDuff.Mode.SRC_ATOP);
        star.setImageResource(getStyle().optionsSurveySelectedIconResId);
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
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }
}
