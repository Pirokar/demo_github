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
            style = ChatStyle.getInstance();
        }

        topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
        bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));

        askForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyTextColorResId));
        thanksForRate.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyTextColorResId));

        thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
        thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
    }

    public void bind(final Survey survey, final ChatAdapter.AdapterInterface adapterInterface) {
        askForRate.setText(survey.getQuestions().get(0).getText());

        boolean hasRate = survey.getQuestions().get(0).hasRate();

        if (hasRate && survey.getQuestions().get(0).getRate() == 1) {
            thumbUp.setImageResource(style.binarySurveyLikeSelectedIconResId);
            thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                    style.surveySelectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);

        } else {
            thumbUp.setImageResource(style.binarySurveyLikeUnselectedIconResId);
            thumbUp.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                    style.surveyUnselectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        }

        if (hasRate && survey.getQuestions().get(0).getRate() == 0) {
            thumbDown.setImageResource(style.binarySurveyDislikeSelectedIconResId);
            thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                    style.surveySelectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);

        } else {
            thumbDown.setImageResource(style.binarySurveyDislikeUnselectedIconResId);
            thumbDown.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                    style.surveyUnselectedColorFilterResId),
                    PorterDuff.Mode.SRC_ATOP);
        }

        thanksForRate.setVisibility(hasRate ? View.VISIBLE : View.GONE);

        if (!hasRate) {
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
}
