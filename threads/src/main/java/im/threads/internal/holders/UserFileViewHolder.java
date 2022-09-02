package im.threads.internal.holders;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageState;
import im.threads.business.models.enums.AttachmentStateEnum;
import im.threads.business.utils.FileUtils;
import im.threads.ui.views.CircularProgressButton;

public final class UserFileViewHolder extends BaseHolder {
    private final CircularProgressButton mCircularProgressButton;
    private final TextView mFileHeader;
    private final TextView fileSizeTextView;
    private final TextView mTimeStampTextView;
    private final TextView errorText;
    private final View mFilterView;
    private final View mFilterSecond;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final ImageView loader;

    private final RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);


    public UserFileViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_chat_file, parent, false));
        mCircularProgressButton = itemView.findViewById(R.id.button_download);
        mFileHeader = itemView.findViewById(R.id.header);
        fileSizeTextView = itemView.findViewById(R.id.file_size);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterSecond = itemView.findViewById(R.id.filter_second);
        View mBubble = itemView.findViewById(R.id.bubble);
        loader = itemView.findViewById(R.id.loader);
        errorText = itemView.findViewById(R.id.errorText);
        setTextColorToViews(new TextView[]{mFileHeader, fileSizeTextView}, getStyle().outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(getStyle().outgoingMessageTimeColor));
        if (getStyle().outgoingMessageTimeTextSize > 0) {
            float textSize = parent.getContext().getResources().getDimension(getStyle().outgoingMessageTimeTextSize);
            mTimeStampTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        mCircularProgressButton.setBackgroundColorResId(getStyle().outgoingMessageTextColor);
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), getStyle().outgoingMessageBubbleBackground));
        mBubble.setPadding(
                itemView.getContext().getResources().getDimensionPixelSize(getStyle().bubbleOutgoingPaddingLeft),
                itemView.getContext().getResources().getDimensionPixelSize(getStyle().bubbleOutgoingPaddingTop),
                itemView.getContext().getResources().getDimensionPixelSize(getStyle().bubbleOutgoingPaddingRight),
                itemView.getContext().getResources().getDimensionPixelSize(getStyle().bubbleOutgoingPaddingBottom)
        );
        mBubble.getBackground().setColorFilter(getColorInt(getStyle().outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setUpProgressButton(mCircularProgressButton);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().chatHighlightingColor));
        mFilterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().chatHighlightingColor));
    }

    public void onBind(
            long timeStamp
            , FileDescription fileDescription
            , View.OnClickListener buttonClickListener
            , View.OnClickListener rowClickListener
            , View.OnLongClickListener onLongClick
            , boolean isFilterVisible
            , MessageState sentState) {
        if (fileDescription == null) return;
        mFileHeader.setText(FileUtils.getFileName(fileDescription));
        long fileSize = fileDescription.getSize();
        fileSizeTextView.setText(android.text.format.Formatter.formatFileSize(itemView.getContext(), fileSize));
        fileSizeTextView.setVisibility(fileSize > 0 ? View.VISIBLE : View.GONE);
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick);
            vg.getChildAt(i).setOnClickListener(rowClickListener);
        }

        updateFileView(fileDescription, buttonClickListener);

        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }
        switch (sentState) {
            case STATE_WAS_READ:
                updateDrawable(itemView.getContext(), R.drawable.threads_message_received,
                        R.color.threads_outgoing_message_received_icon);
                break;
            case STATE_SENT:
                updateDrawable(itemView.getContext(), R.drawable.threads_message_sent,
                        R.color.threads_outgoing_message_sent_icon);
                break;
            case STATE_NOT_SENT:
                updateDrawable(itemView.getContext(), R.drawable.threads_message_waiting,
                        R.color.threads_outgoing_message_not_send_icon);
                break;
            case STATE_SENDING:
                updateDrawable(itemView.getContext(), R.drawable.empty_space_24dp, -1);
                break;
        }
    }

    private void updateDrawable(Context context, int srcDrawableResId, int colorResId) {
        Drawable drawable = AppCompatResources.getDrawable(context, srcDrawableResId);
        if (drawable != null) {
            if (colorResId >= 0) {
                drawable.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_ATOP);
            }
            mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    private void updateFileView(FileDescription fileDescription, View.OnClickListener buttonClickListener) {
        if (fileDescription.getState() == AttachmentStateEnum.ERROR) {
            mCircularProgressButton.setVisibility(View.INVISIBLE);
            loader.setImageResource(getErrorImageResByErrorCode(fileDescription.getErrorCode()));
            loader.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.VISIBLE);
            String errorString = getString(getErrorStringResByErrorCode(fileDescription.getErrorCode()));
            errorText.setText(errorString);
        } else if (fileDescription.getState() == AttachmentStateEnum.PENDING) {
            mCircularProgressButton.setVisibility(View.INVISIBLE);
            loader.setImageResource(R.drawable.im_loading);
            loader.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
            rotateAnimation.setDuration(3000);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            loader.setAnimation(rotateAnimation);
        } else {
            loader.setVisibility(View.INVISIBLE);
            errorText.setVisibility(View.GONE);
            mCircularProgressButton.setVisibility(View.VISIBLE);
            mCircularProgressButton.setProgress(fileDescription.getFileUri() != null ?
                    100 : fileDescription.getDownloadProgress());
            mCircularProgressButton.setOnClickListener(buttonClickListener);
        }
    }
}
