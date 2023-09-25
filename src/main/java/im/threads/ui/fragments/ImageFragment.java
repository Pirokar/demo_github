package im.threads.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.business.formatters.RussianFormatSymbols;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.business.models.FileDescription;
import im.threads.business.utils.FileUtils;
import im.threads.ui.config.Config;

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
        View v = inflater.inflate(R.layout.ecc_fragment_image, container, false);
        if (sdf == null) {
            hoursminutesSdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
            if (Locale.getDefault().getLanguage().equals("ru")) {
                sdf = new SimpleDateFormat("dd MMMM yyyy", new RussianFormatSymbols());
            } else {
                sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            }
        }
        ChatStyle style = Config.getInstance().getChatStyle();
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
            date.setText(sdf.format(fd.getTimeStamp()) + " " + getString(R.string.ecc_in) + " " + hoursminutesSdf.format(fd.getTimeStamp()));
        } else {
            date.setText("");
        }
        if (FileUtils.isImage(fd)) {
            if(fd.getFileUri() != null) {
                ImageLoader
                        .get()
                        .load(fd.getFileUri().toString())
                        .autoRotateWithExif(true)
                        .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE)
                        .errorDrawableResourceId(style.imagePlaceholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(style.imagePlaceholder);
            }
        }
        v.setBackgroundColor(ContextCompat.getColor(getActivity(), style.imagesScreenBackgroundColor));
        from.setTextColor(ContextCompat.getColor(getActivity(), style.imagesScreenAuthorTextColor));
        from.setTextSize(style.imagesScreenAuthorTextSize);
        date.setTextColor(ContextCompat.getColor(getActivity(), style.imagesScreenDateTextColor));
        date.setTextSize(style.imagesScreenDateTextSize);
        return v;
    }
}