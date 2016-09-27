package com.sequenia.threads.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sequenia.threads.AnalyticsTracker;
import com.sequenia.threads.BuildConfig;
import com.sequenia.threads.R;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.PrefUtils;

/**
 * Created by yuri on 01.07.2016.
 */
public class ConsultActivity extends BaseActivity {
    private static final String TAG = "ConsultActivity ";
    private TextView mConsulHeaderTextView;
    private TextView mConsultMotoTextView;
    private ImageView mConsultImageView;

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
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        mConsulHeaderTextView = (TextView) findViewById(R.id.consult_title);
        mConsultMotoTextView = (TextView) findViewById(R.id.consult_moto);
        mConsultImageView = (ImageView) findViewById(R.id.image);
        Intent i = getIntent();
        String imagePath = i.getStringExtra("imagePath");
        String title = i.getStringExtra("title");
        String moto = i.getStringExtra("moto");
        if (null != imagePath && !imagePath.equals("null")) {
            Picasso.with(this).load(imagePath).into(mConsultImageView);
        }
        if (null != title && !title.equals("null")) {
            mConsulHeaderTextView.setText(title);
        }
        if (null != moto && !moto.equals("null"))
            mConsultMotoTextView.setText(moto);
        if (t == null) return;
        t.setTitle("");
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        t.showOverflowMenu();
        Drawable overflowDrawable = t.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT > 20) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) getResources().getDimension(R.dimen.margin_big), 0, 0);
            t.setLayoutParams(lp);
        }
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
