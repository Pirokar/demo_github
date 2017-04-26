//package im.threads.widget;
//
//import android.content.Context;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.Drawable;
//import android.support.v4.content.ContextCompat;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import java.util.ArrayList;
//
//import im.threads.R;
//import im.threads.model.ChatStyle;
//import im.threads.utils.PrefUtils;
//
//
///**
// * Created by Ringo on 03.05.2015.
// * Блок для показа рейтинга и отмечания его
// */
//public class Rating extends LinearLayout {
//
//    Integer ratingCount;
//    Integer countStars = 5;
//    Context context;
//    Boolean listener = false;
//    ArrayList<View> viewsStar;
//
//    public Rating(Context context) {
//        super(context);
//    }
//
//    public Rating(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public void initRating(Context context, Integer ratingCount, int starResource){
//        this.context = context;
//        this.ratingCount = ratingCount;
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        removeAllViews();                   // Чтобы при повторном ините не было в 2 раза больше звезд
//        viewsStar = new ArrayList<View>();
//
//        for(int i = 0; i < countStars; i++ ){
//            View view = null;
//            if (starResource != 0) {
//                view = inflater.inflate(starResource, this, false);
//            } else {
//                view = inflater.inflate(R.layout.rating_star, this, false);
//            }
//
//            setImage(view, i < ratingCount);
//
//            viewsStar.add(view);
//            addView(view);
//        }
//    }
//
//    /**
//     * Вешаем слушатель на клики
//     * звездочек и реакцию
//     */
//    public void setListenerClick(Boolean listener, CallBackListener callBackListener){
//        this.listener = listener;
//        if(listener){
//            clickStarts(callBackListener);
//        }else{
//            noClickStats();
//        }
//    }
//
//    /**
//     * клик по звезде задает рейтинг
//     */
//    public void clickStarts(final CallBackListener callBackListener){
//        for(int i = 0; i < countStars; i++){
//            final int index = i + 1;
//            viewsStar.get(i).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(Rating.this.isEnabled()) {
//                        for (int j = 0; j < countStars; j++) {
//                            setImage(viewsStar.get(j), j < index);
//                        }
//
//                        ratingCount = index;
//                        callBackListener.onListener();
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * убираем слушатель с рейтинга
//     */
//    public void noClickStats(){
//        for(int i = 0; i < countStars; i++){
//            viewsStar.get(i).setOnClickListener(null);
//        }
//    }
//
//    /**
//     * устанавливаем кратинку в соотвесвие с рейтингом
//     */
//    public void setImage(View view, Boolean ratingState){
//        ChatStyle style = PrefUtils.getIncomingStyle(context);
//
//        ImageView star = (ImageView)view.findViewById(R.id.star);
//
//        if(ratingState){
//            star.setImageDrawable(FitHelper.createColoredDrawable(
//                    context,
//                    R.drawable.ic_star_grey600_24dp,
//                    R.color.fit_primary
//            ));
//        } else {
//
//            if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
//                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_star_outline_grey600_24dp);
//                icon.setColorFilter(ContextCompat.getColor(context, style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
//            } else {
//
//            }
//
//            Drawable icon = context.getResources().getDrawable(drawableId).mutate();
//
//            int color = context.getResources().getColor(colorId);
//            icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//
//            return icon;
//
//
//            star.setImageResource(R.drawable.ic_star_outline_grey600_24dp);
//        }
//    }
//
//    public int getRating(){
//        return ratingCount;
//    }
//
//    public abstract static class CallBackListener{
//        public abstract void onListener();
//    }
//}
