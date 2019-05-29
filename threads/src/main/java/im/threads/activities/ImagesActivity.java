package im.threads.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.adapters.ImagesAdapter;
import im.threads.database.DatabaseHolder;
import im.threads.model.ChatStyle;
import im.threads.model.CompletionHandler;
import im.threads.model.FileDescription;
import im.threads.permissions.PermissionsActivity;
import im.threads.utils.FileUtils;
import im.threads.utils.ThreadUtils;

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
        Drawable d = AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back_white_24dp);
        d.setColorFilter(getColorInt(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(d);
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
                    if (FileUtils.isImage(fd) && fd.getFilePath() != null) {
                        files.add(fd);
                    }
                }
                collectionSize = files.size();
                ThreadUtils.runOnUiThread(() -> {
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
                });
            }

            @Override
            public void onError(Throwable e, String message, List<FileDescription> data) {
                finish();
            }
        });
        ChatStyle style = ChatStyle.getInstance();
        mToolbar.setBackgroundColor(getColorInt(style.imagesScreenToolbarColor));
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
            PermissionsActivity.startActivityForResult(this, CODE_REQUQEST_DOWNLOAD,
                    R.string.threads_permissions_write_external_storage_help_text, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        String path = files.get(mViewPager.getCurrentItem()).getFilePath();
        try {
            File file = new File(path);
            if (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) == null) {
                Toast.makeText(this, R.string.threads_unable_to_save, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.threads_saved_to) + " " + out.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.threads_unable_to_save, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUQEST_DOWNLOAD && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            downloadImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(mViewPager.getCurrentItem() + 1 + " " + getString(R.string.threads_from) + " " + collectionSize);
            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
