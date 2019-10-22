package im.threads.internal.formatters;

import android.content.Context;

import im.threads.R;
import im.threads.internal.model.ConsultConnectionMessage;

final class ConnectionPhrase {

    private ConnectionPhrase() {
    }

    static String getConnectionPhrase(Context ctx, ConsultConnectionMessage ccm) {
        String out = "";
        String temp;
        if (!ccm.getSex()
                && ccm.getConnectionType().equalsIgnoreCase(PushMessageType.OPERATOR_JOINED.name())) {
            temp = ctx.getString(R.string.threads_push_connected_female);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1));
        } else if (!ccm.getSex()
                && ccm.getConnectionType().equalsIgnoreCase(PushMessageType.OPERATOR_LEFT.name())) {
            temp = ctx.getString(R.string.threads_push_left_female);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1));
        } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(PushMessageType.OPERATOR_JOINED.name())) {
            temp = ctx.getString(R.string.threads_push_connected);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1));
        } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(PushMessageType.OPERATOR_LEFT.name())) {
            temp = ctx.getString(R.string.threads_push_left);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1));
        }
        return out;
    }
}
