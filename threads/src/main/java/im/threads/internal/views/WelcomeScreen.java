package im.threads.internal.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.ChatStyle;
import im.threads.internal.utils.ColorsHelper;

public class WelcomeScreen extends LinearLayout {

    public WelcomeScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WelcomeScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            return;
        }
        inflater.inflate(R.layout.view_welcome, this, true);
        final ChatStyle style = Config.instance.getChatStyle();
        initLogo(style);
        initTitle(style);
        initSubtitle(style);
    }

    private void initLogo(final @NonNull ChatStyle style) {
        final ImageView logoView = findViewById(R.id.welcome_logo);
        logoView.setImageResource(style.welcomeScreenLogoResId);
    }

    private void initTitle(final @NonNull ChatStyle style) {
        final TextView title = findViewById(R.id.welcome_title);
        ColorsHelper.setTextColor(title, style.welcomeScreenTitleTextColorResId);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(style.welcomeScreenTitleSizeInSp));
        title.setText(style.welcomeScreenTitleTextResId);
    }

    private void initSubtitle(final @NonNull ChatStyle style) {
        final TextView subTitle = findViewById(R.id.welcome_subtitle);
        ColorsHelper.setTextColor(subTitle, style.welcomeScreenSubtitleTextColorResId);
        subTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(style.welcomeScreenSubtitleSizeInSp));
        subTitle.setText(style.welcomeScreenSubtitleTextResId);
    }

}
