package im.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ScheduleInfo;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class ScheduleInfoViewHolder extends RecyclerView.ViewHolder {

    private TextView message;

    public ScheduleInfoViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_info, parent, false));

        message = (TextView) itemView.findViewById(R.id.schedule_message);
    }

    public void bind(ScheduleInfo scheduleInfo) {
        String scheduleMessage = scheduleInfo.getNotification();
        message.setText(scheduleMessage == null ? "" : scheduleMessage);
    }
}
