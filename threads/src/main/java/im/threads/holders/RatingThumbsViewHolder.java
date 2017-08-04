package im.threads.holders;

import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.adapters.ChatAdapter;
import im.threads.model.ChatStyle;
import im.threads.model.Survey;
import im.threads.utils.PrefUtils;

/**
 * ViewHolder для бинарных опросов
 * Created by chybakut2004 on 17.04.17.
 */

public class RatingThumbsViewHolder extends BaseHolder {

    private View topSeparator;
    private View bottomSeparator;
    private ImageView thumbUp;
    private ImageView thumbDown;
    private TextView askForRate;
    private TextView thanksForRate;

    private ChatStyle style;

    public RatingThumbsViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_thumbs, parent, false));

        topSeparator = itemView.findViewById(R.id.top_separator);
        bottomSeparator = itemView.findViewById(R.id.bottom_separator);

        thumbUp = (ImageView) itemView.findViewById(R.id.thumb_up);
        thumbDown = (ImageView) itemView.findViewById(R.id.thumb_down);

        askForRate = (TextView) itemView.findViewById(R.id.ask_for_rate);
        thanksForRate = (TextView) itemView.findViewById(R.id.thanks_for_rate);

        if (style == null) {
            style = PrefUtils.getIncomingStyle(itemView.getContext());
        }

        if (style != null) {
            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
                askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
            }

            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
                thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void bind(final Survey survey, final ChatAdapter.AdapterInterface adapterInterface) {
        askForRate.setText(survey.getQuestions().get(0).getText());
        if (survey.getQuestions().get(0).getRate() == 1) {
            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                thumbUp.setImageResource(R.drawable.ic_like_full_36dp);
                thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
            }
            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
                thumbDown.setImageResource(R.drawable.ic_dislike_empty_36dp);
                thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId), PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                thumbDown.setImageResource(R.drawable.ic_dislike_full_36dp);
                thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
            }
            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
                thumbUp.setImageResource(R.drawable.ic_like_empty_36dp);
                thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId), PorterDuff.Mode.SRC_ATOP);
            }
        }

        thanksForRate.setVisibility(survey.getQuestions().get(0).hasRate() ? View.VISIBLE : View.GONE);

        thumbUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.onRatingClick(survey, 1);
            }
        });

        thumbDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.onRatingClick(survey, 0);
            }
        });

    }
}
