package im.threads.internal.holders;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.adapters.ChatAdapter;

/**
 * ViewHolder для запроса о завершении чата
 */
public final class RequestResolveThreadViewHolder extends BaseHolder {

    private View topSeparator;
    private View bottomSeparator;
    private TextView requestToResolveThread;
    private TextView approveRequest;
    private TextView denyRequest;

    private ChatStyle style;

    public RequestResolveThreadViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_resolve_thread, parent, false));
        topSeparator = itemView.findViewById(R.id.top_separator);
        bottomSeparator = itemView.findViewById(R.id.bottom_separator);
        requestToResolveThread = itemView.findViewById(R.id.request_to_resolve_thread);
        approveRequest = itemView.findViewById(R.id.approve_request);
        denyRequest = itemView.findViewById(R.id.deny_request);
        style = Config.instance.getChatStyle();
        topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
        bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
        requestToResolveThread.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        approveRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyChoicesTextColorResId));
        denyRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), style.surveyChoicesTextColorResId));
        requestToResolveThread.setText(style.requestToResolveThreadTextResId);
        approveRequest.setText(style.approveRequestToResolveThreadTextResId);
        denyRequest.setText(style.denyRequestToResolveThreadTextResId);
    }

    public void bind(final ChatAdapter.AdapterInterface adapterInterface) {
        approveRequest.setOnClickListener(view -> adapterInterface.onResolveThreadClick(true));
        denyRequest.setOnClickListener(view -> adapterInterface.onResolveThreadClick(false));
    }
}
