package im.threads.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import im.threads.R;
import im.threads.adapters.ImagesAdapter;
import im.threads.database.DatabaseHolder;
import im.threads.model.ChatStyle;
import im.threads.model.CompletionHandler;
import im.threads.model.FileDescription;
import im.threads.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 05.08.2016.
 */
public class ImagesGalleryActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private Toolbar mToolbar;
    private int collectionSize;
    private static final String TAG = "ImagesActivity ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.addOnPageChangeListener(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");
        DatabaseHolder.getInstance(this).getFilesAsync(new CompletionHandler<List<FileDescription>>() {
            @Override
            public void onComplete(List<FileDescription> data) {
                List<FileDescription> output = new ArrayList<>();
                for (FileDescription fd : data) {
                    if (fd.hasImage()) {
                        Log.e(TAG, "hasImage()");
                        output.add(fd);
                    }
                }
                Log.e(TAG, "output = "+output);
                collectionSize = output.size();
                viewPager.setAdapter(new ImagesAdapter(output, getFragmentManager()));
                FileDescription fd = getIntent().getParcelableExtra("FileDescription");
                if (fd != null) {
                    int page = output.indexOf(fd);
                    if (page != -1) {
                        viewPager.setCurrentItem(page);
                        onPageSelected(page);
                    }
                }
                onPageSelected(0);
            }

            @Override
            public void onError(Throwable e, String message, List<FileDescription> data) {
                finish();
            }
        });
        ChatStyle style = PrefUtils.getIncomingStyle(this);
       /* if (null != style) {
            if (style.chatBackgroundColor != INVALID)     findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.chatBackgroundColor));
            if (style.chatToolbarColorResId != INVALID)    mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
            if (style.chatToolbarColorResId != INVALID)    mToolbar.setTitleTextColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
            if (style.chatToolbarTextColorResId != INVALID)     ((ImageButton) findViewById(R.id.search)).setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC);
            if (style.chatToolbarTextColorResId != INVALID)     mToolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC);
        }*/
    }

    @Override
    protected void setActivityStyle(ChatStyle style) {

    }

    public static Intent getStartIntent(Context context, FileDescription fileDescription) {
        Intent i = new Intent(context, ImagesActivity.class);
        i.putExtra("FileDescription", fileDescription);
        return i;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mToolbar.setTitle(position+1 + " " + getString(R.string.from) + " " + collectionSize);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
