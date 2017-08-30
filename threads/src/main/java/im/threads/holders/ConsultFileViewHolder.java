package im.threads.holders;

import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;
import im.threads.views.CircularProgressButton;

import static im.threads.model.ChatStyle.INVALID;

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
    private View mBubble;
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
        mBubble = itemView.findViewById(R.id.bubble);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.incomingMessageBubbleColor != INVALID)
                mBubble.getBackground().setColorFilter(getColorInt(style.incomingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            if (style.incomingMessageTextColor != INVALID) {
                setTextColorToViews(new TextView[]{mFileHeader, mSizeTextView, mTimeStampTextView}, style.incomingMessageTextColor);
            }
            if (style.outgoingMessageBubbleColor != INVALID) {
                setTintToProgressButtonConsult(mCircularProgressButton, style.outgoingMessageBubbleColor);
            }
            if (style.chatHighlightingColor != INVALID) {
                mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
                mFilterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
            }
            if (style.chatBackgroundColor != INVALID) {
                mCircularProgressButton.setBackgroundColor(style.chatBackgroundColor);
            }

            if (style.operatorAvatarSize != INVALID) {
                mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
                mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
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
        mSizeTextView.setText(android.text.format.Formatter.formatFileSize(itemView.getContext(),
                fileDescription.getSize()));
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

            float bubbleLeftMarginDp = itemView.getContext().getResources().getDimension(R.dimen.margin_quarter);
            int bubbleLeftMarginPx = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bubbleLeftMarginDp, itemView.getResources().getDisplayMetrics()));
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mBubble.getLayoutParams();
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mBubble.setLayoutParams(lp);

            mConsultAvatar.setVisibility(View.VISIBLE);
            @DrawableRes int resiD = R.drawable.blank_avatar_round;
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

            int avatarSizeRes =  style != null && style.operatorAvatarSize != INVALID ? style.operatorAvatarSize : R.dimen.consultant_photo_size;
            int avatarSizePx = itemView.getContext().getResources().getDimensionPixelSize(avatarSizeRes);
            int bubbleLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);
            int avatarLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mBubble.getLayoutParams();
            lp.setMargins(avatarSizePx + bubbleLeftMarginPx + avatarLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mBubble.setLayoutParams(lp);
        }
    }
}
