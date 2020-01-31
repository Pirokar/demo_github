package im.threads.internal.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.RussianFormatSymbols;
import im.threads.internal.model.FileDescription;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.FileUtils;

public final class ImageFragment extends Fragment {
    private static SimpleDateFormat sdf;
    private static SimpleDateFormat hoursminutesSdf;

    public static ImageFragment getInstance(FileDescription fileDescription) {
        ImageFragment fr = new ImageFragment();
        Bundle b = new Bundle();
        b.putParcelable("fd", fileDescription);
        fr.setArguments(b);
        return fr;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image, container, false);
        if (sdf == null) {
            hoursminutesSdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
            if (Locale.getDefault().getLanguage().equals("ru")) {
                sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
            } else {
                sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            }
        }
        ChatStyle style = Config.instance.getChatStyle();
        final ImageView imageView = v.findViewById(R.id.image);
        TextView from = v.findViewById(R.id.from);
        TextView date = v.findViewById(R.id.date);
        FileDescription fd = getArguments().getParcelable("fd");
        if (fd == null) throw new IllegalStateException("you must provide filedescription");
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
        if (FileUtils.isImage(fd)) {
            Picasso
                    .with(getActivity())
                    .load(new File(fd.getFilePath()))
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
