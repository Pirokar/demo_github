package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.Survey;
import im.threads.widget.Rating;

/**
 * ViewHolder для опросов с рейтингом
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

        if (style == null) style = ChatStyle.getInstance();
        topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
        bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));

        askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyTextColorResId));
        thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyTextColorResId));
    }

    public void bind(Survey survey, Rating.CallBackListener callBackListener) {
        int rate = survey.getQuestions().get(0).getRate();
        int scale = survey.getQuestions().get(0).getScale();
        rating.initRating(itemView.getContext(), rate, scale);
        askForRate.setText(survey.getQuestions().get(0).getText());

        boolean hasRate = survey.getQuestions().get(0).hasRate();
        rating.setListenerClick(hasRate ? null : callBackListener);
        thanksForRate.setVisibility(hasRate ? View.VISIBLE : View.GONE);
    }

}