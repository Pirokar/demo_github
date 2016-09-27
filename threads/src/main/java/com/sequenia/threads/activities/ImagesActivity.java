package com.sequenia.threads.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.ImagesAdapter;
import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 05.08.2016.
 */
public class ImagesActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private Toolbar mToolbar;
    private int collectionSize;
    private static final String TAG = "ImagesActivity ";
    private ViewPager mViewPager;
    private List<FileDescription> files;
    public static final int CODE_REQUQEST_DOWNLOAD = 1;

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
                files = new ArrayList<>();
                for (FileDescription fd : data) {
                    if (fd.hasImage() && fd.getFilePath() != null) {
                        files.add(fd);
                    }
                }
                collectionSize = files.size();
                mViewPager.setAdapter(new ImagesAdapter(files, getFragmentManager()));
                FileDescription fd = getIntent().getParcelableExtra("FileDescription");
                if (fd != null) {
                    int page = files.indexOf(fd);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download) {
            downloadImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadImage() {
        if (files.get(mViewPager.getCurrentItem()).getFilePath() == null) return;
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_REQUQEST_DOWNLOAD);
            return;
        }
        String path = files.get(mViewPager.getCurrentItem()).getFilePath().replaceAll("file://", "");
        try {
            File file = new File(path);
            if (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) == null) {
                Toast.makeText(this, R.string.unable_to_save, Toast.LENGTH_SHORT).show();
                return;
            }
            File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FileUtils.getLastPathSegment(path));
            out.createNewFile();
            InputStream inStream = new FileInputStream(file);
            OutputStream outStram = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inStream.read(buf)) > 0) {
                outStram.write(buf, 0, len);
            }
            inStream.close();
            outStram.close();
            Toast.makeText(this, getString(R.string.saved_to) + " " + out.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.unable_to_save, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUQEST_DOWNLOAD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            }else {
                Toast.makeText(this,R.string.unable_to_save,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
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
        getSupportActionBar().setTitle(mViewPager.getCurrentItem() + 1 + " " + getString(R.string.from) + " " + collectionSize);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
