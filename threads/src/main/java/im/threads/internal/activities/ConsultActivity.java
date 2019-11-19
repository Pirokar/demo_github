package im.threads.internal.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.view.ChatFragment;

public final class ConsultActivity extends BaseActivity {
    private static final String TAG = "ConsultActivity ";
    private TextView mConsulHeaderTextView;
    private TextView mConsultMotoTextView;
    private ImageView mConsultImageView;
    private Toolbar mToolbar;
    private ChatStyle style;

    public static Intent getStartIntent(Activity activity, String avatarPath, String name, String status) {
        Intent i = new Intent(activity, ConsultActivity.class);
        i.putExtra("imagePath", avatarPath);
        i.putExtra("title", name);
        i.putExtra("moto", status);
        return i;
    }

    public static Intent getStartIntent(Activity activity) {
        return new Intent(activity, ConsultActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 20) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getResources().getColor(R.color.threads_black_transparent));
        }
        setContentView(R.layout.activity_consult_page);
        mToolbar = findViewById(R.id.toolbar);
        style = Config.instance.getChatStyle();
        mConsulHeaderTextView = findViewById(R.id.consult_title);
        mConsultMotoTextView = findViewById(R.id.consult_moto);
        mConsultImageView = findViewById(R.id.image);
        mConsultImageView.setBackground(AppCompatResources.getDrawable(this, style.defaultOperatorAvatar));
        Intent i = getIntent();
        String imagePath = i.getStringExtra("imagePath");
        String title = i.getStringExtra("title");
        String moto = i.getStringExtra("moto");
        if (!TextUtils.isEmpty(imagePath)) {
            imagePath = FileUtils.convertRelativeUrlToAbsolute(imagePath);
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
        mToolbar.setNavigationOnClickListener(v -> finish());
        mToolbar.showOverflowMenu();
        Drawable overflowDrawable = mToolbar.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            ThreadsLogger.e(TAG, "onCreate", e);
        }
        if (Build.VERSION.SDK_INT > 20) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) getResources().getDimension(R.dimen.margin_big), 0, 0);
            mToolbar.setLayoutParams(lp);
        }

    }

    protected void setActivityStyle(@NonNull ChatStyle style) {
        //TODO https://track.brooma.ru/issue/THREADS-5811
        findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.chatBackgroundColor));
        mConsulHeaderTextView.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
        mConsultMotoTextView.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
        mToolbar.getNavigationIcon().setColorFilter(new PorterDuffColorFilter(getResources().getColor(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP));
        mToolbar.getOverflowIcon().setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        mToolbar.getNavigationIcon().setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.files_and_media) {
            startActivity(FilesActivity.getStartIntent(this));
            return true;
        }
        if (item.getItemId() == R.id.search) {
            sendBroadcast(new Intent(ChatFragment.ACTION_SEARCH_CHAT_FILES));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
