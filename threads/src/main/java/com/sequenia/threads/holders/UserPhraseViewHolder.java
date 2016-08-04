package com.sequenia.threads.holders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.RussianFormatSymbols;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_user_text_with_file.xml
 */
public class UserPhraseViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "UserPhraseViewHolder ";
    private TextView mPhraseTextView;
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

    public UserPhraseViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
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
    }

    public void onBind(String phrase
            , long timeStamp
            , MessageState sentState
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener fileClickListener
            , View.OnClickListener onRowClickListener
            , View.OnLongClickListener onLongClickListener
            , boolean isChosen) {
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        itemView.setOnLongClickListener(onLongClickListener);
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(onRowClickListener);
            vg.getChildAt(i).setOnLongClickListener(onLongClickListener);
        }
        if (phrase == null || phrase.length() == 0) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(phrase);
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
                mFileImageButton.setVisibility(View.VISIBLE);
                String filename = quote.getFileDescription().getIncomingName();
                if (filename==null){
                    filename = FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath())==null?"":FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath());
                }
                mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), quote.getFileDescription().getSize()));
                mFileImageButton.setOnClickListener(fileClickListener);
                mFileImageButton.setProgress(quote.getFileDescription().getDownloadProgress());
            }
        }
        if (fileDescription != null) {
            mRightTextRow.setVisibility(View.VISIBLE);
            mFileImageButton.setVisibility(View.VISIBLE);
            String filename = fileDescription.getIncomingName();
            if (filename==null){
                filename = FileUtils.getLastPathSegment(fileDescription.getFilePath())==null?"":FileUtils.getLastPathSegment(fileDescription.getFilePath());
            }
            mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
            mRightTextHeader.setText(quote == null ? fileDescription.getFileSentTo() : quote.getPhraseOwnerTitle());
            mRightTextTimeStamp.setText(itemView.getContext().getResources().getText(R.string.sent_at) + " " + fileSdf.format(fileDescription.getTimeStamp()));
            if (fileClickListener != null) {
                mFileImageButton.setOnClickListener(fileClickListener);
            }
            mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
            if (fileDescription.getFilePath()!=null){
                mFileImageButton.setProgress(100);
            }else {
                mFileImageButton.setProgress(fileDescription.getDownloadProgress());
            }

        }
        if (fileDescription == null && quote == null) {
            mRightTextRow.setVisibility(View.GONE);
        }
        mPhraseTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    if (mPhraseTextView.getLayout() == null) {
                        mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        return;
                    }
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    if (mPhraseTextView.getText().length() > 1 && mPhraseTextView.getLayout().getPrimaryHorizontal(mPhraseTextView.getText().length() - 1) > (mTimeStampTextView.getLeft() - density * 40)) {
                        params.setMargins(0, mPhraseTextView.getLineHeight() * mPhraseTextView.getLayout().getLineCount() + (2 * (int) (density)), 0, 0);
                    }
                    mTimeStampTextView.setLayoutParams(params);
                    mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        switch (sentState) {
            case STATE_SENT_AND_SERVER_RECEIVED:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_white_18dp, 0);
                break;
            case STATE_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_white_18dp, 0);
                break;
            case STATE_NOT_SENT:
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cached_white_18dp, 0);
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
