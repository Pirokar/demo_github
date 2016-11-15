package com.sequenia.threads.holders;

import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Callback;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 01.07.2016.
 */
public class ConsultFileViewHolder extends BaseHolder {
    private static final String TAG = "ConsultFileViewHolder ";
    private CircularProgressButton mCircularProgressButton;
    private TextView mFileHeader;
    private TextView mSizeTextView;
    private TextView mTimeStampTextView;
    private ImageView mConsultAvatar;
    private View mFilterView;
    private View mFilterSecond;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private ImageView mBubble;
    private static ChatStyle style;

    public ConsultFileViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_chat_file, parent, false));
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.circ_button);
        mFileHeader = (TextView) itemView.findViewById(R.id.header);
        mSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterSecond = itemView.findViewById(R.id.filter_second);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.consult_avatar);
        mBubble = (ImageView) itemView.findViewById(R.id.bubble_1);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.incomingMessageBubbleColor != INVALID)
                mBubble.setColorFilter(style.incomingMessageBubbleColor, PorterDuff.Mode.SRC_ATOP);
            if (style.incomingMessageTextColor != INVALID) {
                setTextColorToViews(new TextView[]{mFileHeader, mSizeTextView, mTimeStampTextView}, style.incomingMessageTextColor);
            }
            if (style.outgoingMessageBubbleColor != INVALID) {
                setTintToProgressButton(mCircularProgressButton, style.outgoingMessageBubbleColor);
            }
        }
    }

    public void onBind(
            long timeStamp
            , FileDescription fileDescription
            , String avatarPath
            , View.OnClickListener buttonClickListener
            , View.OnLongClickListener onLongClick
            , boolean isAvatarVisible
            , boolean isFilterVisible) {
        String name = null;
        mFileHeader.setText(fileDescription.getIncomingName() == null ? FileUtils.getLastPathSegment(fileDescription.getFilePath()) : fileDescription.getIncomingName());
        if (mFileHeader.getText().toString().equalsIgnoreCase("null")) mFileHeader.setText("");
        mSizeTextView.setText(android.text.format.Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick);
        }
        mCircularProgressButton.setOnClickListener(buttonClickListener);

        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
            @DrawableRes int resiD = R.drawable.defaultprofile_360;
            if (style!=null && style.defaultIncomingMessageAvatar!=INVALID)resiD = style.defaultIncomingMessageAvatar;

            if (avatarPath != null) {
                final int finalResiD = resiD;
                Picasso
                        .with(itemView.getContext())
                        .load(avatarPath)
                        .fit()
                        .noPlaceholder()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso
                                        .with(itemView.getContext())
                                        .load(finalResiD)
                                        .fit()
                                        .noPlaceholder()
                                        .transform(new CircleTransform())
                                        .into(mConsultAvatar);
                            }
                        });
            } else {
                Picasso
                        .with(itemView.getContext())
                        .load(resiD)
                        .fit()
                        .noPlaceholder()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
            mFilterSecond.setVisibility(View.GONE);
        }
    }
}
