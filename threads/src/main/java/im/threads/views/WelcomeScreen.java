package im.threads.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.ColorsHelper;
import im.threads.utils.PrefUtils;

public class WelcomeScreen extends LinearLayout {
    ImageView logoView;
    TextView title;
    TextView subTitle;
    View back;

    public WelcomeScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WelcomeScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ((LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_welcome, this, true);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        title = (TextView) findViewById(R.id.welcome_title);
        subTitle = (TextView) findViewById(R.id.welcome_subtitle);
        logoView = (ImageView) findViewById(R.id.welcome_logo);
        back = findViewById(R.id.welcome_back);

        ChatStyle style = PrefUtils.getIncomingStyle(context);

        if (style != null) {
            ColorsHelper.setBackgroundColor(context, this, style.chatBackgroundColor);
            ColorsHelper.setTextColor(context, title, style.welcomeScreenTitleTextColorResId);
            ColorsHelper.setTextColor(context, subTitle, style.welcomeScreenSubtitleTextColorResId);

            logoView.setImageResource(style.welcomeScreenLogoResId);
            title.setTextSize(style.welcomeScreenTitleSizeInSp);
            subTitle.setTextSize(style.welcomeScreenSubtitleSizeInSp);
            title.setText(style.welcomeScreenTitleTextResId);
            subTitle.setText(style.welcomeScreenSubtitleTextResId);
        }
    }
}
