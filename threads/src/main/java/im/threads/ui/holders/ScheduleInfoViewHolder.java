package im.threads.ui.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.business.models.ScheduleInfo;
import im.threads.ui.config.Config;

/**
 * ViewHolder для расписания
 */
public final class ScheduleInfoViewHolder extends RecyclerView.ViewHolder {

    private ImageView icon;
    private TextView text;

    private ChatStyle style;

    public ScheduleInfoViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_schedule_info, parent, false));
        icon = itemView.findViewById(R.id.schedule_icon);
        text = itemView.findViewById(R.id.schedule_text);
        style = Config.getInstance().getChatStyle();
        text.setTextColor(ContextCompat.getColor(itemView.getContext(), style.scheduleMessageTextColorResId));
        icon.setImageResource(style.scheduleMessageIconResId);
    }

    public void bind(ScheduleInfo scheduleInfo) {
        String scheduleMessage = scheduleInfo.getNotification();
        text.setText(scheduleMessage == null ? "" : scheduleMessage);
    }
}