package im.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.threads.R;

/**
 * ViewHolder для расписания
 * Created by chybakut2004 on 17.04.17.
 */

public class ScheduleInfoViewHolder extends RecyclerView.ViewHolder {

    public ScheduleInfoViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_info, parent, false));
    }

    public void bind() {

    }
}
