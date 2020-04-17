package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;

/**
 * layout/item_consult_connected.xml
 */
public final class ConsultConnectionMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "CCViewHolder ";
    private ImageView mConsultAvatar;
    private TextView headerTextView;
    private TextView connectedMessage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ChatStyle style;

    @DrawableRes
    private int defIcon;

    public ConsultConnectionMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_connected, parent, false));
        mConsultAvatar = itemView.findViewById(R.id.image);
        headerTextView = itemView.findViewById(R.id.quote_header);
        connectedMessage = itemView.findViewById(R.id.text);
        if (null == style) style = Config.instance.getChatStyle();
        headerTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        connectedMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));

        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);

        defIcon = style.defaultOperatorAvatar;
    }

    public void onBind(
            ConsultConnectionMessage consultConnectionMessage
            , View.OnClickListener listener) {
        if (null == consultConnectionMessage.getName()
                || consultConnectionMessage.getName().equals("null")) {
            ThreadsLogger.d(TAG, "consultName is null");
            headerTextView.setText(itemView.getContext().getString(R.string.threads_unknown_operator));
        } else {
            headerTextView.setText(consultConnectionMessage.getName());
        }
        String connectedText;
        boolean isConnected = consultConnectionMessage.getConnectionType().equals(ChatItemType.OPERATOR_JOINED.name());
        boolean sex = consultConnectionMessage.getSex();
        long date = consultConnectionMessage.getTimeStamp();
        if (sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_connected) + " " + sdf.format(new Date(date));
        } else if (!sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_connected_female) + " " + sdf.format(new Date(date));
        } else if (sex) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_left_dialog) + " " + sdf.format(new Date(date));
        } else {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_left_female) + " " + sdf.format(new Date(date));
        }
        connectedMessage.setText(connectedText);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        if (consultConnectionMessage.hasAvatar()) {
            String avatarPath = FileUtils.convertRelativeUrlToAbsolute(consultConnectionMessage.getAvatarPath());
            Picasso.get()
                    .load(avatarPath)
                    .centerInside()
                    .noPlaceholder()
                    .fit()
                    .transform(new CircleTransformation())
                    .into(mConsultAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            showDefIcon();
                        }
                    });
        } else {
            showDefIcon();
        }
    }

    private void showDefIcon() {
        Picasso.get()
                .load(defIcon)
                .centerInside()
                .noPlaceholder()
                .fit()
                .transform(new CircleTransformation())
                .into(mConsultAvatar);
    }
}
