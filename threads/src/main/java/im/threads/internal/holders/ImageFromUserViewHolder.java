package im.threads.internal.holders;

import static im.threads.business.models.MessageState.STATE_SENDING;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.business.imageLoading.ImageModifications;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageState;
import im.threads.business.models.UserPhrase;
import im.threads.business.models.enums.AttachmentStateEnum;

public final class ImageFromUserViewHolder extends BaseHolder {
    private final TextView mTimeStampTextView;
    private final TextView mTimeStampDuplicateTextView;
    private final ImageView mImage;
    private final ImageModifications.MaskedModification maskedTransformation;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final View filter;
    private final ImageView loader;
    private final FrameLayout loaderLayout;
    private final FrameLayout commonLayout;
    private final LinearLayout bubbleLayout;
    private final TextView errorText;
    private final TextView fileName;

    private final RotateAnimation rotateAnimation = new RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
    );

    public ImageFromUserViewHolder(ViewGroup parent, ImageModifications.MaskedModification maskedTransformation) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_image_from, parent, false));
        mImage = itemView.findViewById(R.id.image);
        this.maskedTransformation = maskedTransformation;
        filter = itemView.findViewById(R.id.filter);
        filter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().chatHighlightingColor));
        loader = itemView.findViewById(R.id.loader);
        loaderLayout = itemView.findViewById(R.id.loaderLayout);
        bubbleLayout = itemView.findViewById(R.id.bubble);
        commonLayout = itemView.findViewById(R.id.commonLayout);
        errorText = itemView.findViewById(R.id.errorText);
        fileName = itemView.findViewById(R.id.fileName);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mTimeStampDuplicateTextView = itemView.findViewById(R.id.timestampDuplicate);
        applyTimeStampStyle(parent.getContext());
        applyBubbleLayoutStyle();
    }

    private void applyTimeStampStyle(Context context) {
        mTimeStampTextView.setTextColor(getColorInt(getStyle().outgoingImageTimeColor));
        mTimeStampDuplicateTextView.setTextColor(getColorInt(getStyle().outgoingImageTimeColor));
        int timeColorBg = getColorInt(getStyle().outgoingImageTimeBackgroundColor);
        mTimeStampDuplicateTextView.getBackground().setColorFilter(timeColorBg, PorterDuff.Mode.SRC_ATOP);
        mTimeStampTextView.getBackground().setColorFilter(timeColorBg, PorterDuff.Mode.SRC_ATOP);
        if (getStyle().outgoingMessageTimeTextSize > 0) {
            float textSize = context.getResources().getDimension(getStyle().outgoingMessageTimeTextSize);
            mTimeStampTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTimeStampDuplicateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    private void applyBubbleLayoutStyle() {
        Resources res = itemView.getContext().getResources();
        bubbleLayout.setBackground(
                AppCompatResources.getDrawable(itemView.getContext(),
                        getStyle().outgoingMessageBubbleBackground)
        );
        bubbleLayout.setPadding(
                res.getDimensionPixelSize(getStyle().bubbleOutgoingPaddingLeft),
                res.getDimensionPixelSize(getStyle().bubbleOutgoingPaddingTop),
                res.getDimensionPixelSize(getStyle().bubbleOutgoingPaddingRight),
                res.getDimensionPixelSize(getStyle().bubbleOutgoingPaddingBottom)
        );
    }

    public void onBind(final UserPhrase userPhrase,
                       boolean highlighted,
                       final Runnable clickRunnable,
                       final Runnable longClickRunnable) {
        mImage.setOnClickListener(v -> clickRunnable.run());
        bindImage(userPhrase.getFileDescription(), userPhrase.getSentState(), longClickRunnable);
        bindIsChosen(highlighted, longClickRunnable);
        bindTimeStamp(userPhrase.getSentState(), userPhrase.getTimeStamp(), longClickRunnable);
    }

    private void bindIsChosen(boolean isChosen, Runnable longClickRunnable) {
        filter.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        filter.setVisibility(isChosen ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindImage(FileDescription fileDescription,
                           MessageState messageState,
                           Runnable longClickRunnable) {
        boolean isDownloadError = fileDescription.isDownloadError();
        mImage.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        mImage.setImageResource(0);

        mImage.setVisibility(View.VISIBLE);
        loaderLayout.setVisibility(View.GONE);
        if (fileDescription.getState() == AttachmentStateEnum.PENDING || messageState == STATE_SENDING) {
            showLoaderLayout(fileDescription);
        } else if (fileDescription.getState() == AttachmentStateEnum.ERROR) {
            showErrorLayout(fileDescription);
        } else {
            showCommonLayout();
            if (fileDescription.getFileUri() != null && !isDownloadError) {
                ImageLoader
                        .get()
                        .autoRotateWithExif(true)
                        .load(fileDescription.getFileUri().toString())
                        .scales(ImageView.ScaleType.FIT_END, ImageView.ScaleType.CENTER_CROP)
                        .modifications(maskedTransformation)
                        .errorDrawableResourceId(getStyle().imagePlaceholder)
                        .into(mImage);
            } else if (isDownloadError) {
                mImage.setImageResource(getStyle().imagePlaceholder);
            }
        }
    }

    private void bindTimeStamp(MessageState messageState, long timestamp, Runnable longClickRunnable) {
        mTimeStampTextView.setOnLongClickListener(view -> {
            longClickRunnable.run();
            return true;
        });
        mTimeStampTextView.setText(sdf.format(new Date(timestamp)));
        mTimeStampDuplicateTextView.setText(sdf.format(new Date(timestamp)));
        Drawable rightDrawable = null;
        switch (messageState) {
            case STATE_WAS_READ:
                rightDrawable = getColoredDrawable(R.drawable.threads_image_message_received,
                        R.color.threads_outgoing_message_image_received_icon);
                break;
            case STATE_SENT:
                rightDrawable = getColoredDrawable(R.drawable.threads_message_image_sent,
                        R.color.threads_outgoing_message_image_sent_icon);
                break;
            case STATE_NOT_SENT:
                rightDrawable = getColoredDrawable(R.drawable.threads_message_image_waiting,
                        R.color.threads_outgoing_message_image_not_send_icon);
                break;
            case STATE_SENDING:
                rightDrawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
        }
        mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
        mTimeStampDuplicateTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    private Drawable getColoredDrawable(@DrawableRes int res, @ColorRes int color) {
        final Drawable drawable = AppCompatResources.getDrawable(itemView.getContext(), res);
        if (drawable != null) {
            drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), color), PorterDuff.Mode.SRC_ATOP);
        }
        return drawable;
    }

    private void showLoaderLayout(FileDescription fileDescription) {
        loaderLayout.setVisibility(View.VISIBLE);
        commonLayout.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        fileName.setText(fileDescription.getIncomingName());
        loader.setImageResource(R.drawable.im_loading);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        loader.setAnimation(rotateAnimation);
    }

    private void showErrorLayout(FileDescription fileDescription) {
        errorText.setVisibility(View.VISIBLE);
        loaderLayout.setVisibility(View.VISIBLE);
        commonLayout.setVisibility(View.GONE);
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.getErrorCode()));
        fileName.setText(fileDescription.getIncomingName());
        String errorString = getString(getErrorStringResByErrorCode(fileDescription.getErrorCode()));
        errorText.setText(errorString);
        rotateAnimation.cancel();
        rotateAnimation.reset();
    }

    private void showCommonLayout() {
        commonLayout.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.GONE);
        rotateAnimation.cancel();
        rotateAnimation.reset();
    }
}
