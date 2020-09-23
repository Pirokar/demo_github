package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.MaskedTransformation;

public final class ImageFromUserViewHolder extends BaseHolder {
    private TextView mTimeStampTextView;
    private ImageView mImage;
    private MaskedTransformation maskedTransformation;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private View filter;
    private View filterSecond;
    private ChatStyle style;

    public ImageFromUserViewHolder(ViewGroup parent, MaskedTransformation maskedTransformation) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_image_from, parent, false));
        style = Config.instance.getChatStyle();
        mImage = itemView.findViewById(R.id.image);
        this.maskedTransformation = maskedTransformation;
        filter = itemView.findViewById(R.id.filter);
        filterSecond = itemView.findViewById(R.id.filter_second);
        filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        filterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingImageTimeColor));
        mTimeStampTextView.getBackground().setColorFilter(getColorInt(style.outgoingImageTimeBackgroundColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(final UserPhrase userPhrase, final Runnable clickRunnable, final Runnable longClickRunnable) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(v -> clickRunnable.run());
        }
        bindImage(userPhrase.getFileDescription(), longClickRunnable);
        bindIsChosen(userPhrase.isChosen(), longClickRunnable);
        bindTimeStamp(userPhrase.getSentState(), userPhrase.getTimeStamp(), longClickRunnable);
    }

    private void bindIsChosen(boolean isChosen, Runnable longClickRunnable) {
        filter.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        filter.setVisibility(isChosen ? View.VISIBLE : View.INVISIBLE);
        filterSecond.setVisibility(isChosen ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindImage(FileDescription fileDescription, Runnable longClickRunnable) {
        boolean isDownloadError = fileDescription.isDownloadError();
        mImage.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        mImage.setImageResource(0);
        if (fileDescription.getFileUri() != null && !isDownloadError) {
            Picasso.get()
                    .load(fileDescription.getFileUri())
                    .error(style.imagePlaceholder)
                    .fit()
                    .centerCrop()
                    .transform(maskedTransformation)
                    .into(mImage);
        } else if (isDownloadError) {
            mImage.setImageResource(style.imagePlaceholder);
        }
    }

    private void bindTimeStamp(MessageState messageState, long timestamp, Runnable longClickRunnable) {
        mTimeStampTextView.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        Drawable rightDrawable = null;
        switch (messageState) {
            case STATE_WAS_READ:
                rightDrawable = getColoredDrawable(R.drawable.threads_image_message_received, R.color.threads_outgoing_message_image_received_icon);
                break;
            case STATE_SENT:
                rightDrawable = getColoredDrawable(R.drawable.threads_message_image_sent, R.color.threads_outgoing_message_image_sent_icon);
                break;
            case STATE_NOT_SENT:
                rightDrawable = getColoredDrawable(R.drawable.threads_message_image_waiting, R.color.threads_outgoing_message_image_not_send_icon);
                break;
            case STATE_SENDING:
                rightDrawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
        }
        mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    private Drawable getColoredDrawable(@DrawableRes int res, @ColorRes int color) {
        final Drawable drawable = AppCompatResources.getDrawable(itemView.getContext(), res);
        if (drawable != null) {
            drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), color), PorterDuff.Mode.SRC_ATOP);
        }
        return drawable;
    }
}