package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;
import im.threads.views.CircularProgressButton;

import static im.threads.model.ChatStyle.INVALID;

/**
 *
 */
public class UserFileViewHolder extends BaseHolder {
    private static final String TAG = "UserFileViewHolder ";
    private CircularProgressButton mCircularProgressButton;
    private TextView mFileHeader;
    private TextView mSizeTextView;
    private TextView mTimeStampTextView;
    private View mFilterView;
    private View mFilterSecond;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private static ChatStyle style;
    private View mBubble;
    private static
    @ColorInt
    int messageColor;

    public UserFileViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_chat_file, parent, false));
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);
        mFileHeader = (TextView) itemView.findViewById(R.id.header);
        mSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterSecond = itemView.findViewById(R.id.filter_second);
        mBubble = itemView.findViewById(R.id.bubble);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.outgoingMessageBubbleColor != INVALID) {
                mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            } else {
                mBubble.getBackground().setColorFilter(getColorInt(R.color.threads_chat_outgoing_message_bubble), PorterDuff.Mode.SRC_ATOP);
            }
            if (style.outgoingMessageTextColor != INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                setTextColorToViews(new TextView[]{mFileHeader, mSizeTextView, mTimeStampTextView}, style.outgoingMessageTextColor);
                mCircularProgressButton.setBackgroundColorResId(style.outgoingMessageTextColor);
            }
            if (style.outgoingMessageBubbleBackground != INVALID) {
                mBubble.setBackground(ContextCompat.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
            }
            if (style.incomingMessageBubbleColor != INVALID && style.outgoingMessageBubbleColor != INVALID) {
                setTintToProgressButtonUser(mCircularProgressButton, style.incomingMessageBubbleColor, style.chatBodyIconsTint);
            } else {
                setTintToProgressButtonUser(mCircularProgressButton, R.color.threads_chat_incoming_message_bubble, R.color.threads_chat_icons_tint);
            }

            if (style.chatHighlightingColor != INVALID) {
                mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
                mFilterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
            }
        }
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
        if (fileDescription.getIncomingName() != null) {
            mFileHeader.setText(fileDescription.getIncomingName());
        } else if (fileDescription.getFilePath() != null) {
            mFileHeader.setText(FileUtils.getLastPathSegment(fileDescription.getFilePath()));
        }
        mSizeTextView.setText(Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick);
            vg.getChildAt(i).setOnClickListener(rowClickListener);
        }
        if (fileDescription.getFilePath() != null) {
            mCircularProgressButton.setProgress(100);
        } else {
            mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
        }
        mCircularProgressButton.setOnClickListener(buttonClickListener);

        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }

        Drawable d;
        switch (sentState) {
            case STATE_WAS_READ:
                d = itemView.getResources().getDrawable(R.drawable.threads_message_received);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = itemView.getResources().getDrawable(R.drawable.threads_message_sent);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = itemView.getResources().getDrawable(R.drawable.threads_message_waiting);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = itemView.getResources().getDrawable(R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }
}
