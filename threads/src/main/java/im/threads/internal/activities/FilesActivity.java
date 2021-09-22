package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.adapters.FilesAndMediaAdapter;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.controllers.FilesAndMediaController;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.Keyboard;

public final class FilesActivity extends BaseActivity implements FilesAndMediaAdapter.OnFileClick, ProgressReceiver.Callback {
    private static final String TAG = "FilesActivity ";
    private FilesAndMediaController mFilesAndMediaController;
    private RecyclerView mRecyclerView;
    private EditText mSearchEditText;
    private Toolbar mToolbar;
    private FilesAndMediaAdapter mFilesAndMediaAdapter;

    public static Intent getStartIntent(@NonNull Context context) {
        return new Intent(context, FilesActivity.class);
    }

    public void onFileReceive(List<FileDescription> descriptions) {
        if (descriptions != null && descriptions.size() > 0) {
            mFilesAndMediaAdapter = new FilesAndMediaAdapter(descriptions, this, this);
            mRecyclerView.setAdapter(mFilesAndMediaAdapter);
        }
    }

    @Override
    public void onFileClick(FileDescription fileDescription) {
        mFilesAndMediaController.onFileClick(fileDescription);
    }

    @Override
    public void onDownloadFileClick(FileDescription fileDescription) {
        mFilesAndMediaController.onDownloadFileClick(fileDescription);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatStyle style = Config.instance.getChatStyle();
        if (Build.VERSION.SDK_INT > 20) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(style.chatStatusBarColorResId));
            if (getResources().getBoolean(style.windowLightStatusBarResId)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
        setContentView(R.layout.activity_files_and_media);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            mFilesAndMediaController = FilesAndMediaController.getInstance();
            getSupportFragmentManager().beginTransaction().add(mFilesAndMediaController, TAG).commit();
        } else {
            mFilesAndMediaController = (FilesAndMediaController) getSupportFragmentManager().findFragmentByTag(TAG);
        }
        mFilesAndMediaController.bindActivity(this);
        mFilesAndMediaController.getFilesAsync();
        mRecyclerView = findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSearchEditText = findViewById(R.id.search_edit_text);
        findViewById(R.id.search).setOnClickListener(v -> {
            if (mSearchEditText.getVisibility() == View.VISIBLE) {
                mSearchEditText.setText("");
                mSearchEditText.setVisibility(View.GONE);
                mToolbar.setTitle(getString(R.string.threads_files_and_media));
                if (null != mFilesAndMediaAdapter) {
                    mFilesAndMediaAdapter.undoClear();
                }
            } else {
                mSearchEditText.setVisibility(View.VISIBLE);
                mSearchEditText.requestFocus();
                mToolbar.setTitle("");
                if (null != mFilesAndMediaAdapter) {
                    mFilesAndMediaAdapter.backupAndClear();
                }
                mSearchEditText.setText("");
                Keyboard.show(this, mSearchEditText, 100);
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str;
                if (s == null) {
                    str = "";
                } else {
                    str = s.toString();
                }
                search(str);
            }
        });
        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((mSearchEditText.getVisibility() == View.VISIBLE) && actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.getText().toString());
                return true;
            } else {
                return false;
            }
        });
        setActivityStyle(Config.instance.getChatStyle());
    }

    @Override
    public void onBackPressed() {
        if (mSearchEditText.getVisibility() == View.VISIBLE) {
            mSearchEditText.setText("");
            mSearchEditText.setVisibility(View.GONE);
            mToolbar.setTitle(getString(R.string.threads_files_and_media));
            if (null != mFilesAndMediaAdapter) {
                mFilesAndMediaAdapter.undoClear();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void search(String searchString) {
        if (mFilesAndMediaAdapter != null) {
            mFilesAndMediaAdapter.filter(searchString);
        }
    }

    private void setActivityStyle(@NonNull ChatStyle style) {
        findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.filesAndMediaScreenBackgroundColor));
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
        ((ImageButton) findViewById(R.id.search)).setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        mSearchEditText.setTextColor(getColorInt(style.chatToolbarTextColorResId));
        final Drawable navigationIcon = mToolbar.getNavigationIcon();
        navigationIcon.mutate().setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(navigationIcon);
        mSearchEditText.setHintTextColor(getColorInt(style.chatToolbarHintTextColor));
    }

    @Override
    public void updateProgress(FileDescription fileDescription) {
        mFilesAndMediaAdapter.updateProgress(fileDescription);
    }

    @Override
    public void onDownloadError(FileDescription fileDescription, Throwable t) {
        mFilesAndMediaAdapter.onDownloadError(fileDescription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFilesAndMediaController.unbindActivity();
    }
}
