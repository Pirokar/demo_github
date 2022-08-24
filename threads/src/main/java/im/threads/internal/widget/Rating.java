package im.threads.internal.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.config.BaseConfig;
import im.threads.ui.Config;

/**
 * Контрол для показа и изменения рейтинга
 */
public final class Rating extends LinearLayout {

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
        style = ((Config)BaseConfig.instance).getChatStyle();
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
            viewsStar.get(i).setOnClickListener(v -> {
                if (Rating.this.isEnabled()) {
                    for (int j = 0; j < countStars; j++) {
                        setImage(viewsStar.get(j), j < index);
                    }
                    ratingCount = index;
                    callBackListener.onStarClick(ratingCount);
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
        ImageView star = view.findViewById(R.id.star);
        if (ratingState) {
            star.setImageResource(style.optionsSurveySelectedIconResId);
            star.setColorFilter(ContextCompat.getColor(context, style.surveySelectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
        } else {
            star.setImageResource(style.optionsSurveyUnselectedIconResId);
            if (ratingCount == 0) {
                star.setColorFilter(ContextCompat.getColor(context, style.surveyUnselectedColorFilterResId), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public interface CallBackListener {
        void onStarClick(int ratingCount);
    }
}
