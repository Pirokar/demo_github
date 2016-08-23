package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_connected.xml
 */
public class ConsultConnectionMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "CCViewHolder ";
    public ImageView mConsultAvatar;
    private TextView headerTextView;
    private TextView connectedMessage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public ConsultConnectionMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_connected, parent, false));
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        headerTextView = (TextView) itemView.findViewById(R.id.quote_header);
        connectedMessage = (TextView) itemView.findViewById(R.id.text);
    }

    public ConsultConnectionMessageViewHolder(View itemView) {
        super(itemView);
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        headerTextView = (TextView) itemView.findViewById(R.id.quote_header);
        connectedMessage = (TextView) itemView.findViewById(R.id.text);
    }

    public void onBind(
            String consultName
            , long date
            , boolean sex
            , boolean isConnected
            , View.OnClickListener listener) {
        if (consultName == null || consultName.equals("null")) {
            Log.e(TAG, "consultName is null");
            consultName = itemView.getContext().getString(R.string.unknown_operator);
        }
        headerTextView.setText(consultName);
        String connectedText = "";
        if (sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.connected) + " " + sdf.format(new Date(date));
        } else if (!sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.connected_female) + " " + sdf.format(new Date(date));
        } else if (sex && !isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.left_dialog) + " " + sdf.format(new Date(date));
        } else if (!sex && !isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.left_female) + " " + sdf.format(new Date(date));
        }
        connectedMessage.setText(connectedText);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
    }
}
