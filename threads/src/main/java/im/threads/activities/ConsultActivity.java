package im.threads.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.threads.AnalyticsTracker;
import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 01.07.2016.
 */
public class ConsultActivity extends BaseActivity {
    private static final String TAG = "ConsultActivity ";
    private TextView mConsulHeaderTextView;
    private TextView mConsultMotoTextView;
    private ImageView mConsultImageView;
    private Toolbar mToolbar;
    private ChatStyle style;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsTracker.getInstance(this, PrefUtils.getGaTrackerId(this)).setConsultScreenOpened();
        if (Build.VERSION.SDK_INT > 20) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getResources().getColor(R.color.black_transparent));
        }
        setContentView(R.layout.activity_consult_page);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        style = PrefUtils.getIncomingStyle(this);
        mConsulHeaderTextView = (TextView) findViewById(R.id.consult_title);
        mConsultMotoTextView = (TextView) findViewById(R.id.consult_moto);
        mConsultImageView = (ImageView) findViewById(R.id.image);

        if (style != null && style.imagePlaceholder != ChatStyle.INVALID) {
            mConsultImageView.setBackground(ContextCompat.getDrawable(this, style.imagePlaceholder));
        }

        Intent i = getIntent();
        String imagePath = i.getStringExtra("imagePath");
        String title = i.getStringExtra("title");
        String moto = i.getStringExtra("moto");
        if (null != imagePath && !imagePath.equals("null")) {
            Picasso.with(this)
                    .load(imagePath)
                    .into(mConsultImageView);
        }
        if (null != title && !title.equals("null")) {
            mConsulHeaderTextView.setText(title);
        }
        if (null != moto && !moto.equals("null"))
            mConsultMotoTextView.setText(moto);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.showOverflowMenu();
        Drawable overflowDrawable = mToolbar.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT > 20) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) getResources().getDimension(R.dimen.margin_big), 0, 0);
            mToolbar.setLayoutParams(lp);
        }

    }

    @Override
    protected void setActivityStyle(ChatStyle style) {
        if (style != null) {
            if (style.chatToolbarColorResId != ChatStyle.INVALID)
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
            if (style.chatBackgroundColor != ChatStyle.INVALID)
                findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.chatBackgroundColor));
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID)
                mConsulHeaderTextView.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID)
                mConsultMotoTextView.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID)
                mToolbar.getNavigationIcon().setColorFilter(new PorterDuffColorFilter(getResources().getColor(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP));
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                mToolbar.getOverflowIcon().setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
                mToolbar.getNavigationIcon().setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);

            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenuItem = menu.getItem(0);
        SpannableString s = new SpannableString(searchMenuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, style.menuItemTextColorResId)), 0, s.length(), 0);
        searchMenuItem.setTitle(s);

        MenuItem filesAndMedia = menu.getItem(1);
        SpannableString s2 = new SpannableString(filesAndMedia.getTitle());
        s2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, style.menuItemTextColorResId)), 0, s2.length(), 0);
        filesAndMedia.setTitle(s2);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static Intent getStartIntent(Activity activity, String avatarPath, String name, String status) {
        Intent i = new Intent(activity, ConsultActivity.class);
        i.putExtra("imagePath", avatarPath);
        i.putExtra("title", name);
        i.putExtra("moto", status);
        return i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.files_and_media) {
            startActivity(FilesActivity.getStartIntetent(this));
            return true;
        }
        if (item.getItemId() == R.id.search) {
            sendBroadcast(new Intent(ChatActivity.ACTION_SEARCH_CHAT_FILES));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
