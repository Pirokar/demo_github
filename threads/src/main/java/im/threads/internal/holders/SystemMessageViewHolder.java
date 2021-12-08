package im.threads.internal.holders;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.util.LinkifyCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.SystemMessage;
import im.threads.internal.utils.UrlUtils;

public class SystemMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView tvSystemMessage;
    private ChatStyle style;
    private Context context;

    public SystemMessageViewHolder(@NonNull ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_message, parent, false));
        context = parent.getContext();
        tvSystemMessage = itemView.findViewById(R.id.tv_system_message);
        if (style == null) {
            style = Config.instance.getChatStyle();
        }
    }

    public void onBind(SystemMessage systemMessage, View.OnClickListener listener) {
        final SpannableString text = new SpannableString(systemMessage.getText());
        LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, "");
        tvSystemMessage.setText(text);
        tvSystemMessage.setLinkTextColor(ContextCompat.getColor(itemView.getContext(), style.systemMessageLinkColor));
        tvSystemMessage.setOnClickListener(view -> {
            String deepLink = UrlUtils.extractDeepLink(systemMessage.getText());
            String url = UrlUtils.extractLink(systemMessage.getText());
            if (deepLink != null) {
                UrlUtils.openUrl(context, deepLink);
            } else if (url != null) {
                UrlUtils.openUrl(context, url);
            }
        });

        itemView.setOnClickListener(listener);
        if (!TextUtils.isEmpty(style.systemMessageFont)) {
            tvSystemMessage.setTypeface(Typeface.createFromAsset(itemView.getContext().getAssets(), style.systemMessageFont));
        }
        tvSystemMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.getContext().getResources().getDimension(style.systemMessageTextSize));
        tvSystemMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), style.systemMessageTextColorResId));
        tvSystemMessage.setGravity(style.systemMessageTextGravity);
        int leftRightPadding = (int) itemView.getContext().getResources().getDimension(style.systemMessageLeftRightPadding);
        tvSystemMessage.setPadding(
                leftRightPadding,
                tvSystemMessage.getPaddingTop(),
                leftRightPadding,
                tvSystemMessage.getPaddingBottom()
        );
    }
}
