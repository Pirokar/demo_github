package com.sequenia.threads.holders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.RussianFormatSymbols;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_consultant_text_with_file.xml
 */
public class ConsultPhraseHolder extends RecyclerView.ViewHolder {
    private View fileRow;
    private CircularProgressButton mCircularProgressButton;
    private TextView fileToUserHeader;
    private TextView mFileSpecs;
    private TextView mFileTimeStamp;
    private TextView mTimeStampTextView;
    private TextView mPhraseTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
    private SimpleDateFormat timesSampSdf = new SimpleDateFormat("HH:mm");
    public ImageView mConsultAvatar;

    public ConsultPhraseHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant_text_with_file, parent, false));
        fileRow = itemView.findViewById(R.id.file_row);
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);
        fileToUserHeader = (TextView) itemView.findViewById(R.id.to);
        mFileSpecs = (TextView) itemView.findViewById(R.id.file_specs);
        mFileTimeStamp = (TextView) itemView.findViewById(R.id.send_at);
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
    }


    public ConsultPhraseHolder(View itemView) {
        super(itemView);
        fileRow = itemView.findViewById(R.id.file_row);
        mCircularProgressButton = (CircularProgressButton) itemView.findViewById(R.id.button_download);
        fileToUserHeader = (TextView) itemView.findViewById(R.id.to);
        mFileSpecs = (TextView) itemView.findViewById(R.id.file_specs);
        mFileTimeStamp = (TextView) itemView.findViewById(R.id.send_at);
        mPhraseTextView = (TextView) itemView.findViewById(R.id.text);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        mTimeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
    }

    public void onBind(String consultPhrase
            , long timeStamp
            , boolean isAvatarVisible
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener onAttachClickListener
            , View.OnLongClickListener onRowLongClickListener
            , int progress) {

        itemView.setOnLongClickListener(onRowLongClickListener);
        mTimeStampTextView.setText(timesSampSdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onRowLongClickListener);
        }

        if (consultPhrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(consultPhrase);
        }
        if (quote != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.GONE);
            fileToUserHeader.setText(quote.getHeader());
            mFileSpecs.setText(quote.getText());
            mFileTimeStamp.setText(itemView.getContext().getString(R.string.send_at) + " " + sdf.format(new Date(quote.getTimeStamp())));
        } else if (fileDescription != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.VISIBLE);
            if (onAttachClickListener != null) {
                mCircularProgressButton.setOnClickListener(onAttachClickListener);
            }
            fileToUserHeader.setText(fileDescription.getHeader());
            mFileSpecs.setText(fileDescription.getText());
            mFileTimeStamp.setText(itemView.getContext().getString(R.string.send_at) + " " + sdf.format(new Date(fileDescription.getTimeStamp())));
            mCircularProgressButton.setProgress(progress);

        } else {
            fileRow.setVisibility(View.GONE);
        }
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }

        mPhraseTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    if (mPhraseTextView.getText().length() > 1 && mPhraseTextView.getLayout().getPrimaryHorizontal(mPhraseTextView.getText().length() - 1) > (mTimeStampTextView.getLeft() - density * 50)) {
                        params.setMargins(0, mPhraseTextView.getLineHeight() * mPhraseTextView.getLayout().getLineCount() + (3 * (int) (density)), 0, 0);
                    }
                    mTimeStampTextView.setLayoutParams(params);
                    mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void setDownloadProgress(int progress) {
        mCircularProgressButton.setProgress(progress);
    }
}
