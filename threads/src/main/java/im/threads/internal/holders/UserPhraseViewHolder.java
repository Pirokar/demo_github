package im.threads.internal.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.text.util.LinkifyCompat;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Quote;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.opengraph.OGData;
import im.threads.internal.opengraph.OGDataProvider;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.UrlUtils;
import im.threads.internal.utils.ViewUtils;
import im.threads.internal.views.CircularProgressButton;
import im.threads.internal.widget.text_view.BubbleMessageTextView;
import im.threads.internal.widget.text_view.BubbleTimeTextView;
import io.reactivex.android.schedulers.AndroidSchedulers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * layout/item_user_text_with_file.xml
 */
public final class UserPhraseViewHolder extends BaseHolder {
    private static final String TAG = "UserPhraseViewHolder ";
    private final BubbleMessageTextView mPhraseTextView;
    private final TableRow mRightTextRow;
    private final ImageView mImage;
    private final TextView mRightTextDescr;
    private final TextView mRightTextHeader;
    private final TextView mRightTextTimeStamp;
    private final BubbleTimeTextView mTimeStampTextView;
    private final FrameLayout mPhraseFrame;
    private final ImageView mFileImage;
    private final CircularProgressButton mFileImageButton;
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat fileSdf;
    private final View mFilterView;
    private final View mFilterViewSecond;
    private final ChatStyle style;
    private final ViewGroup ogDataLayout;
    private final ImageView ogImage;
    private final TextView ogTitle;
    private final TextView ogDescription;
    private final TextView ogUrl;
    private final TextView ogTimestamp;
    private Context context;

    public UserPhraseViewHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        context = parent.getContext();
        mPhraseTextView = itemView.findViewById(R.id.text);
        mImage = itemView.findViewById(R.id.image);
        mRightTextRow = itemView.findViewById(R.id.right_text_row);
        mRightTextDescr = itemView.findViewById(R.id.file_specs);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mFileImage = itemView.findViewById(R.id.file_image);
        mFileImageButton = itemView.findViewById(R.id.button_download);
        mPhraseFrame = itemView.findViewById(R.id.phrase_frame);
        ogDataLayout = itemView.findViewById(R.id.og_data_layout);
        ogImage = itemView.findViewById(R.id.og_image);
        ogTitle = itemView.findViewById(R.id.og_title);
        ogDescription = itemView.findViewById(R.id.og_description);
        ogUrl = itemView.findViewById(R.id.og_url);
        ogTimestamp = itemView.findViewById(R.id.og_timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
        mRightTextHeader = itemView.findViewById(R.id.to);
        mRightTextTimeStamp = itemView.findViewById(R.id.send_at);
        View mBubble = itemView.findViewById(R.id.bubble);

        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        }
        style = Config.instance.getChatStyle();
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mRightTextDescr, mPhraseTextView, mRightTextHeader, mRightTextTimeStamp}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        ogTimestamp.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        itemView.findViewById(R.id.delimeter).setBackgroundColor(getColorInt(style.outgoingMessageTextColor));
        mFileImageButton.setBackgroundColorResId(style.outgoingMessageTextColor);
        mPhraseTextView.setLinkTextColor(getColorInt(style.outgoingMessageLinkColor));
        setTintToProgressButtonUser(mFileImageButton, style.chatBodyIconsTint);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
    }

    public void onBind(final UserPhrase userPhrase,
                       final String phrase,
                       final long timeStamp,
                       final MessageState sendState,
                       final Quote quote,
                       final FileDescription fileDescription,
                       final View.OnClickListener imageClickListener,
                       @Nullable final View.OnClickListener fileClickListener,
                       final View.OnClickListener onRowClickListener,
                       final View.OnClickListener onQuoteClickListener,
                       final View.OnLongClickListener onLongClickListener,
                       final boolean isChosen) {
        ViewUtils.setClickListener((ViewGroup) itemView, onLongClickListener);
        ViewUtils.setClickListener((ViewGroup) itemView, onRowClickListener);
        setTimestamp(timeStamp);
        setSendState(sendState);
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.bindTimestampView(mTimeStampTextView);
            String deepLink = UrlUtils.extractDeepLink(phrase);
            String url = UrlUtils.extractLink(phrase);
            if (deepLink != null) {
                final SpannableString text = new SpannableString(phrase);
                LinkifyCompat.addLinks(text, UrlUtils.DEEPLINK_URL, "");
                mPhraseTextView.setText(text);
                mPhraseTextView.setOnClickListener(view -> {
                    UrlUtils.openUrl(context, deepLink);
                });
            } else if (url != null) {
                final SpannableString text = new SpannableString(phrase);
                LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, "");
                mPhraseTextView.setText(text);
                mPhraseTextView.setOnClickListener(view -> {
                    UrlUtils.openUrl(context, url);
                });
                if (userPhrase.ogData == null) {
                    loadOGData(userPhrase, url);
                } else {
                    bindOGData(userPhrase.ogData, url);
                }
            } else {
                mPhraseTextView.setText(phrase);
                mPhraseTextView.setOnClickListener(null);
                hideOGView();
            }
        }
        mImage.setVisibility(View.GONE);
        mFileImage.setVisibility(View.GONE);
        mFileImageButton.setVisibility(View.GONE);
        if (fileDescription != null) {
            if (FileUtils.isImage(fileDescription)) {
                mImage.setVisibility(View.VISIBLE);
                mImage.setOnClickListener(imageClickListener);
                // User image can be already available locally
                if (fileDescription.getFileUri() == null) {
                    Picasso.get()
                            .load(fileDescription.getDownloadPath())
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerCrop()
                            .into(mImage);
                } else {
                    Picasso.get()
                            .load(fileDescription.getFileUri())
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerCrop()
                            .into(mImage);
                }
            } else {
                if (fileDescription.getFileUri() != null) fileDescription.setDownloadProgress(100);
                mRightTextRow.setVisibility(View.VISIBLE);
                ViewUtils.setClickListener(mRightTextRow, (View.OnClickListener) null);
                mFileImageButton.setVisibility(View.VISIBLE);
                long fileSize = fileDescription.getSize();
                mRightTextDescr.setText(FileUtils.getFileName(fileDescription) + "\n" + (fileSize > 0 ? "\n" + Formatter.formatFileSize(itemView.getContext(), fileSize) : ""));
                mRightTextHeader.setText(quote == null ? fileDescription.getFrom() : quote.getPhraseOwnerTitle());
                mRightTextTimeStamp
                        .setText(itemView.getContext().getString(R.string.threads_sent_at, fileSdf.format(new Date(fileDescription.getTimeStamp()))));
                if (fileClickListener != null) {
                    mFileImageButton.setOnClickListener(fileClickListener);
                }
                mFileImageButton.setProgress(fileDescription.getFileUri() != null ? 100 : fileDescription.getDownloadProgress());
            }
        } else if (quote != null) {
            mRightTextRow.setVisibility(View.VISIBLE);
            ViewUtils.setClickListener(mRightTextRow, onQuoteClickListener);
            mRightTextDescr.setText(quote.getText());
            mRightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextTimeStamp.setText(itemView.getContext().getResources().getString(R.string.threads_sent_at, fileSdf.format(new Date(quote.getTimeStamp()))));
            if (quote.getFileDescription() != null) {
                if (FileUtils.isVoiceMessage(quote.getFileDescription())) {
                    mRightTextDescr.setText(R.string.threads_voice_message);
                } else {
                    if (FileUtils.isImage(quote.getFileDescription())) {
                        mFileImage.setVisibility(View.VISIBLE);
                        if (quote.getFileDescription().getFileUri() != null) {
                            Picasso.get()
                                    .load(quote.getFileDescription().getFileUri())
                                    .error(style.imagePlaceholder)
                                    .fit()
                                    .centerCrop()
                                    .into(mFileImage);
                        } else if (quote.getFileDescription().getDownloadPath() != null) {
                            Picasso.get()
                                    .load(quote.getFileDescription().getDownloadPath())
                                    .error(style.imagePlaceholder)
                                    .fit()
                                    .centerCrop()
                                    .into(mFileImage);
                        }
                        if (onQuoteClickListener != null) {
                            mFileImage.setOnClickListener(onQuoteClickListener);
                        }
                    } else {
                        mFileImageButton.setVisibility(View.VISIBLE);
                        long fileSize = quote.getFileDescription().getSize();
                        mRightTextDescr.setText(FileUtils.getFileName(quote.getFileDescription()) + (fileSize > 0 ? "\n" + Formatter.formatFileSize(itemView.getContext(), fileSize) : ""));
                        if (onQuoteClickListener != null) {
                            mFileImageButton.setOnClickListener(onQuoteClickListener);
                        }
                        mFileImageButton.setProgress(quote.getFileDescription().getFileUri() != null ? 100 : quote.getFileDescription().getDownloadProgress());
                    }
                }
            }
        } else {
            mRightTextRow.setVisibility(View.GONE);
        }
        if (quote != null || fileDescription != null) {
            mPhraseFrame.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            mPhraseFrame.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        if (mRightTextHeader.getText() == null ||
                mRightTextHeader.getText().toString().equals("null")) {
            mRightTextHeader.setVisibility(View.GONE);
        } else {
            mRightTextHeader.setVisibility(View.VISIBLE);
        }
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
    }

    private void setTimestamp(long timeStamp) {
        String timeText = sdf.format(new Date(timeStamp));
        mTimeStampTextView.setText(timeText);
        ogTimestamp.setText(timeText);
    }

    private void setSendState(MessageState sendState) {
        final Drawable d;
        switch (sendState) {
            case STATE_WAS_READ:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                ogTimestamp.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                ogTimestamp.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                ogTimestamp.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                ogTimestamp.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
    }

    private void loadOGData(final UserPhrase chatItem, final String url) {
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
                }, e -> ThreadsLogger.w(TAG, "OpenGraph data load failed: ", e))
        );
    }

    private void bindOGData(final OGData ogData, String url) {
        if (ogData == null || ogData.areTextsEmpty()) {
            hideOGView();
            return;
        }
        showOGView();
        if (!TextUtils.isEmpty(ogData.getTitle())) {
            ogTitle.setVisibility(View.VISIBLE);
            ogTitle.setText(ogData.getTitle());
            ogTitle.setTypeface(ogTitle.getTypeface(), Typeface.BOLD);
        } else {
            ogTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(ogData.getDescription())) {
            ogDescription.setVisibility(View.VISIBLE);
            ogDescription.setText(ogData.getDescription());
        } else {
            ogDescription.setVisibility(View.GONE);
        }
        ogUrl.setText(!TextUtils.isEmpty(ogData.getUrl()) ? ogData.getUrl() : url);
        if (TextUtils.isEmpty(ogData.getImageUrl())) {
            ogImage.setVisibility(View.GONE);
        } else {
            Picasso.get()
                    .load(ogData.getImageUrl())
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            ogImage.setVisibility(View.VISIBLE);
                            Picasso.get()
                                    .load(ogData.getImageUrl())
                                    .error(style.imagePlaceholder)
                                    .fit()
                                    .centerInside()
                                    .into(ogImage);
                        }

                        @Override
                        public void onError(Exception e) {
                            ogImage.setVisibility(View.GONE);
                            ThreadsLogger.d(TAG, "Could not load OpenGraph image: " + e.getLocalizedMessage());
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


