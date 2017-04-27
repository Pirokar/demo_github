package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.ScheduleInfo;
import im.threads.utils.PrefUtils;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class ScheduleInfoViewHolder extends RecyclerView.ViewHolder {

    private ImageView icon;
    private TextView text;

    private ChatStyle style;

    public ScheduleInfoViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_info, parent, false));

        icon = (ImageView) itemView.findViewById(R.id.image);
        text = (TextView) itemView.findViewById(R.id.schedule_text);

        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null) {
            if(style.scheduleMessageTextColorResId != ChatStyle.INVALID) {
                text.setTextColor(ContextCompat.getColor(itemView.getContext(), style.scheduleMessageTextColorResId));
            }
            if(style.scheduleMessageIconResId != ChatStyle.INVALID) {
                icon.setImageResource(style.scheduleMessageIconResId);
            }
        }
    }

    public void bind(ScheduleInfo scheduleInfo) {
        String scheduleMessage = scheduleInfo.getNotification();
        text.setText(scheduleMessage == null ? "" : scheduleMessage);
    }
}