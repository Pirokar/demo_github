package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import im.threads.R;
import im.threads.business.models.Survey;
import im.threads.ui.widget.Rating;

/**
 * ViewHolder для опросов с рейтингом
 */
public final class RatingStarsViewHolder extends BaseHolder {

    private View topSeparator;
    private View bottomSeparator;
    private TextView askForRate;
    private Rating rating;
    private TextView thanksForRate;

    public RatingStarsViewHolder(ViewGroup parent) {
        super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_stars, parent, false),
                null
        );
        topSeparator = itemView.findViewById(R.id.top_separator);
        bottomSeparator = itemView.findViewById(R.id.bottom_separator);
        rating = itemView.findViewById(R.id.mark);
        askForRate = itemView.findViewById(R.id.ask_for_rate);
        thanksForRate = itemView.findViewById(R.id.thanks_for_rate);
        topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().iconsAndSeparatorsColor));
        bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().iconsAndSeparatorsColor));
        askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyTextColorResId));
        thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyTextColorResId));
    }

    public void bind(Survey survey, Rating.CallBackListener callBackListener) {
        int rate = survey.getQuestions().get(0).getRate();
        int scale = survey.getQuestions().get(0).getScale();
        rating.initRating(itemView.getContext(), rate, scale);
        askForRate.setText(survey.getQuestions().get(0).getText());
        boolean hasRate = survey.getQuestions().get(0).hasRate();
        rating.setListenerClick(callBackListener);
        thanksForRate.setVisibility(hasRate ? View.VISIBLE : View.GONE);
    }
}
