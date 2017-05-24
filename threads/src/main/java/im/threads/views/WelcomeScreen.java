package im.threads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 08.06.2016.
 */
public class WelcomeScreen extends LinearLayout {
    ImageView logoView;
    TextView title;
    TextView subTitle;

    public WelcomeScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WelcomeScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ((LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_welcome, this, true);
        setOrientation(VERTICAL);

        ChatStyle style = PrefUtils.getIncomingStyle(context);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WelcomeScreen, 0, 0);
        setGravity(Gravity.CENTER);
        logoView = (ImageView) findViewById(R.id.welcome_logo);

        int drawableResourse;
        if (style != null && style.welcomeScreenLogoResId != ChatStyle.INVALID) {
            drawableResourse = typedArray.getResourceId(R.styleable.WelcomeScreen_image_src, style.welcomeScreenLogoResId);
        } else {
            drawableResourse = R.drawable.logo;
        }
        logoView.setImageResource(drawableResourse);

        float titleSize;
        if (style != null && style.welcomeScreenTitleSizeInSp != ChatStyle.INVALID) {
            titleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_title, style.welcomeScreenTitleSizeInSp);
        } else {
            titleSize = getResources().getInteger(R.integer.title_size);
        }

        float subtitleSize;
        if (style != null && style.welcomeScreenSubtitleSizeInSp != ChatStyle.INVALID) {
            subtitleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_subtitle, style.welcomeScreenSubtitleSizeInSp);
        } else {
            subtitleSize = getResources().getInteger(R.integer.subtitle_size);
        }


        @ColorInt int textColor;
        if (style != null && style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
            textColor = typedArray.getColor(R.styleable.WelcomeScreen_text_color, ContextCompat.getColor(context, style.welcomeScreenTextColorResId));
        } else {
            textColor = typedArray.getColor(R.styleable.WelcomeScreen_text_color, ContextCompat.getColor(context, android.R.color.black));
        }


        String titleText;
        if (style != null && style.welcomeScreenTitleTextResId != ChatStyle.INVALID) {
            titleText = context.getString(style.welcomeScreenTitleTextResId);
//            String titleText = typedArray.getString(R.styleable.WelcomeScreen_title_text);
        } else {
            titleText = context.getString(R.string.title_text);
        }

        String subtitleText;
        if (style != null && style.welcomeScreenSubtitleTextResId != ChatStyle.INVALID) {
            subtitleText = context.getString(style.welcomeScreenSubtitleTextResId);
//        String subtitleText = typedArray.getString(R.styleable.WelcomeScreen_subtitle_text);
        } else {
            subtitleText = context.getString(R.string.subtitle_text);
        }

        title = (TextView) findViewById(R.id.welcome_title);
        subTitle = (TextView) findViewById(R.id.welcome_subtitle);
        title.setTextColor(textColor);
        title.setTextSize(titleSize);
        if (TextUtils.isEmpty(titleText)) {
            title.setVisibility(GONE);
        } else {
            title.setText(titleText);
        }
        if (TextUtils.isEmpty(subtitleText)) {
            subTitle.setVisibility(GONE);
        } else {
            subTitle.setText(subtitleText);
        }
        subTitle.setTextSize(subtitleSize);
        subTitle.setTextColor(textColor);
        typedArray.recycle();
    }

    public WelcomeScreen setBackground(@ColorRes int color) {
        setBackgroundColor(ContextCompat.getColor(getContext(), color));
        return this;
    }

    public WelcomeScreen setTitletextSize(float size) {
        title.setTextSize(size);
        return this;
    }

    public WelcomeScreen setSubtitleSize(float size) {
        subTitle.setTextSize(size);
        return this;
    }

    public WelcomeScreen setTextColor(int color) {
        if (color != ChatStyle.INVALID) {
            title.setTextColor(getContext().getResources().getColor(color));
            subTitle.setTextColor(getContext().getResources().getColor(color));
        }
        return this;
    }

    public WelcomeScreen setText(String title, String subtitle) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(GONE);
        } else {
            this.title.setVisibility(VISIBLE);
            this.title.setText(title);
        }
        if (TextUtils.isEmpty(subtitle)) {
            this.subTitle.setVisibility(GONE);
        } else {
            this.subTitle.setVisibility(VISIBLE);
            this.subTitle.setText(subtitle);
        }
        return this;
    }

    public WelcomeScreen setLogo(int resId) {
        if (resId != ChatStyle.INVALID) {
            logoView.setImageResource(resId);
        }
        return this;
    }
}
