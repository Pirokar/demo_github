package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.ChatStyle;

public final class FilesDateStampHolder extends RecyclerView.ViewHolder {
    private TextView mDateTextView;
    private SimpleDateFormat sdf;
    private ChatStyle style;

    public FilesDateStampHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_files_date_mark, parent, false));
        mDateTextView = itemView.findViewById(R.id.text);
        LinearLayout linearLayout = itemView.findViewById(R.id.line);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
            sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
        } else {
            sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        }
        if (style == null) style = Config.instance.getChatStyle();
        mDateTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatBackgroundColor));
    }

    public void onBind(long timeStamp) {
        Calendar date = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        date.setTimeInMillis(timeStamp);
        if (date.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)) {
            mDateTextView.setText(itemView.getResources().getString(R.string.threads_recently));
        } else if ((current.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR) == 1)) {
            mDateTextView.setText(itemView.getResources().getString(R.string.threads_yesterday));
        } else {
            mDateTextView.setText(sdf.format(new Date(timeStamp)));
        }
    }
}