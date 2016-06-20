package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.RussianFormatSymbols;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        sdf=new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
    }
    public DateViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.text);
        sdf=new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
    }
    public void onBind(long timeStamp){
        mTextView.setText(sdf.format(new Date(timeStamp)));
    }
}
