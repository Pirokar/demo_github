package com.sequenia.threads.holders;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.formatters.RussianFormatSymbols;
import com.sequenia.threads.utils.UserPhrasesCash;
import com.sequenia.threads.utils.ViewUtils;
import com.sequenia.threads.views.CashedLayoutTextView;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_user_text_with_file.xml
 */
public class UserPhraseViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "UserPhraseViewHolder ";
    private CashedLayoutTextView mPhraseTextView;
    private TableRow mRightTextRow;
    private TextView mRightTextDescr;
    private TextView mRightTextHeader;
    private TextView mRightTextTimeStamp;
    private TextView mTimeStampTextView;
    private CircularProgressButton mFileImageButton;
    private SimpleDateFormat sdf;
    private SimpleDateFormat fileSdf;
    private View mFilterView;
    private View mFilterViewSecond;
    private RelativeLayout phraseFrame;
    private UserPhrasesCash cash = UserPhrasesCash.getInstance();


    public UserPhraseViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        mPhraseTextView = (CashedLayoutTextView) itemView.findViewById(R.id.text);
        mRightTextRow = (TableRow) itemView.findViewById(R.id.right_text_row);
        mRightTextDescr = (TextView) itemView.findViewById(R.id.file_specs);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFileImageButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);
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
        phraseFrame = (RelativeLayout) itemView.findViewById(R.id.phrase_frame);
    }

    public void onBind(final String phrase
            , long timeStamp
            , MessageState sentState
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener fileClickListener
            , View.OnClickListener onRowClickListener
            , View.OnLongClickListener onLongClickListener
            , boolean isChosen) {
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(phrase);
        }
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
            mRightTextTimeStamp.setText(itemView.getContext().getResources().getText(R.string.sent_at) + " " + fileSdf.format(quote.getTimeStamp()));
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
            if (fileDescription.getFilePath() != null) fileDescription.setDownloadProgress(100);
            mRightTextRow.setVisibility(View.VISIBLE);
            mFileImageButton.setVisibility(View.VISIBLE);
            String filename = fileDescription.getIncomingName();
            if (filename == null) {
                filename = FileUtils.getLastPathSegment(fileDescription.getFilePath()) == null ? "" : FileUtils.getLastPathSegment(fileDescription.getFilePath());
            }
            mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
            mRightTextHeader.setText(quote == null ? fileDescription.getFileSentTo() : quote.getPhraseOwnerTitle());
            mRightTextTimeStamp.setText(itemView.getContext().getResources().getText(R.string.sent_at) + " " + fileSdf.format(fileDescription.getTimeStamp()));
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
        RelativeLayout.LayoutParams timeStampParams = (RelativeLayout.LayoutParams) mTimeStampTextView.getLayoutParams();
        int ruleRightOf = RelativeLayout.RIGHT_OF;
        int ruleAlignRight = RelativeLayout.ALIGN_RIGHT;
        int ruleAlignParentRight = RelativeLayout.ALIGN_PARENT_RIGHT;
        if (quote == null && fileDescription == null) {
            phraseFrame.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            timeStampParams.removeRule(ruleAlignParentRight);
            int screenWidth = itemView.getResources().getDisplayMetrics().widthPixels;
            int maxOneLineLength = 21;
            if (screenWidth == 768) maxOneLineLength = 23;
            if (screenWidth == 1080) maxOneLineLength = 24;
            if (phrase == null || phrase.length() < maxOneLineLength) {
                timeStampParams.removeRule(ruleAlignRight);
                timeStampParams.addRule(ruleRightOf, mPhraseTextView.getId());
            } else {
                timeStampParams.removeRule(ruleRightOf);
                timeStampParams.addRule(ruleAlignRight, mPhraseTextView.getId());
                if (cash.contains(phrase)) {
                    mPhraseTextView.setText(phrase+"\n");
                }else {
                    mPhraseTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            Layout l = mPhraseTextView.getLayout();
                            if (l == null) return;
                            if (l.getPrimaryHorizontal(mPhraseTextView.getText().length())
                                    > (mTimeStampTextView.getLeft())) {
                                mPhraseTextView.setText(phrase + "\n");
                                cash.add(phrase);
                            }
                        }
                    });
                }
            }
        } else {
            phraseFrame.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            timeStampParams.addRule(ruleAlignParentRight);
            int lastSize = phrase.length() % 36;
            if (lastSize > 28) {
                mPhraseTextView.setText(phrase + "\n ");
            }
        }
        if (mRightTextHeader.getText() == null || mRightTextHeader.getText().toString().equals("null")) {
            mRightTextHeader.setVisibility(View.GONE);
        } else {
            mRightTextHeader.setVisibility(View.VISIBLE);
        }
        switch (sentState) {
            case STATE_WAS_READ:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_white_18dp, 0);
                break;
            case STATE_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_white_18dp, 0);
                break;
            case STATE_NOT_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cached_white_18dp, 0);
                break;
            case STATE_SENDING:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.empty_space_24dp, 0);
                break;
        }
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
        if (phrase == null) return;
    }
}

