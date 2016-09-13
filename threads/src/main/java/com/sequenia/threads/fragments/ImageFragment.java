package com.sequenia.threads.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Callback;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.RussianFormatSymbols;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by yuri on 05.08.2016.
 */
public class ImageFragment extends Fragment {
    private static SimpleDateFormat sdf;
    private static SimpleDateFormat hoursminutesSdf;

    public static ImageFragment getInstance(FileDescription fileDescription) {
        ImageFragment fr = new ImageFragment();
        Bundle b = new Bundle();
        b.putParcelable("fd", fileDescription);
        fr.setArguments(b);
        return fr;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image, container, false);
        if (sdf == null) {
            hoursminutesSdf = new SimpleDateFormat("hh:mm");
            if (Locale.getDefault().getLanguage().equals("ru")) {
                sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
            } else {
                sdf = new SimpleDateFormat("dd MMMM yyyy");
            }
        }
        final ImageView imageView = (ImageView) v.findViewById(R.id.image);
        FileDescription fd = getArguments().getParcelable("fd");
        if (fd == null) throw new IllegalStateException("you must provide filedescription");
        TextView from = (TextView) v.findViewById(R.id.from);
        TextView date = (TextView) v.findViewById(R.id.date);
        if (fd.getFrom() != null && !fd.getFrom().equals("null")) {
            from.setText(fd.getFrom());
        } else {
            from.setText("");
        }
        if (fd.getTimeStamp() != 0) {
            date.setText(sdf.format(fd.getTimeStamp()) + " " + getString(R.string.in) + " " + hoursminutesSdf.format(fd.getTimeStamp()));
        } else {
            date.setText("");
        }
        if (fd.getFilePath() != null && (FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.JPEG || FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.PNG)) {
            Picasso
                    .with(getActivity())
                    .load(fd.getFilePath())
                    .fit()
                    .centerInside()
                    .error(R.drawable.no_image)
                    .into(imageView);
        }
        return v;
    }


}
