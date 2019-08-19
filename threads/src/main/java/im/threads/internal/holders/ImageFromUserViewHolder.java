package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.MaskedTransformation;

public class ImageFromUserViewHolder extends BaseHolder {
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private MaskedTransformation maskedTransformation;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private View filter;
    private View filterSecond;
    private ChatStyle style;

    public ImageFromUserViewHolder(ViewGroup parent, MaskedTransformation maskedTransformation) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_image_from, parent, false));
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mImage = itemView.findViewById(R.id.image);
        this.maskedTransformation = maskedTransformation;
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        if (null == style) {
            style = Config.instance.getChatStyle();
        }
        filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingImageTimeColor));
        mTimeStampTextView.getBackground().setColorFilter(getColorInt(style.outgoingImageTimeBackgroundColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(
            final FileDescription fileDescription
            , long timestamp
            , final View.OnClickListener rowClickListener
            , View.OnLongClickListener longListener
            , boolean isDownloadError
            , boolean isChosen
            , MessageState sentState) {
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
            Picasso.with(itemView.getContext())
                    .load(new File(fileDescription.getFilePath()))
                    .error(style.imagePlaceholder)
                    .fit()
                    .centerCrop()
                    .transform(maskedTransformation)
                    .into(mImage);
        } else if (isDownloadError) {
            mImage.setImageResource(style.imagePlaceholder);
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
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_image_message_received);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_image_received_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_image_sent);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_image_sent_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_image_waiting);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_image_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        }
    }
}
