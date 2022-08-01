package im.threads.internal.holders;

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

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.AttachmentStateEnum;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.views.CircularProgressButton;

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
        ChatStyle style = Config.instance.getChatStyle();
        setTextColorToViews(new TextView[]{mFileHeader, fileSizeTextView}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        if (style.outgoingMessageTimeTextSize > 0)
            mTimeStampTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.getContext().getResources().getDimension(style.outgoingMessageTimeTextSize));
        mCircularProgressButton.setBackgroundColorResId(style.outgoingMessageTextColor);
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.setPadding(
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingLeft),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingTop),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingRight),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingBottom)
        );
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setUpProgressButton(mCircularProgressButton);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
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

        if (fileDescription.getState() == AttachmentStateEnum.ERROR) {
            mCircularProgressButton.setVisibility(View.INVISIBLE);
            loader.setImageResource(getErrorImageResByErrorCode(fileDescription.getErrorCode()));
            loader.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(Config.instance.context.getString(getErrorStringResByErrorCode(fileDescription.getErrorCode())));
        } else if (fileDescription.getState() == AttachmentStateEnum.PENDING) {
            mCircularProgressButton.setVisibility(View.INVISIBLE);
            loader.setImageResource(R.drawable.im_loading);
            loader.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            rotate.setDuration(3000);
            rotate.setRepeatCount(Animation.INFINITE);
            loader.setAnimation(rotate);
        } else {
            loader.setVisibility(View.INVISIBLE);
            errorText.setVisibility(View.GONE);
            mCircularProgressButton.setVisibility(View.VISIBLE);
            mCircularProgressButton.setProgress(fileDescription.getFileUri() != null ? 100 : fileDescription.getDownloadProgress());
            mCircularProgressButton.setOnClickListener(buttonClickListener);
        }
        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }
        Drawable drawable;
        switch (sentState) {
            case STATE_WAS_READ:
                drawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                if (drawable != null) {
                    drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                    mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                }
                break;
            case STATE_SENT:
                drawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                if (drawable != null) {
                    drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                    mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                }
                break;
            case STATE_NOT_SENT:
                drawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                if (drawable != null) {
                    drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                    mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                }
                break;
            case STATE_SENDING:
                drawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                if (drawable != null) {
                    mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                }
                break;
        }
    }
}
