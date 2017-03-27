package im.threads.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Array adapter with simple animation for every row added
 */
public class AnimatedArrayAdapter extends ArrayAdapter<String> {
    Animation animation;

    public AnimatedArrayAdapter(Context context, int resource) {
        super(context, resource);
    }


    public AnimatedArrayAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }


    public AnimatedArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }


    public AnimatedArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }


    public AnimatedArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }


    public AnimatedArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void setAnimationToRows(Animation animation) {
        this.animation = animation;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (animation != null) {
            v.startAnimation(animation);
        }
        return v;
    }
}
