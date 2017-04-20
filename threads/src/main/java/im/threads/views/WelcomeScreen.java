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
        int drawableResourse = typedArray.getResourceId(R.styleable.WelcomeScreen_image_src, style.welcomeScreenLogoResId);
        setGravity(Gravity.CENTER);
        logoView = (ImageView) findViewById(R.id.welcome_logo);
        logoView.setImageResource(drawableResourse);
        float titleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_title, style.titleSizeInSp);
        float subtitleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_subtitle, style.subtitleSizeInSp);
        @ColorInt int textColor = typedArray.getColor(R.styleable.WelcomeScreen_text_color, ContextCompat.getColor(context, style.welcomeScreenTextColorResId));
//        String titleText = typedArray.getString(R.styleable.WelcomeScreen_title_text);
        String titleText = context.getString(style.welcomeScreenTitleTextResId);
//        String subtitleText = typedArray.getString(R.styleable.WelcomeScreen_subtitle_text);
        String subtitleText = context.getString(style.welcomeScreenSubtitleTextResId);
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
    public WelcomeScreen setBackground(@ColorRes int color){
        setBackgroundColor(ContextCompat.getColor(getContext(),color));
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
        if(color != ChatStyle.INVALID){
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
        if(resId != ChatStyle.INVALID) {
            logoView.setImageResource(resId);
        }
        return this;
    }
}
