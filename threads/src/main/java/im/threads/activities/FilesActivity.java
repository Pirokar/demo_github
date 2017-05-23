package im.threads.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import im.threads.AnalyticsTracker;
import im.threads.R;
import im.threads.adapters.FilesAndMediaAdapter;
import im.threads.controllers.FilesAndMediaController;
import im.threads.model.ChatStyle;
import im.threads.model.FileDescription;
import im.threads.utils.PrefUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesActivity extends BaseActivity implements FilesAndMediaAdapter.OnFileClick {
    private static final String TAG = "FilesActivity ";
    private FilesAndMediaController mFilesAndMediaController;
    private RecyclerView mRecyclerView;
    private EditText mSearchEditText;
    private Toolbar mToolbar;
    private FilesAndMediaAdapter mFilesAndMediaAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final Context ctx = this;
        super.onCreate(savedInstanceState);
        ChatStyle style = PrefUtils.getIncomingStyle(this);
        if (Build.VERSION.SDK_INT > 20) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (style != null && style.chatStatusBarColorResId != ChatStyle.INVALID) {
                window.setStatusBarColor(getResources().getColor(style.chatStatusBarColorResId));
            } else {
                window.setStatusBarColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
        setContentView(R.layout.activity_files_and_media);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getFragmentManager().findFragmentByTag(TAG) == null) {
            mFilesAndMediaController = FilesAndMediaController.getInstance();
            getFragmentManager().beginTransaction().add(mFilesAndMediaController, TAG).commit();
        } else {
            mFilesAndMediaController = (FilesAndMediaController) getFragmentManager().findFragmentByTag(TAG);
        }
        mFilesAndMediaController.bindActivity(this);
        mFilesAndMediaController.getFilesAcync();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsTracker.getInstance(ctx, PrefUtils.getGaTrackerId(ctx)).setFileSearchWasOpened();
                AnalyticsTracker.getInstance(ctx, PrefUtils.getGaTrackerId(ctx)).setImageSearchWasOpened();
                if (mSearchEditText.getVisibility() == View.VISIBLE) {
                    mSearchEditText.setText("");
                    mSearchEditText.setVisibility(View.GONE);
                    mToolbar.setTitle(getString(R.string.files_and_media));
                    if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.undoClear();
                } else {
                    mSearchEditText.setVisibility(View.VISIBLE);
                    mSearchEditText.requestFocus();
                    mToolbar.setTitle("");
                    if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.backupAndClear();
                    mSearchEditText.setText("");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }, 100);
                }
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
                String str = null;
                if (s == null) {
                    str = "";
                } else {
                    str = s.toString();
                }
                if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.filter(str);
            }
        });
        setActivityStyle(PrefUtils.getIncomingStyle(this));
    }

    @Override
    protected void setActivityStyle(ChatStyle style) {
        if (null != style) {
            if (style.chatBackgroundColor != ChatStyle.INVALID) {
                findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, style.chatBackgroundColor));
            } else {
                findViewById(R.id.activity_root).setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            }

            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
                mToolbar.setTitleTextColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
            } else {
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                mToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            }

            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                ((ImageButton) findViewById(R.id.search)).setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
                mSearchEditText.setTextColor(getColorInt(style.chatToolbarTextColorResId));
                mToolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageButton) findViewById(R.id.search)).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                mSearchEditText.setTextColor(getColorInt(android.R.color.white));
                mToolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
            }

            if (style.chatToolbarHintTextColor != ChatStyle.INVALID) {
                mSearchEditText.setHintTextColor(getColorInt(style.chatToolbarHintTextColor));
            } else {
                mSearchEditText.setHintTextColor(getColorInt(android.R.color.darker_gray));
            }
        }
    }

    public void onFileReceive(List<FileDescription> descriptions) {
        if (descriptions != null && descriptions.size() > 0) {
            for (Iterator<FileDescription> iter = descriptions.iterator(); iter.hasNext(); ) {
                if (iter.next().getFilePath() == null) iter.remove();
            }
            mFilesAndMediaAdapter = new FilesAndMediaAdapter(descriptions, this);
            mRecyclerView.setAdapter(mFilesAndMediaAdapter);

        }
    }

    @Override
    public void onFileClick(FileDescription fileDescription) {
        mFilesAndMediaController.onFileClick(fileDescription);
    }

    public static Intent getStartIntetent(Activity activity) {
        Intent i = new Intent(activity, FilesActivity.class);
        return i;
    }

    @Override
    public void onBackPressed() {
        if (mSearchEditText.getVisibility() == View.VISIBLE) {
            mSearchEditText.setText("");
            mSearchEditText.setVisibility(View.GONE);
            mToolbar.setTitle(getString(R.string.files_and_media));
            if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.undoClear();
        } else {
            super.onBackPressed();
        }
    }
}
