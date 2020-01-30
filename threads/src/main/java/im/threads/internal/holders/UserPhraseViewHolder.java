package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import im.threads.internal.picasso_url_connection_only.Callback;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.UrlUtils;
import im.threads.internal.utils.ViewUtils;
import im.threads.internal.views.CircularProgressButton;

/**
 * layout/item_user_text_with_file.xml
 */
public final class UserPhraseViewHolder extends BaseHolder {
    private static final String TAG = "UserPhraseViewHolder ";
    private TextView mPhraseTextView;
    private TableRow mRightTextRow;
    private ImageView mImage;
    private TextView mRightTextDescr;
    private TextView mRightTextHeader;
    private TextView mRightTextTimeStamp;
    private TextView mTimeStampTextView;
    private FrameLayout mPhraseFrame;
    private CircularProgressButton mFileImageButton;
    private SimpleDateFormat sdf;
    private SimpleDateFormat fileSdf;
    private View mFilterView;
    private View mFilterViewSecond;
    private ChatStyle style;
    private View mBubble;
    private ViewGroup ogDataLayout;
    private ImageView ogImage;
    private TextView ogTitle;
    private TextView ogDescription;
    private TextView ogUrl;
    private TextView ogTimestamp;

    public UserPhraseViewHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        mPhraseTextView = itemView.findViewById(R.id.text);
        mImage = itemView.findViewById(R.id.image);
        mRightTextRow = itemView.findViewById(R.id.right_text_row);
        mRightTextDescr = itemView.findViewById(R.id.file_specs);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
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
        mBubble = itemView.findViewById(R.id.bubble);

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
        mFileImageButton.setBackgroundColor(getColorInt(style.outgoingMessageTextColor));
        mPhraseTextView.setLinkTextColor(getColorInt(style.outgoingMessageLinkColor));
        setTintToProgressButtonUser(mFileImageButton, style.outgoingMessageTextColor, style.chatBodyIconsTint);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
    }

    public void onBind(final UserPhrase userPhrase,
                       final String phrase,
                       final long timeStamp,
                       final MessageState sentState,
                       final Quote quote,
                       final FileDescription fileDescription,
                       final View.OnClickListener imageClickListener,
                       @Nullable final View.OnClickListener fileClickListener,
                       final View.OnClickListener onRowClickListener,
                       final View.OnLongClickListener onLongClickListener,
                       Runnable onItemChangedListener,
                       final boolean isChosen) {
        ViewUtils.setClickListener((ViewGroup) itemView, onLongClickListener);
        ViewUtils.setClickListener((ViewGroup) itemView, onRowClickListener);
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(phrase);
            List<String> urls = UrlUtils.extractLinks(phrase);
            if (!urls.isEmpty()) {
                final String url = urls.get(0);
                if (userPhrase.ogData == null) {
                    loadOGData(onItemChangedListener, userPhrase, url);
                } else {
                    if (!userPhrase.ogData.areTextsEmpty()) {
                        bindOGData(userPhrase.ogData, url);
                        showOGView();
                    } else {
                        hideOGView();
                    }
                }
            } else {
                hideOGView();
            }
        }
        mImage.setVisibility(View.GONE);
        String timeText = sdf.format(new Date(timeStamp));
        mTimeStampTextView.setText(timeText);
        ogTimestamp.setText(timeText);
        if (fileDescription == null && quote == null) {
            mRightTextRow.setVisibility(View.GONE);
        }
        if (quote != null) {
            mRightTextRow.setVisibility(View.VISIBLE);
            mRightTextRow.setVisibility(View.VISIBLE);
            mFileImageButton.setVisibility(View.GONE);
            mRightTextDescr.setText(quote.getText());
            mRightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextTimeStamp.setText(itemView.getContext().getResources().getText(R.string.threads_sent_at) + " " + fileSdf.format(quote.getTimeStamp()));
            mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));//TODO why set it here? It is already set earlier
            if (quote.getFileDescription() != null) {
                if (quote.getFileDescription().getFilePath() != null)
                    quote.getFileDescription().setDownloadProgress(100);
                mFileImageButton.setVisibility(View.VISIBLE);
                String filename = quote.getFileDescription().getIncomingName();
                if (filename == null) {
                    filename = FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath()) == null ? "" : FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath());
                }
                mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), quote.getFileDescription().getSize()));
                if (null != fileClickListener)
                    mFileImageButton.setOnClickListener(fileClickListener);
                mFileImageButton.setProgress(quote.getFileDescription().getDownloadProgress());
            }
        }
        if (fileDescription != null) {
            if (FileUtils.isImage(fileDescription)) {
                mRightTextRow.setVisibility(View.GONE);
                mFileImageButton.setVisibility(View.GONE);
                mImage.setVisibility(View.VISIBLE);
                mImage.setOnClickListener(imageClickListener);
                // User image can be already available locally
                if (TextUtils.isEmpty(fileDescription.getFilePath())) {
                    Picasso.with(itemView.getContext())
                            .load(fileDescription.getDownloadPath())
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerCrop()
                            .into(mImage);
                } else {
                    Picasso.with(itemView.getContext())
                            .load(new File(fileDescription.getFilePath()))
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerCrop()
                            .into(mImage);
                }
            } else {
                if (fileDescription.getFilePath() != null) fileDescription.setDownloadProgress(100);
                mRightTextRow.setVisibility(View.VISIBLE);
                mFileImageButton.setVisibility(View.VISIBLE);
                String filename = fileDescription.getIncomingName();
                if (filename == null) {
                    filename = FileUtils.getLastPathSegment(fileDescription.getFilePath()) == null ? "" : FileUtils
                            .getLastPathSegment(fileDescription.getFilePath());
                }
                mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
                mRightTextHeader.setText(quote == null ? fileDescription.getFrom() : quote.getPhraseOwnerTitle());
                mRightTextTimeStamp
                        .setText(itemView.getContext().getResources().getText(R.string.threads_sent_at) + " " + fileSdf.format(fileDescription.getTimeStamp()));
                if (fileClickListener != null) {
                    mFileImageButton.setOnClickListener(fileClickListener);
                }
                mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));//TODO why set it here? It is already set earlier
                if (fileDescription.getFilePath() != null) {
                    mFileImageButton.setProgress(100);
                } else {
                    mFileImageButton.setProgress(fileDescription.getDownloadProgress());
                }
            }
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
        final Drawable d;
        switch (sentState) {
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
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
    }

    private void loadOGData(Runnable onItemChangedListener, final UserPhrase chatItem, final String url) {
        OGDataProvider.getOGData(url, new im.threads.internal.utils.Callback<OGData, Throwable>() {
            @Override
            public void onSuccess(OGData ogData) {
                ThreadsLogger.d(TAG, "OGData for url: " + url + "\n received: " + ogData);
                if (ogData != null && !ogData.isEmpty()) {
                    chatItem.ogData = ogData;
                    chatItem.ogUrl = url;
                    onItemChangedListener.run();
                }
            }

            @Override
            public void onError(Throwable error) {
                ThreadsLogger.w(TAG, "OpenGraph data load failed: ", error);
            }
        });
    }

    private void bindOGData(@NonNull final OGData ogData, String url) {
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
            Picasso.with(itemView.getContext())
                    .load(ogData.image)
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            ogImage.setVisibility(View.VISIBLE);
                            Picasso.with(itemView.getContext())
                                    .load(ogData.image)
                                    .error(style.imagePlaceholder)
                                    .fit()
                                    .centerInside()
                                    .into(ogImage);
                        }

                        @Override
                        public void onError() {
                            ogImage.setVisibility(View.GONE);
                            ThreadsLogger.d(TAG, "Could not load OpenGraph image");
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


