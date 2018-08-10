package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultPhrase;
import im.threads.model.FileDescription;
import im.threads.model.Quote;
import im.threads.opengraph.OGData;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.LogUtils;
import im.threads.utils.ViewUtils;
import im.threads.views.CircularProgressButton;

import static android.text.TextUtils.isEmpty;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_consultant_text_with_file.xml
 */
public class ConsultPhraseHolder extends BaseHolder {
    private static final String TAG = "ConsultPhraseHolder ";
    private View fileRow;
    private CircularProgressButton mCircularProgressButton;
    private TextView rightTextHeader;
    private ImageView mImage;
    private TextView mRightTextDescr;
    private TextView rightTextFileStamp;
    private TextView mTimeStampTextView;
    private TextView mPhraseTextView;
    private SimpleDateFormat quoteSdf;
    private SimpleDateFormat timeStampSdf = new SimpleDateFormat("HH:mm");
    public ImageView mConsultAvatar;
    private View mFilterView;
    private View mFilterViewSecond;
    private ChatStyle style;
    private View mBubble;
    private View mPhraseFrame;
    private ViewGroup mOgDataLayout;
    private ImageView mOgImage;
    private TextView mOgTitle;
    private TextView mOgDescription;
    private TextView mOgUrl;
    private TextView mOgTimestamp;
    @DrawableRes
    private int defIcon;

    public ConsultPhraseHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant_text_with_file, parent, false));
        fileRow = itemView.findViewById(R.id.right_text_row);
        mCircularProgressButton = itemView.findViewById(R.id.button_download);
        mImage = itemView.findViewById(R.id.image);
        rightTextHeader = itemView.findViewById(R.id.to);
        mRightTextDescr = itemView.findViewById(R.id.file_specs);
        rightTextFileStamp = itemView.findViewById(R.id.send_at);
        mPhraseTextView = itemView.findViewById(R.id.text);
        mConsultAvatar = itemView.findViewById(R.id.consult_avatar);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
        mPhraseFrame = itemView.findViewById(R.id.phrase_frame);
        mOgDataLayout = itemView.findViewById(R.id.og_data_layout);
        mOgImage = itemView.findViewById(R.id.og_image);
        mOgTitle = itemView.findViewById(R.id.og_title);
        mOgDescription = itemView.findViewById(R.id.og_description);
        mOgUrl = itemView.findViewById(R.id.og_url);
        mOgTimestamp = itemView.findViewById(R.id.og_timestamp);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        mBubble = itemView.findViewById(R.id.bubble);
        if (style == null) style = ChatStyle.getInstance();

        mBubble.setBackground(ContextCompat.getDrawable(itemView.getContext(), style.incomingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.incomingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mPhraseTextView,
                rightTextHeader,
                mRightTextDescr,
                rightTextFileStamp}, style.incomingMessageTextColor);

        mTimeStampTextView.setTextColor(getColorInt(style.incomingMessageTimeColor));
        mOgTimestamp.setTextColor(getColorInt(style.incomingMessageTimeColor));

        mPhraseTextView.setLinkTextColor(getColorInt(style.incomingMessageLinkColor));

        defIcon = style.defaultOperatorAvatar;
        setTintToProgressButtonConsult(mCircularProgressButton, style.chatBodyIconsTint);
        itemView.findViewById(R.id.delimeter).setBackgroundColor(getColorInt(style.chatToolbarColorResId));

        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mCircularProgressButton.setBackgroundColorResId(style.chatBackgroundColor);

        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorAvatarSize);
    }


    public void onBind(ConsultPhrase message, String phrase
            , String avatarPath
            , long timeStamp
            , boolean isAvatarVisible
            , Quote quote
            , FileDescription fileDescription
            , final View.OnClickListener imageClickListener
            , @Nullable View.OnClickListener fileClickListener
            , View.OnLongClickListener onRowLongClickListener
            , View.OnClickListener onAvatarClickListener
            , View.OnClickListener onOgClickListener, boolean isChosen) {

        mConsultAvatar.setImageBitmap(null);
        if (phrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(phrase);
        }

        OGData ogData = message.ogData;
        LogUtils.logDev(String.valueOf(ogData));

        if (ogData == null || ogData.isEmpty()) {
            mOgDataLayout.setVisibility(View.GONE);
            mTimeStampTextView.setVisibility(View.VISIBLE);
        } else {
            mOgDataLayout.setVisibility(View.VISIBLE);
            mTimeStampTextView.setVisibility(View.GONE);

            ViewUtils.setClickListener(mOgDataLayout, onOgClickListener);

            if (TextUtils.isEmpty(ogData.title)) {
                mOgTitle.setVisibility(View.GONE);
            } else {
                mOgTitle.setText(ogData.title);
                mOgTitle.setTypeface(mOgTitle.getTypeface(), Typeface.BOLD);
            }

            if (TextUtils.isEmpty(ogData.description)) {
                mOgDescription.setVisibility(View.GONE);
            } else {
                mOgDescription.setText(ogData.description);
            }

            if (TextUtils.isEmpty(ogData.url)) {
                mOgUrl.setVisibility(View.GONE);
            } else {
                mOgUrl.setText(ogData.url);
            }

            if (TextUtils.isEmpty(ogData.image)) {
                mOgImage.setVisibility(View.GONE);
            } else {
                Picasso.with(itemView.getContext())
                        .load(ogData.image)
                        .error(style.imagePlaceholder)
                        .fit()
                        .centerCrop()
                        .into(mOgImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                mOgImage.setVisibility(View.GONE);
                            }
                        });
            }
        }

        mImage.setVisibility(View.GONE);
        ViewUtils.setClickListener((ViewGroup) itemView, onRowLongClickListener);
        String timeText = timeStampSdf.format(new Date(timeStamp));
        mTimeStampTextView.setText(timeText);
        mOgTimestamp.setText(timeText);
        if (quote != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.GONE);
            rightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextDescr.setText(quote.getText());
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.threads_sent_at) + " " + quoteSdf.format(new Date(quote.getTimeStamp())));
            if (isEmpty(quote.getPhraseOwnerTitle())) {
                rightTextHeader.setVisibility(View.GONE);
            } else {
                rightTextHeader.setVisibility(View.VISIBLE);
            }
            if (quote.getFileDescription() != null) {
                mCircularProgressButton.setVisibility(View.VISIBLE);
                String filename = quote.getFileDescription().getIncomingName();
                if (filename == null) {
                    filename = FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath()) == null ? "" : FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath());
                }
                mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), quote.getFileDescription().getSize()));
                if (fileDescription != null)
                    mCircularProgressButton.setOnClickListener(fileClickListener);
                mCircularProgressButton.setProgress(quote.getFileDescription().getDownloadProgress());
            } else {
                mCircularProgressButton.setVisibility(View.GONE);
            }
        }
        if (fileDescription != null) {
            if (FileUtils.isImage(fileDescription)) {
                fileRow.setVisibility(View.GONE);
                mCircularProgressButton.setVisibility(View.GONE);
                mImage.setVisibility(View.VISIBLE);
                mImage.setOnClickListener(imageClickListener);

                Picasso.with(itemView.getContext())
                        .load(fileDescription.getDownloadPath())
                        .error(style.imagePlaceholder)
                        .fit()
                        .centerCrop()
                        .into(mImage);
            } else {
                fileRow.setVisibility(View.VISIBLE);
                mCircularProgressButton.setVisibility(View.VISIBLE);

                if (fileClickListener != null) {
                    mCircularProgressButton.setOnClickListener(fileClickListener);
                }
                rightTextHeader.setText(fileDescription.getFileSentTo() == null ? "" : fileDescription.getFileSentTo());
                if (!isEmpty(rightTextHeader.getText())) {
                    rightTextHeader.setVisibility(View.VISIBLE);
                } else {
                    rightTextHeader.setVisibility(View.GONE);
                }
                String fileHeader = fileDescription.getIncomingName() == null ? FileUtils.getLastPathSegment(fileDescription.getFilePath()) : fileDescription
                        .getIncomingName();
                mRightTextDescr.setText(fileHeader == null ? "" : fileHeader + "\n" + android.text.format.Formatter
                        .formatFileSize(itemView.getContext(), fileDescription.getSize()));
                rightTextFileStamp
                        .setText(itemView.getContext().getString(R.string.threads_sent_at) + " " + quoteSdf.format(new Date(fileDescription.getTimeStamp())));
                mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
            }
        }
        if (fileDescription == null && quote == null) {
            fileRow.setVisibility(View.GONE);
        }
        if (fileDescription != null || quote != null) {
            mPhraseFrame.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        } else {
            mPhraseFrame.getLayoutParams().width = FrameLayout.LayoutParams.WRAP_CONTENT;
        }
        if (isAvatarVisible) {
            float bubbleLeftMarginDp = itemView.getContext().getResources().getDimension(R.dimen.margin_quarter);
            int bubbleLeftMarginPx = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bubbleLeftMarginDp, itemView.getResources().getDisplayMetrics()));
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBubble.getLayoutParams();
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mBubble.setLayoutParams(lp);

            mConsultAvatar.setVisibility(View.VISIBLE);
            mConsultAvatar.setOnClickListener(onAvatarClickListener);
            if (!isEmpty(avatarPath)) {
                avatarPath = FileUtils.convertRelativeUrlToAbsolute(itemView.getContext(), avatarPath);
                Picasso
                        .with(itemView.getContext())
                        .load(avatarPath)
                        .fit()
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                showDefIcon();
                            }
                        });
            } else {
                showDefIcon();
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);

            int avatarSizeRes = style.operatorAvatarSize;
            int avatarSizePx = itemView.getContext().getResources().getDimensionPixelSize(avatarSizeRes);

            int bubbleLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);
            int avatarLeftMarginPx = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_half);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBubble.getLayoutParams();
            lp.setMargins(avatarSizePx + bubbleLeftMarginPx + avatarLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mBubble.setLayoutParams(lp);

        }

        mFilterView.setVisibility(isChosen ? View.VISIBLE : View.INVISIBLE);
        mFilterViewSecond.setVisibility(isChosen && isAvatarVisible ? View.VISIBLE : View.INVISIBLE);
    }

    private void showDefIcon() {
        Picasso.with(itemView.getContext())
                .load(defIcon)
                .centerInside()
                .noPlaceholder()
                .fit()
                .transform(new CircleTransform())
                .into(mConsultAvatar);
    }
}
