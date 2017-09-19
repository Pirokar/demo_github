package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_date.xml
 */
public class DateViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private SimpleDateFormat sdf;
    private static ChatStyle style;

    public DateViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
        style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            sdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        if (style != null && style.welcomeScreenTitleTextColorResId != ChatStyle.INVALID) {
            mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTitleTextColorResId));
        }
    }

    public void onBind(long timeStamp) {
        mTextView.setText(sdf.format(new Date(timeStamp)));
    }
}
