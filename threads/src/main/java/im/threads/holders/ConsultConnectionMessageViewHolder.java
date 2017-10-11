package im.threads.holders;

import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultConnectionMessage;
import im.threads.picasso_url_connection_only.Callback;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.FileUtils;
import im.threads.utils.PrefUtils;

import static im.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_connected.xml
 */
public class ConsultConnectionMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "CCViewHolder ";
    public ImageView mConsultAvatar;
    private TextView headerTextView;
    private TextView connectedMessage;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private static ChatStyle style;
    private @DrawableRes int defIcon;

    public ConsultConnectionMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_connected, parent, false));
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        headerTextView = (TextView) itemView.findViewById(R.id.quote_header);
        connectedMessage = (TextView) itemView.findViewById(R.id.text);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (null != style) {
            if (style.chatSystemMessageTextColor != ChatStyle.INVALID) {
                headerTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
                connectedMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
            }

            if (style.operatorSystemAvatarSize != INVALID) {
                mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
                mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
            }
        }
        defIcon = style != null && style.defaultIncomingMessageAvatar != ChatStyle.INVALID ?
                style.defaultIncomingMessageAvatar :
                R.drawable.threads_operator_avatar_placeholder;
    }

    public void onBind(
            ConsultConnectionMessage consultConnectionMessage
            , View.OnClickListener listener) {
        if (consultConnectionMessage.getName() == null
                || consultConnectionMessage.getName().equals("null")) {
            Log.d(TAG, "consultName is null");
            headerTextView.setText(itemView.getContext().getString(R.string.threads_unknown_operator));
        } else {
            headerTextView.setText(consultConnectionMessage.getName());
        }
        String connectedText = "";
        boolean isConnected = consultConnectionMessage.getConnectionType().equals(ConsultConnectionMessage.TYPE_JOINED);
        boolean sex = consultConnectionMessage.getSex();
        long date = consultConnectionMessage.getTimeStamp();
        if (sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_connected) + " " + sdf.format(new Date(date));
        } else if (!sex && isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_connected_female) + " " + sdf.format(new Date(date));
        } else if (sex && !isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_left_dialog) + " " + sdf.format(new Date(date));
        } else if (!sex && !isConnected) {
            connectedText = itemView.getContext().getResources().getString(R.string.threads_left_female) + " " + sdf.format(new Date(date));
        }
        connectedMessage.setText(connectedText);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        if (consultConnectionMessage.hasAvatar()) {
            String avatarPath = FileUtils.convertRelativeUrlToAbsolute(itemView.getContext(), consultConnectionMessage.getAvatarPath());
            Picasso.with(itemView.getContext())
                    .load(avatarPath)
                    .centerInside()
                    .noPlaceholder()
                    .fit()
                    .transform(new CircleTransform())
                    .into(mConsultAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            showDefIcon();
                        }
                    });
        } else {
            showDefIcon();
        }
    }

    private void showDefIcon() {
        Picasso.with(itemView.getContext())
                .load(defIcon)
                .centerInside()
                .noPlaceholder()
                .fit()
                .transform(new CircleTransform())
                .into(mConsultAvatar);
    }
}
