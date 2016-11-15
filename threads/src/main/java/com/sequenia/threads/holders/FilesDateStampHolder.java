package com.sequenia.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.formatters.RussianFormatSymbols;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.utils.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesDateStampHolder extends RecyclerView.ViewHolder {
    private TextView mDateTextView;
    private SimpleDateFormat sdf;
    private static ChatStyle style;

    public FilesDateStampHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_files_date_mark, parent, false));
        mDateTextView = (TextView) itemView.findViewById(R.id.text);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            sdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (null != style) {
            if (style.connectionMessageTextColor != INVALID) {
                mDateTextView.setTextColor(ContextCompat.getColor(itemView.getContext(),style.connectionMessageTextColor));
            }
        }
    }

    public void onBind(long timeStamp) {
        Calendar date = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        date.setTimeInMillis(timeStamp);
        if (date.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)) {
            mDateTextView.setText(itemView.getResources().getString(R.string.recently));
        } else if ((current.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR) == 1)) {
            mDateTextView.setText(itemView.getResources().getString(R.string.yesterday));
        } else {
            mDateTextView.setText(sdf.format(new Date(timeStamp)));
        }
    }
}
