package im.threads.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import im.threads.R;
import im.threads.formatters.RussianFormatSymbols;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.FileUtils;

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

        ChatStyle style = ChatStyle.getInstance();

        final ImageView imageView = v.findViewById(R.id.image);
        FileDescription fd = getArguments().getParcelable("fd");
        if (fd == null) throw new IllegalStateException("you must provide filedescription");
        TextView from = v.findViewById(R.id.from);
        TextView date = v.findViewById(R.id.date);
        if (fd.getFrom() != null && !fd.getFrom().equals("null")) {
            from.setText(fd.getFrom());
        } else {
            from.setText("");
        }
        if (fd.getTimeStamp() != 0) {
            date.setText(sdf.format(fd.getTimeStamp()) + " " + getString(R.string.threads_in) + " " + hoursminutesSdf.format(fd.getTimeStamp()));
        } else {
            date.setText("");
        }
        if (fd.getFilePath() != null && (FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.JPEG || FileUtils.getExtensionFromPath(fd.getFilePath()) == FileUtils.PNG)) {
            Picasso
                    .with(getActivity())
                    .load(fd.getFilePath())
                    .fit()
                    .centerInside()
                    .error(style.imagePlaceholder)
                    .into(imageView);
        }

        v.setBackgroundColor(ContextCompat.getColor(getActivity(), style.imagesScreenBackgroundColor));
        from.setTextColor(ContextCompat.getColor(getActivity(), style.imagesScreenAuthorTextColor));
        from.setTextSize(style.imagesScreenAuthorTextSize);
        date.setTextColor(ContextCompat.getColor(getActivity(), style.imagesScreenDateTextColor));
        date.setTextSize(style.imagesScreenDateTextSize);

        return v;
    }


}
