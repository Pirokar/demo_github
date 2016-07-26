package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.utils.RussianFormatSymbols;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_date.xml
 */
public class DateViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private SimpleDateFormat sdf;
    public DateViewHolder(ViewGroup parent){
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date,parent,false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")){
            sdf=new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        }else {
            sdf=new SimpleDateFormat("dd MMMM yyyy");
        }
    }
    public void onBind(long timeStamp){
        mTextView.setText(sdf.format(new Date(timeStamp)));
    }
}
