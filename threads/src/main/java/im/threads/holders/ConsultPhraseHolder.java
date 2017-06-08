package im.threads.holders;

import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.model.Quote;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;
import im.threads.utils.ViewUtils;
import im.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private TextView mRightTextDescr;
    private TextView rightTextFileStamp;
    private TextView mTimeStampTextView;
    private TextView mPhraseTextView;
    private SimpleDateFormat quoteSdf;
    private SimpleDateFormat timeStampSdf = new SimpleDateFormat("HH:mm");
    public ImageView mConsultAvatar;
    private View mFilterView;
    private View mFilterViewSecond;
    private static ChatStyle style;
    private ImageView mBubble;
    private FrameLayout mPhraseFrame;
    @DrawableRes
    private int defIcon;

    public ConsultPhraseHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant_text_with_file, parent, false));
        fileRow = itemView.findViewById(R.id.right_text_row);
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);

        rightTextHeader = (TextView) itemView.findViewById(R.id.to);
        mRightTextDescr = (TextView) itemView.findViewById(R.id.file_specs);
        rightTextFileStamp = (TextView) itemView.findViewById(R.id.send_at);
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
        mPhraseFrame = (FrameLayout) itemView.findViewById(R.id.phrase_frame);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        mBubble = (ImageView) itemView.findViewById(R.id.bubble);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if (style.incomingMessageBubbleColor != ChatStyle.INVALID)
                mBubble.getDrawable().setColorFilter(getColorInt(style.incomingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
            if (style.incomingMessageTextColor != ChatStyle.INVALID) {
                setTextColorToViews(new TextView[]{mPhraseTextView,
                        mTimeStampTextView,
                        rightTextHeader,
                        mRightTextDescr,
                        rightTextFileStamp}, style.incomingMessageTextColor);
            } else {
                setTextColorToViews(new TextView[]{mPhraseTextView,
                        mTimeStampTextView,
                        rightTextHeader,
                        mRightTextDescr,
                        rightTextFileStamp}, android.R.color.black);
            }
            defIcon = style.defaultIncomingMessageAvatar == ChatStyle.INVALID ? R.drawable.blank_avatar_round : style.defaultIncomingMessageAvatar;
            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                setTintToProgressButtonConsult(mCircularProgressButton, style.chatToolbarColorResId);
                itemView.findViewById(R.id.delimeter).setBackgroundColor(getColorInt(style.chatToolbarColorResId));
            }
            if (style.chatHighlightingColor != ChatStyle.INVALID) {
                mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
                mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
            }
            if (style.chatBackgroundColor != ChatStyle.INVALID) {
                mCircularProgressButton.setBackgroundColor(style.chatBackgroundColor);
            }
        }
    }


    public void onBind(String phrase
            , String avatarPath
            , long timeStamp
            , boolean isAvatarVisible
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener fileClickListener
            , View.OnLongClickListener onRowLongClickListener
            , View.OnClickListener onAvatarClickListener
            , boolean isChosen) {
        mConsultAvatar.setImageBitmap(null);
        if (phrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(Html.fromHtml(phrase.trim().replaceAll("\n", "<br>") + " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
        }
        ViewUtils.setClickListener((ViewGroup) itemView, onRowLongClickListener);
        mTimeStampTextView.setText(timeStampSdf.format(new Date(timeStamp)));
        if (quote != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.GONE);
            rightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextDescr.setText(quote.getText());
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.sent_at) + " " + quoteSdf.format(new Date(quote.getTimeStamp())));
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
            String fileHeader = fileDescription.getIncomingName() == null ? FileUtils.getLastPathSegment(fileDescription.getFilePath()) : fileDescription.getIncomingName();
            mRightTextDescr.setText(fileHeader == null ? "" : fileHeader + "\n" + android.text.format.Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.sent_at) + " " + quoteSdf.format(new Date(fileDescription.getTimeStamp())));
            mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
        }
        if (fileDescription == null && quote == null) {
            fileRow.setVisibility(View.GONE);
        }
        if (fileDescription != null
                || quote != null) {
            mPhraseFrame.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        } else {
            mPhraseFrame.getLayoutParams().width = FrameLayout.LayoutParams.WRAP_CONTENT;
        }
        if (isAvatarVisible) {
            mBubble.setPadding(0, 0, 0, ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, itemView.getResources().getDisplayMetrics())));
            mBubble.invalidate();
            mConsultAvatar.setVisibility(View.VISIBLE);
            mConsultAvatar.setOnClickListener(onAvatarClickListener);
            if (!isEmpty(avatarPath)) {
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
                                Picasso
                                        .with(itemView.getContext())
                                        .load(defIcon)
                                        .fit()
                                        .noPlaceholder()
                                        .transform(new CircleTransform())
                                        .into(mConsultAvatar);
                            }
                        });
            } else {
                Picasso
                        .with(itemView.getContext())
                        .load(defIcon)
                        .fit()
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
            mBubble.setPadding(0, 0, 0, ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, itemView.getResources().getDisplayMetrics())));
        }
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }

    }
}
