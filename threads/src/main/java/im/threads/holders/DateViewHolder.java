package im.threads.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;

/**
 * Created by yuri on 08.06.2016.
 * layout/item_date.xml
 */
public class DateViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private SimpleDateFormat sdf;

    public DateViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
        ChatStyle style = ChatStyle.getInstance();
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            sdf = new SimpleDateFormat("dd MMMM yyyy");
        }
        mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
    }

    public void onBind(long timeStamp) {
        mTextView.setText(sdf.format(new Date(timeStamp)));
    }
}
