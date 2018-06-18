package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.Quote;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.FileUtils;
import im.threads.utils.ViewUtils;
import im.threads.views.CircularProgressButton;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_user_text_with_file.xml
 */
public class UserPhraseViewHolder extends BaseHolder {
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
    private static
    @ColorInt
    int messageColor;

    public UserPhraseViewHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        mRightTextRow = (TableRow) itemView.findViewById(R.id.right_text_row);
        mRightTextDescr = (TextView) itemView.findViewById(R.id.file_specs);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFileImageButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);
        mPhraseFrame = (FrameLayout) itemView.findViewById(R.id.phrase_frame);
        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            fileSdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
        mRightTextHeader = (TextView) itemView.findViewById(R.id.to);
        mRightTextTimeStamp = (TextView) itemView.findViewById(R.id.send_at);
        mBubble = itemView.findViewById(R.id.bubble);

        if (style == null) style = ChatStyle.getInstance();
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);

        mBubble.setBackground(ContextCompat.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        messageColor = ContextCompat.getColor(itemView.getContext(), style.outgoingMessageTextColor);
        setTextColorToViews(new TextView[]{mRightTextDescr, mPhraseTextView, mRightTextHeader, mRightTextTimeStamp, mTimeStampTextView}, style.outgoingMessageTextColor);
        itemView.findViewById(R.id.delimeter).setBackgroundColor(getColorInt(style.outgoingMessageTextColor));
        mFileImageButton.setBackgroundColor(getColorInt(style.outgoingMessageTextColor));

        mPhraseTextView.setLinkTextColor(getColorInt(style.outgoingMessageLinkColor));

        setTintToProgressButtonUser(mFileImageButton, style.outgoingMessageTextColor, style.chatBodyIconsTint);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterViewSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
    }

    public void onBind(final String phrase
            , final long timeStamp
            , final MessageState sentState
            , final Quote quote
            , final FileDescription fileDescription
            , final View.OnClickListener imageClickListener
            , @Nullable final View.OnClickListener fileClickListener
            , final View.OnClickListener onRowClickListener
            , final View.OnLongClickListener onLongClickListener
            , final boolean isChosen) {
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(phrase);
        }
        mImage.setVisibility(View.GONE);
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        ViewUtils.setClickListener((ViewGroup) itemView, onLongClickListener);
        ViewUtils.setClickListener((ViewGroup) itemView, onRowClickListener);
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
            mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
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

                Picasso.with(itemView.getContext())
                        .load(fileDescription.getFilePath())
                        .error(style.imagePlaceholder)
                        .fit()
                        .centerCrop()
                        .into(mImage);
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
                mRightTextHeader.setText(quote == null ? fileDescription.getFileSentTo() : quote.getPhraseOwnerTitle());
                mRightTextTimeStamp
                        .setText(itemView.getContext().getResources().getText(R.string.threads_sent_at) + " " + fileSdf.format(fileDescription.getTimeStamp()));
                if (fileClickListener != null) {
                    mFileImageButton.setOnClickListener(fileClickListener);
                }
                mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
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
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
    }
}


