package com.sequenia.threads.holders;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.picasso_url_connection_only.Callback;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.formatters.RussianFormatSymbols;
import com.sequenia.threads.utils.ViewUtils;
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
    private View mTextFrame;

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
        mTextFrame = itemView.findViewById(R.id.frame);
    }

    public void onBind(final String consultPhrase
            , String avatarPath
            , long timeStamp
            , boolean isAvatarVisible
            , Quote quote
            , FileDescription fileDescription
            , @Nullable View.OnClickListener fileClickListener
            , View.OnLongClickListener onRowLongClickListener
            , View.OnClickListener onAvatarClickListener
            , boolean isChosen) {
        if (consultPhrase == null) {
            mPhraseTextView.setVisibility(View.GONE);
        } else {
            mPhraseTextView.setVisibility(View.VISIBLE);
            mPhraseTextView.setText(consultPhrase);
        }
        mCircularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "BANGGGGGGG!!!");
            }
        });
        ViewUtils.setClickListener((ViewGroup) itemView, onRowLongClickListener);
        mTimeStampTextView.setText(timeStampSdf.format(new Date(timeStamp)));
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
            String fileHeader = fileDescription.getIncomingName() == null ? FileUtils.getLastPathSegment(fileDescription.getFilePath()) : fileDescription.getIncomingName();
            mRightTextDescr.setText(fileHeader == null ? "" : fileHeader + "\n" + android.text.format.Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
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
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso
                                        .with(itemView.getContext())
                                        .load(R.drawable.defaultprofile_360)
                                        .fit()
                                        .noPlaceholder()
                                        .transform(new CircleTransform())
                                        .into(mConsultAvatar);
                            }
                        });
            } else {
                Picasso
                        .with(itemView.getContext())
                        .load(R.drawable.defaultprofile_360)
                        .fit()
                        .noPlaceholder()
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(mConsultAvatar);
            }
        } else {
            mConsultAvatar.setVisibility(View.GONE);
        }
        if (isChosen) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterViewSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterViewSecond.setVisibility(View.INVISIBLE);
        }
        if (consultPhrase == null) {
            return;
        }
        if (mPhraseTextView.getLayout() != null) {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            if (consultPhrase.length() > 1 && mPhraseTextView.getLayout().getPrimaryHorizontal(consultPhrase.length() - 1) > (mTimeStampTextView.getLeft() - density * 40)) {
                mPhraseTextView.setText(consultPhrase + "\n ");
                Intent i = new Intent(ChatAdapter.ACTION_CHANGED).putExtra(ChatAdapter.ACTION_CHANGED, getAdapterPosition());
                LocalBroadcastManager.getInstance(itemView.getContext()).sendBroadcast(i);
            }
        } else {
            mPhraseTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        if (mPhraseTextView.getLayout() == null) {
                            mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            return;
                        }
                        float density = itemView.getContext().getResources().getDisplayMetrics().density;
                        if (consultPhrase != null
                                && consultPhrase.length() > 1
                                && mPhraseTextView.getLayout().getPrimaryHorizontal(mPhraseTextView.getText().length())
                                > (mTimeStampTextView.getLeft() - density * 40)) {
                            mPhraseTextView.setText(consultPhrase + "\n ");
                            Intent i = new Intent(ChatAdapter.ACTION_CHANGED).putExtra(ChatAdapter.ACTION_CHANGED, getAdapterPosition());
                            LocalBroadcastManager.getInstance(itemView.getContext()).sendBroadcast(i);
                        }
                        mPhraseTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
