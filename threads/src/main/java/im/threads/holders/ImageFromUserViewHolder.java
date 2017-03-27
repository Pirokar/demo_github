package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.utils.MaskedTransformer;
import im.threads.R;
import im.threads.model.MessageState;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 30.06.2016.
 */
public class ImageFromUserViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ImageFromUserViewHolde ";
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private View filter;
    private View filterSecond;
    private static ChatStyle style;
    private static
    @ColorInt
    int messageColor;

    public ImageFromUserViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_image_from, parent, false));
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        if (null == style) {
            style = PrefUtils.getIncomingStyle(itemView.getContext());
        }
        if (null != style) {
            if (style.outgoingMessageTextColor != ChatStyle.INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                mTimeStampTextView.setTextColor(messageColor);
            }
            if (style.chatHighlightingColor != ChatStyle.INVALID) {
                filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
                filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
            }
        }
    }

    public void onBind(
            final FileDescription fileDescription
            , long timestamp
            , final View.OnClickListener rowClickListener
            , View.OnLongClickListener longListener
            , boolean isDownloadError
            , boolean isChosen
            , MessageState sentState) {
        final Picasso p = Picasso.with(itemView.getContext());
        mTimeStampTextView.setOnLongClickListener(longListener);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(rowClickListener);
        }
        mImage.setOnLongClickListener(longListener);
        filter.setOnLongClickListener(longListener);
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        mImage.setImageResource(0);
        if (fileDescription.getFilePath() != null && !isDownloadError) {
            p
                    .load(fileDescription.getFilePath())
                    .fit()
                    .centerCrop()
                    .transform(new MaskedTransformer(itemView.getContext(), MaskedTransformer.TYPE_USER))
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            if (style!=null && style.imagePlaceholder!= ChatStyle.INVALID){
                                mImage.setImageResource(style.imagePlaceholder);
                            }else {
                                mImage.setImageResource(R.drawable.no_image);
                            }

                        }
                    });
        } else if (isDownloadError) {
            if (style!=null && style.imagePlaceholder!= ChatStyle.INVALID){
                mImage.setImageResource(style.imagePlaceholder);
            }else {
                mImage.setImageResource(R.drawable.no_image);
            }
        }
        if (isChosen) {
            filter.setVisibility(View.VISIBLE);
            filterSecond.setVisibility(View.VISIBLE);
        } else {
            filter.setVisibility(View.INVISIBLE);
            filterSecond.setVisibility(View.INVISIBLE);
        }
        Drawable d;
        switch (sentState) {
            case STATE_WAS_READ:
                d = itemView.getResources().getDrawable(R.drawable.ic_done_all_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = itemView.getResources().getDrawable(R.drawable.ic_done_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = itemView.getResources().getDrawable(R.drawable.ic_cached_white_18dp);
                if (messageColor != ChatStyle.INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = itemView.getResources().getDrawable(R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        }
    }
}
