package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.config.BaseConfig;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.ui.Config;

/**
 * layout/item_date.xml
 */
public final class DateViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private SimpleDateFormat sdf;

    public DateViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false));
        mTextView = itemView.findViewById(R.id.text);
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        }
        mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
    }

    public void onBind(long timeStamp) {
        mTextView.setText(sdf.format(new Date(timeStamp)));
    }
}
