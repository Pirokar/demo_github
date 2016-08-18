package com.sequenia.threads.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.ImagesAdapter;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.FileDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 05.08.2016.
 */
public class ImagesActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private Toolbar mToolbar;
    private int collectionSize;
    private static final String TAG = "ImagesActivity ";
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.addOnPageChangeListener(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle("");
        DatabaseHolder.getInstance(this).getFilesAsync(new CompletionHandler<List<FileDescription>>() {
            @Override
            public void onComplete(List<FileDescription> data) {
                List<FileDescription> output = new ArrayList<>();
                for (FileDescription fd : data) {
                    if (fd.hasImage()&& fd.getFilePath()!=null) {
                        Log.e(TAG, "hasImage()");
                        output.add(fd);
                    }
                }
                Log.e(TAG, "output = "+output);
                collectionSize = output.size();
                mViewPager.setAdapter(new ImagesAdapter(output, getFragmentManager()));
                FileDescription fd = getIntent().getParcelableExtra("FileDescription");
                if (fd != null) {
                    int page = output.indexOf(fd);
                    if (page != -1) {
                        mViewPager.setCurrentItem(page);
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
        getSupportActionBar().setTitle(mViewPager.getCurrentItem()  +1 + " " + getString(R.string.from) + " " + collectionSize);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
