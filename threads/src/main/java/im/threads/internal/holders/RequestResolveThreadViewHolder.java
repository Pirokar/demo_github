package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import im.threads.R;
import im.threads.ui.adapters.ChatAdapter;

/**
 * ViewHolder для запроса о завершении чата
 */
public final class RequestResolveThreadViewHolder extends BaseHolder {

    private final TextView approveRequest;
    private final TextView denyRequest;

    public RequestResolveThreadViewHolder(ViewGroup parent) {
        super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_resolve_thread, parent, false),
                null
        );
        View topSeparator = itemView.findViewById(R.id.top_separator);
        View bottomSeparator = itemView.findViewById(R.id.bottom_separator);
        TextView requestToResolveThread = itemView.findViewById(R.id.request_to_resolve_thread);
        approveRequest = itemView.findViewById(R.id.approve_request);
        denyRequest = itemView.findViewById(R.id.deny_request);
        topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().iconsAndSeparatorsColor));
        bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), getStyle().iconsAndSeparatorsColor));
        requestToResolveThread.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().chatSystemMessageTextColor));
        approveRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyChoicesTextColorResId));
        denyRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), getStyle().surveyChoicesTextColorResId));
        requestToResolveThread.setText(getStyle().requestToResolveThreadTextResId);
        approveRequest.setText(getStyle().approveRequestToResolveThreadTextResId);
        denyRequest.setText(getStyle().denyRequestToResolveThreadTextResId);
    }

    public void bind(final ChatAdapter.Callback callback) {
        approveRequest.setOnClickListener(view -> callback.onResolveThreadClick(true));
        denyRequest.setOnClickListener(view -> callback.onResolveThreadClick(false));
    }
}
