package im.threads.internal.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.FileDescription;
import im.threads.business.secureDatabase.DatabaseHolder;
import im.threads.business.utils.FileUtils;
import im.threads.internal.Config;
import im.threads.internal.adapters.ImagesAdapter;
import im.threads.internal.fragments.PermissionDescriptionAlertDialogFragment;
import im.threads.internal.permissions.PermissionsActivity;
import im.threads.internal.utils.ColorsHelper;
import im.threads.internal.utils.ThreadUtils;
import im.threads.internal.utils.ThreadsPermissionChecker;
import im.threads.styles.permissions.PermissionDescriptionType;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class ImagesActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        PermissionDescriptionAlertDialogFragment.OnAllowPermissionClickListener {
    private static final int CODE_REQUEST_DOWNLOAD = 1;

    private ChatStyle style;
    private int collectionSize;
    private ViewPager mViewPager;
    private List<FileDescription> files;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Nullable
    private PermissionDescriptionAlertDialogFragment permissionDescriptionAlertDialogFragment;

    public static Intent getStartIntent(Context context, FileDescription fileDescription) {
        return new Intent(context, ImagesActivity.class).putExtra("FileDescription", fileDescription);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        mViewPager = findViewById(R.id.pager);
        mViewPager.addOnPageChangeListener(this);
        style = Config.instance.getChatStyle();
        initToolbar(findViewById(R.id.toolbar), findViewById(R.id.toolbar_shadow));
        compositeDisposable.add(DatabaseHolder.getInstance().getAllFileDescriptions()
                .doOnSuccess(data -> {
                    files = new ArrayList<>();
                    for (FileDescription fd : data) {
                        if (FileUtils.isImage(fd) && fd.getFileUri() != null) {
                            files.add(fd);
                        }
                    }
                    collectionSize = files.size();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            mViewPager.setAdapter(new ImagesAdapter(files, getSupportFragmentManager()));
                            FileDescription fd = getIntent().getParcelableExtra("FileDescription");
                            if (fd != null) {
                                int page = files.indexOf(fd);
                                if (page != -1) {
                                    mViewPager.setCurrentItem(page);
                                    onPageSelected(page);
                                }
                            }
                            onPageSelected(0);
                        },
                        e -> LoggerEdna.error("getAllFileDescriptions error: ", e))
        );
    }

    private void initToolbar(Toolbar toolbar, View toolbarShadow) {
        setSupportActionBar(toolbar);
        Drawable drawable = AppCompatResources.getDrawable(this,
                R.drawable.ic_arrow_back_white_24dp);
        if (drawable != null) {
            drawable = drawable.mutate();
            ColorsHelper.setDrawableColor(this, drawable,
                    R.color.threads_attachments_toolbar_text);
            toolbar.setNavigationIcon(drawable);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle("");
        boolean isShadowVisible = getResources().getBoolean(style.isChatTitleShadowVisible);
        toolbarShadow.setVisibility(isShadowVisible ? View.VISIBLE : View.INVISIBLE);
        if (!isShadowVisible) {
            toolbar.setElevation(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        if (menu.size() > 0) {
            ColorsHelper.setDrawableColor(this, menu.getItem(0).getIcon(),
                    R.color.threads_attachments_toolbar_text);
            menu.getItem(0).getIcon();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download) {
            downloadImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST_DOWNLOAD && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            downloadImage();
        }
    }

    private void downloadImage() {
        if (files.get(mViewPager.getCurrentItem()).getFileUri() == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && !ThreadsPermissionChecker.isWriteExternalPermissionGranted(this)) {
            requestPermission();
            return;
        }
        compositeDisposable.add(Completable.fromAction(() -> FileUtils.saveToDownloads(files.get(mViewPager.getCurrentItem())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(ImagesActivity.this, getString(R.string.threads_saved_to_downloads), Toast.LENGTH_SHORT).show(),
                        throwable -> {
                            LoggerEdna.error("downloadImage", throwable);
                            Toast.makeText(ImagesActivity.this, R.string.threads_unable_to_save, Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    private void requestPermission() {
        if (style.arePermissionDescriptionDialogsEnabled) {
            showStoragePermissionDescriptionDialog();
        } else {
            startStoragePermissionActivity(CODE_REQUEST_DOWNLOAD);
        }
    }

    private void showStoragePermissionDescriptionDialog() {
        if (permissionDescriptionAlertDialogFragment == null) {
            permissionDescriptionAlertDialogFragment =
                    PermissionDescriptionAlertDialogFragment.newInstance(
                            PermissionDescriptionType.STORAGE, CODE_REQUEST_DOWNLOAD);
            permissionDescriptionAlertDialogFragment.show(getSupportFragmentManager(),
                    PermissionDescriptionAlertDialogFragment.TAG);
        }
    }

    @Override
    public void onAllowClick(@NonNull PermissionDescriptionType type, int requestCode) {
        if (PermissionDescriptionType.STORAGE == type) {
            startStoragePermissionActivity(requestCode);
        }
    }

    private void startStoragePermissionActivity(int requestCode) {
        if (requestCode == CODE_REQUEST_DOWNLOAD) {
            PermissionsActivity.startActivityForResult(this,
                    CODE_REQUEST_DOWNLOAD,
                    R.string.threads_permissions_write_external_storage_help_text,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onDialogDetached() {
        permissionDescriptionAlertDialogFragment = null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ThreadUtils.runOnUiThread(() -> getSupportActionBar().setTitle(mViewPager.getCurrentItem() + 1 + " " + getString(R.string.threads_from) + " " + collectionSize));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
