package im.threads.ui.holders;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import im.threads.R;
import im.threads.business.models.Survey;
import im.threads.ui.adapters.ChatAdapter;

/**
 * ViewHolder для бинарных опросов
 */
public final class RatingThumbsViewHolder extends BaseHolder {
    private ImageView thumbUp;
    private ImageView thumbDown;
    private TextView askForRate;
    private TextView thanksForRate;

    public RatingThumbsViewHolder(ViewGroup parent) {
        super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_rate_thumbs, parent, false),
                null,
                null
        );
        thumbUp = itemView.findViewById(R.id.thumb_up);
        thumbDown = itemView.findViewById(R.id.thumb_down);
        askForRate = itemView.findViewById(R.id.ask_for_rate);
        thanksForRate = itemView.findViewById(R.id.thanks_for_rate);
        askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyTextColorResId));
        thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyTextColorResId));
        thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(), getStyle().surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
        thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(), getStyle().surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
    }

    public void bind(final Survey survey, final ChatAdapter.Callback callback) {
        askForRate.setText(survey.getQuestions().get(0).getText());
        boolean hasRate = survey.getQuestions().get(0).hasRate();
        if (hasRate && survey.getQuestions().get(0).getRate() == 1) {
            thumbUp.setImageResource(getStyle().binarySurveyLikeSelectedIconResId);
            thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            getStyle().surveySelectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        } else {
            thumbUp.setImageResource(getStyle().binarySurveyLikeUnselectedIconResId);
            thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            getStyle().surveyUnselectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (hasRate && survey.getQuestions().get(0).getRate() == 0) {
            thumbDown.setImageResource(getStyle().binarySurveyDislikeSelectedIconResId);
            thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            getStyle().surveySelectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        } else {
            thumbDown.setImageResource(getStyle().binarySurveyDislikeUnselectedIconResId);
            thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            getStyle().surveyUnselectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        }
        thanksForRate.setVisibility(hasRate ? View.VISIBLE : View.GONE);
        if (!hasRate) {
            thumbUp.setOnClickListener(view -> callback.onRatingClick(survey, 1));
            thumbDown.setOnClickListener(view -> callback.onRatingClick(survey, 0));
        }
    }
}
