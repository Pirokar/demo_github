package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.internal.markdown.MarkdownProcessorHolder;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.Quote;
import im.threads.internal.opengraph.OGData;
import im.threads.internal.opengraph.OGDataProvider;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.UrlUtils;
import im.threads.internal.utils.ViewUtils;
import im.threads.internal.views.CircularProgressButton;
import im.threads.internal.widget.text_view.BubbleMessageTextView;
import im.threads.internal.widget.text_view.BubbleTimeTextView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * layout/item_consultant_text_with_file.xml
 */
public final class ConsultPhraseHolder extends BaseHolder {
    private static final String TAG = "ConsultPhraseHolder ";
    private View fileRow;
    private CircularProgressButton mCircularProgressButton;
    private TextView rightTextHeader;
    private ImageView mImage;
    private TextView mRightTextDescr;
    private TextView rightTextFileStamp;
    private BubbleTimeTextView mTimeStampTextView;
    private BubbleMessageTextView mPhraseTextView;
    private SimpleDateFormat quoteSdf;
    private SimpleDateFormat timeStampSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ImageView mConsultAvatar;
    private View mFilterView;
    private View mFilterViewSecond;
    private ChatStyle style;
    private View mBubble;
    private View mPhraseFrame;
    private ViewGroup ogDataLayout;
    private ImageView ogImage;
    private TextView ogTitle;
    private TextView ogDescription;
    private TextView ogUrl;
    private TextView ogTimestamp;
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
        ogDataLayout = itemView.findViewById(R.id.og_data_layout);
        ogImage = itemView.findViewById(R.id.og_image);
        ogTitle = itemView.findViewById(R.id.og_title);
        ogDescription = itemView.findViewById(R.id.og_description);
        ogUrl = itemView.findViewById(R.id.og_url);
        ogTimestamp = itemView.findViewById(R.id.og_timestamp);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        }
        mBubble = itemView.findViewById(R.id.bubble);
        if (style == null) style = Config.instance.getChatStyle();

        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.incomingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.incomingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mPhraseTextView,
                rightTextHeader,
                mRightTextDescr,
                rightTextFileStamp}, style.incomingMessageTextColor);

        mTimeStampTextView.setTextColor(getColorInt(style.incomingMessageTimeColor));
        ogTimestamp.setTextColor(getColorInt(style.incomingMessageTimeColor));

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

    public void onBind(ConsultPhrase consultPhrase,
                       String phrase,
                       String avatarPath,
                       long timeStamp,
                       boolean isAvatarVisible,
                       Quote quote,
                       FileDescription fileDescription,
                       final View.OnClickListener imageClickListener,
                       @Nullable View.OnClickListener fileClickListener,
                       View.OnLongClickListener onRowLongClickListener,
                       View.OnClickListener onAvatarClickListener,
                       boolean isChosen) {
        ViewUtils.setClickListener((ViewGroup) itemView, onRowLongClickListener);
        mConsultAvatar.setImageBitmap(null);
        String timeText = timeStampSdf.format(new Date(timeStamp));
        mTimeStampTextView.setText(timeText);
        ogTimestamp.setText(timeText);
        if (phrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.bindTimestampView(mTimeStampTextView);
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setMovementMethod(LinkMovementMethod.getInstance());
            if (consultPhrase.getFormattedPhrase() != null) {
                mPhraseTextView.setText(MarkdownProcessorHolder.getMarkdownProcessor().parse(consultPhrase.getFormattedPhrase()));
            } else {
                mPhraseTextView.setText(phrase);
            }
            String url = UrlUtils.extractLink(phrase);
            if (url != null) {
                if (consultPhrase.ogData == null) {
                    loadOGData(consultPhrase, url);
                } else {
                    bindOGData(consultPhrase.ogData, url);
                }
                ViewUtils.setClickListener(ogDataLayout, v -> {
                    UrlUtils.openUrl(itemView.getContext(), url);
                });
            } else {
                hideOGView();
            }
        }
        mImage.setVisibility(View.GONE);
        if (quote != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.GONE);
            rightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextDescr.setText(quote.getText());
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.threads_sent_at) + " " + quoteSdf.format(new Date(quote.getTimeStamp())));
            if (TextUtils.isEmpty(quote.getPhraseOwnerTitle())) {
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
                Picasso.get()
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
                rightTextHeader.setText(fileDescription.getFrom() == null ? "" : fileDescription.getFrom());
                if (!TextUtils.isEmpty(rightTextHeader.getText())) {
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
            if (!TextUtils.isEmpty(avatarPath)) {
                avatarPath = FileUtils.convertRelativeUrlToAbsolute(avatarPath);
                Picasso.get()
                        .load(avatarPath)
                        .fit()
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransformation())
                        .into(mConsultAvatar, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
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
        Picasso.get()
                .load(defIcon)
                .centerInside()
                .noPlaceholder()
                .fit()
                .transform(new CircleTransformation())
                .into(mConsultAvatar);
    }

    private void loadOGData(final ConsultPhrase chatItem, final String url) {
        hideOGView();
        subscribe(OGDataProvider.getInstance().getOGData(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ogData -> {
                    ThreadsLogger.d(TAG, "OGData for url: " + url + "\n received: " + ogData);
                    if (ogData != null && !ogData.isEmpty()) {
                        chatItem.ogData = ogData;
                        chatItem.ogUrl = url;
                    }
                    bindOGData(ogData, url);
                }, e ->ThreadsLogger.w(TAG, "OpenGraph data load failed: ", e))
        );
    }

    private void bindOGData(final OGData ogData, String url) {
        if (ogData == null || ogData.areTextsEmpty()) {
            hideOGView();
            return;
        }
        showOGView();
        if (!TextUtils.isEmpty(ogData.title)) {
            ogTitle.setVisibility(View.VISIBLE);
            ogTitle.setText(ogData.title);
            ogTitle.setTypeface(ogTitle.getTypeface(), Typeface.BOLD);
        } else {
            ogTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(ogData.description)) {
            ogDescription.setVisibility(View.VISIBLE);
            ogDescription.setText(ogData.description);
        } else {
            ogDescription.setVisibility(View.GONE);
        }
        ogUrl.setText(!TextUtils.isEmpty(ogData.url) ? ogData.url : url);
        if (TextUtils.isEmpty(ogData.image)) {
            ogImage.setVisibility(View.GONE);
        } else {
            Picasso.get()
                    .load(ogData.image)
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            ogImage.setVisibility(View.VISIBLE);
                            Picasso.get()
                                    .load(ogData.image)
                                    .error(style.imagePlaceholder)
                                    .fit()
                                    .centerInside()
                                    .into(ogImage);
                        }

                        @Override
                        public void onError(Exception e) {
                            ogImage.setVisibility(View.GONE);
                            ThreadsLogger.d(TAG, "Could not load OpenGraph image: " + e.getMessage());
                        }
                    });
        }
    }

    private void showOGView() {
        ogDataLayout.setVisibility(View.VISIBLE);
        mTimeStampTextView.setVisibility(View.GONE);
    }

    private void hideOGView() {
        ogDataLayout.setVisibility(View.GONE);
        mTimeStampTextView.setVisibility(View.VISIBLE);
    }

}
