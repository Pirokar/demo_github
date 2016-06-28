package com.sequenia.threads.holders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import com.sequenia.threads.RussianFormatSymbols;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;

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
    private TableRow mFileRow;
    private TextView mFileDescrTextView;
    private TextView mTimeStampTextView;
    private ImageButton mFileImageButton;
    private SimpleDateFormat sdf;
    private SimpleDateFormat fileSdf;
    private View mFilterView;
    private View mFilterViewSecond;

    public UserPhraseViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_text_with_file, parent, false));
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
        mFileRow = (TableRow) itemView.findViewById(R.id.file_row);
        mFileDescrTextView = (TextView) itemView.findViewById(R.id.text_description);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        mFileImageButton = (ImageButton) itemView.findViewById(R.id.file);
        sdf = new SimpleDateFormat("HH:mm", Locale.US);
        fileSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterViewSecond = itemView.findViewById(R.id.filter_bottom);
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
        ViewGroup vg = (ViewGroup) itemView;
        itemView.setOnLongClickListener(onLongClickListener);
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
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
            mFileRow.setVisibility(View.VISIBLE);
            mFileImageButton.setVisibility(View.GONE);
            String text = quote.getHeader() + "\n" + quote.getText() + "\n" + fileSdf.format(new Date(quote.getTimeStamp()));
            mFileDescrTextView.setText(text);
            mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        } else if (fileDescription != null) {
            mFileRow.setVisibility(View.VISIBLE);
            mFileImageButton.setVisibility(View.VISIBLE);
            String text = fileDescription.getHeader() + "\n" + fileDescription.getText() + "\n" + fileSdf.format(new Date(fileDescription.getTimeStamp()));
            mFileDescrTextView.setText(text);
            if (fileClickListener != null) {
                mFileImageButton.setOnClickListener(fileClickListener);
            }
            mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        } else {
            mFileRow.setVisibility(View.GONE);
        }
        mPhraseTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
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
