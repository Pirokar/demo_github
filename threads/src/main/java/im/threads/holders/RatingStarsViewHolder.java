package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.Survey;
import im.threads.utils.PrefUtils;
import im.threads.widget.Rating;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class RatingStarsViewHolder extends BaseHolder {

    private View topSeparator;
    private View bottomSeparator;
    private TextView askForRate;
    private Rating rating;
    private TextView thanksForRate;

    private ChatStyle style;

    public RatingStarsViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_stars, parent, false));

        topSeparator = itemView.findViewById(R.id.top_separator);
        bottomSeparator = itemView.findViewById(R.id.bottom_separator);

        rating = (Rating) itemView.findViewById(R.id.mark);

        askForRate = (TextView) itemView.findViewById(R.id.ask_for_rate);
        thanksForRate = (TextView) itemView.findViewById(R.id.thanks_for_rate);

        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {

            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
                askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
                bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
            }

        }
    }

    public void bind(Survey survey, Rating.CallBackListener callBackListener) {
        int rate = survey.getQuestions().get(0).getRate();
        rating.initRating(itemView.getContext(), rate);
        askForRate.setText(survey.getQuestions().get(0).getText());
        if (!rating.getHasListener()) {
            rating.setListenerClick(true, callBackListener);
        }
        if (rate != 0) {
            thanksForRate.setVisibility(View.VISIBLE);
            bottomSeparator.setVisibility(View.VISIBLE);
        } else {
            thanksForRate.setVisibility(View.GONE);
            bottomSeparator.setVisibility(View.GONE);
        }
    }
}