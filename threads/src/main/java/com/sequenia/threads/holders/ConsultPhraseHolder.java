package com.sequenia.threads.holders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.RussianFormatSymbols;
import com.sequenia.threads.views.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_consultant_text_with_file.xml
 */
public class ConsultPhraseHolder extends RecyclerView.ViewHolder {
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
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            quoteSdf = new SimpleDateFormat("dd MMMM yyyy");
        }
    }

    public void onBind(String consultPhrase
            , String avatarPath
            , long timeStamp
            , boolean isAvatarVisible
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener fileClickListener
            , View.OnLongClickListener onRowLongClickListener
            , View.OnClickListener onAvatarClickListener
            , boolean isChosen) {
        itemView.setOnLongClickListener(onRowLongClickListener);
        mTimeStampTextView.setText(timeStampSdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onRowLongClickListener);
        }
        if (quote != null) {
            fileRow.setVisibility(View.VISIBLE);
            mCircularProgressButton.setVisibility(View.GONE);
            rightTextHeader.setText(quote.getPhraseOwnerTitle());
            mRightTextDescr.setText(quote.getText());
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.sent_at) + " " + quoteSdf.format(new Date(quote.getTimeStamp())));
            if (TextUtils.isEmpty(quote.getPhraseOwnerTitle())) {
                rightTextHeader.setVisibility(View.GONE);
            } else {
                rightTextHeader.setVisibility(View.VISIBLE);
            }
            if (quote.getFileDescription() != null) {
                mCircularProgressButton.setVisibility(View.VISIBLE);
                String filename = quote.getFileDescription().getIncomingName();
                if (filename==null){
                    filename = FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath())==null?"":FileUtils.getLastPathSegment(quote.getFileDescription().getFilePath());
                }
                mRightTextDescr.setText(filename + "\n" + Formatter.formatFileSize(itemView.getContext(), quote.getFileDescription().getSize()));
                mCircularProgressButton.setOnClickListener(fileClickListener);
                mCircularProgressButton.setProgress(quote.getFileDescription().getDownloadProgress());
            }else {
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
            if (!TextUtils.isEmpty(rightTextHeader.getText())) {
                rightTextHeader.setVisibility(View.VISIBLE);
            } else {
                rightTextHeader.setVisibility(View.GONE);
            }
            String fileHeader = fileDescription.getIncomingName()==null? FileUtils.getLastPathSegment(fileDescription.getFilePath()):fileDescription.getIncomingName();
            mRightTextDescr.setText(fileHeader==null?"":fileHeader + "\n" + android.text.format.Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
            rightTextFileStamp.setText(itemView.getContext().getString(R.string.sent_at) + " " + quoteSdf.format(new Date(fileDescription.getTimeStamp())));
            mCircularProgressButton.setProgress(fileDescription.getDownloadProgress());
        }
        if (fileDescription == null && quote == null) {
            fileRow.setVisibility(View.GONE);
        }
        if (isAvatarVisible) {
            mConsultAvatar.setVisibility(View.VISIBLE);
            mConsultAvatar.setOnClickListener(onAvatarClickListener);
            if (avatarPath != null) {
                Picasso
                        .with(itemView.getContext())
                        .load(avatarPath)
                        .fit()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }
        //  Log.e(TAG, "consultPhrase = "+consultPhrase);// TODO: 14.07.2016
        mPhraseTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    if (mPhraseTextView.getLayout() == null) {
                        mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        //      Log.e(TAG, "mPhraseTextView.getLayout()==null");// TODO: 14.07.2016
                        return;
                    }
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    if (mPhraseTextView.getText().length() > 1 && mPhraseTextView.getLayout().getPrimaryHorizontal(mPhraseTextView.getText().length() - 1) > (mTimeStampTextView.getLeft() - density * 50)) {
                        params.setMargins(0, mPhraseTextView.getLineHeight() * mPhraseTextView.getLayout().getLineCount() + (3 * (int) (density)), 0, 0);
                    }
                    //   Log.e(TAG, " mPhraseTextView.getLineHeight() * mPhraseTextView.getLayout().getLineCount() + (3 * (int) (density)) = "+ mPhraseTextView.getLineHeight() * mPhraseTextView.getLayout().getLineCount() + (3 * (int) (density)));// TODO: 14.07.2016
                    mTimeStampTextView.setLayoutParams(params);
                    mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        if (consultPhrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(consultPhrase);
        }
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
        if (avatarPath == null) {
            mConsultAvatar.setVisibility(View.GONE);
        }

    }
}
