package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
    private View mEmptyListLayout;
    private View mSearchImageView;
    private EditText mSearchEditText;
    private Toolbar mToolbar;
    private FilesAndMediaAdapter mFilesAndMediaAdapter;

    public static Intent getStartIntent(@NonNull Context context) {
        return new Intent(context, FilesActivity.class);
    }

    public void onFileReceive(List<FileDescription> descriptions) {
        if (descriptions != null && descriptions.size() > 0) {
            mSearchImageView.setVisibility(View.VISIBLE);
            mEmptyListLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
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
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(style.mediaAndFilesStatusBarColorResId));
        if (getResources().getBoolean(style.mediaAndFilesWindowLightStatusBarResId)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
        mEmptyListLayout = findViewById(R.id.empty_list_layout);
        mRecyclerView = findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSearchEditText = findViewById(R.id.search_edit_text);
        mSearchImageView = findViewById(R.id.search);
        mSearchImageView.setOnClickListener(v -> {
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
        findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.mediaAndFilesScreenBackgroundColor));
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.mediaAndFilesToolbarColorResId));
        ((ImageButton) findViewById(R.id.search)).setColorFilter(ContextCompat.getColor(this, style.mediaAndFilesToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        mSearchEditText.setTextColor(getColorInt(style.mediaAndFilesToolbarTextColorResId));
        final Drawable navigationIcon = mToolbar.getNavigationIcon();
        navigationIcon.mutate().setColorFilter(ContextCompat.getColor(this, style.mediaAndFilesToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(navigationIcon);
        mSearchEditText.setHintTextColor(getColorInt(style.mediaAndFilesToolbarHintTextColor));

        setUpTextViewStyle(R.id.empty_list_header,
                style.emptyMediaAndFilesHeaderText,
                style.emptyMediaAndFilesHeaderTextSize,
                style.emptyMediaAndFilesHeaderTextColor,
                style.emptyMediaAndFilesHeaderFontPath);
        setUpTextViewStyle(R.id.empty_list_description,
                style.emptyMediaAndFilesDescriptionText,
                style.emptyMediaAndFilesDescriptionTextSize,
                style.emptyMediaAndFilesDescriptionTextColor,
                style.emptyMediaAndFilesDescriptionFontPath);
    }

    private void setUpTextViewStyle(@IdRes int textViewResId,
                                    @StringRes int textResId,
                                    @DimenRes int sizeResId,
                                    @ColorRes int colorResId,
                                    @Nullable String fontPath) {
        TextView textView = findViewById(textViewResId);
        textView.setText(textResId);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(sizeResId));
        textView.setTextColor(getColorInt(colorResId));
        if (!TextUtils.isEmpty(fontPath)) {
            textView.setTypeface(Typeface.createFromAsset(getAssets(), fontPath));
        }
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
