package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.R;
import im.threads.adapters.ChatAdapter;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

/**
 * ViewHolder для запроса о завершении чата
 */

public class RequestResolveThreadViewHolder extends BaseHolder {

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

        requestToResolveThread = (TextView) itemView.findViewById(R.id.request_to_resolve_thread);
        approveRequest = (TextView) itemView.findViewById(R.id.approve_request);
        denyRequest = (TextView) itemView.findViewById(R.id.deny_request);

        if (style == null) {
            style = PrefUtils.getIncomingStyle(itemView.getContext());
        }

        if (style != null) {
            if (style.iconsAndSeparatorsColor != ChatStyle.INVALID) {
                topSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
                bottomSeparator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.iconsAndSeparatorsColor));
            }

            if (style.chatSystemMessageTextColor != ChatStyle.INVALID) {
                requestToResolveThread.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
            }

            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                approveRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId));
                denyRequest.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId));
            }

            if (style.requestToResolveThreadTextResId != ChatStyle.INVALID) {
                requestToResolveThread.setText(style.requestToResolveThreadTextResId);
            }

            if (style.approveRequestToResolveThreadTextResId != ChatStyle.INVALID) {
                approveRequest.setText(style.approveRequestToResolveThreadTextResId);
            }

            if (style.denyRequestToResolveThreadTextResId != ChatStyle.INVALID) {
                denyRequest.setText(style.denyRequestToResolveThreadTextResId);
            }
        }
    }

    public void bind(final ChatAdapter.AdapterInterface adapterInterface) {
        approveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.onResolveThreadClick(true);
            }
        });
        denyRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.onResolveThreadClick(false);
            }
        });
    }
}
