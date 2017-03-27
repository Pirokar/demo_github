package im.threads.formatters;

import android.content.Context;

import im.threads.R;
import im.threads.model.ConsultConnectionMessage;

/**
 * Created by yuri on 14.09.2016.
 */
public class ConnectionPhrase {
    final Context ctx;

    public ConnectionPhrase(Context ctx) {
        this.ctx = ctx;
    }

    String getConnectionPhrase(ConsultConnectionMessage ccm) {
        String out = "";
        String temp = "";
        if (!ccm.getSex()
                && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
            temp = ctx.getString(R.string.push_connected_female);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1, temp.length()));
        } else if (!ccm.getSex()
                && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
            temp = ctx.getString(R.string.push_left_female);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1, temp.length()));
        } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
            temp = ctx.getString(R.string.push_connected);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1, temp.length()));
        } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
            temp = ctx.getString(R.string.push_left);
            out = temp.toUpperCase().substring(0, 1).concat(temp.substring(1, temp.length()));
        }
        return out;
    }
}
