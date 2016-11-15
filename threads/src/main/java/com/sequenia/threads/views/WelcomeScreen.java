package com.sequenia.threads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sequenia.threads.R;

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
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WelcomeScreen, 0, 0);
        int drawableResourse = typedArray.getResourceId(R.styleable.WelcomeScreen_image_src, 0);
        setGravity(Gravity.CENTER);
        logoView = (ImageView) findViewById(R.id.welcome_logo);
        logoView.setImageResource(drawableResourse);
        float titleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_title, 12);
        float subtitleSize = typedArray.getDimension(R.styleable.WelcomeScreen_text_size_subtitle, 12);
        @ColorInt int textColor = typedArray.getColor(R.styleable.WelcomeScreen_text_color, getResources().getColor(android.R.color.black));
        String titleText = typedArray.getString(R.styleable.WelcomeScreen_title_text);
        String subtitleText = typedArray.getString(R.styleable.WelcomeScreen_subtitle_text);
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
        title.setTextColor(getContext().getResources().getColor(color));
        subTitle.setTextColor(getContext().getResources().getColor(color));
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
        logoView.setImageResource(resId);
        return this;
    }
}
