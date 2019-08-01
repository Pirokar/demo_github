package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.adapters.ImagesAdapter;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.ChatStyle;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadUtils;

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
                    if (FileUtils.isImage(fd)) {
                        ThreadsLogger.e(TAG, "hasImage()");
                        output.add(fd);
                    }
                }
                ThreadsLogger.e(TAG, "output = " + output);
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
        ChatStyle style = Config.instance.getChatStyle();
        mToolbar.setBackgroundColor(getColorInt(style.imagesScreenToolbarColor));
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
    public void onPageSelected(final int position) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToolbar.setTitle(position + 1 + " " + getString(R.string.threads_from) + " " + collectionSize);
            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
