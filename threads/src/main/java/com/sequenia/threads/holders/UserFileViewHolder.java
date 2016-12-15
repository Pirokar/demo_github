package com.sequenia.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sequenia.threads.model.ChatStyle.INVALID;

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
    private ImageView mBubble;
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
        mBubble = (ImageView) itemView.findViewById(R.id.bubble_1);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.outgoingMessageBubbleColor != INVALID) {
                mBubble.setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            }
            if (style.outgoingMessageTextColor != INVALID) {
                messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
                setTextColorToViews(new TextView[]{mFileHeader, mSizeTextView, mTimeStampTextView}, style.outgoingMessageTextColor);
            }
            if (style.incomingMessageBubbleColor != INVALID) {
                setTintToProgressButton(mCircularProgressButton, style.incomingMessageBubbleColor);
            }else {
                setTintToProgressButton(mCircularProgressButton, android.R.color.white);
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
                d = itemView.getResources().getDrawable(R.drawable.ic_done_all_white_18dp);
                if (messageColor != INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = itemView.getResources().getDrawable(R.drawable.ic_done_white_18dp);
                if (messageColor != INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = itemView.getResources().getDrawable(R.drawable.ic_cached_white_18dp);
                if (messageColor != INVALID) {
                    d.setColorFilter(messageColor, PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = itemView.getResources().getDrawable(R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }
}
