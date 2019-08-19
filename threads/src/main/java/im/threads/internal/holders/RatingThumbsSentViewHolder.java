package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.Survey;

/**
 * ViewHolder для результатов бинарного опроса
 */
public final class RatingThumbsSentViewHolder extends BaseHolder {
    private ImageView thumb;
    private TextView mHeader;
    private TextView mTimeStampTextView;
    private SimpleDateFormat sdf;
    private ChatStyle style;
    private View mBubble;

    public RatingThumbsSentViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_thumbs_sent, parent, false));
        thumb = itemView.findViewById(R.id.thumb);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mHeader = itemView.findViewById(R.id.header);
        mBubble = itemView.findViewById(R.id.bubble);
        sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        style = Config.instance.getChatStyle();
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mHeader}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
    }

    public void bind(Survey survey) {
        int rate = survey.getQuestions().get(0).getRate();
        if (rate == 1) {
            thumb.setImageResource(style.binarySurveyLikeSelectedIconResId);
        } else {
            thumb.setImageResource(style.binarySurveyDislikeSelectedIconResId);
        }
        thumb.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor), PorterDuff.Mode.SRC_ATOP);
        mHeader.setText(survey.getQuestions().get(0).getText());
        mTimeStampTextView.setText(sdf.format(new Date(survey.getTimeStamp())));
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
