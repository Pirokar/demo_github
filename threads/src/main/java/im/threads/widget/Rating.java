package im.threads.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
 * Блок для показа рейтинга и отмечания его
 */
public class Rating extends LinearLayout {

    private static final int DEFAULT_RATING_STARS_COUNT = 3;
    private ChatStyle style;
    private Integer ratingCount;
    private Integer countStars;
    private Context context;
    Boolean hasListener = false;
    private ArrayList<View> viewsStar;

    public Rating(Context context) {
        super(context);
    }

    public Rating(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initRating(Context context, Integer ratingCount) {
        this.context = context;
        this.ratingCount = ratingCount;
        style = PrefUtils.getIncomingStyle(context);

        if (style != null && style.ratingStarsCount != ChatStyle.INVALID) {
            countStars = style.ratingStarsCount;
        } else {
            countStars = DEFAULT_RATING_STARS_COUNT;
        }

        LayoutInflater inflater = LayoutInflater.from(context);

        removeAllViews();                   // Чтобы при повторном ините не было в 2 раза больше звезд
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
    public void setListenerClick(Boolean listener, CallBackListener callBackListener) {
        this.hasListener = listener;
        if (listener) {
            clickStarts(callBackListener);
        } else {
            noClickStats();
        }
    }

    /**
     * клик по звезде задает рейтинг
     */
    public void clickStarts(final CallBackListener callBackListener) {
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
    public void noClickStats() {
        for (int i = 0; i < countStars; i++) {
            viewsStar.get(i).setOnClickListener(null);
        }
    }

    /**
     * устанавливаем кратинку в соотвесвие с рейтингом
     */
    public void setImage(View view, Boolean ratingState) {

        ImageView star = (ImageView) view.findViewById(R.id.star);

        if (ratingState) {
            if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_grey600_24dp);
                icon.setColorFilter(ContextCompat.getColor(context, style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
                star.setImageDrawable(icon);
            } else {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_grey600_24dp);
                icon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_ATOP);
                star.setImageDrawable(icon);
            }
        } else if (ratingCount == 0) {
            if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_outline_grey600_24dp);
                icon.setColorFilter(ContextCompat.getColor(context, style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
                star.setImageDrawable(icon);
            } else {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_outline_grey600_24dp);
                icon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_ATOP);
                star.setImageDrawable(icon);
            }
        } else {
            if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_outline_grey600_24dp);
                star.setImageDrawable(icon);
            } else {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_outline_grey600_24dp);
                star.setImageDrawable(icon);
            }
        }

    }

    public int getRating() {
        return ratingCount;
    }

    public interface CallBackListener {
        void onStarClick(int ratingCount);
    }

    public Boolean getHasListener() {
        return hasListener;
    }
}
