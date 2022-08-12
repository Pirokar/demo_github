package im.threads.internal.holders;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.domain.logger.LoggerEdna;
import im.threads.internal.domain.ogParser.OpenGraphParser;
import im.threads.internal.domain.ogParser.OpenGraphParserJsoupImpl;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.internal.imageLoading.ImageLoader;
import im.threads.internal.markdown.MarkdownProcessor;
import im.threads.internal.markdown.MarkwonMarkdownProcessor;
import im.threads.internal.model.CampaignMessage;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Quote;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.UrlUtils;
import im.threads.internal.utils.ViewUtils;
import im.threads.internal.views.CircularProgressButton;
import im.threads.internal.views.VoiceTimeLabelFormatter;
import im.threads.internal.views.VoiceTimeLabelFormatterKt;
import im.threads.internal.widget.text_view.BubbleMessageTextView;
import im.threads.internal.widget.text_view.BubbleTimeTextView;

/**
 * layout/item_user_text_with_file.xml
 */
public final class UserPhraseViewHolder extends VoiceMessageBaseHolder {
    private final BubbleMessageTextView mPhraseTextView;
    private final TableRow mRightTextRow;
    private final ImageView mImage;
    private final TextView mRightTextDescr;
    private final TextView mRightTextHeader;
    private final TextView mRightTextTimeStamp;
    private final TableRow quoteTextRow;
    private final ImageView quoteImage;
    private final TextView quoteTextDescr;
    private final TextView quoteTextHeader;
    private final TextView quoteTextTimeStamp;
    private final BubbleTimeTextView mTimeStampTextView;
    private final FrameLayout mPhraseFrame;
    private final CircularProgressButton mFileImageButton;
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat fileSdf;
    private final View mFilterView;
    private final View mFilterViewSecond;
    private final ChatStyle style;
    private final ViewGroup ogDataLayout;
    private final TextView ogTimestamp;
    private final ViewGroup voiceMessage;
    private final Slider slider;
    private final ImageView buttonPlayPause;
    private final TextView fileSizeTextView;

    private final Context context;
    private FileDescription fileDescription = null;
    @NonNull
    private String formattedDuration = "";
    private final MarkdownProcessor markdownProcessor = new MarkwonMarkdownProcessor();
    private final OpenGraphParser openGraphParser = new OpenGraphParserJsoupImpl();

    public UserPhraseViewHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        context = parent.getContext();
        mPhraseTextView = itemView.findViewById(R.id.text);
        mImage = itemView.findViewById(R.id.image);
        quoteTextRow = itemView.findViewById(R.id.quote_text_row);
        quoteTextDescr = itemView.findViewById(R.id.quote_file_specs);
        quoteImage = itemView.findViewById(R.id.quote_image);
        quoteTextHeader = itemView.findViewById(R.id.quote_to);
        quoteTextTimeStamp = itemView.findViewById(R.id.quote_send_at);
        mRightTextRow = itemView.findViewById(R.id.right_text_row);
        mRightTextDescr = itemView.findViewById(R.id.file_specs);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mFileImageButton = itemView.findViewById(R.id.button_download);
        mPhraseFrame = itemView.findViewById(R.id.phrase_frame);
        ogDataLayout = itemView.findViewById(R.id.og_data_layout);
        ogTimestamp = itemView.findViewById(R.id.og_timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
        mRightTextHeader = itemView.findViewById(R.id.to);
        mRightTextTimeStamp = itemView.findViewById(R.id.send_at);
        View mBubble = itemView.findViewById(R.id.bubble);
        voiceMessage = itemView.findViewById(R.id.voice_message);
        buttonPlayPause = itemView.findViewById(R.id.voice_message_user_button_play_pause);
        slider = itemView.findViewById(R.id.voice_message_user_slider);
        fileSizeTextView = itemView.findViewById(R.id.file_size);

        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        }
        style = Config.instance.getChatStyle();
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.setPadding(
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingLeft),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingTop),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingRight),
                itemView.getContext().getResources().getDimensionPixelSize(style.bubbleOutgoingPaddingBottom)
        );
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        setTextColorToViews(new TextView[]{mRightTextDescr, mPhraseTextView, mRightTextHeader, mRightTextTimeStamp, fileSizeTextView, quoteTextDescr, quoteTextHeader, quoteTextTimeStamp}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        if (style.outgoingMessageTimeTextSize > 0)
            mTimeStampTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.getContext().getResources().getDimension(style.outgoingMessageTimeTextSize));
        ogTimestamp.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        itemView.findViewById(R.id.delimeter).setBackgroundColor(getColorInt(style.outgoingMessageTextColor));
        mFileImageButton.setBackgroundColorResId(style.outgoingMessageTextColor);
        mPhraseTextView.setLinkTextColor(getColorInt(style.outgoingMessageLinkColor));
        setUpProgressButton(mFileImageButton);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        buttonPlayPause.setColorFilter(getColorInt(style.outgoingPlayPauseButtonColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(final UserPhrase userPhrase,
                       final String formattedDuration,
                       final View.OnClickListener imageClickListener,
                       @Nullable final View.OnClickListener fileClickListener,
                       View.OnClickListener buttonClickListener,
                       View.OnClickListener onRowClickListener,
                       Slider.OnChangeListener onChangeListener,
                       Slider.OnSliderTouchListener onSliderTouchListener,
                       final View.OnClickListener onQuoteClickListener,
                       final View.OnLongClickListener onLongClickListener,
                       final boolean isChosen) {
        final String phrase = userPhrase.getPhraseText() != null ? userPhrase.getPhraseText().trim() : null;
        final long timeStamp = userPhrase.getTimeStamp();
        final MessageState sendState = userPhrase.getSentState();
        final Quote quote = userPhrase.getQuote();
        final CampaignMessage campaignMessage = userPhrase.getCampaignMessage();
        this.fileDescription = userPhrase.getFileDescription();
        this.formattedDuration = formattedDuration;
        ViewUtils.setClickListener((ViewGroup) itemView, onLongClickListener);
        ViewUtils.setClickListener((ViewGroup) itemView, onRowClickListener);
        buttonPlayPause.setOnClickListener(buttonClickListener);
        slider.addOnChangeListener(onChangeListener);
        slider.addOnSliderTouchListener(onSliderTouchListener);
        slider.setLabelFormatter(new VoiceTimeLabelFormatter());
        fileSizeTextView.setText(formattedDuration);
        setTimestamp(timeStamp);
        setSendState(sendState);
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.bindTimestampView(mTimeStampTextView);
            String deepLink = UrlUtils.extractDeepLink(phrase);
            String url = UrlUtils.extractLink(phrase);
            highlightClientText(mPhraseTextView, phrase);
            if (url != null) {
                bindOGData(ogDataLayout, mTimeStampTextView, url);
            } else {
                hideOGView(ogDataLayout, mTimeStampTextView);
            }
        }
        mImage.setVisibility(View.GONE);
        quoteImage.setVisibility(View.GONE);
        mFileImageButton.setVisibility(View.GONE);
        voiceMessage.setVisibility(View.GONE);
        quoteTextRow.setVisibility(View.GONE);
        mRightTextRow.setVisibility(View.GONE);
        if (fileDescription != null) {
            if (FileUtils.isVoiceMessage(fileDescription)) {
                mPhraseTextView.setVisibility(View.GONE);
                voiceMessage.setVisibility(View.VISIBLE);
            } else {
                if (FileUtils.isImage(fileDescription)) {
                    mImage.setVisibility(View.VISIBLE);
                    mImage.setOnClickListener(imageClickListener);
                    // User image can be already available locally
                    String downloadPath;
                    if (fileDescription.getFileUri() == null) {
                        downloadPath = fileDescription.getDownloadPath();
                    } else {
                        downloadPath = fileDescription.getFileUri().toString();
                    }

                    ImageLoader
                            .get()
                            .load(downloadPath)
                            .autoRotateWithExif(true)
                            .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                            .errorDrawableResourceId(style.imagePlaceholder)
                            .into(mImage);
                } else {
                    if (fileDescription.getFileUri() != null) {
                        fileDescription.setDownloadProgress(100);
                    }
                    mRightTextRow.setVisibility(View.VISIBLE);
                    ViewUtils.setClickListener(mRightTextRow, (View.OnClickListener) null);
                    mFileImageButton.setVisibility(View.VISIBLE);
                    long fileSize = fileDescription.getSize();
                    mRightTextDescr.setText(FileUtils.getFileName(fileDescription) + "\n" + (fileSize > 0 ? "\n" + Formatter.formatFileSize(itemView.getContext(), fileSize) : ""));
                    mRightTextHeader.setText(fileDescription.getFrom());
                    mRightTextTimeStamp
                            .setText(itemView.getContext().getString(R.string.threads_sent_at, fileSdf.format(new Date(fileDescription.getTimeStamp()))));
                    if (fileClickListener != null) {
                        mFileImageButton.setOnClickListener(fileClickListener);
                    }
                    mFileImageButton.setProgress(fileDescription.getFileUri() != null ? 100 : fileDescription.getDownloadProgress());
                }
            }
        }
        if (quote != null) {
            showQuote(quote, onQuoteClickListener);
        } else if (campaignMessage != null) {
            showCampaign(campaignMessage);
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

    private void showQuote(Quote quote, View.OnClickListener onQuoteClickListener) {
        quoteTextRow.setVisibility(View.VISIBLE);
        ViewUtils.setClickListener(quoteTextRow, onQuoteClickListener);
        quoteTextDescr.setText(quote.getText());
        quoteTextHeader.setText(quote.getPhraseOwnerTitle());
        quoteTextTimeStamp.setText(itemView.getContext().getResources().getString(R.string.threads_sent_at, fileSdf.format(new Date(quote.getTimeStamp()))));
        if (quote.getFileDescription() != null) {
            if (FileUtils.isImage(quote.getFileDescription())) {
                quoteImage.setVisibility(View.VISIBLE);
                String downloadPath = "";
                if (quote.getFileDescription().getFileUri() != null) {
                    downloadPath = quote.getFileDescription().getFileUri().toString();
                } else if (quote.getFileDescription().getDownloadPath() != null) {
                    downloadPath = quote.getFileDescription().getDownloadPath();
                }

                ImageLoader
                        .get()
                        .autoRotateWithExif(true)
                        .load(downloadPath)
                        .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                        .errorDrawableResourceId(style.imagePlaceholder)
                        .into(quoteImage);
                if (onQuoteClickListener != null) {
                    quoteImage.setOnClickListener(onQuoteClickListener);
                }
            } else if (FileUtils.isVoiceMessage(quote.getFileDescription())) {
                quoteTextDescr.setText(R.string.threads_voice_message);
            } else {
                quoteTextDescr.setText(R.string.threads_file);
            }
        }
    }

    private void showCampaign(CampaignMessage campaignMessage) {
        quoteTextRow.setVisibility(View.VISIBLE);
        quoteTextDescr.setText(campaignMessage.getText());
        quoteTextHeader.setText(campaignMessage.getSenderName());
        quoteTextTimeStamp.setText(itemView.getContext().getResources().getString(R.string.threads_sent_at, fileSdf.format(campaignMessage.getReceivedDate())));
    }

    @Nullable
    @Override
    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public void init(int maxValue, int progress, boolean isPlaying) {
        int effectiveProgress = Math.min(progress, maxValue);
        fileSizeTextView.setText(VoiceTimeLabelFormatterKt.formatAsDuration(effectiveProgress));
        slider.setEnabled(true);
        slider.setValueTo(maxValue);
        slider.setValue(effectiveProgress);
        buttonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
    }

    @Override
    public void updateProgress(int progress) {
        fileSizeTextView.setText(VoiceTimeLabelFormatterKt.formatAsDuration(progress));
        slider.setValue(Math.min(progress, slider.getValueTo()));
    }

    @Override
    public void updateIsPlaying(boolean isPlaying) {
        buttonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
    }

    @Override
    public void resetProgress() {
        fileSizeTextView.setText(formattedDuration);
        slider.setEnabled(false);
        slider.setValue(0);
        buttonPlayPause.setImageResource(style.voiceMessagePlayButton);
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
}


