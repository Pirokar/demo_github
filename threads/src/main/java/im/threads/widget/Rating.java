package im.threads.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

/**
 * Контрол для показа и изменения рейтинга
 */
public class Rating extends LinearLayout {

    private ChatStyle style;
    private int ratingCount;
    private Integer countStars;
    private Context context;
    private ArrayList<View> viewsStar;

    public Rating(Context context) {
        super(context);
    }

    public Rating(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initRating(Context context, int ratingCount, int starsCount) {
        this.context = context;
        this.ratingCount = ratingCount;
        style = PrefUtils.getIncomingStyle(context);

        countStars = starsCount;

        LayoutInflater inflater = LayoutInflater.from(context);

        // Чтобы при повторной инициализации не было в 2 раза больше звезд
        removeAllViews();
        viewsStar = new ArrayList<>();

        for (int i = 0; i < countStars; i++) {
            View view = inflater.inflate(R.layout.rating_star, this, false);

            setImage(view, i < ratingCount);

            viewsStar.add(view);
            addView(view);
        }
    }

    /**
     * Вешаем слушатель на клики
     * звездочек и реакцию
     */
    public void setListenerClick(CallBackListener callBackListener) {
        if (callBackListener != null) {
            setClickListeners(callBackListener);
        } else {
            deleteClickListeners();
        }
    }

    /**
     * клик по звезде задает рейтинг
     */
    public void setClickListeners(final CallBackListener callBackListener) {
        for (int i = 0; i < countStars; i++) {
            final int index = i + 1;
            viewsStar.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Rating.this.isEnabled()) {
                        for (int j = 0; j < countStars; j++) {
                            setImage(viewsStar.get(j), j < index);
                        }

                        ratingCount = index;
                        callBackListener.onStarClick(ratingCount);
                    }
                }
            });
        }
    }

    /**
     * убираем слушатель с рейтинга
     */
    public void deleteClickListeners() {
        for (int i = 0; i < countStars; i++) {
            viewsStar.get(i).setOnClickListener(null);
        }
    }

    /**
     * устанавливаем картинку в соответствие с рейтингом
     */
    public void setImage(View view, Boolean ratingState) {

        ImageView star = (ImageView) view.findViewById(R.id.star);

        if (style != null) {
            if (ratingState) {
                if (style.optionsSurveySelectedIconResId != ChatStyle.INVALID) {
                    star.setImageResource(style.optionsSurveySelectedIconResId);
                }
                else {
                    star.setImageResource(R.drawable.threads_options_survey_selected);
                }

                if (style.surveySelectedColorFilterResId != ChatStyle.INVALID) {
                    star.setColorFilter(ContextCompat.getColor(context, style.surveySelectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
                }
                else {
                    star.setColorFilter(ContextCompat.getColor(context, R.color.threads_survey_selected_icon_tint), PorterDuff.Mode.SRC_ATOP);
                }
            } else {
                if (style.optionsSurveyUnselectedIconResId != ChatStyle.INVALID) {
                    star.setImageResource(style.optionsSurveyUnselectedIconResId );
                }
                else {
                    star.setImageResource(R.drawable.threads_options_survey_unselected);
                }

                if (ratingCount == 0 && style.surveyUnselectedColorFilterResId != ChatStyle.INVALID) {
                    star.setColorFilter(ContextCompat.getColor(context, style.surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
                }
                else {
                    star.setColorFilter(ContextCompat.getColor(context, R.color.threads_survey_unselected_icon_tint), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public int getRating() {
        return ratingCount;
    }

    public interface CallBackListener {
        void onStarClick(int ratingCount);
    }
}
